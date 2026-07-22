package com.aztu.hr_erp.infrastructure.storage;

import com.aztu.hr_erp.common.exception.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Saves uploads to the VPS filesystem and returns the stored path only (never base64/S3). */
@Service
public class FileStorageService {

    public static final Set<String> CV_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final Path basePath;
    private final long maxSizeBytes;

    public FileStorageService(@Value("${app.storage.base-path:./hr-storage}") String basePath,
                              @Value("${app.storage.max-size-bytes:10485760}") long maxSizeBytes) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeBytes;
    }

    /** Store a CV, restricting the MIME type to pdf/doc/docx. */
    public StoredFile storeCv(MultipartFile file, String subDir) {
        return store(file, subDir, CV_MIME_TYPES);
    }

    /** Store any document (contracts, approval docs, termination docs). */
    public StoredFile storeDocument(MultipartFile file, String subDir) {
        return store(file, subDir, null);
    }

    private StoredFile store(MultipartFile file, String subDir, Set<String> allowedMime) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException("File exceeds the maximum allowed size");
        }
        String mime = file.getContentType();
        if (allowedMime != null && (mime == null || !allowedMime.contains(mime))) {
            throw new BadRequestException("Unsupported file type. Allowed: " + allowedMime);
        }
        try {
            Path dir = basePath.resolve(subDir).normalize();
            if (!dir.startsWith(basePath)) {
                throw new BadRequestException("Invalid storage path");
            }
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String stored = UUID.randomUUID() + ext;
            Path target = dir.resolve(stored);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(target.toString(), original, mime == null ? "application/octet-stream" : mime, file.getSize());
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    public Path resolve(String storagePath) {
        return Paths.get(storagePath);
    }
}
