package academy.redoak.samples.moodmeter.news;

import com.rometools.rome.feed.synd.SyndEntry;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Data
@Entity
@Table(name = "news_entry")
public class NewsEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;


    public static NewsEntry from(SyndEntry entry) {
        NewsEntry newsEntry = new NewsEntry();
        newsEntry.setTitle(entry.getTitle());
        newsEntry.setDescription(entry.getDescription() != null ? entry.getDescription().getValue() : "");
        newsEntry.setPublishDate(convertToLocalDateTime(entry.getPublishedDate()));
        return newsEntry;
    }

    /**
     * Converts given date to a {@link LocalDateTime}, but ignores Minutes!
     *
     * @param date the {@link Date} to convert.
     * @return LocalDateTime of {@link Date} with Minute 0;
     */
    private static LocalDateTime convertToLocalDateTime(Date date) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return LocalDateTime.of(
                localDateTime.getYear(),
                localDateTime.getMonth(),
                localDateTime.getDayOfMonth(),
                localDateTime.getHour(),
                0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsEntry newsEntry = (NewsEntry) o;
        return Objects.equals(publishDate, newsEntry.publishDate) &&
                Objects.equals(title, newsEntry.title) &&
                Objects.equals(description, newsEntry.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publishDate, title, description);
    }
}
