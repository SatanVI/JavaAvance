package projetjava.presentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import projetjava.model.Medecin;
import projetjava.model.Patient;
import projetjava.model.RendezVous;
import projetjava.service.MedicalService;

public class SimpleHttpServer {
    private MedicalService medicalService;

    public SimpleHttpServer(MedicalService service) {
        this.medicalService = service;
    }

    public void start() throws IOException {
        // Création du serveur sur le port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // 1. Endpoint Santé (Health Check)
        server.createContext("/health", exchange -> sendResponse(exchange, 200, "{\"status\":\"OK\"}"));

        // 2. Gestion des Patients
        server.createContext("/patients", new PatientHandler());

        // 3. Gestion des Médecins
        server.createContext("/medecins", new MedecinHandler());

        // 4. Création des Rendez-vous
        server.createContext("/appointments", new RendezVousHandler());

        // 5. Gestion du cycle de vie des Rendez-vous (Démarrer / Terminer)
        server.createContext("/appointments/start", new RendezVousActionHandler("START"));
        server.createContext("/appointments/finish", new RendezVousActionHandler("FINISH"));

        // 6. Gestion Undo / Redo
        server.createContext("/actions/undo", exchange -> handleUndoRedo(exchange, "UNDO"));
        server.createContext("/actions/redo", exchange -> handleUndoRedo(exchange, "REDO"));

        server.setExecutor(null); // Default executor
        server.start();
        System.out.println("✅ Serveur démarré sur http://localhost:8000");
    }

