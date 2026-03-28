package cz.cvut.fel.ts.ts_semestralni_prace.service;

import cz.cvut.fel.ts.ts_semestralni_prace.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private static final String FILENAME = "products.json";
    private final FileStorageService fileStorageService;

    public ProductService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public List<Product> getAll() {
        return fileStorageService.readList(FILENAME, Product.class);
    }

    public List<Product> getByShopId(String shopId) {
        return getAll().stream()
                .filter(p -> p.getShopId().equals(shopId))
                .toList();
    }

    public List<Product> getAvailableByShopId(String shopId) {
        return getAll().stream()
                .filter(p -> p.getShopId().equals(shopId) && p.isAvailable() && p.getStockQuantity() > 0)
                .toList();
    }

    public List<Product> filterByShopId(String shopId, String search, String flavor,
                                         BigDecimal minPrice, BigDecimal maxPrice, boolean onlyAvailable) {
        return getAll().stream()
                .filter(p -> p.getShopId().equals(shopId))
                .filter(p -> search == null || search.isBlank()
                        || p.getName().toLowerCase().contains(search.toLowerCase())
                        || p.getDescription().toLowerCase().contains(search.toLowerCase()))
                .filter(p -> flavor == null || flavor.isBlank()
                        || p.getFlavor().equalsIgnoreCase(flavor))
                .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> !onlyAvailable || (p.isAvailable() && p.getStockQuantity() > 0))
                .toList();
    }

    public List<String> getFlavorsByShopId(String shopId) {
        return getAll().stream()
                .filter(p -> p.getShopId().equals(shopId))
                .map(Product::getFlavor)
                .distinct()
                .sorted()
                .toList();
    }

    public Optional<Product> findById(String id) {
        return getAll().stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Product save(Product product) {
        List<Product> products = getAll();
        if (product.getId() == null || product.getId().isBlank()) {
            product.setId(UUID.randomUUID().toString());
            products.add(product);
        } else {
            boolean found = products.stream().anyMatch(p -> p.getId().equals(product.getId()));
            if (found) {
                products.replaceAll(p -> p.getId().equals(product.getId()) ? product : p);
            } else {
                products.add(product);
            }
        }
        fileStorageService.writeList(FILENAME, products);
        return product;
    }

    public void delete(String id) {
        List<Product> products = getAll();
        products.removeIf(p -> p.getId().equals(id));
        fileStorageService.writeList(FILENAME, products);
    }

    public boolean reduceStock(String productId, int quantity) {
        List<Product> products = getAll();
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                if (p.getStockQuantity() >= quantity) {
                    p.setStockQuantity(p.getStockQuantity() - quantity);
                    if (p.getStockQuantity() == 0) {
                        p.setAvailable(false);
                    }
                    fileStorageService.writeList(FILENAME, products);
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
