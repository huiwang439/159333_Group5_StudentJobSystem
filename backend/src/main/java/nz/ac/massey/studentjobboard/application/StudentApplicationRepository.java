package nz.ac.massey.studentjobboard.application;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {

    List<StudentApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    long countByStudentId(Long studentId);

    long countByStudentIdAndStatusIn(Long studentId, List<String> statuses);
}
