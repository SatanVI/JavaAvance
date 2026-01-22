package projetjava.observer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileAuditLogger implements IObserver {
    private static final String FILE_PATH = "audit.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void update(String message) {
        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(LocalDateTime.now().format(FORMATTER) + " - [AUDIT] - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}