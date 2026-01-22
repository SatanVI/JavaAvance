package projetjava.patterns;

import projetjava.model.RendezVous;
import java.util.List;

/**
 * Interface pour définir les stratégies de tri des rendez-vous.
 */
public interface IPriorityStrategy {
    List<RendezVous> sort(List<RendezVous> items);
}