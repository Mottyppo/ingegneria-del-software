package it.unibs.ingesw.controller;

import it.unibs.ingesw.io.IOManager;
import it.unibs.ingesw.model.Campo;
import it.unibs.ingesw.model.Categoria;
import it.unibs.ingesw.model.Configuratore;
import it.unibs.ingesw.model.SystemConfig;
import it.unibs.ingesw.model.TipoCampo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: everything here

public class SystemManager {
    private final IOManager ioManager;
    private final List<Configuratore> configuratori;
    private final List<Categoria> categorie;
    private final SystemConfig config;

    public SystemManager() {
        this.ioManager = new IOManager();
        this.config = ioManager.leggiConfig();
        this.categorie = ioManager.leggiCategorie();
        this.configuratori = ioManager.leggiConfiguratori();

        if (this.configuratori.isEmpty()) {
            inizializzaConfiguratoriPredefiniti();
        }
    }

    private void inizializzaConfiguratoriPredefiniti() {
        Configuratore c1 = new Configuratore("config1", "config1");
        Configuratore c2 = new Configuratore("config2", "config2");
        this.configuratori.add(c1);
        this.configuratori.add(c2);
        ioManager.scriviConfiguratori(this.configuratori);
    }

    public Configuratore autenticaConfiguratore(String username, String password) {
        for (Configuratore configuratore : configuratori) {
            if (configuratore.getUsername().equalsIgnoreCase(username)
                    && configuratore.getPassword().equals(password)) {
                return configuratore;
            }
        }
        return null;
    }

