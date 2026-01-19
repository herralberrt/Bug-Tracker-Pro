package main.utiliz;

import java.util.List;
import java.time.LocalDate;

public final class Manager extends Utilizator {

    private final LocalDate hireDate;
    private final List<String> subordinates;

    public Manager(final String username, final String email, final LocalDate hireDate,
                   final List<String> subordinates) {
        super(username, email);
        this.hireDate = hireDate;
        this.subordinates = subordinates;
    }
}
