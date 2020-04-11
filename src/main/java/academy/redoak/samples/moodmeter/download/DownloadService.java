package academy.redoak.samples.moodmeter.download;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
public class DownloadService {

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    private RestTemplate restTemplate;
    private DownloadRepository downloadRepository;

    @Value("${academy.redoak.moodmeter.rssurls}")
    private List<String> rssURLs;

    @Value("${academy.redoak.moodmeter.cron:'(DEFAULT) 0 0 * * * *'}")
    private String cron;

    @Autowired
    public DownloadService(RestTemplate restTemplate, DownloadRepository downloadRepository) {
        this.restTemplate = restTemplate;
        this.downloadRepository = downloadRepository;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("created DownloadService with: cron={}, rssURL={}", cron, rssURLs);
    }

    @Scheduled(cron = "${academy.redoak.moodmeter.cron:0 0 * * * *}")
    public boolean doDownloads() {
        for (String rssURL : rssURLs) {
            log.debug("downloading '{}'", rssURL);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(rssURL, String.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                log.info("download succeeded");
                log.debug("content: '" + responseEntity.getBody() + "'");
                DownloadEntity entity = new DownloadEntity();
                entity.setSource(rssURL);
                entity.setDownloadedAt(LocalDateTime.now());
                try {
                    entity.setContent(compress(responseEntity.getBody()));
                } catch (IOException e) {
                    log.error("failed to compress content!", e);
                    entity.setContent("ERROR".getBytes(CHARSET));
                }
                downloadRepository.save(entity);
            } else {
                log.warn("download failed: " + responseEntity.getStatusCode() + " with message '" + responseEntity.getBody() + "'");
                return false;
            }
        }
        return true;
    }

    private byte[] compress(String string) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        gzipOutputStream.write(string.getBytes(CHARSET));
        gzipOutputStream.close();
        byte[] bytes = outputStream.toByteArray();
        log.debug("compressed " + string.getBytes().length + " bytes to " + bytes.length + " bytes");
        return bytes;
    }
}
