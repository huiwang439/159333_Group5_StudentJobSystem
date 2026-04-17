package nz.ac.massey.studentjobboard.dashboard;

import java.util.List;
import nz.ac.massey.studentjobboard.favorite.FavoriteRepository;
import nz.ac.massey.studentjobboard.job.Job;
import nz.ac.massey.studentjobboard.job.JobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final JobRepository jobRepository;
    private final FavoriteRepository favoriteRepository;

    public DashboardController(JobRepository jobRepository, FavoriteRepository favoriteRepository) {
        this.jobRepository = jobRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping
    public DashboardResponse getDashboard(@RequestParam(defaultValue = "1") Long studentId) {
        List<Job> featured = jobRepository.findAllByOrderByIdDesc(PageRequest.of(0, 3));
        String preferredIndustry = featured.isEmpty() ? "Technology" : featured.get(0).getIndustry();
        List<Job> recommendations = jobRepository.findByIndustryOrderByIdDesc(preferredIndustry, PageRequest.of(0, 3));
        long totalJobs = jobRepository.count();
        long favoriteCount = favoriteRepository.countByStudentId(studentId);
        int newMatches = Math.min(3, recommendations.size());

        return new DashboardResponse(
            totalJobs,
            favoriteCount,
            newMatches,
            72,
            recommendations,
            featured
        );
    }
}
