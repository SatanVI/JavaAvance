package projetjava.patterns;

import projetjava.model.RendezVous;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stratégie de tri par défaut (par ID croissant).
 */
public class DefaultPriorityStrategy implements IPriorityStrategy {
    @Override
    public List<RendezVous> sort(List<RendezVous> items) {
        // Par défaut : tri par ID (ordre de création)
        return items.stream()
                .sorted(Comparator.comparing(RendezVous::getId))
                .collect(Collectors.toList());
    }
}