import java.math.BigDecimal;

public class User {

    private Gender gender;
    private PsychoType type;
    private BigDecimal cash;

    public User(Gender gender, PsychoType type, BigDecimal cash) {
        this.gender = gender;
        this.type = type;
        this.cash = cash == null ? BigDecimal.ZERO : cash;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public PsychoType getType() {
        return type;
    }

    public void setType(PsychoType type) {
        this.type = type;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public void leave() {
        System.out.println("User leaves.");
    }
}
