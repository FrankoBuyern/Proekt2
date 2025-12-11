import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {

    private int productId;
    private String name;
    private Category category;
    private BigDecimal price;
    private LocalDate expireDate;
    private String description;

    public Product(int productId, String name, Category category,
                   BigDecimal price, LocalDate expireDate, String description) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price == null ? BigDecimal.ZERO : price;
        this.expireDate = expireDate;
        this.description = description;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
