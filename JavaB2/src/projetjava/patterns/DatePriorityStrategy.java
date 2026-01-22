package projetjava.patterns;

import projetjava.model.RendezVous;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DatePriorityStrategy implements IPriorityStrategy {
    
    private static final DateTimeFormatter[] DATE_TIME_FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    };
    private static final DateTimeFormatter[] DATE_ONLY_FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE, // YYYY-MM-DD
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    };
    @Override
    public List<RendezVous> sort(List<RendezVous> items) {
        return items.stream()
                .sorted(Comparator.comparing(this::parseDateTime))
                .collect(Collectors.toList());
    }
    private LocalDateTime parseDateTime(RendezVous rdv) {
        String dateStr = rdv.getDate();
        if (dateStr == null) return LocalDateTime.MAX;
        for (DateTimeFormatter fmt : DATE_TIME_FORMATTERS) {
            try { return LocalDateTime.parse(dateStr, fmt); } catch (DateTimeParseException e) { }
        }
        for (DateTimeFormatter fmt : DATE_ONLY_FORMATTERS) {
            try { return LocalDate.parse(dateStr, fmt).atStartOfDay(); } catch (DateTimeParseException e) { }
        }
        return LocalDateTime.MAX; 
    }
}