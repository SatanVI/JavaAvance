package projetjava.repositories;

import projetjava.model.RendezVous;
import projetjava.util.MedicalFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RendezVousRepository implements IRepository<RendezVous> {
    private Map<Long, RendezVous> database = new HashMap<>();
    private static final String FILE_PATH = "rendezvous.csv";

    public RendezVousRepository() {
        load();
    }

    private void load() {
        Path path = Path.of(FILE_PATH);
        if (!Files.exists(path)) return;

        try {
            for (String line : Files.readAllLines(path)) {
                String[] parts = line.split(";");
                if (parts.length >= 5) {
                    Long id = Long.parseLong(parts[0]);
                    Long pId = Long.parseLong(parts[1]);
                    Long mId = Long.parseLong(parts[2]);
                    String date = parts[3];
                    String statut = parts[4];

                    // On utilise le constructeur standard
                    RendezVous rdv = new RendezVous(id, pId, mId, date);
                    rdv.setStatut(statut); // On restaure le statut
                    
                    database.put(id, rdv);
                    MedicalFactory.initRdvId(id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(RendezVous rdv) {
        database.put(rdv.getId(), rdv);
        saveToFile();
    }

    @Override public RendezVous findById(Long id) { return database.get(id); }
    @Override public List<RendezVous> findAll() { return new ArrayList<>(database.values()); }
    @Override public void delete(Long id) { database.remove(id); saveToFile(); }

    private void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (RendezVous r : database.values()) {
            lines.add(r.getId() + ";" + r.getPatientId() + ";" + r.getMedecinId() + ";" + r.getDate() + ";" + r.getStatut());
        }
        try {
            Files.write(Path.of(FILE_PATH), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}