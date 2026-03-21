package cz.cvut.fel.ts.ts_semestralni_prace.service;

import cz.cvut.fel.ts.ts_semestralni_prace.model.Shop;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShopService {

    private static final String FILENAME = "shops.json";
    private final FileStorageService fileStorageService;

    public ShopService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public List<Shop> getAll() {
        return fileStorageService.readList(FILENAME, Shop.class);
    }

    public List<Shop> getActive() {
        return getAll().stream().filter(Shop::isActive).toList();
    }

    public Optional<Shop> findById(String id) {
        return getAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Shop save(Shop shop) {
        List<Shop> shops = getAll();
        if (shop.getId() == null || shop.getId().isBlank()) {
            shop.setId(UUID.randomUUID().toString());
            shops.add(shop);
        } else {
            boolean found = shops.stream().anyMatch(s -> s.getId().equals(shop.getId()));
            if (found) {
                shops.replaceAll(s -> s.getId().equals(shop.getId()) ? shop : s);
            } else {
                shops.add(shop);
            }
        }
        fileStorageService.writeList(FILENAME, shops);
        return shop;
    }

    public void delete(String id) {
        List<Shop> shops = getAll();
        shops.removeIf(s -> s.getId().equals(id));
        fileStorageService.writeList(FILENAME, shops);
    }
}
