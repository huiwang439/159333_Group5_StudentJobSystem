package nz.ac.massey.studentjobboard.notification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentNotificationRepository extends JpaRepository<StudentNotification, Long> {

    List<StudentNotification> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<StudentNotification> findByStudentIdAndTypeOrderByCreatedAtDesc(Long studentId, String type);
}
