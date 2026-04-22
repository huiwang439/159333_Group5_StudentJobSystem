package nz.ac.massey.studentjobboard.notification;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/student/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final StudentNotificationRepository notificationRepository;

    public NotificationController(StudentNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<StudentNotification> list(
        @RequestParam(defaultValue = "1") Long studentId,
        @RequestParam(required = false) String type
    ) {
        if (type == null || type.isBlank()) {
            return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        }
        return notificationRepository.findByStudentIdAndTypeOrderByCreatedAtDesc(studentId, type.trim().toLowerCase());
    }

    @PatchMapping("/{notificationId}/read")
    public StudentNotification markRead(
        @PathVariable Long notificationId,
        @RequestParam(defaultValue = "1") Long studentId
    ) {
        StudentNotification notification = getOwnedNotification(notificationId, studentId);
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(
        @RequestParam(defaultValue = "1") Long studentId,
        @RequestParam(required = false) String type
    ) {
        List<StudentNotification> list = (type == null || type.isBlank())
            ? notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
            : notificationRepository.findByStudentIdAndTypeOrderByCreatedAtDesc(studentId, type.trim().toLowerCase());
        for (StudentNotification item : list) {
            if (!item.isRead()) {
                item.setRead(true);
                notificationRepository.save(item);
            }
        }
    }

    private StudentNotification getOwnedNotification(Long notificationId, Long studentId) {
        StudentNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getStudentId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        return notification;
    }
}
