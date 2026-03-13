package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.List;

//TODO: documentazione + toString

public class Categoria {
    private String nome;
    private List<Campo> campiSpecifici;

    public Categoria(String nome, List<Campo> campiSpecifici) {
        this.nome = nome;
        this.campiSpecifici = campiSpecifici == null ? new ArrayList<>() : campiSpecifici;
    }

    public String getNome() {
        return nome;
    }

    public List<Campo> getCampiSpecifici() {
        if (campiSpecifici == null) {
            campiSpecifici = new ArrayList<>();
        }
        return campiSpecifici;
    }

    public void aggiungiCampoSpecifico(Campo campo) {
        campiSpecifici.add(campo);
    }

    public void rimuoviCampoSpecifico(int index) {
        campiSpecifici.remove(index);
    }

    public void modificaObbligatorio(int index) {
        campiSpecifici.get(index).toggleObbligatorio();
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "nome='" + nome + '\'' +
                ", campiSpecifici=" + campiSpecifici +
                '}';
    }
}
