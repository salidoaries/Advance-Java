package tablemanager.service;

import tablemanager.model.CellModel;
import tablemanager.model.RowModel;
import tablemanager.utility.FileUtility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TableManager handles table operations and coordinates with TableRow and Cell classes.
 */
public class TableService {
    private final Path filePath;
    private final Scanner scan = new Scanner(System.in);
    private final Random rnd = new Random();
    private final List<RowModel> table = new ArrayList<>();
    private static final Pattern CELL_SPLIT = Pattern.compile("\\s*;\\s*");

    public TableService(Path filePath) {
        this.filePath = filePath;
    }

    // --- File methods inside the service ---
    public void loadTable() throws IOException {
        table.clear();
        List<String> lines = FileUtility.readFile(filePath.toString());

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            RowModel row = new RowModel();

            // Split cells on semicolon
            String[] rawCells = CELL_SPLIT.split(trimmed);

            for (String rawCell : rawCells) {
                String cell = rawCell.trim();
                if (cell.isEmpty()) {
                    continue;
                }

                // Split key and value on comma, max 2 pieces
                String[] parts = cell.split("\\s*,\\s*", 2);

                String key = parts.length > 0 ? parts[0].trim() : "";
                String value = parts.length > 1 ? parts[1].trim() : "";

                // Skip empty " , " cases
                if (key.isEmpty() && value.isEmpty()) {
                    continue;
                }

                row.addCell(new CellModel(key, value));
            }

            table.add(row);
        }
    }

    public void saveTable() throws IOException {
        List<String> lines = new ArrayList<>();
        for (RowModel row : table) {
            StringBuilder sb = new StringBuilder();
            List<CellModel> cells = row.getCells();
            for (int i = 0; i < cells.size(); i++) {
                CellModel c = cells.get(i);
                sb.append(c.getKey()).append(" , ").append(c.getValue());
                if (i < cells.size() - 1) sb.append(" ; ");
            }
            lines.add(sb.toString());
        }
        FileUtility.writeFile(filePath.toString(), lines);
    }

    // PRINT TABLE
    public void printTable() {
        System.out.println("\nCurrent Table:");
        if (table.isEmpty()) {
            System.out.println("[empty]");
            return;
        }

        for (RowModel row : table) {
            List<CellModel> cells = row.getCells();
            for (int j = 0; j < cells.size(); j++) {
                CellModel c = cells.get(j);
                System.out.printf("%-9s", c.getKey() + " , " + c.getValue());
                if (j < cells.size() - 1) System.out.print(" ; ");
            }
            System.out.println();
        }
    }

    // SEARCH
    public void search() {
        System.out.print("Search term: ");
        String term = scan.nextLine().trim();
        if (term.isEmpty()) {
            System.out.println("Please enter a search term.");
            return;
        }

        int total = 0;
        System.out.println("\nOutput:\n");

        for (int r = 0; r < table.size(); r++) {
            List<CellModel> row = table.get(r).getCells();
            for (int c = 0; c < row.size(); c++) {
                CellModel cell = row.get(c);
                int keyCount = countOccurrencesOverlapping(cell.getKey(), term);
                int valueCount = countOccurrencesOverlapping(cell.getValue(), term);

                if (keyCount > 0 || valueCount > 0) {
                    total += keyCount + valueCount;
                    if (keyCount > 0 && valueCount > 0)
                        System.out.printf("%d <%s> at key and %d <%s> at value of [%d,%d]%n",
                                keyCount, term, valueCount, term, r, c);
                    else if (keyCount > 0)
                        System.out.printf("%d <%s> at key of [%d,%d]%n", keyCount, term, r, c);
                    else
                        System.out.printf("%d <%s> at value of [%d,%d]%n", valueCount, term, r, c);
                }
            }
        }

        if (total == 0) System.out.println("No matches found for \"" + term + "\".");
        else System.out.println("Total matches: " + total);
    }

    private int countOccurrencesOverlapping(String text, String term) {
        int count = 0;
        for (int i = 0; i <= text.length() - term.length(); i++) {
            if (text.regionMatches(true, i, term, 0, term.length())) count++;
        }
        return count;
    }

    // EDIT
    public void edit() {
        if (table.isEmpty()) {
            System.out.println("Table is empty. Load or generate one first.");
            return;
        }

        int rowIdx, colIdx;
        while (true) {
            System.out.print("\nEdit (format [row,col]): ");
            String input = scan.nextLine().trim();
            if (!input.matches("\\d+,\\d+")) {
                System.out.println("Invalid format. Use [row,col] (e.g., 0,2)");
                continue;
            }

            String[] pos = input.split(",");
            try {
                rowIdx = Integer.parseInt(pos[0].trim());
                colIdx = Integer.parseInt(pos[1].trim());

                if (rowIdx < 0 || rowIdx >= table.size()) {
                    System.out.println("Invalid row index.");
                    continue;
                }

                List<CellModel> row = table.get(rowIdx).getCells();
                if (colIdx < 0 || colIdx >= row.size()) {
                    System.out.println("Invalid column index.");
                    continue;
                }

                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid numbers. Use digits only.");
            }
        }

        CellModel cell = table.get(rowIdx).getCells().get(colIdx);
        String option;
        while (true) {
            System.out.print("\nEdit (key/value/both): ");
            option = scan.nextLine().trim().toLowerCase();
            if (option.equals("key") || option.equals("value") || option.equals("both")) break;
            System.out.println("Invalid option.");
        }

        switch (option) {
            case "key" -> editKey(cell);
            case "value" -> {
                System.out.print("New value: ");
                cell.setValue(scan.nextLine());
            }
            case "both" -> {
                editKey(cell);
                System.out.print("New value: ");
                cell.setValue(scan.nextLine());
            }
        }

        try { saveTable(); } catch (IOException e) {
            System.out.println("Failed to save changes: " + e.getMessage());
        }
        System.out.println("Cell updated.\n");
        printTable();
    }

    private void editKey(CellModel cell) {
        while (true) {
            System.out.print("New key: ");
            String newKey = scan.nextLine();
            if (isDuplicateKey(newKey, cell)) {
                System.out.print("Key \"" + newKey + "\" exists. Try again? (y/n): ");
                if (scan.nextLine().trim().equalsIgnoreCase("n")) {
                    System.out.println("Update cancelled.");
                    return;
                }
            } else {
                cell.setKey(newKey);
                break;
            }
        }
    }

    private boolean isDuplicateKey(String key, CellModel currentCell) {
        for (RowModel row : table) {
            for (CellModel c : row.getCells()) {
                if (c == currentCell) continue;
                if (c.getKey().equalsIgnoreCase(key)) return true;
            }
        }
        return false;
    }

    // ADD NEW ROW
    public void addRow() {
        int n = -1;
        while (n <= 0) {
            System.out.print("Number of cells to add: ");
            try {
                n = Integer.parseInt(scan.nextLine().trim());
                if (n <= 0) System.out.println("Enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }

        int after;
        while (true) {
            String rangeMsg = table.isEmpty()
                    ? "(-1 for start)"
                    : "(-1 for start, 0-" + (table.size() - 1) + " to insert after that row)";
            System.out.print("Insert after which row " + rangeMsg + ": ");
            String input = scan.nextLine().trim();

            try {
                after = Integer.parseInt(input);
                if (after < -1 || after > table.size() - 1) {
                    System.out.println("Out of range. Enter a number between -1 and " + (table.size() - 1) + ".");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter an integer.");
            }
        }

        RowModel newRow = new RowModel();
        for (int i = 0; i < n; i++) {
            newRow.addCell(new CellModel(randomAscii(), randomAscii()));
        }

        table.add(after + 1, newRow);

        try { saveTable(); } catch (IOException e) {
            System.out.println("Failed to save changes: " + e.getMessage());
        }
        System.out.println("Row added.");
        printTable();
    }

    // SORT ROW
    public void sortRow() {
        if (table.isEmpty()) {
            System.out.println("Table is empty.");
            return;
        }

        int r = -1;
        while (r < 0 || r >= table.size()) {
            System.out.print("Row to sort (0-" + (table.size() - 1) + "): ");
            try { r = Integer.parseInt(scan.nextLine()); }
            catch (NumberFormatException e) { System.out.println("Invalid input."); }
        }

        String order;
        while (true) {
            System.out.print("Order (asc/desc): ");
            order = scan.nextLine().trim().toLowerCase();
            if (order.equals("asc") || order.equals("desc")) break;
            System.out.println("Invalid order.");
        }

        List<CellModel> row = table.get(r).getCells();
        row.sort(Comparator.comparing(CellModel::concat, String.CASE_INSENSITIVE_ORDER));
        if (order.equals("desc")) Collections.reverse(row);

        try { saveTable(); } catch (IOException e) {
            System.out.println("Failed to save changes: " + e.getMessage());
        }
        System.out.println("Row sorted.\n");
        printTable();
    }

    // RESET TABLE (with user input)
    public void resetTable() {
        System.out.print("Enter new dimensions (rows x cols): ");
        String input = scan.nextLine().trim();
        Matcher m = Pattern.compile("(\\d+)x(\\d+)").matcher(input);

        if (!m.matches()) {
            System.out.println("Invalid format.");
            return;
        }

        int rows = Integer.parseInt(m.group(1));
        int cols = Integer.parseInt(m.group(2));

        generateTable(rows, cols);

        System.out.println("Table reset.");
        printTable();
    }

    // GENERATE TABLE programmatically
    public void generateNewTable(int rows, int cols) {
        generateTable(rows, cols);
    }

    // COMMON HELPER METHOD
    private void generateTable(int rows, int cols) {
        table.clear();
        for (int r = 0; r < rows; r++) {
            RowModel newRow = new RowModel();
            for (int c = 0; c < cols; c++) {
                newRow.addCell(new CellModel(randomAscii(), randomAscii()));
            }
            table.add(newRow);
        }

        try {
            saveTable();
        } catch (IOException e) {
            System.out.println("Failed to save changes: " + e.getMessage());
        }
    }

    // Random ASCII generator
    private String randomAscii() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append((char) (33 + rnd.nextInt(94)));
        return sb.toString();
    }

}
