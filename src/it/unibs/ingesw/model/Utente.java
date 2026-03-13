package it.unibs.ingesw.model;

//TODO: documentazione + toString

public abstract class Utente {
    protected String username;
    protected String password;

    public Utente(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Utente{" +
                "username='" + username + '\'' +
                '}';
    }
}
