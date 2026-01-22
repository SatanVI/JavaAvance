package projetjava.util;
import projetjava.model.Medecin;
import projetjava.model.Patient;
import projetjava.model.RendezVous;
import java.util.concurrent.atomic.AtomicLong;

public class MedicalFactory {
    private static final AtomicLong patientIdGenerator = new AtomicLong(1);
    private static final AtomicLong medecinIdGenerator = new AtomicLong(1);
    private static final AtomicLong rdvIdGenerator = new AtomicLong(1);

    public static Patient createPatient(String nom, String email) {
        return new Patient(patientIdGenerator.getAndIncrement(), nom, email);
    }

    public static Medecin createMedecin(String nom, String specialite) {
        return new Medecin(medecinIdGenerator.getAndIncrement(), nom, specialite);
    }

    public static RendezVous createRendezVous(Long patientId, Long medecinId, String date) {
        return new RendezVous(rdvIdGenerator.getAndIncrement(), patientId, medecinId, date);
    }

    public static void initPatientId(long id) {
        if (id >= patientIdGenerator.get()) patientIdGenerator.set(id + 1);
    }
    public static void initMedecinId(long id) {
        if (id >= medecinIdGenerator.get()) medecinIdGenerator.set(id + 1);
    }
    public static void initRdvId(long id) {
        if (id >= rdvIdGenerator.get()) rdvIdGenerator.set(id + 1);
    }
}