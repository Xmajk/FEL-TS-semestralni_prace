# PLAN.md — Revize semestrální práce (TS)

Revize pokrývá:
1. dokument `docs/Hrouda.typ`,
2. testy v `src/test/java/**`,
3. soulad s definicemi z přednášek v `docs/source/`.

Žádné soubory zatím nejsou upravované — tento plán popisuje, **co** je potřeba přepracovat a **proč**. Závažnost každé položky je označena:
- **[KRITICKÉ]** — dokument tvrdí něco, co v kódu neexistuje, nebo v dokumentu chybí hlavní náležitost podle přednášek.
- **[STŘEDNÍ]** — nesoulad, který oslabí obhajobu, ale nejde o přímou faktickou chybu.
- **[DROBNÉ]** — překlepy, formátování, zjednodušení.

---

## 1. Přehled shody s přednáškami

| Přednáška | Technika | Stav v práci |
|---|---|---|
| P2 — kombinace vstupů | třídy ekvivalence, BVA, pairwise | EC ✓, pairwise ✓, BVA jen implicitně |
| P3 — průchody (CPT/PCT) | TDL=1/2/3 nad orient. grafem | CPT TDL=2 ✓ (8 cest) |
| P4 — životní cyklus dat | CRUD matice, state transition | **Chybí** — ani zmínka, přestože doména to nabízí (Order má stavy NEW→CONFIRMED→READY→PICKED_UP/CANCELLED) |
| P5 — testovací prostředí a data | izolace, příprava dat, parametrizace | Parametrizace přes CSV ✓, izolace prostředí **chybí v popisu** (nepopsán cleanup, běh proti produkčnímu `./data/`) |
| P6–8 — JUnit, psaní testů | unit, test doubles, isolation | Unit testy v práci **neexistují** (všechny testy jsou E2E Selenium) — chybí argumentace, proč se obejde bez unit / test pyramidy |
| P11 — testovací strategie / BDTM | test goals, rizika, test levels, entry/exit, traceability | **Zásadně nedotažené** — viz sekce 3 |
| E2E (Frajták) | Selenium, page objects, horizontal E2E | ✓ Implementováno, ale v dokumentu není zmíněno ani rozlišení horizontálního/vertikálního E2E |
| Writing Testable Code | smells, isolation, determinism | **Chybí** — v dokumentu žádná reflexe nad kvalitou testů |

---

## 2. Kritické nesoulady dokument ↔ kód

### 2.1 [KRITICKÉ] Chybný popis `IceCreamConfigurationPairwiseTests`

Dokument `docs/Hrouda.typ` řádky 484–488 tvrdí:

> „Testy jsou implementovány jako JUnit 5 parametrizovaný test (`@ParameterizedTest` + `@MethodSource`) ve třídě `ConfiguratorControllerPairwiseTest` a využívají `@WebMvcTest` s mockovanými závislostmi `ShopService` a `CartService`. Pro každý případ se ověřuje, že: — odpověď serveru je přesměrování na `/configurator` (HTTP 3xx), — cena zachycená v `CartItem` odpovídá očekávané hodnotě.“

Realita (`src/test/java/.../IceCreamConfigurationPairwiseTests.java`):
- třída se jmenuje `IceCreamConfigurationPairwiseTests` (ne `ConfiguratorControllerPairwiseTest`),
- používá `@CsvFileSource` (ne `@MethodSource`),
- **rozšiřuje `SeleniumBaseTest`** — nejde o `@WebMvcTest` s mocky, ale o plný Selenium E2E test se skutečným prohlížečem,
- neasertuje HTTP 3xx ani obsah `CartItem` — ověřuje jen text ceny v UI před submitem a přítomnost `alert-success` / nepřítomnost `alert-danger` po submitu.

