package com.aztu.hr_erp.infrastructure.storage;

/** Result of a successful upload: the VPS path plus metadata persisted by the DB. */
public record StoredFile(String storagePath, String originalName, String mimeType, long sizeBytes) {}
