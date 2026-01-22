import projetjava.repositories.PatientRepository;
import projetjava.repositories.MedecinRepository;
import projetjava.repositories.RendezVousRepository;
import projetjava.service.MedicalService;
import projetjava.observer.NotificationManager;
import projetjava.observer.ConsoleLogger;
import projetjava.observer.FileAuditLogger;
import projetjava.patterns.DatePriorityStrategy;
import projetjava.presentation.SimpleHttpServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            PatientRepository patientRepo = new PatientRepository();
            MedecinRepository medecinRepo = new MedecinRepository();
            RendezVousRepository rdvRepo = new RendezVousRepository();
            NotificationManager notifManager = new NotificationManager();
            notifManager.subscribe(new ConsoleLogger());
            notifManager.subscribe(new FileAuditLogger());

            MedicalService service = new MedicalService(
                    patientRepo,
                    medecinRepo,
                    rdvRepo,
                    notifManager
            );
            
            // ACTIVATION DE LA STRATÉGIE DE TRI PAR DATE
            service.setPriorityStrategy(new DatePriorityStrategy());

            SimpleHttpServer server = new SimpleHttpServer(service);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}