package com.kos.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction for item images. Implementations decide where bytes
 * physically live (local disk, S3, Cloudflare R2, etc.). Callers work only
 * with opaque storage keys and the URLs the implementation hands back.
 */
public interface ImageStorageService {

    /**
     * Validate and persist the uploaded file. Returns the storage key
     * (e.g. "77/TandooriRoti-a3f9c1d2.jpg") to be saved on the Item row.
     *
     * Throws ImageStorageException on validation failure or I/O error so the
     * caller's @Transactional rolls back any associated DB changes.
     */
    String store(MultipartFile file, String restaurantId, String nameHint);

    /** Delete by key. No-op if the key doesn't exist. */
    void delete(String storageKey);

    /** Build the URL the browser uses in <img src="">. */
    String publicUrlFor(String storageKey);
}
