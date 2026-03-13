package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: documentazione + toString

public class SystemConfig {
    private List<Campo> campiBase;
    private List<Campo> campiComuni;

    public SystemConfig() {
        this.campiBase = new ArrayList<>();
        this.campiComuni = new ArrayList<>();
    }

    public boolean isCampiBaseImpostati() {
        return campiBase != null && !campiBase.isEmpty();
    }

    public boolean impostaCampiBase(List<Campo> campiBase) {
        if (isCampiBaseImpostati()) {
            return false;
        }
        this.campiBase = new ArrayList<>(campiBase);
        return true;
    }

    public List<Campo> getCampiBase() {
        return campiBase == null ? List.of() : Collections.unmodifiableList(campiBase);
    }

    public List<Campo> getCampiComuni() {
        return campiComuni == null ? List.of() : Collections.unmodifiableList(campiComuni);
    }

    public void aggiungiCampoComune(Campo campo) {
        if (campiComuni == null) {
            campiComuni = new ArrayList<>();
        }
        campiComuni.add(campo);
    }

    public void rimuoviCampoComune(int index) {
        if (campiComuni == null) {
            return;
        }
        campiComuni.remove(index);
    }

    public void modificaObbligatorioComune(int index) {
        if (campiComuni == null) {
            return;
        }
        campiComuni.get(index).toggleObbligatorio();
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "campiBase=" + campiBase +
                ", campiComuni=" + campiComuni +
                '}';
    }
}
