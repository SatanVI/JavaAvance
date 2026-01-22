package projetjava.model;

public class Medecin {
    private Long id;
    private String nom;
    private String specialite;

    public Medecin(Long id, String nom, String specialite) {
        this.id = id;
        this.nom = nom;
        this.specialite = specialite;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getSpecialite() { return specialite; }

    // Conversion manuelle en JSON
    public String toJson() {
        return String.format(
                "{\"id\":%d, \"nom\":\"%s\", \"specialite\":\"%s\"}",
                id, nom, specialite
        );
    }
}