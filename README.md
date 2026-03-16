# Bug Tracker Pro

A bug tracking system built in Java that simulates a real-world issue management workflow. The application processes JSON-based commands to manage tickets, users, milestones, and notifications — supporting roles like Developer, Manager, and Reporter.

## Features

- **Ticket Management** — create and track three ticket types: `Bug`, `FeatureRequest`, and `UiFeedback`, each with priority, severity, status, and business metadata
- **User Roles** — role-based access for Reporters (submit tickets), Developers (resolve them), and Managers (oversee milestones and assignments)
- **Milestone Tracking** — group tickets under milestones with progress monitoring and a testing phase lifecycle
- **Notifications** — users subscribed to a milestone are automatically notified on relevant events (Observer pattern)
- **Undo Support** — key actions (assign, comment, status change) can be undone
- **Search** — filter tickets by various criteria
- **Reports & Metrics** — generate reports for customer impact, ticket risk, resolution efficiency, app stability, and performance

## Project Structure

```
src/main/java/main/
├── commands/     # All executable actions (Command pattern)
├── ticket/       # Ticket types and factory (Bug, FeatureRequest, UiFeedback)
├── milestone/    # Milestone definition and management
├── notif/        # Notification system (Observer pattern)
├── utiliz/       # User roles: Developer, Manager, Reporter
├── enums/        # Enums for status, priority, severity, roles, etc.
├── App.java      # Entry point — reads and dispatches JSON input
└── AppState.java # Central application state
```

## Design Patterns

| Pattern | Where | Why |
|---|---|---|
| **Builder** | `Ticket` construction | Cleanly assembles tickets with many optional fields |
| **Command** | `main.commands` package | Each action is an object; easy to extend, undo, or log |
| **Factory** | `CommandFactory` | Creates the right command instance from JSON input |
| **Singleton** | `CommandFactory` (enum) | One global factory instance shared across the app |
| **Observer** | `main.notif` package | Milestone subscribers get notified without tight coupling |

## Tech Stack

- **Java 11+**
- **Maven** — build and dependency management
- **JSON** — input/output format for all commands and state
- **Checkstyle** — enforced code style

