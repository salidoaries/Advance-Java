package tablemanager.model;

public class CellModel {
    private String key;
    private String value;

    public CellModel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + ": " + value;
    }

    // Concatenate key and value if needed
    public String concat() {
        return key + value;
    }
}
