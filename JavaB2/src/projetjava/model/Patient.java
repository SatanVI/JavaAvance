package projetjava.model;

public class Patient {
    private Long id;
    private String nom;
    private String email;

    public Patient(Long id, String nom, String email) {
        this.id = id;
        this.nom = nom;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }

    public String toJson() {
        return String.format("{\"id\":%d, \"nom\":\"%s\", \"email\":\"%s\"}", id, nom, email);
    }
}