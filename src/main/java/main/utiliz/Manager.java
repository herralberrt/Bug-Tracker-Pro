package main.utiliz;

import java.time.LocalDate;
import java.util.List;

public final class Manager extends Utilizator {

    private final LocalDate hireDate;
    private final List<String> subordinates;

    public Manager(String username, String email, LocalDate hireDate,
                   List<String> subordinates) {
        super(username, email);
        this.hireDate = hireDate;
        this.subordinates = subordinates;
    }
}