**Akce:** přepsat celý odstavec podle skutečného stavu. Pokud se má trvat na „WebMvc + mocky“, je třeba naopak doplnit druhou (unit/MVC) třídu testů a v dokumentu pak uvést obě. Totéž opravit v `CLAUDE.md` (sekce „Unit / MVC tests“).

### 2.2 [KRITICKÉ] Chybné URL tabulka zákaznická část

V sekci „Zákaznická část“ (tabulka řádky 186–194) jsou uvedené URL, které neodpovídají routování:

| Doc | Skutečnost v kódu |
|---|---|
| `/order/checkout` | ✓ OK |
| `/order/confirmation` | `/order/confirmation/{orderId}` (doc zamlčuje `{orderId}`) |
| `/order/my-orders` | **není URL** — skutečné je `/order/moje` (`OrderController#myOrders`). `my-orders` je jen název Thymeleaf template. |

**Akce:** sjednotit tabulku s reálnými mappings v `OrderController`.

### 2.3 [KRITICKÉ] Pairwise matematika — 2×3×3×3 ≠ 162

`docs/Hrouda.typ` řádek 421:

> „Namísto úplného kartézského součinu (jenž by u 4 faktorů s hodnotami 2 × 3 × 3 × 3 = 162 kombinací) stačí 9 testů.“

Správný výsledek `2×3×3×3 = 54` (ne 162). 9 z 54 je stále přesvědčivá redukce (~17 %), tak se tím argument neoslabí; chyba jen rozbíjí důvěryhodnost.

**Akce:** opravit na 54.

### 2.4 [KRITICKÉ] Chybí dokumentace test cases pro 3 ze 4 Selenium tříd

Sekce „Implementace testů“:
- `IceCreamConfigurationPairwiseTests` — pairwise tabulka ✓,
- `CreateOrderProcessTests` — tabulka P1..P8 s CPT cestou a popisem ✓,
- `NoAccountClientOrderTests` — pouze odrážky funkcionality, **žádný seznam konkrétních test case** (ve skutečnosti 6 metod: quiz+5),
- `AuthClientOrderTests` — pouze odrážky, **žádné test case** (ve skutečnosti 4 metody),
- `AdminPageTests` — pouze odrážky, **žádné test case** (ve skutečnosti 4 metody).

V přednášce 11 je traceability (požadavek → test → výsledek) uvedena jako klíčový výstup testovací strategie. Bez tabulek test case není možné:
- ověřit pokrytí funkcionality,
- svázat testy s požadavky ze zadání,
- reportovat výsledky.

**Akce:** doplnit pro každou ze 3 zbývajících tříd tabulku ve stylu `CreateOrderProcessTests` (ID, krok/aserce, očekávaný výsledek, mapování na požadavek ze zadání).

### 2.5 [STŘEDNÍ] Nezmíněné stavy objednávky (ztracená CRUD + state transition)

V kódu existuje `OrderStatus` (NEW, CONFIRMED, READY, PICKED_UP, CANCELLED) a `OrderController` + admin panel provádí `updateStatus`. To je typický **state-transition automat** i CRUD nad entitou `Order`. Přednáška 4 tomu věnuje celý blok (CRUD matice, Test datové konzistence, State transition test), přednáška 11 to pak vyžaduje v testovací strategii.

Práce to vůbec nezmiňuje — zkušební otázka „jak jste pokryli životní cyklus objednávky?“ je zde zásadní slabina.

**Akce:**
- doplnit CRUD matici (entity: `Shop`, `Product`, `Order`, `CartItem`, `User`; funkce: admin shop CRUD, admin product CRUD, guest cart, checkout, cancel, status change),
- doplnit state-diagram pro `OrderStatus` a uvést, které přechody pokrývají `AuthClientOrderTests` (NEW→CANCELLED) a `AdminPageTests` (NEW→CONFIRMED),
- otevřeně přiznat, které přechody zatím pokryté nejsou (CONFIRMED→READY, READY→PICKED_UP).

