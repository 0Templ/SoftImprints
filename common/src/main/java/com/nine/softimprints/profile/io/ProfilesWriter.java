package com.nine.softimprints.profile.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ProfilesWriter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void write(
            Path file,
            JsonElement json
    ) throws IOException {
        Files.createDirectories(file.getParent());
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");

        try (var w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            GSON.toJson(json, w);
        }

        try {
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void delete(Path file) throws IOException {
        Files.deleteIfExists(file);
    }

}
