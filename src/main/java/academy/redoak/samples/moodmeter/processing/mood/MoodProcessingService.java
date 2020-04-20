package academy.redoak.samples.moodmeter.processing.mood;

import academy.redoak.samples.moodmeter.download.DownloadRepository;
import academy.redoak.samples.moodmeter.news.NewsEntry;
import academy.redoak.samples.moodmeter.news.NewsEntryRepository;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
public class MoodProcessingService {

    private DownloadRepository downloadRepository;
    private NewsEntryRepository newsEntryRepository;
    private MoodRepository moodRepository;
    private MoodProcessor moodProcessor;

    @Getter
    private Boolean isProcessing = false;

    private Map<LocalDateTime, List<NewsEntry>> entriesPerHour;

    @Autowired
    public MoodProcessingService(DownloadRepository downloadRepository, NewsEntryRepository newsEntryRepository, MoodRepository moodRepository, MoodProcessor moodProcessor) {
        this.downloadRepository = downloadRepository;
        this.newsEntryRepository = newsEntryRepository;
        this.moodRepository = moodRepository;
        this.moodProcessor = moodProcessor;
    }


    @Transactional
    public void reprocessNewsEntries() {
        synchronized (isProcessing) {
            if (!isProcessing) {
                this.isProcessing = true;
                this.newsEntryRepository.deleteAll();
                Map<LocalDateTime, List<NewsEntry>> entries =  StreamSupport.stream(downloadRepository.findAll().spliterator(), true)
                        .map(entity -> decompress(entity.getContent()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(content -> new StringReader(content))
                        .map(MoodProcessingService::readFeed)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(feed -> feed.getEntries().stream())
                        .map(NewsEntry::from)
                        .collect(Collectors.toMap(
                                NewsEntry::getPublishDate,
                                this::listForMood,
                                this::combineLists)
                        );
                entries.keySet().forEach(dt -> entries.put(dt, entries.get(dt)
                        .parallelStream()
                        .distinct() // remove duplicates
                        .collect(Collectors.toList())));
                this.entriesPerHour = entries;
                this.newsEntryRepository.saveAll(
                        entries.values()
                                .parallelStream()
                                .flatMap(List::stream)
                                .collect(Collectors.toList())
                );
                this.moodRepository.deleteAll();
                this.moodRepository.saveAll(
                        entries.keySet()
                                .parallelStream()
                                .map(key -> this.moodProcessor.processMoodForNews(key, entries.get(key)))
                                .collect(Collectors.toList())
                );

                this.isProcessing = false;
            } else {
                throw new RuntimeException("Processing already in progress!");
            }
        }
    }

    public Map<LocalDateTime, List<NewsEntry>> getEntriesPerHour() {
        return Collections.unmodifiableMap(entriesPerHour);
    }

    private List<NewsEntry> listForMood(NewsEntry newsEntry) {
        List<NewsEntry> list = new ArrayList<>();
        list.add(newsEntry);
        return list;
    }

    private <T> List<T> combineLists(List<T> dis, List<T> dat) {
        dis.addAll(dat);
        return dis;
    }

    private Optional<String> decompress(byte[] bytes) {
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String result = sb.toString();
            log.debug("decompressed " + bytes.length + " bytes to " + result.length() + " bytes");
            return Optional.of(result);
        } catch (IOException e) {
            log.error("Failed decompression of {}", Base64.getEncoder().encode(bytes), e);
            return Optional.empty();
        }
    }

    private static Optional<SyndFeed> readFeed(StringReader reader) {
        SyndFeed feed = null;
        try {
            feed = new SyndFeedInput().build(reader);
        } catch (FeedException e) {
            log.error("Failed to read RSS Feed from StringReader {}", reader, e);
        }
        return Optional.ofNullable(feed);
    }
}
