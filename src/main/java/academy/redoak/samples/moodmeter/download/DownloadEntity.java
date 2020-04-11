package academy.redoak.samples.moodmeter.download;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "download")
public class DownloadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "source")
    private String source;

    @Column(name = "content")
    private byte[] content;

    @Column(name = "downloaded_at")
    private LocalDateTime downloadedAt;
}
