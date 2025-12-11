import java.math.BigDecimal;

public class Payment {

    private BigDecimal totalCash;

    public Payment() {
        this.totalCash = BigDecimal.ZERO;
    }

    public Payment(BigDecimal totalCash) {
        this.totalCash = totalCash == null ? BigDecimal.ZERO : totalCash;
    }

    public BigDecimal getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(BigDecimal totalCash) {
        this.totalCash = totalCash;
    }

    public void addAmount(BigDecimal amount) {
        if (amount != null) totalCash = totalCash.add(amount);
    }
}
