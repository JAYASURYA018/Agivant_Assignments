package com.bookpulse.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class ImageInitializer {

    @PostConstruct
    public void initImages() {
        try {
            Path sourceDir = Paths.get("C:\\Users\\NambariLikhitha\\.gemini\\antigravity-ide\\brain\\0ad2d4ab-a2c5-446e-8678-966804acf815\\.user_uploaded");
            Path targetStaticDir = Paths.get("src/main/resources/static/images");
            Path targetRuntimeDir = Paths.get("target/classes/static/images");

            Files.createDirectories(targetStaticDir);
            Files.createDirectories(targetRuntimeDir);

            copyIfSourceExists(sourceDir.resolve("media_1787417535148.jpg"), targetStaticDir.resolve("library-bg.jpg"), targetRuntimeDir.resolve("library-bg.jpg"));
            copyIfSourceExists(sourceDir.resolve("media_1787417535176.jpg"), targetStaticDir.resolve("vintage-books.jpg"), targetRuntimeDir.resolve("vintage-books.jpg"));
            copyIfSourceExists(sourceDir.resolve("media_1787417834023.jpg"), targetStaticDir.resolve("menu-icon.png"), targetRuntimeDir.resolve("menu-icon.png"));
            
            System.out.println("✅ Static images initialized successfully for UI interface.");
        } catch (Exception e) {
            System.out.println("ℹ️ Image initialization notice: " + e.getMessage());
        }
    }

    private void copyIfSourceExists(Path source, Path target1, Path target2) {
        try {
            if (Files.exists(source)) {
                Files.copy(source, target1, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(source, target2, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
        }
    }
}
