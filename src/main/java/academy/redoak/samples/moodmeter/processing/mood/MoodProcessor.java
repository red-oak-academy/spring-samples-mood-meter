package academy.redoak.samples.moodmeter.processing.mood;

import academy.redoak.samples.moodmeter.news.NewsEntry;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MoodProcessor {

    public MoodEntity processMoodForNews(LocalDateTime date, List<NewsEntry> newsEntries) {
        MoodEntity mood = new MoodEntity();
        mood.setDate(date);
        mood.setLevel(Math.random() * 15);
        return mood;
    }
}
