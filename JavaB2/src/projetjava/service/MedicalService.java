package projetjava.service;


import projetjava.model.Patient;
import projetjava.model.Medecin;
import projetjava.model.RendezVous;
import projetjava.repositories.IRepository;
import projetjava.util.MedicalFactory;
import projetjava.observer.NotificationManager;
import projetjava.patterns.CommandManager;
import projetjava.patterns.RendezVousStatusCommand;
import projetjava.patterns.IPriorityStrategy;
import projetjava.patterns.DefaultPriorityStrategy;

import java.util.List;

public class MedicalService {
    private IRepository<Patient> patientRepo;
    private IRepository<Medecin> medecinRepo;
    private IRepository<RendezVous> rdvRepo;
    private NotificationManager notificationManager;
    private CommandManager commandManager;
    private IPriorityStrategy priorityStrategy;

    public MedicalService(IRepository<Patient> patientRepo,
                          IRepository<Medecin> medecinRepo,
                          IRepository<RendezVous> rdvRepo,
                          NotificationManager notifManager) {
        this.patientRepo = patientRepo;
        this.medecinRepo = medecinRepo;
        this.rdvRepo = rdvRepo;
        this.notificationManager = notifManager;
        this.commandManager = new CommandManager();
        this.priorityStrategy = new DefaultPriorityStrategy(); // Stratégie par défaut
    }

    public Patient registerPatient(String nom, String email) {

        Patient p = MedicalFactory.createPatient(nom, email);

        patientRepo.save(p);

        notificationManager.notifyAll("Nouveau patient créé : " + nom);

        return p;
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public void deletePatient(Long id) {
        patientRepo.delete(id);
        notificationManager.notifyAll("Patient supprimé : ID " + id);
    }


    public Medecin registerMedecin(String nom, String specialite) {
        Medecin m = MedicalFactory.createMedecin(nom, specialite);
        medecinRepo.save(m);
        notificationManager.notifyAll("Nouveau médecin créé : " + nom);
        return m;
    }

    public List<Medecin> getAllMedecins() {
        return medecinRepo.findAll();
    }

    public void deleteMedecin(Long id) {
        medecinRepo.delete(id);
        notificationManager.notifyAll("Médecin supprimé : ID " + id);
    }



    public RendezVous createRendezVous(Long patientId, Long medecinId, String date) {
        if (patientRepo.findById(patientId) == null) {
            throw new IllegalArgumentException("Patient introuvable (ID " + patientId + ")");
        }
        if (medecinRepo.findById(medecinId) == null) {
            throw new IllegalArgumentException("Medecin introuvable (ID " + medecinId + ")");
        }

        RendezVous rdv = MedicalFactory.createRendezVous(patientId, medecinId, date);
        rdvRepo.save(rdv);
        notificationManager.notifyAll("Nouveau RDV planifié le " + date);
        return rdv;
    }

    public boolean startRendezVous(Long id) {
        RendezVous rdv = rdvRepo.findById(id);

        if (rdv != null && "PLANIFIE".equals(rdv.getStatut())) {
            // Utilisation du Command Pattern
            commandManager.executeCommand(new RendezVousStatusCommand(rdv, "EN_COURS", notificationManager));
            rdvRepo.save(rdv); // Sauvegarde le changement de statut dans le CSV
            return true;
        }
        return false;
    }

    public boolean finishRendezVous(Long id) {
        RendezVous rdv = rdvRepo.findById(id);

        if (rdv != null && "EN_COURS".equals(rdv.getStatut())) {
            // Utilisation du Command Pattern
            commandManager.executeCommand(new RendezVousStatusCommand(rdv, "TERMINE", notificationManager));
            rdvRepo.save(rdv); // Sauvegarde le changement de statut dans le CSV
            return true;
        }
        return false;
    }

    // Méthodes pour Undo / Redo
    public void undoLastAction() {
        commandManager.undo();
    }

    public void redoLastAction() {
        commandManager.redo();
    }

    // Setter pour changer la stratégie de priorité dynamiquement
    public void setPriorityStrategy(IPriorityStrategy strategy) {
        this.priorityStrategy = strategy;
    }

    public List<RendezVous> getAllRendezVous() {
        // Application de la stratégie de tri
        return priorityStrategy.sort(rdvRepo.findAll());
    }
}