### 2.6 [STŘEDNÍ] Test levels vs. intenzita testování

Sekce „Test levels“ (ř. 219–280) ve skutečnosti popisuje **intenzitu testování / priority** podle přednášky „TS1 intenzita testovani vs techniky“ — třídy rizika A/B/C a volba techniky. Přednáška 1 a 11 ovšem definuje *test levels* jinak: developer/integration/system/UAT.

**Akce:**
- přejmenovat sekci na „Intenzita testování a volba technik“ (nebo „Prioritizace a rizika“),
- případně zvlášť krátce vysvětlit, že v této práci existuje pouze jeden test level (systémové E2E testy) a odůvodnit to.

### 2.7 [STŘEDNÍ] Nesoulad: pokrytí pairwise kolidovalo s uvažovanou hloubkou testu

Pairwise test kombinuje 4 faktory (obal, kopečky, posypky, sušenky), ale:
- pro **konkrétní příchutě** neexistuje žádná kombinatorická strategie — test vybírá vždy první N položek z `TOPPINGS` / `COOKIES` (`selectFirstN`), takže reálně testuje pouze *počet*, ne *konkrétní hodnoty*. To je v rozporu s tezí „pokrývají všechny dvojice faktorů“: faktor „který topping“ není vůbec testován.
- dokument tuto redukci nepřiznává.

**Akce:** v sekci „Faktory a hodnoty“ doplnit poznámku, že faktorem je *kardinalita* (počet), ne konkrétní výběr z výčtu, a v „Třídách ekvivalence“ (E11, E16) samostatně odkázat, že pokrytí „hodnota mimo výčet“ je jen tvrzené, nikoli implementované.

### 2.8 [STŘEDNÍ] CPT TDL=2 v dokumentu nemá matici vstup/výstup pro uzly

Přednáška 3 ukazuje pro hloubku pokrytí 2 povinnou tabulku `větvící bod → kombinace vstupních akcí × výstupních akcí`. V práci je jen odkaz na `CPT-test-depth2-scenarios.json` a obrázek `process-UML.drawio.svg`, ale bez této tabulky. Samotných 8 test cest (P1..P8) nedokládá, že pokrývají *všechny* páry hran v každém uzlu.

**Akce:**
- doplnit tabulku uzlů (A, B, C, …) se vstupními a výstupními hranami,
- pro každý uzel vypsat požadované páry a namapovat je na P1..P8 (analogicky k příkladu ze slide „Příklad pro hloubku pokrytí 2“ v přednášce 3).

---

## 3. Chybějící součásti dle Přednášky 11 (MTP / BDTM)

Dokument postrádá hlavní pilíře testovací strategie dle TMAP/ISTQB. Každá položka je samostatná doplňková sekce:

1. **Test Goals** (overall + departmental) — „Proč testujeme?“ Ne „otestuje to ať to funguje“.
2. **Analýza rizik**: pro každou část aplikace `pravděpodobnost selhání × možné poškození`, z toho odvozená třída rizika. Dnes je v dokumentu uvedena jen třída A/B/C bez výpočtu/zdůvodnění — doplnit dvě tabulky (pravděpodobnost × odhad, možné poškození × odhad) a výslednou konsolidační matici podle příkladu ze slide 24–26 přednášky 11.
3. **Scope** — co je a co není v scope testování (přiznat, že registrace, logout, výchozí stránka a vyhledávání nejsou testované).
4. **Entry / exit criteria** testování (kdy se může začít, kdy jsou testy považovány za hotové — např. „všechny testy P1..P8 + pairwise prošly, žádné A-chyby otevřené“).
5. **Popis testovacího prostředí**:
   - Chrome headless, RANDOM_PORT, JSON storage v `./data/`,
   - není izolace mezi běhy testů (viz 4.4),
   - nezmíněny verze (Spring Boot, Selenium, JUnit, Chrome).
