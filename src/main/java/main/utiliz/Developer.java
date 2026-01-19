package main.utiliz;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import main.milestone.Observer;
import main.enums.Seniority;
import main.enums.ExpertiseArea;

public final class Developer extends Utilizator implements Observer {

    private final LocalDate hireDate;
    private final Seniority seniority;
    private final ExpertiseArea expertiseArea;
    private final List<String> notifications = new ArrayList<>();
    private double performanceScore = 0.0;

    public Developer(final String username, final String email, final LocalDate hireDate,
                     final Seniority seniority, final ExpertiseArea expertiseArea) {

        super(username, email);
        this.hireDate = hireDate;
        this.seniority = seniority;
        this.expertiseArea = expertiseArea;
    }

    @Override
    public void update(final String message) {
        notifications.add(message);
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public Seniority getSeniority() {
        return seniority;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public String getHireDate() {
        return hireDate.toString();
    }

    public double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(final double performanceScore) {
        this.performanceScore = performanceScore;
    }
}
