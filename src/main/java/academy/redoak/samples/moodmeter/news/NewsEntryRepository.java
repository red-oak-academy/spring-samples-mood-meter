package academy.redoak.samples.moodmeter.news;

import org.springframework.data.repository.CrudRepository;

public interface NewsEntryRepository extends CrudRepository<NewsEntry, Integer> {
    // default implementation is sufficient
}
