package com.example.homework.config;
import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentStatus;
import org.springframework.boot.CommandLineRunner;

public class DemoRunner implements CommandLineRunner {
    private final AssignmentService service;

    public DemoRunner(AssignmentService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        AssignmentId id = AssignmentId.newId();
        log.info("ASSIGNED -> {}", service.move(id, AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED));
        try {
            service.move(id, AssignmentStatus.APPROVED, AssignmentStatus.SUBMITTED);
        } catch (IllegalStateException e) {
            log.info("Rejected: {}", e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("---------- MENU ----------");
        System.out.println("1. Create assignment");
        System.out.println("2. Show all assignments");
        System.out.println("3. Submit assignment");
        System.out.println("4. Mark assignment as checked");
        System.out.println("5. Approve assignment");
        System.out.println("6. Show assignment status");
        System.out.println("0. Exit");
    }

    private void createAssignment(Scanner scanner) {
        System.out.print("Assignment ID (e.g. HW-001): ");
        String id = scanner.nextLine();
        System.out.print("Assignment title: ");
        String title = scanner.nextLine();
        service.create(id, title);
        System.out.println("Assignment created. Status: ASSIGNED");
    }

    private void showAssignments() {
        Map<String, AssignmentStatus> assignments = service.list();
        if (assignments.isEmpty()) {
            System.out.println("No assignments yet. Choose 1 to create one.");
            return;
        }
        System.out.println("ID       | TITLE | STATUS");
        System.out.println("---------+-------+--------");
        assignments.forEach((id, status) ->
                System.out.printf("%-8s | %-30s | %s%n", id, service.titleOf(id), status));
    }

    private void changeStatus(Scanner scanner, String action) {
        System.out.print("Assignment ID: ");
        String id = scanner.nextLine();
        AssignmentStatus previous = service.statusOf(id);
        AssignmentStatus next = switch (action) {
            case "submit" -> service.submit(id);
            case "check" -> service.check(id);
            case "approve" -> service.approve(id);
            default -> throw new IllegalStateException("Unsupported action: " + action);
        };
        System.out.println("Status changed: " + previous + " -> " + next);
    }

    private void showStatus(Scanner scanner) {
        System.out.print("Assignment ID: ");
        String id = scanner.nextLine();
        System.out.println(service.titleOf(id) + " — status: " + service.statusOf(id));
    }
}
