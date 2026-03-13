package it.unibs.ingesw.model;

//TODO: documentazione + toString

public class Configuratore extends Utente {
    private boolean primoAccesso;

    public Configuratore(String username, String password) {
        super(username, password);
        this.primoAccesso = true;
    }

    public boolean isPrimoAccesso() {
        return primoAccesso;
    }

    public void setCredenziali(String username, String password) {
        this.username = username;
        this.password = password;
        this.primoAccesso = false;
    }

    public void setPrimoAccesso(boolean primoAccesso) {
        this.primoAccesso = primoAccesso;
    }

    @Override
    public String toString() {
        return "Configuratore{" +
                "username='" + username + '\'' +
                ", primoAccesso=" + primoAccesso +
                '}';
    }
}
