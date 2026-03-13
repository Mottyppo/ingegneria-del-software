package it.unibs.ingesw.io;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.lib.Alignment;
import it.unibs.ingesw.lib.CommandLineTable;
import it.unibs.ingesw.lib.InputData;
import it.unibs.ingesw.lib.Menu;
import it.unibs.ingesw.model.Campo;
import it.unibs.ingesw.model.Categoria;
import it.unibs.ingesw.model.Configuratore;
import it.unibs.ingesw.model.TipoCampo;
import it.unibs.ingesw.model.TipoDato;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//TODO: everything here

public class UserInteraction {
    private static final List<String[]> CAMPI_BASE = List.of(
            new String[]{"Titolo", "nome di fantasia (esplicativo) attribuito all'iniziativa"},
            new String[]{"Numero di partecipanti", "numero di persone da coinvolgere nell'iniziativa"},
            new String[]{"Termine ultimo di iscrizione", "ultimo giorno utile per iscriversi all'iniziativa"},
            new String[]{"Luogo", "indirizzo del luogo che ospitera' l'iniziativa"},
            new String[]{"Data", "data di inizio dell'iniziativa"},
            new String[]{"Ora", "ora di ritrovo dei partecipanti"},
            new String[]{"Quota individuale", "spesa individuale stimata per l'iniziativa"},
            new String[]{"Data conclusiva", "data di conclusione dell'iniziativa"}
    );

    private final SystemManager manager;

    public UserInteraction(SystemManager manager) {
        this.manager = manager;
    }

    public void start() {
        Menu.clearConsole();
        System.out.println("=== Backend Configuratore ===");
        Configuratore configuratore = login();
        if (configuratore == null) {
            return;
        }

        if (configuratore.isPrimoAccesso()) {
            gestisciPrimoAccesso(configuratore);
        }

        if (!manager.isCampiBaseImpostati()) {
            System.out.println("\nPrima configurazione: impostazione campi base.");
            setupCampiBase();
        }

        menuPrincipale();
    }

    private Configuratore login() {
        while (true) {
            String username = InputData.readNonEmptyString("Username: ", true).trim();
            String password = InputData.readNonEmptyString("Password: ", false);
            Configuratore configuratore = manager.autenticaConfiguratore(username, password);
            if (configuratore != null) {
                return configuratore;
            }
            System.out.println("Credenziali non valide. Riprova.\n");
        }
    }

    private void gestisciPrimoAccesso(Configuratore configuratore) {
        System.out.println("\nPrimo accesso: scegli le tue credenziali personali.");
        while (true) {
            String nuovoUsername = InputData.readNonEmptyString("Nuovo username: ", true).trim();
            String nuovaPassword = InputData.readNonEmptyString("Nuova password: ", false);
            boolean ok = manager.aggiornaCredenziali(configuratore, nuovoUsername, nuovaPassword);
            if (ok) {
                System.out.println("Credenziali aggiornate con successo.\n");
                return;
            }
            System.out.println("Username gia' in uso. Riprova.\n");
        }
    }

    private void setupCampiBase() {
        List<Campo> campi = new ArrayList<>();
        for (String[] definizione : CAMPI_BASE) {
            String nome = definizione[0];
            String descrizione = definizione[1];
            TipoDato tipoDato = scegliTipoDato("Scegli il tipo dato per il campo base \"" + nome + "\"");
            if (tipoDato == null) {
                System.out.println("Operazione annullata.\n");
                return;
            }
            campi.add(new Campo(nome, descrizione, true, TipoCampo.BASE, tipoDato));
        }

        boolean success = manager.impostaCampiBase(campi);
        if (success) {
            System.out.println("Campi base impostati correttamente.\n");
        } else {
            System.out.println("I campi base risultano gia' impostati.\n");
        }
    }

