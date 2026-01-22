package projetjava.model;

public class RendezVous {
    private Long id;
    private Long patientId;
    private Long medecinId;
    private String date;
    private String statut;

    public RendezVous(Long id, Long patientId, Long medecinId, String date) {
        this.id = id;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.date = date;
        this.statut = "PLANIFIE";
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getMedecinId() { return medecinId; }
    public String getDate() { return date; }
    public String getStatut() { return statut; }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String toJson() {
        return String.format(
                "{\"id\":%d, \"patientId\":%d, \"medecinId\":%d, \"date\":\"%s\", \"statut\":\"%s\"}",
                id, patientId, medecinId, date, statut
        );
    }
}