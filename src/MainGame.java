import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Console game: seller must confirm payments and restock.
 * Puts nice ASCII formatting and ANSI colors.
 */
public class MainGame {

    // ANSI color codes (may not work on some consoles)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BLUE = "\u001B[34m";

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Random RND = new Random();

    private final Warehouse warehouse;
    private final List<Product> productCatalog;
    private final Payment paymentBox = new Payment();
    private int nextBasketId = 1;
    private boolean running = true;
    private final long intervalMillis;

    public MainGame(long intervalMillis) {
        this.intervalMillis = intervalMillis;
        this.productCatalog = new ArrayList<>();
        this.warehouse = new Warehouse(200); // стартовая вместимость
        initCatalogAndStock();
    }

    private void initCatalogAndStock() {
        // Создаём набор товаров и начальное количество на складе.
        Product apple = new Product(1, "Apple", Category.FOOD, new BigDecimal("0.50"),
                LocalDate.now().plusDays(10), "Fresh red apple");
        Product milk = new Product(2, "Milk 1L", Category.FOOD, new BigDecimal("1.20"),
                LocalDate.now().plusDays(7), "Whole milk 1L");
        Product usbCable = new Product(3, "USB Cable", Category.ELECTRONICS, new BigDecimal("5.00"),
                null, "USB-A to USB-C cable");
        Product bread = new Product(4, "Bread", Category.FOOD, new BigDecimal("0.80"),
                LocalDate.now().plusDays(3), "Loaf of bread");
        Product jeans = new Product(5, "Jeans", Category.CLOTHING, new BigDecimal("25.00"),
                null, "Blue denim jeans");
        Product pen = new Product(6, "Pen", Category.HOME, new BigDecimal("0.30"),
                null, "Ballpoint pen");
        Product mug = new Product(7, "Coffee Mug", Category.HOME, new BigDecimal("7.50"),
                null, "Ceramic mug 350ml");

        productCatalog.add(apple);
        productCatalog.add(milk);
        productCatalog.add(usbCable);
        productCatalog.add(bread);
        productCatalog.add(jeans);
        productCatalog.add(pen);
        productCatalog.add(mug);

        // Начальные остатки (умышленно небольшие для динамики игры)
        warehouse.addProduct(apple.getProductId(), 10);
        warehouse.addProduct(milk.getProductId(), 8);
        warehouse.addProduct(usbCable.getProductId(), 3);
        warehouse.addProduct(bread.getProductId(), 12);
        warehouse.addProduct(jeans.getProductId(), 2);
        warehouse.addProduct(pen.getProductId(), 25);
        warehouse.addProduct(mug.getProductId(), 5);

        printlnBanner("STORE INITIALIZED", ANSI_CYAN);
        System.out.println("Catalog:");
        productCatalog.forEach(p -> System.out.println("  " + p.getProductId() + ": " + p.getName() + " - " + p.getPrice()));
        System.out.println();
        System.out.println("Warehouse: " + warehouse);
        System.out.println("Interval between customers: " + (intervalMillis/1000) + "s");
        System.out.println("Type 'help' and press Enter for commands.");
        System.out.println();
    }

    public void run() {
        Thread inputThread = new Thread(this::consoleCommandLoop, "console-loop");
        inputThread.setDaemon(true);
        inputThread.start();

        while (running) {
            simulateCustomerArrival();
            // Sleep in chunks so exit is responsive
            long slept = 0;
            long chunk = 500;
            try {
                while (slept < intervalMillis && running) {
                    Thread.sleep(Math.min(chunk, intervalMillis - slept));
                    slept += chunk;
                }
            } catch (InterruptedException ignored) {}
        }

        printlnBanner("GAME ENDED", ANSI_RED);
        System.out.println("Final warehouse: " + warehouse);
        System.out.println("Cash in register: " + paymentBox.getTotalCash());
    }

