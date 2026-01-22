package projetjava.repositories;

import projetjava.model.Patient;
import projetjava.util.MedicalFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientRepository implements IRepository<Patient> {
    private Map<Long, Patient> database = new HashMap<>();
    private static final String FILE_PATH = "patients.csv";

    public PatientRepository() {
        load();
    }

    private void load() {
        Path path = Path.of(FILE_PATH);
        if (!Files.exists(path)) return;

        try {
            for (String line : Files.readAllLines(path)) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    Long id = Long.parseLong(parts[0]);
                    String nom = parts[1];
                    String email = parts[2];
                    Patient p = new Patient(id, nom, email);
                    database.put(id, p);
                    MedicalFactory.initPatientId(id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Patient patient) {
        database.put(patient.getId(), patient);
        saveToFile();
    }

    @Override public Patient findById(Long id) { return database.get(id); }
    @Override public List<Patient> findAll() { return new ArrayList<>(database.values()); }
    @Override public void delete(Long id) { database.remove(id); saveToFile(); }

    private void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (Patient p : database.values()) {
            lines.add(p.getId() + ";" + p.getNom() + ";" + p.getEmail());
        }
        try {
            Files.write(Path.of(FILE_PATH), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}