6. **Traceability**: matice požadavek (bod ze zadání) → část aplikace → test class / test case.
7. **Quality characteristics**: dokument se věnuje pouze funkční správnosti. Na minimum zmínit, co se *netestuje* (bezpečnost, výkon, přístupnost).

---

## 4. Problémy v kódu testů

### 4.1 [KRITICKÉ] `OrderConfirmationPage.isOnConfirmationPage` je téměř tautologie

```java
// OrderConfirmationPage.java:17-20
return (
    driver.getCurrentUrl().contains("/order/confirmation") ||
    driver.getCurrentUrl().contains("/order/")
);
```

Druhá podmínka matchuje také `/order/checkout`, `/order/cart`, `/order/moje`, … Tím se stírá rozdíl mezi „objednávka potvrzena“ a „jsme někde v order sekci, třeba pády“. Pokud `CreateOrderProcessTests.path1/path2` skončí po checkoutu redirectem zpět na `/cart` (kvůli chybě), tento test **stejně projde**.

**Akce:** odstranit druhou disjunkci — testy pak spolehlivě detekují úspěch vs. chybu.

### 4.2 [KRITICKÉ] `LoginPage.loginAs` má chybný wait regex

```java
// LoginPage.java:50
.until(ExpectedConditions.urlMatches("\\d+\\/$"));
```

`urlMatches` hledá regex v *celém* URL, ale `\d+\/$` vyžaduje pouze číslice před `/`. Pro `http://localhost:54321/` funguje, ale jen shodou okolností přes `find`-sémantiku. Pokud Spring Security redirektuje po přihlášení na cílovou stránku (ne root), test zamrzne na 10 s až do timeoutu.

**Akce:** buď `ExpectedConditions.urlMatches(".*:\\d+/?$")` pro root, nebo lépe `urlToBe(baseUrl + "/")`.

### 4.3 [STŘEDNÍ] Persistent state / žádný teardown

Aplikace ukládá data do `./data/*.json` (`FileStorageService`). Všechny Selenium testy:
- `AdminPageTests#createShop_asAdmin` přidá skutečný shop,
- `AdminPageTests#addProduct_asAdmin` přidá skutečný produkt,
- `AdminPageTests#restockProduct_asAdmin` přepíše stock existujícího produktu,
- `AuthClientOrderTests#placeOrder_*` přidá skutečné objednávky do `orders.json`,
- `CreateOrderProcessTests#path1/2/8` přidá další objednávky,
- `NoAccountClientOrderTests#completeOrder_asGuest` přidá guest objednávku.

Tyto změny **zůstávají i po běhu testů** a ovlivňují příští běh. To je přímo kodifikovaný *Erratic Test* smell z přednášky „Writing Testable Code“ (obscure test, erratic test).

**Akce:**
- buď izolovat přes `app.data.dir` ukazující do `@TempDir` (vyžaduje Spring profil pro testy),
- nebo přidat `@AfterEach`/`@AfterAll` co umaže přidané entity,
- nebo alespoň v dokumentu **přiznat** tento kompromis a proč se zvolil (plus uvést, že testy se spouštějí proti jednorázově inicializovanému `DataInitializer` stavu).

### 4.4 [STŘEDNÍ] Test order dependence

Všechny Selenium třídy používají `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` s `@Order(1..N)`. V kombinaci s bodem 4.3 vzniká faktická závislost pořadí. Přednáška 6–8 uvádí garanci pořadí jako *nedoporučený* postup pro JUnit 4.11+ a P+TestSmells lekce to explicitně označuje jako smell.

**Akce:** buď testy skutečně izolovat a `@Order` odstranit, nebo v dokumentu přidat větu odůvodňující proč se s pořadím záměrně počítá (pokud je to kvůli „sériové“ produkci dat pro následující testy).

### 4.5 [STŘEDNÍ] Zbytečné přihlášení u pairwise testu

```java
// IceCreamConfigurationPairwiseTests.java:32
loginAsAdmin();
```

