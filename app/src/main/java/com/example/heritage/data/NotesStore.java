package com.example.heritage.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * File I/O for site notes. Each site gets one text file inside the app's
 * private "notes" directory: files/notes/site_{id}.txt
 */
public class NotesStore {

    private final File dir;

    public NotesStore(Context context) {
        dir = new File(context.getFilesDir(), "notes");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }

    private File fileFor(long siteId) {
        return new File(dir, "site_" + siteId + ".txt");
    }

    public void write(long siteId, String text) throws IOException {
        try (FileOutputStream out = new FileOutputStream(fileFor(siteId))) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String read(long siteId) throws IOException {
        File file = fileFor(siteId);
        if (!file.exists()) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
        }
        return sb.toString();
    }

    public boolean delete(long siteId) {
        return fileFor(siteId).delete();
    }

    public boolean exists(long siteId) {
        return fileFor(siteId).exists();
    }

    public long lastModified(long siteId) {
        return fileFor(siteId).lastModified();
    }

    public int count() {
        File[] files = dir.listFiles();
        return files == null ? 0 : files.length;
    }
}
