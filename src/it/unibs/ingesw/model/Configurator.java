package it.unibs.ingesw.model;

//TODO: Documentazione

public class Configurator extends User {
    private boolean firstAccess;

    public Configurator(String username, String password) {
        super(username, password);
        this.firstAccess = true;
    }

    public boolean isFirstAccess() {
        return firstAccess;
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        this.firstAccess = false;
    }

    @Override
    public String toString() {
        return "Configuratore{" +
                "username='" + username + '\'' +
                ", primo accesso=" + firstAccess +
                '}';
    }
}
