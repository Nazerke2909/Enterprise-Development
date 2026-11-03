package com.example.homework.config;

import com.example.homework.domain.AccessPolicy;
import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.AuthenticatedUser;
import com.example.homework.domain.Role;
import com.example.homework.persistence.AssignmentRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

@Component
@Order(2)
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true")
public class InteractiveRunner implements CommandLineRunner {
    private final AssignmentService service;
    private final UserService users;

    public InteractiveRunner(AssignmentService service, UserService users) {
        this.service = service;
        this.users = users;
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                AuthenticatedUser user = authenticate(scanner);
                running = user != null && session(user, scanner);
            }
        } catch (NoSuchElementException endOfInput) {
            System.out.println();
        }
        System.out.println("Goodbye!");
    }

    private AuthenticatedUser authenticate(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("=== Homework Control: sign in ===");
            System.out.println("1. Login");
            System.out.println("2. Register as student");
            System.out.println("0. Exit");
            System.out.print("Choose an action: ");
            String command = scanner.nextLine().trim();
            try {
                switch (command) {
                    case "1" -> {
                        System.out.print("Username: ");
                        String username = scanner.nextLine().trim();
                        AuthenticatedUser user = users.login(username, readPassword(scanner, "Password: "));
                        System.out.println("Logged in as " + user.username() + " (" + user.role() + ").");
                        return user;
                    }
                    case "2" -> registerStudent(scanner);
                    case "0" -> { return null; }
                    default -> System.out.println("Unknown command.");
                }
            } catch (RuntimeException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }
    }

    private void registerStudent(Scanner scanner) {
        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();
        AuthenticatedUser user = users.registerStudent(username, readPassword(scanner, "Choose a password: "));
        System.out.println("Student " + user.username() + " registered. You can log in now.");
    }

    private static String readPassword(Scanner scanner, String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return chars == null ? "" : new String(chars);
        }
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private boolean session(AuthenticatedUser user, Scanner scanner) {
        while (true) {
            printMenu(user);
            String command = scanner.nextLine().trim();
            try {
                switch (command) {
                    case "1" -> create(user, scanner);
                    case "2" -> printAll();
                    case "3" -> find(scanner);
                    case "4" -> changeStatus(user, scanner);
                    case "5" -> { System.out.println("Logged out."); return true; }
                    case "0" -> { return false; }
                    default -> System.out.println("Unknown command.");
                }
            } catch (RuntimeException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }
    }

    private void create(AuthenticatedUser user, Scanner scanner) {
        AccessPolicy.requireCreate(user);
        System.out.print("Enter assignment key (for example, HW-1001): ");
        String key = scanner.nextLine().trim();
        System.out.print("Enter assignment title: ");
        String title = scanner.nextLine().trim();
        service.register(AssignmentId.newId(), key, title);
        System.out.println("Assignment saved with status ASSIGNED.");
    }

    private void printAll() {
        List<AssignmentRecord> assignments = service.findAll();
        if (assignments.isEmpty()) {
            System.out.println("No assignments yet.");
            return;
        }
        assignments.forEach(assignment -> System.out.printf(
                "%s | %s | %s | %s%n",
                assignment.businessKey(), assignment.status(), assignment.title(), assignment.id()));
    }

    private void find(Scanner scanner) {
        System.out.print("Enter assignment key: ");
        AssignmentRecord assignment = service.find(scanner.nextLine().trim());
        System.out.printf("Key: %s, status: %s, title: %s, id: %s%n",
                assignment.businessKey(), assignment.status(), assignment.title(), assignment.id());
    }

    private void changeStatus(AuthenticatedUser user, Scanner scanner) {
        System.out.print("Enter assignment key: ");
        String key = scanner.nextLine().trim();
        System.out.println("Available statuses: ASSIGNED, SUBMITTED, CHECKED, APPROVED");
        System.out.print("Enter new status: ");
        AssignmentStatus status = AssignmentStatus.valueOf(scanner.nextLine().trim().toUpperCase());
        AccessPolicy.requireMove(user, status);
        service.move(key, status);
        System.out.println("Status saved.");
    }

    private static void printMenu(AuthenticatedUser user) {
        System.out.println();
        System.out.println("=== Assignment Management: " + user.username() + " (" + user.role() + ") ===");
        if (user.role() == Role.TEACHER) {
            System.out.println("1. Create assignment");
        }
        System.out.println("2. Show all assignments");
        System.out.println("3. Find assignment");
        System.out.println(user.role() == Role.TEACHER
                ? "4. Check / approve assignment"
                : "4. Submit assignment");
        System.out.println("5. Logout");
        System.out.println("0. Exit");
        System.out.print("Choose an action: ");
    }
}
