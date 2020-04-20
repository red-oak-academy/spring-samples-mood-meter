package academy.redoak.samples.moodmeter.processing.mood;

import org.springframework.data.repository.CrudRepository;

public interface MoodRepository extends CrudRepository<MoodEntity, Integer> {
}