    // =========================================================
    // HANDLER PATIENTS (GET, POST, DELETE)
    // =========================================================
    class PatientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                // Lister les patients
                List<Patient> patients = medicalService.getAllPatients();
                StringBuilder json = new StringBuilder("[");
                for (Patient p : patients) {
                    json.append(p.toJson()).append(",");
                }
                if (patients.size() > 0) json.deleteCharAt(json.length() - 1);
                json.append("]");
                sendResponse(exchange, 200, json.toString());

            } else if ("POST".equals(method)) {
                // Créer un patient
                String body = getBody(exchange);
                String nom = extractJsonValue(body, "nom");
                String email = extractJsonValue(body, "email");

                if (nom != null && !nom.isEmpty() && email != null && !email.isEmpty()) {
                    Patient created = medicalService.registerPatient(nom, email);
                    sendResponse(exchange, 201, created.toJson());
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Nom ou email manquant\"}");
                }

            } else if ("DELETE".equals(method)) {
                // Supprimer un patient (via URL ?id=X)
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("id=")) {
                    try {
                        // Harmonisation du parsing pour être aussi robuste que MedecinHandler
                        String idStr = query.split("id=")[1].split("&")[0];
                        Long id = Long.parseLong(idStr);
                        medicalService.deletePatient(id);
                        sendResponse(exchange, 204, "");
                    } catch (Exception e) {
                        sendResponse(exchange, 400, "{\"error\":\"ID invalide\"}");
                    }
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"ID manquant\"}");
                }

            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Erreur interne serveur\"}");
            }
        }
    }

    // =========================================================
    // HANDLER MEDECINS (GET, POST)
    // =========================================================
    class MedecinHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Medecin> list = medicalService.getAllMedecins();
                StringBuilder json = new StringBuilder("[");
                for (Medecin m : list) json.append(m.toJson()).append(",");
                if (list.size() > 0) json.deleteCharAt(json.length() - 1);
                json.append("]");
                sendResponse(exchange, 200, json.toString());

            } else if ("POST".equals(method)) {
                String body = getBody(exchange);
                String nom = extractJsonValue(body, "nom");
                String spec = extractJsonValue(body, "specialite");
                if (nom != null && !nom.isEmpty() && spec != null && !spec.isEmpty()) {
                    Medecin created = medicalService.registerMedecin(nom, spec);
                    sendResponse(exchange, 201, created.toJson());
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Nom ou specialite manquant\"}");
                }

            } else if ("DELETE".equals(method)) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("id=")) {
                    try {
                        // Attention: parsing simple, suppose que id est le dernier ou seul paramètre
                        String idStr = query.split("id=")[1].split("&")[0];
                        Long id = Long.parseLong(idStr);
                        medicalService.deleteMedecin(id);
                        sendResponse(exchange, 204, "");
                    } catch (Exception e) {
                        sendResponse(exchange, 400, "{\"error\":\"ID invalide\"}");
                    }
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"ID manquant\"}");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
            } catch (Exception e) {
                e.printStackTrace(); // Affiche l'erreur dans la console serveur
                sendResponse(exchange, 500, "{\"error\":\"Erreur interne serveur\"}");
            }
        }
    }

    // =========================================================
    // HANDLER RENDEZ-VOUS - CRÉATION (POST)
    // =========================================================
    class RendezVousHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<RendezVous> list = medicalService.getAllRendezVous();
                StringBuilder json = new StringBuilder("[");
                for (RendezVous r : list) {
                    json.append(r.toJson()).append(",");
                }
                if (list.size() > 0) json.deleteCharAt(json.length() - 1);
                json.append("]");
                sendResponse(exchange, 200, json.toString());
            } else if ("POST".equals(method)) {
                String body = getBody(exchange);
                try {
                    Long pId = Long.parseLong(extractJsonValue(body, "patientId"));
                    Long mId = Long.parseLong(extractJsonValue(body, "medecinId"));
                    String date = extractJsonValue(body, "date");

                    RendezVous rdv = medicalService.createRendezVous(pId, mId, date);
                    sendResponse(exchange, 201, rdv.toJson());
                } catch (IllegalArgumentException e) {
                    // Capture les erreurs de validation (ID introuvable) ou de format
                    sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\":\"Erreur interne serveur\"}");
                }
            } else {
                // Correction : 405 est le code approprié pour une méthode non autorisée
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }
    }

    // =========================================================
    // HANDLER RENDEZ-VOUS - ACTIONS (START / FINISH)
    // =========================================================
    class RendezVousActionHandler implements HttpHandler {
        private String actionType; // "START" ou "FINISH"

        public RendezVousActionHandler(String actionType) {
            this.actionType = actionType;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();

                if (query != null && query.contains("id=")) {
                    try {
                        String idStr = query.split("id=")[1].split("&")[0];
                        Long id = Long.parseLong(idStr);
                        boolean success = false;

                        if ("START".equals(actionType)) {
                            success = medicalService.startRendezVous(id);
                        } else if ("FINISH".equals(actionType)) {
                            success = medicalService.finishRendezVous(id);
                        }

                        if (success) {
                            sendResponse(exchange, 200, "{\"status\":\"Succes\", \"action\":\"" + actionType + "\"}");
                        } else {
                            sendResponse(exchange, 400, "{\"error\":\"Impossible de changer le statut (RDV introuvable ou etat incorrect)\"}");
                        }
                    } catch (NumberFormatException e) {
                        sendResponse(exchange, 400, "{\"error\":\"ID invalide (format incorrect)\"}");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendResponse(exchange, 500, "{\"error\":\"Erreur interne serveur\"}");
                    }
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"ID manquant dans l'URL (ex: ?id=1)\"}");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed (Use POST)");
            }
        }
    }

    // =========================================================
    // HANDLER UNDO / REDO
    // =========================================================
    private void handleUndoRedo(HttpExchange exchange, String type) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            if ("UNDO".equals(type)) {
                medicalService.undoLastAction();
            } else {
                medicalService.redoLastAction();
            }
            sendResponse(exchange, 200, "{\"status\":\"Action " + type + " effectuee\"}");
        } else {
            sendResponse(exchange, 405, "Method Not Allowed (Use POST)");
        }
    }

    // =========================================================
    // MÉTHODES UTILITAIRES (HELPERS)
    // =========================================================

    // 1. Lire le corps de la requête
    private String getBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // 2. Envoyer la réponse HTTP
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // IMPORTANT : Fermer le flux d'entrée pour éviter le "socket hang up" si le client a envoyé des données non lues
        exchange.getRequestBody().close();

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        if (statusCode == 204) {
            // 204 No Content ne doit pas avoir de corps (-1 indique "pas de corps")
            exchange.sendResponseHeaders(statusCode, -1);
            exchange.close(); // Fermeture explicite pour 204
        } else {
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // 3. Parser JSON manuel simple
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;

            startIndex += searchKey.length();

            while (startIndex < json.length() && json.charAt(startIndex) == ' ') {
                startIndex++;
            }

            if (json.charAt(startIndex) == '"') {
                startIndex++;
                int endIndex = json.indexOf("\"", startIndex);
                return json.substring(startIndex, endIndex);
            } else {
                int endIndex = json.indexOf(",", startIndex);
                if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
                return json.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            return null;
        }
    }
}