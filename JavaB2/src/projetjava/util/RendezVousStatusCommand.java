package projetjava.patterns;

import projetjava.model.RendezVous;
import projetjava.observer.NotificationManager;

public class RendezVousStatusCommand implements Command {
    private RendezVous rdv;
    private String oldStatus;
    private String newStatus;
    private NotificationManager notificationManager;

    public RendezVousStatusCommand(RendezVous rdv, String newStatus, NotificationManager notificationManager) {
        this.rdv = rdv;
        this.oldStatus = rdv.getStatut();
        this.newStatus = newStatus;
        this.notificationManager = notificationManager;
    }

    @Override
    public void execute() {
        rdv.setStatut(newStatus);
        notificationManager.notifyAll("Statut RDV " + rdv.getId() + " changé : " + newStatus);
    }

    @Override
    public void undo() {
        rdv.setStatut(oldStatus);
        notificationManager.notifyAll("Annulation : Statut RDV " + rdv.getId() + " revenu à " + oldStatus);
    }
}