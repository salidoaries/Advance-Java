package tablemanager.app;

import tablemanager.service.TableService;
import tablemanager.utility.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class TableManagerApp {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Path filePath = null;

        try {
            // Determine file path from argument or prompt
            if (args.length > 0) {
                filePath = Paths.get(args[0]);
                if (!Files.exists(filePath)) {
                    System.out.println("File not found: " + filePath);
                    filePath = promptForFile(scan);
                } else {
                    System.out.println("Using existing file: " + filePath.getFileName());
                }
            } else {
                filePath = promptForFile(scan);
            }

            // Initialize manager
            TableService manager = new TableService(filePath);

            // Load or create table
            if (Files.size(filePath) > 0) {
                manager.loadTable();
                System.out.println("\nLoaded table from: " + filePath.getFileName());
                manager.printTable();
            } else {
                int[] dims = askTableDimensions(scan);
                manager.generateNewTable(dims[0], dims[1]);
                System.out.println("\nNew table created successfully:");
                manager.printTable();
            }

            // Run menu
            runMenu(manager, scan);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            System.out.println("\nExiting program. Goodbye!");
            scan.close();
        }
    }

    // --- Prompt user to create or validate file ---
    private static Path promptForFile(Scanner scan) {
        while (true) {
            System.out.print("Enter file name (without extension for new, or full name for existing): ");
            String input = scan.nextLine().trim();

            try {
                if (FileUtility.fileExists(input)) {
                    System.out.println("File found and ready to load.");
                    return FileUtility.validateFile(input);
                } else {
                    System.out.println("Creating new file...");
                    return FileUtility.createFile(input);
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --- Ask user for table dimensions (rows x cols) ---
    private static int[] askTableDimensions(Scanner scan) {
        while (true) {
            System.out.print("Enter table dimension (e.g., 3x3): ");
            String input = scan.nextLine().trim();
            String[] parts = input.split("x");

            if (parts.length != 2) {
                System.out.println("Invalid format. Use [rows]x[cols].");
                continue;
            }

            try {
                int rows = Integer.parseInt(parts[0].trim());
                int cols = Integer.parseInt(parts[1].trim());
                if (rows <= 0 || cols <= 0) {
                    System.out.println("Rows and columns must be positive.");
                    continue;
                }
                return new int[]{rows, cols};
            } catch (NumberFormatException e) {
                System.out.println("Invalid numbers. Enter digits only.");
            }
        }
    }

    // --- Menu Loop ---
    private static void runMenu(TableService manager, Scanner scan) {
        boolean running = true;

        while (running) {
            System.out.println("\nMenu:");
            System.out.println("[search]  - Search");
            System.out.println("[edit]    - Edit");
            System.out.println("[add_row] - Add Row");
            System.out.println("[sort]    - Sort");
            System.out.println("[print]   - Print");
            System.out.println("[reset]   - Reset");
            System.out.println("[x]       - Exit");
            System.out.print("\nChoose an option: ");

            String choice = scan.nextLine().trim().toLowerCase();

            switch (choice) {
                case "search" -> manager.search();
                case "edit" -> manager.edit();
                case "add_row" -> manager.addRow();
                case "sort" -> manager.sortRow();
                case "print" -> manager.printTable();
                case "reset" -> manager.resetTable();
                case "x" -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
}
