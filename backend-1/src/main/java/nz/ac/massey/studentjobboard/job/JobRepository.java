package nz.ac.massey.studentjobboard.job;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
        SELECT j FROM Job j
        WHERE (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:location IS NULL OR j.location = :location)
          AND (:industry IS NULL OR j.industry = :industry)
          AND (:jobType IS NULL OR j.jobType = :jobType)
        """)
    Page<Job> search(
        @Param("keyword") String keyword,
        @Param("location") String location,
        @Param("industry") String industry,
        @Param("jobType") String jobType,
        Pageable pageable
    );

    List<Job> findAllByOrderByIdDesc(Pageable pageable);

    List<Job> findByIndustryOrderByIdDesc(String industry, Pageable pageable);
}
