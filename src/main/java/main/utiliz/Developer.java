package main.utiliz;

import java.time.LocalDate;
import main.enums.ExpertiseArea;
import main.enums.Seniority;

public final class Developer extends Utilizator {

    private final LocalDate hireDate;
    private final Seniority seniority;
    private final ExpertiseArea expertiseArea;

    public Developer(String username, String email, LocalDate hireDate,
                    Seniority seniority, ExpertiseArea expertiseArea) {
        super(username, email);
        this.hireDate = hireDate;
        this.seniority = seniority;
        this.expertiseArea = expertiseArea;
    }
}