Konfigurátor je dle `SecurityConfig` dostupný všem (není pod `/admin/**`). Přihlášení jako admin je zde nepotřebné a přidává ~1s na každý test. Navíc semanticky zavádějící — čtenář si může myslet, že se ověřuje oprávnění admina.

**Akce:** odstranit přihlášení, nebo v dokumentu uvést důvod (např. „sdílená fixture přes SeleniumBaseTest“ — což by ale bylo nevhodné).

### 4.6 [DROBNÉ] Duplicita konstant mezi test class a main src

`IceCreamConfigurationPairwiseTests.TOPPINGS` a `COOKIES` duplikují `ConfiguratorController.TOPPINGS` / `COOKIES`. Pokud se produkční výčet změní, test selže v neočekávaném místě.

**Akce:** buď vytáhnout výčty do samostatné domain třídy a importovat, nebo otevřeně v dokumentu přiznat duplicitu jako záměrný sanity-check.

### 4.7 [DROBNÉ] `SeleniumBaseTest` má zakomentované `driver.wait`

```java
// SeleniumBaseTest.java:63-65
//try {
//    this.driver.wait(1_000);
//}catch (Exception e){}
```

Nepoužitý dead code. (Hlavně: `driver.wait(1000)` by stejně volal `Object#wait`, ne čekání prohlížeče — byla to chyba i tak.)

**Akce:** smazat.

### 4.8 [DROBNÉ] `CLAUDE.md` vs. `SeleniumBaseTest`

`CLAUDE.md` uvádí:

> „Headless mode is commented out in SeleniumBaseTest — enable with `--headless=new` for CI.“

Reálně je na řádku `options.addArguments("--headless=new");` **aktivní**. `CLAUDE.md` tedy také patří do opravy.

---

## 5. Problémy v dokumentu `docs/Hrouda.typ`

### 5.1 Obsah a struktura

- [STŘEDNÍ] Sekce „Testovací strategie“ (ř. 143) by po rozšíření (body 3.1–3.7) měla stát před sekcí „Testy vstupů“ — aktuální pořadí „zadání → popis → priority → vstupy → průchody → implementace“ je logické, ale chybí v tom *proč* (strategie) předcházející *co* (techniky).
- [STŘEDNÍ] Chybí závěrečná sekce „Výsledky testů / nalezené defekty“ — přinejmenším seznam známých chyb v aplikaci (část z nich je dokumentovaná v EC tabulce E5/E10/E11/E15/E16 — vyžaduje vlastní sekci „Seznam nalezených defektů“ mimo tabulku tříd ekvivalence).
- [STŘEDNÍ] Chybí screenshot pro *registraci* (zmíněná v zadání jako požadavek, popsaná v `processes.md` jako proces 1, ale v práci netestovaná).
- [STŘEDNÍ] Chybí screenshot pro *pokladnu (checkout)*, *košík (cart)*, *detail zmrzlinárny* — všechny tyto stránky jsou klíčové pro procesní testy, ale nejsou vyobrazené.

### 5.2 Text a typografie

Pouze drobné:
- ř. 147 „tří rolý“ → „tří rolí“
- ř. 151 „nabýdky“ → „nabídky“
- ř. 153 „správá“ → „správa“
- ř. 214–217 Číslování priorit `1) 1) 2) 3)` — dvě „1)“ jsou záměrné jako stejná priorita? Pokud ano, lépe `1a) 1b) 2) 3)` nebo slovní označení. Pokud překlep, sjednotit.
- ř. 421 `162` → `54` (viz 2.3).
- ř. 610 „optřen“ → „opatřen“
- ř. 631 totéž.
- ř. 637 „Testy se soustředí funkcionality“ → „soustředí na funkcionality“.
- ř. 620 „odzkoušení nemožnosti objednání pokud je celková cena pod nakonfigurovanou“ — nesrozumitelné; pravděpodobně míněno „ověření, že objednávka s celkovou cenou pod minimem (50 Kč) je zamítnuta“.