    private void consoleCommandLoop() {
        while (running) {
            System.out.print(ANSI_BLUE + "> " + ANSI_RESET);
            String line;
            try {
                if (!SCANNER.hasNextLine()) break;
                line = SCANNER.nextLine().trim();
            } catch (NoSuchElementException e) { break; }
            if (line.isEmpty()) continue;
            handleCommand(line);
        }
    }

    private void handleCommand(String line) {
        String[] parts = line.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help":
                printHelp();
                break;
            case "status":
                System.out.println(ANSI_YELLOW + "Warehouse: " + ANSI_RESET + warehouse);
                System.out.println(ANSI_YELLOW + "Register: " + ANSI_RESET + paymentBox.getTotalCash());
                break;
            case "products":
                productCatalog.forEach(p -> System.out.println("  " + p));
                break;
            case "restock":
                if (parts.length < 3) {
                    System.out.println("Usage: restock <productId> <amount>");
                } else {
                    try {
                        int pid = Integer.parseInt(parts[1]);
                        int amount = Integer.parseInt(parts[2]);
                        if (amount <= 0) {
                            System.out.println("Amount must be > 0");
                            break;
                        }
                        if (findProduct(pid) == null) {
                            System.out.println("Product id=" + pid + " not found");
                            break;
                        }
                        warehouse.addProduct(pid, amount);
                        System.out.println("Restocked productId=" + pid + " by " + amount);
                    } catch (NumberFormatException ex) {
                        System.out.println("Number format error");
                    }
                }
                break;
            case "exit":
                running = false;
                break;
            default:
                System.out.println("Unknown command. Type 'help'.");
        }
    }

    private void printHelp() {
        printlnBanner("HELP", ANSI_GREEN);
        System.out.println(" help              - show this help");
        System.out.println(" status            - show warehouse + register");
        System.out.println(" products          - list products");
        System.out.println(" restock id amt    - restock product by amount");
        System.out.println(" exit              - exit game");
        System.out.println();
    }

    private void simulateCustomerArrival() {
        AuthUser customer = randomCustomer();
        List<ProductQuantity> desired = randomCart();

        printlnBanner("CUSTOMER ARRIVED", ANSI_YELLOW);
        System.out.println("ID=" + customer.getUserId() + " Name=" + customer.getName()
                + " Cash=" + customer.getCash());
        System.out.println("Wants:");
        desired.forEach(pq -> {
            Product p = findProduct(pq.getProductId());
            String n = p == null ? ("id:" + pq.getProductId()) : p.getName();
            System.out.println("  " + n + " x" + pq.getQuantity());
        });

        Basket basket = new Basket(nextBasketId++, customer.getUserId());

        for (ProductQuantity pq : desired) {
            Product pr = findProduct(pq.getProductId());
            if (pr == null) continue;
            int want = pq.getQuantity();
            Integer available = warehouse.getStock().get(pq.getProductId());
            int have = available == null ? 0 : available;
            if (have >= want) {
                boolean removed = warehouse.removeProduct(pq.getProductId(), want);
                if (removed) {
                    basket.add(pq.getProductId(), want, pr.getPrice());
                    System.out.println(ANSI_GREEN + " Reserved " + want + " x " + pr.getName() + ANSI_RESET);
                } else {
                    System.out.println(ANSI_RED + " Failed to reserve " + pr.getName() + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_RED + " Not enough " + pr.getName() + " in stock (have " + have + ", need " + want + ")" + ANSI_RESET);
                boolean decision = askYesNo("Restock now? (y/n): ");
                if (decision) {
                    int add = askInt("Enter amount to add (>0): ");
                    if (add > 0) {
                        warehouse.addProduct(pr.getProductId(), add);
                        System.out.println(" Restocked " + add + " units. Trying to reserve...");
                        if (warehouse.removeProduct(pr.getProductId(), want)) {
                            basket.add(pr.getProductId(), want, pr.getPrice());
                            System.out.println(ANSI_GREEN + " Reserved " + want + " x " + pr.getName() + ANSI_RESET);
                        } else {
                            System.out.println(ANSI_RED + " Still failed to reserve " + pr.getName() + ANSI_RESET);
                        }
                    } else {
                        System.out.println("Skipping " + pr.getName());
                    }
                } else {
                    System.out.println("Skipping " + pr.getName());
                }
            }
        }

        if (basket.getItems().isEmpty()) {
            System.out.println("Basket empty — customer leaves.");
            return;
        }

        System.out.println(ANSI_CYAN + "Total: " + basket.getTotalPrice() + ANSI_RESET);
        boolean confirm = askYesNo("Confirm payment at register? (y/n): ");
        if (!confirm) {
            basket.getItems().forEach((pid, qty) -> warehouse.addProduct(pid, qty));
            System.out.println("Payment declined by seller. Items returned to stock.");
            return;
        }

        if (customer.getCash().compareTo(basket.getTotalPrice()) >= 0) {
            customer.setCash(customer.getCash().subtract(basket.getTotalPrice()));
            paymentBox.addAmount(basket.getTotalPrice());
            basket.buy();
            System.out.println(ANSI_GREEN + "Payment successful. Customer leaves." + ANSI_RESET);
            System.out.println("Seller's register now: " + paymentBox.getTotalCash());
        } else {
            System.out.println(ANSI_RED + "Customer has insufficient funds. Transaction rolled back." + ANSI_RESET);
            basket.getItems().forEach((pid, qty) -> warehouse.addProduct(pid, qty));
            System.out.println("Items returned to stock.");
        }
    }

    // Helpers

    private Product findProduct(int productId) {
        return productCatalog.stream().filter(p -> p.getProductId() == productId).findFirst().orElse(null);
    }

    private AuthUser randomCustomer() {
        int userId = 1000 + RND.nextInt(9000);
        String name = randomName();
        int age = 18 + RND.nextInt(50);
        Gender gender = RND.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        PsychoType type = PsychoType.values()[RND.nextInt(PsychoType.values().length)];

        // Use BigDecimal.valueOf to avoid locale issues
        BigDecimal cash = BigDecimal.valueOf(RND.nextDouble() * 100)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        double sale = 0.0;
        return new AuthUser(userId, name, age, gender, type, cash, sale);
    }

    private String randomName() {
        String[] names = {"Ivan", "Olga", "Alex", "Maria", "Petr", "Anna", "Sergey", "Dmitry", "Elena"};
        return names[RND.nextInt(names.length)];
    }

    private List<ProductQuantity> randomCart() {
        int itemsCount = 1 + RND.nextInt(3); // 1..3 different products
        List<Product> shuffled = new ArrayList<>(productCatalog);
        Collections.shuffle(shuffled, RND);
        List<ProductQuantity> list = new ArrayList<>();
        for (int i = 0; i < itemsCount && i < shuffled.size(); i++) {
            Product p = shuffled.get(i);
            int qty = 1 + RND.nextInt(5); // 1..5
            list.add(new ProductQuantity(p.getProductId(), qty));
        }
        return list;
    }

    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(ANSI_BLUE + prompt + ANSI_RESET);
            String line = safeReadLine();
            if (line == null) return false;
            line = line.trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("Please input y or n.");
        }
    }

    private int askInt(String prompt) {
        while (true) {
            System.out.print(ANSI_BLUE + prompt + ANSI_RESET);
            String line = safeReadLine();
            if (line == null) return 0;
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number, try again.");
            }
        }
    }

    private String safeReadLine() {
        try {
            if (!SCANNER.hasNextLine()) return null;
            return SCANNER.nextLine();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    private static void printlnBanner(String title, String color) {
        String bar = "========================================";
        System.out.println(color + bar);
        System.out.printf("= %s%n", title);
        System.out.println(bar + ANSI_RESET);
    }

    public static void main(String[] args) {
        long intervalMs = 60_000L;
        if (args.length > 0 && "fast".equalsIgnoreCase(args[0])) {
            intervalMs = 5_000L;
        }
        MainGame game = new MainGame(intervalMs);
        game.run();
    }
}
