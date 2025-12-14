// src/main/java/org/itmo/service/FileStorageService.java
package org.itmo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.storage.root-path:uploads}")
    private String uploadRootPath;

    @Value("${app.storage.user-photo-path:user-photos}")
    private String userPhotoPath;

    @Value("${app.storage.event-photo-path:event-photos}")
    private String eventPhotoPath;

    public String storeFile(MultipartFile file, String type) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        // Определяем директорию назначения в зависимости от типа
        String destinationPath;
        if ("user".equals(type)) {
            destinationPath = Paths.get(uploadRootPath, userPhotoPath).toString();
        } else if ("event".equals(type)) {
            destinationPath = Paths.get(uploadRootPath, eventPhotoPath).toString();
        } else {
            throw new IllegalArgumentException("Unknown file type: " + type);
        }

        Path uploadPath = Paths.get(destinationPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Генерируем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath);
        return filePath.toString(); // Возвращаем полный путь или относительный URL
    }
}