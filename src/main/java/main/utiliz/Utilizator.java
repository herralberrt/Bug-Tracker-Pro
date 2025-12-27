package main.utiliz;

public abstract class Utilizator {

    protected final String username;
    protected final String email;

    protected Utilizator(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }
}
