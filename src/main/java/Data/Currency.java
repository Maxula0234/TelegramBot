package Data;

public enum Currency {

    USD(431),
    EUR(451),
    RUB(456),
    BYN(0),
    TRY(460);

    private final int code;

    Currency(int code) {
        this.code = code;
    }

    public int getId() {
        return code;
    }
}