### 5.3 Tabulka tříd ekvivalence (ř. 317–414)

- [DROBNÉ] E1 „existující ID aktivní zmrzlinárny (např. `shop-1`)“ — uvést pro konzistenci s E2 i invariant „a zmrzlinárna je `active=true`“.
- [STŘEDNÍ] `flavors` — chybí mezní podmínka na `příchuť délka řetězce` (technicky invalid třída, lecture P2 slide 15). Stačí krátce přiznat, že testování technických limitů na serverové straně nebylo provedeno.
- [STŘEDNÍ] U E5, E10, E11, E15, E16 („server akceptuje — chybí validace“) není jasně odkázáno, **který test** tuto třídu pokryl (pokud vůbec). Pairwise test tyto negativní třídy nepokrývá (viz 2.7). Buď doplnit samostatnou negativní sadu (ideálně přes MockMvc, což pak odůvodní bod 2.1 automaticky), nebo tyto řádky označit jako „nalezený defekt v aplikaci — test by selhal“ a přesunout do sekce „Seznam nalezených defektů“.

---

## 6. Doporučený postup zpracování revize

Krok-za-krokem, v pořadí podle dopadu na obhajobu:

1. **Opravit faktické chyby v popisu pairwise testu** (2.1, 2.3) — 10 min, zásadně zvyšuje důvěryhodnost.
2. **Opravit URL tabulku** (2.2) — 5 min.
3. **Rozšířit sekci „Testovací strategie“** podle osnovy MTP/BDTM (kap. 3) — test goals, rizika (matice), scope, entry/exit, prostředí, traceability. Toto je největší blok a nejvíc přidané hodnoty z pohledu předmětu.
4. **Doplnit tabulky test case** pro `NoAccountClientOrderTests`, `AuthClientOrderTests`, `AdminPageTests` (2.4).
5. **Doplnit sekci CRUD + state transition** pro `Order` (2.5) — odůvodnit, proč není dělaná a co by doplňovalo.
6. **Doplnit CPT TDL=2 tabulku uzlů** (2.8).
7. **Přejmenovat „Test levels“** na „Intenzita a priority“, doplnit výpočet rizika (2.6, 3.2).
8. **Doplnit sekci „Nalezené defekty“** (5.3) — explicitně přesunout E5/E10/E11/E15/E16 do „server neprovádí max. validaci / validaci výčtu“ se severity a reprodukcí.
9. **Opravit kódové smells** (4.1, 4.2) — kritické, protože tiše kazí vypovídací hodnotu testů.
10. **Zvážit izolaci testovacího prostředí** (4.3) — buď implementovat (profilem + `@TempDir`), nebo alespoň v dokumentu přiznat.
11. **Pro-forma úklid** (4.4, 4.5, 4.6, 4.7, 4.8, 5.2).
12. **Finální aktualizace `CLAUDE.md`** podle nové reality (aby se výklad nelišil od dokumentu).

---

## 7. Co NEMĚNIT

- **Výběr technik** (EC, pairwise, CPT TDL=2, E2E přes Selenium) — je konzistentní s přednáškami 2, 3 a E2E lekcí. Jen chybí zdůvodnění (bod 3 výše).
- **Rozdělení testů do 5 tříd** — odpovídá priority-based rozdělení A/B/C.
- **Page Object pattern** v Selenium testech — dobře strukturováno.
- **CSV data-driven testování** pro pairwise a kvíz — přesně odpovídá přednášce 5 („parametrizace testovacího scénáře“).

---

Plán připraven. Pokud souhlasíš s prioritami (sekce 6), první kroky 1–2 jsou rychlé opravy; kroky 3–8 jsou hlavní obsahová rozšíření dokumentu; kroky 9–10 jsou úpravy v kódu testů.
