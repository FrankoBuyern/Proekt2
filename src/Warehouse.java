import java.util.HashMap;
import java.util.Map;

public class Warehouse {

    private int capacity;
    private int freeSpace;
    private int size;
    private final Map<Integer, Integer> stock;

    public Warehouse(int capacity) {
        this.capacity = Math.max(0, capacity);
        this.stock = new HashMap<>();
        this.size = 0;
        this.freeSpace = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getFreeSpace() {
        return freeSpace;
    }

    public int getSize() {
        return size;
    }

    public Map<Integer, Integer> getStock() {
        return stock;
    }

    public boolean addProduct(int productId, int quantity) {
        if (quantity <= 0 || quantity > freeSpace) return false;
        stock.put(productId, stock.getOrDefault(productId, 0) + quantity);
        recalcSpace();
        return true;
    }

    public boolean removeProduct(int productId, int quantity) {
        Integer curr = stock.get(productId);
        if (curr == null || quantity <= 0 || quantity > curr) return false;
        if (quantity == curr) stock.remove(productId);
        else stock.put(productId, curr - quantity);
        recalcSpace();
        return true;
    }

    private void recalcSpace() {
        size = stock.values().stream().mapToInt(Integer::intValue).sum();
        freeSpace = capacity - size;
    }
}
