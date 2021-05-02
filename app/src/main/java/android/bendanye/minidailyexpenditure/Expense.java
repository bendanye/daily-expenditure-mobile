package android.bendanye.minidailyexpenditure;

/**
 * Created by benjamin ng on 21/8/2015.
 */
public class Expense {

    private final String description;
    private final String amount;

    public Expense(String description, String amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public String getAmount() {
        return amount;
    }
}
