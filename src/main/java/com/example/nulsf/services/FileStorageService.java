package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class FileStorageService {

    private Path root;

    public FileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        try {
            this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (Files.notExists(this.root)) {
                Files.createDirectories(this.root);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not initialize upload directory at " + uploadDir + ", falling back to ./uploads: " + e.getMessage());
            this.root = Paths.get("./uploads").toAbsolutePath().normalize();
            try {
                Files.createDirectories(this.root);
            } catch (IOException ignored) {}
        }
    }

    public Optional<String> storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        try {
            if (Files.notExists(this.root)) {
                Files.createDirectories(this.root);
            }
            String originalFilename = file.getOriginalFilename();
            String cleanName = (originalFilename != null) ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "image.jpg";
            String filename = System.currentTimeMillis() + "_" + cleanName;
            Path destination = this.root.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(filename);
        } catch (IOException e) {
            System.err.println("Upload failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Path getRoot() {
        return root;
    }
}
