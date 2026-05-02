package nz.ac.massey.studentjobboard.dashboard;

import java.util.List;
import nz.ac.massey.studentjobboard.job.Job;

public record DashboardResponse(
    long totalJobs,
    long favoriteCount,
    int newMatches,
    int profileCompleteness,
    List<Job> recommendations,
    List<Job> featuredJobs
) {}
