import java.math.BigDecimal;
import java.util.*;

public class Basket {

    private int basketId;
    private int userId;
    private final Map<Integer, Integer> items;
    private BigDecimal totalPrice;
    private boolean status;

    public Basket(int basketId, int userId) {
        this.basketId = basketId;
        this.userId = userId;
        this.items = new LinkedHashMap<>();
        this.totalPrice = BigDecimal.ZERO;
        this.status = false;
    }

    public int getBasketId() {
        return basketId;
    }

    public int getUserId() {
        return userId;
    }

    public Map<Integer, Integer> getItems() {
        return items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public boolean isStatus() {
        return status;
    }

    public void add(int productId, int quantity, BigDecimal unitPrice) {
        if (status) throw new IllegalStateException("Basket closed");
        items.put(productId, items.getOrDefault(productId, 0) + quantity);
        if (unitPrice != null)
            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public void delete(int productId, int quantity, BigDecimal unitPrice) {
        if (status) throw new IllegalStateException("Basket closed");
        Integer current = items.get(productId);
        if (current == null) return;

        int removed = Math.min(quantity, current);
        if (removed == current) items.remove(productId);
        else items.put(productId, current - removed);

        if (unitPrice != null)
            totalPrice = totalPrice.subtract(unitPrice.multiply(BigDecimal.valueOf(removed)));
    }

    public List<ProductQuantity> getAll() {
        List<ProductQuantity> list = new ArrayList<>();
        items.forEach((id, q) -> list.add(new ProductQuantity(id, q)));
        return list;
    }

    public void buy() {
        this.status = true;
    }
}
