package projetjava.repositories;

import projetjava.model.Medecin;
import projetjava.util.MedicalFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedecinRepository implements IRepository<Medecin> {
    private Map<Long, Medecin> database = new HashMap<>();
    private static final String FILE_PATH = "medecins.csv";

    public MedecinRepository() {
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
                    String spec = parts[2];
                    Medecin m = new Medecin(id, nom, spec);
                    database.put(id, m);
                    MedicalFactory.initMedecinId(id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Medecin medecin) {
        database.put(medecin.getId(), medecin);
        saveToFile();
    }

    @Override
    public Medecin findById(Long id) {
        return database.get(id);
    }

    @Override
    public List<Medecin> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void delete(Long id) {
        database.remove(id);
        saveToFile();
    }

    private void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (Medecin m : database.values()) {
            lines.add(m.getId() + ";" + m.getNom() + ";" + m.getSpecialite());
        }
        try {
            Files.write(Path.of(FILE_PATH), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}