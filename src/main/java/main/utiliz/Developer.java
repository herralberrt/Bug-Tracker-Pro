package main.utiliz;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import main.enums.ExpertiseArea;
import main.enums.Seniority;
import main.milestone.Observer;

public final class Developer extends Utilizator implements Observer {

    private final LocalDate hireDate;
    private final Seniority seniority;
    private final ExpertiseArea expertiseArea;

    private final List<String> notifications = new ArrayList<>();
    private double performanceScore = 0.0;

    public Developer(String username, String email, LocalDate hireDate,
                     Seniority seniority, ExpertiseArea expertiseArea) {

        super(username, email);
        this.hireDate = hireDate;
        this.seniority = seniority;
        this.expertiseArea = expertiseArea;
    }

    @Override
    public void update(String message) {
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

    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }
}
