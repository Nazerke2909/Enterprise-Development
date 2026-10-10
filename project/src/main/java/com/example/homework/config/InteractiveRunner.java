package com.example.homework.config;

import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.persistence.AssignmentJdbc;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true")
public class InteractiveRunner implements CommandLineRunner {
    private final AssignmentService service;

    public InteractiveRunner(AssignmentService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                String command = scanner.nextLine().trim();
                try {
                    running = execute(command, scanner);
                } catch (RuntimeException exception) {
                    System.out.println("Error: " + exception.getMessage());
                }
            }
        }
    }

    private boolean execute(String command, Scanner scanner) {
        return switch (command) {
            case "1" -> {
                register(scanner);
                yield true;
            }
            case "2" -> {
                printAll();
                yield true;
            }
            case "3" -> {
                find(scanner);
                yield true;
            }
            case "4" -> {
                changeStatus(scanner);
                yield true;
            }
            case "0" -> {
                System.out.println("Goodbye!");
                yield false;
            }
            default -> {
                System.out.println("Unknown command.");
                yield true;
            }
        };
    }

    private void register(Scanner scanner) {
        System.out.print("Enter assignment key (for example, HW-1001): ");
        String key = scanner.nextLine().trim();
        System.out.print("Enter assignment title: ");
        String title = scanner.nextLine().trim();
        service.register(AssignmentId.newId(), key, title);
        System.out.println("Assignment saved with status ASSIGNED.");
    }

    private void printAll() {
        List<AssignmentJdbc.AssignmentRecord> assignments = service.findAll();
        if (assignments.isEmpty()) {
            System.out.println("No assignments yet.");
            return;
        }
        assignments.forEach(assignment -> System.out.printf(
                "%s | %s | %s | %s%n",
                assignment.businessKey(),
                assignment.status(),
                assignment.title(),
                assignment.id()));
    }

    private void find(Scanner scanner) {
        System.out.print("Enter assignment key: ");
        AssignmentJdbc.AssignmentRecord assignment = service.find(scanner.nextLine().trim());
        System.out.printf("Key: %s, status: %s, title: %s, id: %s%n",
                assignment.businessKey(), assignment.status(), assignment.title(), assignment.id());
    }

    private void changeStatus(Scanner scanner) {
        System.out.print("Enter assignment key: ");
        String key = scanner.nextLine().trim();
        System.out.println("Available statuses: ASSIGNED, SUBMITTED, CHECKED, APPROVED");
        System.out.print("Enter new status: ");
        AssignmentStatus status = AssignmentStatus.valueOf(scanner.nextLine().trim().toUpperCase());
        service.move(key, status);
        System.out.println("Status saved.");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== Assignment Management ===");
        System.out.println("1. Create assignment");
        System.out.println("2. Show all assignments");
        System.out.println("3. Find assignment");
        System.out.println("4. Change status");
        System.out.println("0. Exit");
        System.out.print("Choose an action: ");
    }
}
