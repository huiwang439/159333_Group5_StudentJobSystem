package nz.ac.massey.studentjobboard.favorite;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByStudentIdOrderByIdDesc(Long studentId);

    Optional<Favorite> findByStudentIdAndJobId(Long studentId, Long jobId);

    long countByStudentId(Long studentId);
}
