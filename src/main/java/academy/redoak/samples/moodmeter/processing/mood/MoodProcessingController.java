package academy.redoak.samples.moodmeter.processing.mood;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
public class MoodProcessingController {

    private MoodProcessingService moodProcessingService;
    private MoodRepository moodRepository;

    @Autowired
    public MoodProcessingController(MoodProcessingService moodProcessingService, MoodRepository moodRepository) {
        this.moodProcessingService = moodProcessingService;
        this.moodRepository = moodRepository;
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("Created MoodProcessingController");
    }

    @GetMapping("/reprocess")
    public Iterable<MoodEntity> reprocessEverything() {
        this.moodProcessingService.reprocessNewsEntries();

        return this.moodRepository.findAll();
    }
}
