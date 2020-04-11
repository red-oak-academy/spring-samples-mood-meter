package academy.redoak.samples.moodmeter.download;

import org.springframework.data.repository.CrudRepository;

public interface DownloadRepository extends CrudRepository<DownloadEntity, Integer> {
    // default implementation is succifient
}
