# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

Semestrální práce z předmětu Testování softwaru (TS) na FEL ČVUT. Testovaná aplikace je Spring Boot e-shop pro objednávání zmrzliny. Dokumentace k testování se píše v Typstu (`docs/Hrouda.typ`). Zdrojové materiály (přednášky) jsou v `docs/source/`.

## Commands

```bash
# Build & run
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ConfiguratorControllerPairwiseTest

# Run a single test method
mvn test -Dtest=ConfiguratorControllerPairwiseTest#addCustomIceCream_pairwise_correctPrice

# Build without running tests
mvn package -DskipTests
```

## Architecture

**Persistence** — no database. All data (`shops.json`, `products.json`, `users.json`, `orders.json`) is stored as JSON files in `./data/` via `FileStorageService`. `DataInitializer` seeds shops, products, and two users (`admin` / `admin123`, `jan` / `jan123`) on first startup.

**Cart** — stored in `HttpSession` (key `"cart"`), not persisted. `CartService` enforces single-shop constraint: adding an item from a different shop than what's already in the cart throws `IllegalStateException`.

**Security** — Spring Security with `UserService` as `UserDetailsService`. `/admin/**` requires `ROLE_ADMIN`; everything else is `permitAll()`. CSRF is active (default Spring Security behaviour).

**Configurator** — `ConfiguratorController` handles `POST /configurator/add`. Calculates price server-side: container (kornout 15 Kč / kelimek 10 Kč) + scoops × 25 + toppings × 10 + cookies × 15. Validates only `shopId`; max-count and enum validation for flavors/toppings/cookies is intentionally missing (known defects documented in `docs/Hrouda.typ`).

**Testing layer** — use `@WebMvcTest` + `@MockitoBean` for controller tests, `.with(csrf())` from `SecurityMockMvcRequestPostProcessors` for POST requests.

## Documentation

`docs/Hrouda.typ` is the main test report (Typst). Compile with `typst compile docs/Hrouda.typ`. Requires `docs/coverCVUT.typ` template and `docs/imgs/logo_CVUT.jpg`.
