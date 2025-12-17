package tablemanager.model;

import java.util.ArrayList;
import java.util.List;

public class RowModel {
    private final List<CellModel> cells = new ArrayList<>();

    // Add a single cell
    public void addCell(CellModel cell) {
        cells.add(cell);
    }

    public List<CellModel> getCells() {
        return cells;
    }

    public int size() {
        return cells.size();
    }

    @Override
    public String toString() {
        return cells.toString();
    }
}
