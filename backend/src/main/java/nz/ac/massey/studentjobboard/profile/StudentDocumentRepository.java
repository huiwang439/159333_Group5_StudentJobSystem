package nz.ac.massey.studentjobboard.profile;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {

    List<StudentDocument> findByStudentIdOrderByUploadedAtDesc(Long studentId);

    List<StudentDocument> findByStudentIdAndDocumentType(Long studentId, String documentType);
}
