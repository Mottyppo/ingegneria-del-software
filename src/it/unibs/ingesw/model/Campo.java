package it.unibs.ingesw.model;

//TODO: Documentazione + toString

public class Campo {
    private String nome;
    private String descrizione;
    private boolean obbligatorio;
    private TipoCampo tipo;
    private TipoDato tipoDiDato;

    public Campo(String nome, String descrizione, boolean obbligatorio, TipoCampo tipo, TipoDato tipoDiDato) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.obbligatorio = obbligatorio;
        this.tipo = tipo;
        this.tipoDiDato = tipoDiDato;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public boolean isObbligatorio() {
        return obbligatorio;
    }

    public TipoCampo getTipo() {
        return tipo;
    }

    public TipoDato getTipoDiDato() {
        return tipoDiDato;
    }

    public void toggleObbligatorio() {
        obbligatorio = !obbligatorio;
    }

    @Override
    public String toString() {
        return "Campo{" +
                "nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", obbligatorio=" + obbligatorio +
                ", tipo=" + tipo +
                ", tipoDiDato=" + tipoDiDato +
                '}';
    }
}
