import java.math.BigDecimal;

public class AuthUser extends User {

    private int userId;
    private String name;
    private int age;
    private double sale;

    public AuthUser(int userId, String name, int age,
                    Gender gender, PsychoType type, BigDecimal cash, double sale) {
        super(gender, type, cash);
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.sale = sale;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getSale() {
        return sale;
    }

    public void setSale(double sale) {
        this.sale = sale;
    }
}
