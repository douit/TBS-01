package sa.tamkeentech.tbs.domain.enumeration;

public enum TypeStatistics {
    MONTHLY(1),
    ANNUAL(2);

    private int value;

    TypeStatistics(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
