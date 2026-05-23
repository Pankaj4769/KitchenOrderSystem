package com.kos.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Filesystem-backed image storage. Files are written under
 * {item.image.upload.path}/{restaurantId}/{filename}, and served back via
 * Spring's static resource handler at {item.image.public-base-url}/{key}.
 *
 * The Stage 2 R2 implementation will replace this bean via Spring profiles.
 */
@Service
public class LocalDiskImageStorage implements ImageStorageService {

    private static final Logger logger = LogManager.getLogger(LocalDiskImageStorage.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${item.image.upload.path}")
    private String uploadPath;

    @Value("${item.image.public-base-url}")
    private String publicBaseUrl;

    @Override
    public String store(MultipartFile file, String restaurantId, String nameHint) {
        if (file == null || file.isEmpty()) {
            throw new ImageStorageException("Image file is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ImageStorageException("Invalid content type: " + contentType);
        }

        String ext = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ImageStorageException("Unsupported image extension: " + ext);
        }

        String safeRestaurantId = sanitizeSegment(restaurantId);
        if (safeRestaurantId.isEmpty()) {
            throw new ImageStorageException("restaurantId is required");
        }

        String baseName = sanitizeSegment(nameHint);
        if (baseName.isEmpty()) baseName = "item";

        String fileName = baseName + "-" + shortUuid() + "." + ext;
        String storageKey = safeRestaurantId + "/" + fileName;

        try {
            // Resolve to absolute path BEFORE handing to transferTo — Spring's
            // MultipartFile.transferTo(File) resolves relative paths against
            // Tomcat's work directory, not the JVM CWD, so a relative
            // uploadPath silently writes to the wrong place (and createDirectories
            // resolves elsewhere, causing FileNotFoundException).
            Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path dir = root.resolve(safeRestaurantId);
            Files.createDirectories(dir);
            Path dest = dir.resolve(fileName);
            file.transferTo(dest.toFile());
            logger.info("Stored image at {}", dest);
            return storageKey;
        } catch (IOException e) {
            throw new ImageStorageException("Failed to write image to disk", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;
        try {
            Path resolved = resolveKey(storageKey);
            if (resolved != null) {
                Files.deleteIfExists(resolved);
            }
        } catch (IOException e) {
            // Don't fail the calling transaction if cleanup fails — just log it.
            logger.warn("Failed to delete image {}: {}", storageKey, e.getMessage());
        }
    }

    @Override
    public String publicUrlFor(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return null;
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
        return base + storageKey;
    }

    /**
     * Resolve a storage key safely under uploadPath. Rejects keys that try to
     * escape the upload root via "../" or absolute paths.
     */
    private Path resolveKey(String storageKey) {
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path candidate = root.resolve(storageKey).normalize();
        if (!candidate.startsWith(root)) {
            logger.warn("Refusing to resolve key outside upload root: {}", storageKey);
            return null;
        }
        return candidate;
    }

    private static String sanitizeSegment(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[^a-zA-Z0-9]", "");
    }

    private static String extractExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) return "";
        return originalFilename.substring(dot + 1).toLowerCase();
    }

    private static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
