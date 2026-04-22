package nz.ac.massey.studentjobboard.profile;

import java.time.LocalDateTime;
import java.util.List;
import nz.ac.massey.studentjobboard.profile.dto.ProfileUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/student/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final StudentProfileRepository profileRepository;
    private final StudentDocumentRepository documentRepository;

    public ProfileController(StudentProfileRepository profileRepository, StudentDocumentRepository documentRepository) {
        this.profileRepository = profileRepository;
        this.documentRepository = documentRepository;
    }

    @GetMapping
    public StudentProfile getProfile(@RequestParam(defaultValue = "1") Long studentId) {
        return profileRepository.findByStudentId(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @PatchMapping
    public StudentProfile updateProfile(@RequestBody ProfileUpdateRequest request) {
        Long studentId = request.studentId() == null ? 1L : request.studentId();
        StudentProfile profile = profileRepository.findByStudentId(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        profile.setFullName(valueOrFallback(request.fullName(), profile.getFullName()));
        profile.setEmail(valueOrFallback(request.email(), profile.getEmail()));
        profile.setStudentNumber(valueOrFallback(request.studentNumber(), profile.getStudentNumber()));
        profile.setPhone(valueOrFallback(request.phone(), profile.getPhone()));
        profile.setMajor(valueOrFallback(request.major(), profile.getMajor()));
        profile.setLocation(valueOrFallback(request.location(), profile.getLocation()));
        profile.setBio(valueOrFallback(request.bio(), profile.getBio()));
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    @GetMapping("/documents")
    public List<StudentDocument> listDocuments(@RequestParam(defaultValue = "1") Long studentId) {
        return documentRepository.findByStudentIdOrderByUploadedAtDesc(studentId);
    }

    @PostMapping("/documents/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDocument uploadDocument(
        @RequestParam(defaultValue = "1") Long studentId,
        @RequestParam String documentType,
        @RequestParam(defaultValue = "false") boolean defaultResume,
        @RequestParam MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (defaultResume && "resume".equalsIgnoreCase(documentType)) {
            unsetDefaultResume(studentId);
        }
        StudentDocument document = new StudentDocument();
        document.setStudentId(studentId);
        document.setDocumentType(documentType.toLowerCase());
        document.setFileName(file.getOriginalFilename() == null ? "uploaded-file" : file.getOriginalFilename());
        document.setFileUrl("/uploads/" + System.currentTimeMillis() + "-" + document.getFileName().replace(" ", "_"));
        document.setFileSize(file.getSize());
        document.setDefaultResume(defaultResume && "resume".equalsIgnoreCase(documentType));
        document.setUploadedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }

    @PatchMapping("/documents/{documentId}/default-resume")
    public StudentDocument setDefaultResume(
        @PathVariable Long documentId,
        @RequestParam(defaultValue = "1") Long studentId
    ) {
        StudentDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (!document.getStudentId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        if (!"resume".equalsIgnoreCase(document.getDocumentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only resume can be default");
        }
        unsetDefaultResume(studentId);
        document.setDefaultResume(true);
        return documentRepository.save(document);
    }

    private void unsetDefaultResume(Long studentId) {
        List<StudentDocument> resumes = documentRepository.findByStudentIdAndDocumentType(studentId, "resume");
        for (StudentDocument resume : resumes) {
            if (resume.isDefaultResume()) {
                resume.setDefaultResume(false);
                documentRepository.save(resume);
            }
        }
    }

    private String valueOrFallback(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }
}
