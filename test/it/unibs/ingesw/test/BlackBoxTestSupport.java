package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.persistence.JsonArchiveRepository;
import it.unibs.ingesw.persistence.JsonParticipantRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BlackBoxTestSupport {
    private static final DateTimeFormatter USER_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private BlackBoxTestSupport() {
    }

    static TestApplicationContext newContext() {
        return new TestApplicationContext();
    }

    static TestApplicationContext configuredSportContext() {
        TestApplicationContext context = newContext();
        context.getConfigurationService().setBaseFields(baseFields());
        context.getConfigurationService().addCommonField(noteField());
        context.getConfigurationService().addCategory("Sport", List.of(medicalCertificateField()));
        return context;
    }

    static List<Field> baseFields() {
        return List.of(
                new Field("Titolo", "nome di fantasia", true, FieldType.BASE, DataType.STRING),
                new Field("Numero di partecipanti", "numero partecipanti", true, FieldType.BASE, DataType.INTEGER),
                new Field("Termine ultimo di iscrizione", "deadline", true, FieldType.BASE, DataType.DATE),
                new Field("Luogo", "luogo evento", true, FieldType.BASE, DataType.STRING),
                new Field("Data", "data inizio", true, FieldType.BASE, DataType.DATE),
                new Field("Ora", "ora ritrovo", true, FieldType.BASE, DataType.TIME),
                new Field("Quota individuale", "quota", true, FieldType.BASE, DataType.DECIMAL),
                new Field("Data conclusiva", "data fine", true, FieldType.BASE, DataType.DATE)
        );
    }

    static Field noteField() {
        return new Field("Note", "informazioni aggiuntive", false, FieldType.COMMON, DataType.STRING);
    }

    static Field medicalCertificateField() {
        return new Field("Certificato medico", "requisito medico", true, FieldType.SPECIFIC, DataType.BOOLEAN);
    }

    static Field levelField() {
        return new Field("Livello", "livello richiesto", false, FieldType.SPECIFIC, DataType.STRING);
    }

    static Map<String, String> validSportValues(int participants) {
        LocalDate deadline = LocalDate.now().plusDays(2);
        LocalDate startDate = LocalDate.now().plusDays(5);
        return sportValues(participants, deadline, startDate, startDate);
    }

    static Map<String, String> sportValues(
            int participants,
            LocalDate deadline,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Titolo", "Camminata");
        values.put("Numero di partecipanti", Integer.toString(participants));
        values.put("Termine ultimo di iscrizione", USER_DATE_FORMAT.format(deadline));
        values.put("Luogo", "Brescia");
        values.put("Data", USER_DATE_FORMAT.format(startDate));
        values.put("Ora", "15:00");
        values.put("Quota individuale", "12.5");
        values.put("Data conclusiva", USER_DATE_FORMAT.format(endDate));
        values.put("Note", "Portare scarpe comode");
        values.put("Certificato medico", "si");
        return values;
    }

    static Map<String, String> withoutField(Map<String, String> values, String fieldName) {
        Map<String, String> copy = new LinkedHashMap<>(values);
        copy.remove(fieldName);
        return copy;
    }

    static Participant participant(String name, String surname, String username) {
        return new Participant(name, surname, username, "pwd");
    }

    static void persistParticipants(Participant... participants) {
        new JsonParticipantRepository().writeAll(List.of(participants));
    }

    static void persistArchive(Proposal... proposals) {
        Archive archive = new Archive();
        for (Proposal proposal : proposals) {
            archive.saveProposal(proposal);
        }
        new JsonArchiveRepository().write(archive);
    }

    static Proposal openProposal(
            int id,
            int expectedParticipants,
            LocalDate deadline,
            LocalDate startDate,
            LocalDate endDate,
            String... subscribers
    ) {
        Proposal proposal = new Proposal(id, "Sport", canonicalValues(id, expectedParticipants, deadline, startDate, endDate));
        proposal.markAsValid();
        proposal.markAsOpen();
        for (String subscriber : subscribers) {
            proposal.addSubscriber(subscriber, expectedParticipants);
        }
        return proposal;
    }

    static Proposal confirmedProposal(
            int id,
            int expectedParticipants,
            LocalDate deadline,
            LocalDate startDate,
            LocalDate endDate,
            String... subscribers
    ) {
        Proposal proposal = openProposal(id, expectedParticipants, deadline, startDate, endDate, subscribers);
        proposal.markAsConfirmed();
        return proposal;
    }

    private static Map<String, String> canonicalValues(
            int id,
            int expectedParticipants,
            LocalDate deadline,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Titolo", "Evento " + id);
        values.put("Numero di partecipanti", Integer.toString(expectedParticipants));
        values.put("Termine ultimo di iscrizione", deadline.format(DateTimeFormatter.ISO_LOCAL_DATE));
        values.put("Luogo", "Brescia");
        values.put("Data", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        values.put("Ora", "15:00");
        values.put("Quota individuale", "0");
        values.put("Data conclusiva", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return values;
    }
}