    private void menuPrincipale() {
        boolean exit = false;
        while (!exit) {
            List<String> entries = new ArrayList<>();
            if (manager.isCampiBaseImpostati()) {
                entries.add("Visualizza campi base");
            } else {
                entries.add("Imposta campi base");
            }
            entries.add("Gestisci campi comuni");
            entries.add("Gestisci categorie");
            entries.add("Visualizza categorie e campi");

            Menu menu = new Menu("Menu Configuratore", entries, true, Alignment.CENTER, true);
            int choice = menu.choose();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    if (manager.isCampiBaseImpostati()) {
                        visualizzaCampi("Campi base", manager.getCampiBase());
                    } else {
                        setupCampiBase();
                    }
                }
                case 2 -> {
                    if (manager.isCampiBaseImpostati()) {
                        menuCampiComuni();
                    } else {
                        System.out.println("Prima imposta i campi base.\n");
                    }
                }
                case 3 -> {
                    if (manager.isCampiBaseImpostati()) {
                        menuCategorie();
                    } else {
                        System.out.println("Prima imposta i campi base.\n");
                    }
                }
                case 4 -> {
                    if (manager.isCampiBaseImpostati()) {
                        visualizzaCategorieComplete();
                    } else {
                        System.out.println("Prima imposta i campi base.\n");
                    }
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void menuCampiComuni() {
        boolean exit = false;
        while (!exit) {
            Menu menu = new Menu("Campi Comuni",
                    List.of("Aggiungi campo comune", "Rimuovi campo comune", "Cambia obbligatorieta'", "Visualizza campi comuni"),
                    true, Alignment.CENTER, true);
            int scelta = menu.choose();
            switch (scelta) {
                case 0 -> exit = true;
                case 1 -> aggiungiCampoComune();
                case 2 -> rimuoviCampoComune();
                case 3 -> toggleCampoComune();
                case 4 -> visualizzaCampi("Campi comuni", manager.getCampiComuni());
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void aggiungiCampoComune() {
        String nome = InputData.readNonEmptyString("Nome campo comune: ", false).trim();
        if (!manager.isNomeCampoCategoriaDisponibile(nome, null)) {
            System.out.println("Nome campo gia' in uso (tra base o comuni).\n");
            return;
        }
        String descrizione = InputData.readNonEmptyString("Descrizione: ", false).trim();
        boolean obbligatorio = InputData.readYesOrNo("Il campo e' obbligatorio");
        TipoDato tipoDato = scegliTipoDato("Scegli tipo dato");
        if (tipoDato == null) {
            System.out.println("Operazione annullata.\n");
            return;
        }
        Campo campo = new Campo(nome, descrizione, obbligatorio, TipoCampo.COMUNE, tipoDato);
        if (manager.aggiungiCampoComune(campo)) {
            System.out.println("Campo comune aggiunto.\n");
        } else {
            System.out.println("Impossibile aggiungere il campo comune.\n");
        }
    }

    private void rimuoviCampoComune() {
        List<Campo> campi = manager.getCampiComuni();
        int index = scegliIndiceCampo(campi, "Seleziona il campo comune da rimuovere");
        if (index < 0) {
            return;
        }
        if (manager.rimuoviCampoComune(index)) {
            System.out.println("Campo comune rimosso.\n");
        } else {
            System.out.println("Impossibile rimuovere il campo comune.\n");
        }
    }

    private void toggleCampoComune() {
        List<Campo> campi = manager.getCampiComuni();
        int index = scegliIndiceCampo(campi, "Seleziona il campo comune da modificare");
        if (index < 0) {
            return;
        }
        if (manager.modificaObbligatorioCampoComune(index)) {
            System.out.println("Obbligatorieta' aggiornata.\n");
        } else {
            System.out.println("Impossibile aggiornare il campo comune.\n");
        }
    }

    private void menuCategorie() {
        boolean exit = false;
        while (!exit) {
            Menu menu = new Menu("Categorie",
                    List.of("Aggiungi categoria", "Rimuovi categoria", "Gestisci campi specifici", "Visualizza categorie"),
                    true, Alignment.CENTER, true);
            int scelta = menu.choose();
            switch (scelta) {
                case 0 -> exit = true;
                case 1 -> aggiungiCategoria();
                case 2 -> rimuoviCategoria();
                case 3 -> gestisciCampiSpecifici();
                case 4 -> visualizzaCategorieComplete();
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void aggiungiCategoria() {
        String nome = InputData.readNonEmptyString("Nome categoria: ", false).trim();
        if (!manager.isNomeCategoriaDisponibile(nome)) {
            System.out.println("Nome categoria gia' in uso.\n");
            return;
        }

        List<Campo> campiSpecifici = new ArrayList<>();
        Set<String> nomiSpecifici = new HashSet<>();

        boolean aggiungi = InputData.readYesOrNo("Vuoi aggiungere un campo specifico");
        while (aggiungi) {
            Campo campo = creaCampoSpecifico(nomiSpecifici);
            if (campo != null) {
                campiSpecifici.add(campo);
                nomiSpecifici.add(campo.getNome().toLowerCase());
            }
            aggiungi = InputData.readYesOrNo("Aggiungere un altro campo specifico");
        }

        if (manager.aggiungiCategoria(nome, campiSpecifici)) {
            System.out.println("Categoria aggiunta.\n");
        } else {
            System.out.println("Impossibile aggiungere la categoria.\n");
        }
    }

    private void rimuoviCategoria() {
        int index = scegliIndiceCategoria("Seleziona la categoria da rimuovere");
        if (index < 0) {
            return;
        }
        if (manager.rimuoviCategoria(index)) {
            System.out.println("Categoria rimossa.\n");
        } else {
            System.out.println("Impossibile rimuovere la categoria.\n");
        }
    }

    private void gestisciCampiSpecifici() {
        int catIndex = scegliIndiceCategoria("Seleziona la categoria");
        if (catIndex < 0) {
            return;
        }
        Categoria categoria = manager.getCategorie().get(catIndex);
        boolean exit = false;
        while (!exit) {
            Menu menu = new Menu("Campi specifici: " + categoria.getNome(),
                    List.of("Aggiungi campo specifico", "Rimuovi campo specifico", "Cambia obbligatorieta'", "Visualizza campi"),
                    true, Alignment.CENTER, true);
            int scelta = menu.choose();
            switch (scelta) {
                case 0 -> exit = true;
                case 1 -> aggiungiCampoSpecifico(catIndex, categoria);
                case 2 -> rimuoviCampoSpecifico(catIndex, categoria);
                case 3 -> toggleCampoSpecifico(catIndex, categoria);
                case 4 -> visualizzaCampi("Campi specifici - " + categoria.getNome(), categoria.getCampiSpecifici());
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void aggiungiCampoSpecifico(int catIndex, Categoria categoria) {
        String nome = InputData.readNonEmptyString("Nome campo specifico: ", false).trim();
        if (!manager.isNomeCampoCategoriaDisponibile(nome, categoria)) {
            System.out.println("Nome campo gia' in uso (tra base, comuni o specifici).\n");
            return;
        }
        String descrizione = InputData.readNonEmptyString("Descrizione: ", false).trim();
        boolean obbligatorio = InputData.readYesOrNo("Il campo e' obbligatorio");
        TipoDato tipoDato = scegliTipoDato("Scegli tipo dato");
        if (tipoDato == null) {
            System.out.println("Operazione annullata.\n");
            return;
        }
        Campo campo = new Campo(nome, descrizione, obbligatorio, TipoCampo.SPECIFICO, tipoDato);
        if (manager.aggiungiCampoSpecifico(catIndex, campo)) {
            System.out.println("Campo specifico aggiunto.\n");
        } else {
            System.out.println("Impossibile aggiungere il campo specifico.\n");
        }
    }

    private void rimuoviCampoSpecifico(int catIndex, Categoria categoria) {
        int campoIndex = scegliIndiceCampo(categoria.getCampiSpecifici(), "Seleziona il campo specifico da rimuovere");
        if (campoIndex < 0) {
            return;
        }
        if (manager.rimuoviCampoSpecifico(catIndex, campoIndex)) {
            System.out.println("Campo specifico rimosso.\n");
        } else {
            System.out.println("Impossibile rimuovere il campo specifico.\n");
        }
    }

    private void toggleCampoSpecifico(int catIndex, Categoria categoria) {
        int campoIndex = scegliIndiceCampo(categoria.getCampiSpecifici(), "Seleziona il campo specifico da modificare");
        if (campoIndex < 0) {
            return;
        }
        if (manager.modificaObbligatorioCampoSpecifico(catIndex, campoIndex)) {
            System.out.println("Obbligatorieta' aggiornata.\n");
        } else {
            System.out.println("Impossibile aggiornare il campo specifico.\n");
        }
    }

    private Campo creaCampoSpecifico(Set<String> nomiSpecifici) {
        String nome = InputData.readNonEmptyString("Nome campo specifico: ", false).trim();
        if (nomiSpecifici.contains(nome.toLowerCase()) || !manager.isNomeCampoCategoriaDisponibile(nome, null)) {
            System.out.println("Nome campo gia' in uso.\n");
            return null;
        }
        String descrizione = InputData.readNonEmptyString("Descrizione: ", false).trim();
        boolean obbligatorio = InputData.readYesOrNo("Il campo e' obbligatorio");
        TipoDato tipoDato = scegliTipoDato("Scegli tipo dato");
        if (tipoDato == null) {
            return null;
        }
        return new Campo(nome, descrizione, obbligatorio, TipoCampo.SPECIFICO, tipoDato);
    }

    private void visualizzaCategorieComplete() {
        List<Categoria> categorie = manager.getCategorie();
        if (categorie.isEmpty()) {
            System.out.println("Nessuna categoria presente.\n");
            return;
        }
        for (Categoria categoria : categorie) {
            System.out.println("\nCategoria: " + categoria.getNome());
            List<Campo> campi = manager.getCampiCategoriaCondivisi(categoria);
            visualizzaCampi("Campi", campi);
        }
    }

    private void visualizzaCampi(String titolo, List<Campo> campi) {
        System.out.println("\n== " + titolo + " ==");
        if (campi == null || campi.isEmpty()) {
            System.out.println("Nessun campo presente.\n");
            return;
        }
        CommandLineTable table = new CommandLineTable();
        table.setShowVLines(true);
        table.setCellsAlignment(Alignment.LEFT);
        table.addHeaders(List.of("N", "Nome", "Descrizione", "Obblig.", "Tipo", "Dato"));
        List<List<String>> rows = new ArrayList<>();
        int i = 1;
        for (Campo campo : campi) {
            rows.add(List.of(
                    String.valueOf(i++),
                    campo.getNome(),
                    campo.getDescrizione(),
                    campo.isObbligatorio() ? "Si" : "No",
                    campo.getTipo().name(),
                    campo.getTipoDiDato().name()
            ));
        }
        table.addRows(rows);
        System.out.println(table);
    }

    private int scegliIndiceCategoria(String titolo) {
        List<Categoria> categorie = manager.getCategorie();
        if (categorie.isEmpty()) {
            System.out.println("Nessuna categoria presente.\n");
            return -1;
        }
        List<String> entries = new ArrayList<>();
        for (Categoria categoria : categorie) {
            entries.add(categoria.getNome());
        }
        Menu menu = new Menu(titolo, entries, true, Alignment.CENTER, true);
        int scelta = menu.choose();
        if (scelta == 0) {
            return -1;
        }
        return scelta - 1;
    }

    private int scegliIndiceCampo(List<Campo> campi, String titolo) {
        if (campi == null || campi.isEmpty()) {
            System.out.println("Nessun campo disponibile.\n");
            return -1;
        }
        List<String> entries = new ArrayList<>();
        for (Campo campo : campi) {
            entries.add(campo.getNome());
        }
        Menu menu = new Menu(titolo, entries, true, Alignment.CENTER, true);
        int scelta = menu.choose();
        if (scelta == 0) {
            return -1;
        }
        return scelta - 1;
    }

    private TipoDato scegliTipoDato(String titolo) {
        List<String> entries = Arrays.stream(TipoDato.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        Menu menu = new Menu(titolo, entries, true, Alignment.CENTER, true);
        int scelta = menu.choose();
        if (scelta == 0) {
            return null;
        }
        return TipoDato.values()[scelta - 1];
    }
}