    public boolean isUsernameDisponibile(String username, Configuratore exclude) {
        for (Configuratore configuratore : configuratori) {
            if (exclude != null && configuratore == exclude) {
                continue;
            }
            if (configuratore.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        return true;
    }

    public boolean aggiornaCredenziali(Configuratore configuratore, String newUsername, String newPassword) {
        if (newUsername == null || newUsername.isBlank()) {
            return false;
        }
        String normalized = newUsername.trim();
        if (!isUsernameDisponibile(normalized, configuratore)) {
            return false;
        }
        configuratore.setCredenziali(normalized, newPassword);
        ioManager.scriviConfiguratori(this.configuratori);
        return true;
    }

    public boolean isCampiBaseImpostati() {
        return config.isCampiBaseImpostati();
    }

    public boolean impostaCampiBase(List<Campo> campiBase) {
        boolean success = config.impostaCampiBase(campiBase);
        if (success) {
            ioManager.scriviConfig(config);
        }
        return success;
    }

    public List<Campo> getCampiBase() {
        return config.getCampiBase();
    }

    public List<Campo> getCampiComuni() {
        return config.getCampiComuni();
    }

    public boolean aggiungiCampoComune(Campo campo) {
        if (!isNomeCampoDisponibile(campo.getNome(), null)) {
            return false;
        }
        config.aggiungiCampoComune(campo);
        ioManager.scriviConfig(config);
        return true;
    }

    public boolean rimuoviCampoComune(int index) {
        if (index < 0 || index >= config.getCampiComuni().size()) {
            return false;
        }
        config.rimuoviCampoComune(index);
        ioManager.scriviConfig(config);
        return true;
    }

    public boolean modificaObbligatorioCampoComune(int index) {
        if (index < 0 || index >= config.getCampiComuni().size()) {
            return false;
        }
        config.modificaObbligatorioComune(index);
        ioManager.scriviConfig(config);
        return true;
    }

    public List<Categoria> getCategorie() {
        return Collections.unmodifiableList(categorie);
    }

    public boolean aggiungiCategoria(String nome, List<Campo> campiSpecifici) {
        if (!isNomeCategoriaDisponibile(nome)) {
            return false;
        }
        String normalized = nome.trim();
        List<Campo> specificiValidi = campiSpecifici == null ? new ArrayList<>() : new ArrayList<>(campiSpecifici);
        if (!verificaNomiCampiSpecifici(specificiValidi)) {
            return false;
        }
        Categoria categoria = new Categoria(normalized, specificiValidi);
        categorie.add(categoria);
        ioManager.scriviCategorie(categorie);
        return true;
    }

    public boolean rimuoviCategoria(int index) {
        if (index < 0 || index >= categorie.size()) {
            return false;
        }
        categorie.remove(index);
        ioManager.scriviCategorie(categorie);
        return true;
    }

    public boolean aggiungiCampoSpecifico(int categoriaIndex, Campo campo) {
        if (categoriaIndex < 0 || categoriaIndex >= categorie.size()) {
            return false;
        }
        Categoria categoria = categorie.get(categoriaIndex);
        if (!isNomeCampoDisponibile(campo.getNome(), categoria)) {
            return false;
        }
        categoria.aggiungiCampoSpecifico(campo);
        ioManager.scriviCategorie(categorie);
        return true;
    }

    public boolean rimuoviCampoSpecifico(int categoriaIndex, int campoIndex) {
        if (categoriaIndex < 0 || categoriaIndex >= categorie.size()) {
            return false;
        }
        Categoria categoria = categorie.get(categoriaIndex);
        if (campoIndex < 0 || campoIndex >= categoria.getCampiSpecifici().size()) {
            return false;
        }
        categoria.rimuoviCampoSpecifico(campoIndex);
        ioManager.scriviCategorie(categorie);
        return true;
    }

    public boolean modificaObbligatorioCampoSpecifico(int categoriaIndex, int campoIndex) {
        if (categoriaIndex < 0 || categoriaIndex >= categorie.size()) {
            return false;
        }
        Categoria categoria = categorie.get(categoriaIndex);
        if (campoIndex < 0 || campoIndex >= categoria.getCampiSpecifici().size()) {
            return false;
        }
        categoria.modificaObbligatorio(campoIndex);
        ioManager.scriviCategorie(categorie);
        return true;
    }

    public boolean isNomeCategoriaDisponibile(String nome) {
        if (nome == null || nome.trim().isBlank()) {
            return false;
        }
        String normalized = nome.trim();
        for (Categoria categoria : categorie) {
            if (categoria.getNome().equalsIgnoreCase(normalized)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNomeCampoDisponibile(String nomeCampo, Categoria categoria) {
        if (nomeCampo == null) {
            return false;
        }
        String normalized = nomeCampo.trim();
        if (normalized.isBlank()) {
            return false;
        }

        if (esisteNomeCampo(config.getCampiBase(), normalized)) {
            return false;
        }
        if (esisteNomeCampo(config.getCampiComuni(), normalized)) {
            return false;
        }
        if (categoria != null && esisteNomeCampo(categoria.getCampiSpecifici(), normalized)) {
            return false;
        }
        return true;
    }

    private boolean esisteNomeCampo(List<Campo> campi, String nomeCampo) {
        for (Campo campo : campi) {
            if (campo.getNome() != null && campo.getNome().equalsIgnoreCase(nomeCampo)) {
                return true;
            }
        }
        return false;
    }

    private boolean verificaNomiCampiSpecifici(List<Campo> campiSpecifici) {
        List<String> nomiVisti = new ArrayList<>();
        for (Campo campo : campiSpecifici) {
            if (campo == null || campo.getNome() == null) {
                return false;
            }
            String nome = campo.getNome().trim();
            if (nome.isBlank()) {
                return false;
            }
            if (!isNomeCampoDisponibile(nome, null)) {
                return false;
            }
            for (String nomeVisto : nomiVisti) {
                if (nomeVisto.equalsIgnoreCase(nome)) {
                    return false;
                }
            }
            nomiVisti.add(nome);
        }
        return true;
    }

    public boolean isNomeCampoCategoriaDisponibile(String nomeCampo, Categoria categoria) {
        return isNomeCampoDisponibile(nomeCampo, categoria);
    }

    public List<Campo> getCampiCategoriaCondivisi(Categoria categoria) {
        List<Campo> campi = new ArrayList<>();
        campi.addAll(config.getCampiBase());
        campi.addAll(config.getCampiComuni());
        if (categoria != null) {
            campi.addAll(categoria.getCampiSpecifici());
        }
        return campi;
    }

    public boolean isTipoCampoSpecifico(Campo campo) {
        return campo != null && campo.getTipo() == TipoCampo.SPECIFICO;
    }
}
