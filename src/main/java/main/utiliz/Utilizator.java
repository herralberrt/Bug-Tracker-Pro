package main.utiliz;

public abstract class Utilizator {

    protected final String username;
    protected final String email;


    protected Utilizator(final String username, final String email) {
        this.username = username;
        this.email = email;
    }

    public final String getUsername() {
        return username;
    }
}
