package nz.ac.massey.studentjobboard.favorite;

import java.util.List;
import nz.ac.massey.studentjobboard.job.Job;
import nz.ac.massey.studentjobboard.job.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/student/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final JobRepository jobRepository;

    public FavoriteController(FavoriteRepository favoriteRepository, JobRepository jobRepository) {
        this.favoriteRepository = favoriteRepository;
        this.jobRepository = jobRepository;
    }

    @GetMapping
    public List<Job> getFavorites(@RequestParam(defaultValue = "1") Long studentId) {
        return favoriteRepository.findByStudentIdOrderByIdDesc(studentId)
            .stream()
            .map(Favorite::getJob)
            .toList();
    }

    @GetMapping("/ids")
    public List<Long> getFavoriteIds(@RequestParam(defaultValue = "1") Long studentId) {
        return favoriteRepository.findByStudentIdOrderByIdDesc(studentId)
            .stream()
            .map(favorite -> favorite.getJob().getId())
            .toList();
    }

    @PostMapping("/{jobId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFavorite(
        @PathVariable Long jobId,
        @RequestParam(defaultValue = "1") Long studentId
    ) {
        if (favoriteRepository.findByStudentIdAndJobId(studentId, jobId).isPresent()) {
            return;
        }
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        Favorite favorite = new Favorite();
        favorite.setStudentId(studentId);
        favorite.setJob(job);
        favoriteRepository.save(favorite);
    }

    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
        @PathVariable Long jobId,
        @RequestParam(defaultValue = "1") Long studentId
    ) {
        Favorite favorite = favoriteRepository.findByStudentIdAndJobId(studentId, jobId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found"));
        favoriteRepository.delete(favorite);
    }
}
