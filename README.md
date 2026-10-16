# Duck Store

A warehouse application (list, add, edit, delete ducks) and an order-pricing API.

| Part | Technology |
|---|---|
| Frontend | React 18 + Vite |
| Backend | Java 21, Spring Boot 3.3, Spring Data JPA |
| Database | In-memory H2 by default; PostgreSQL (or any JPA-supported database) by configuration |

## Run it

Prerequisites: JDK 17+, Maven 3.9+, Node 18+. No database setup is needed.

**Backend** (http://localhost:8080)

```bash
cd backend
mvn spring-boot:run
```

**Frontend** (http://localhost:5173), in a second terminal

```bash
cd frontend
npm install
npm run dev
```

In development Vite proxies `/api` to the backend, so no CORS setup is needed. Set `BACKEND_URL` if the backend is elsewhere.

**Tests**

```bash
cd backend
mvn test
```

All tests run on the embedded database. No Docker and no external services are needed.

## Switching the database

Persistence uses only portable JPA features (derived queries, one JPQL update, schema generated from the entity), so
changing database is configuration, not code.

| Database | How |
|---|---|
| H2, in memory (default) | Nothing to do. Data lives only while the app runs. Just run `mvn spring-boot:run` |
| PostgreSQL | `mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"`. Defaults: `jdbc:postgresql://localhost:5432/duckstore`, user and password `duckstore`; override with `DB_URL`, `DB_USER`, `DB_PASSWORD`. The database must exist; the tables are created on startup. |
| Any other JPA database | Add its JDBC driver to `pom.xml` and set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`. Hibernate detects the dialect. |

One requirement for any database: a unique column must allow several NULLs (H2, PostgreSQL, MySQL, MariaDB and Oracle do;
SQL Server does not). See "Concurrency" below for why.

## API

### Warehouse

| Method | Path | Notes |
|---|---|---|
| GET | `/api/ducks?sort=quantity&dir=desc` | Active ducks only. `sort`: id, color, size, price, quantity. `dir`: asc, desc |
| GET | `/api/ducks/{id}` | 404 if missing or deleted |
| POST | `/api/ducks` | `{"color":"Red","size":"XLarge","price":200,"quantity":10}`. 201 when created, 200 when merged into an existing duck |
| PUT | `/api/ducks/{id}` | `{"price":210,"quantity":5}`. Only price and quantity change |
| DELETE | `/api/ducks/{id}` | Logical delete, 204 |

### Store

`POST /api/store/orders`

```bash
curl -s localhost:8080/api/store/orders -H 'Content-Type: application/json' -d '{
  "color": "Red", "size": "XLarge", "quantity": 10,
  "destinationCountry": "USA", "shippingMode": "Air"
}'
```

```json
{
  "duckId": 1, "quantity": 10, "unitPrice": 200.00, "packageType": "Wood",
  "protections": ["Polystyrene balls"],
  "subtotal": 2000.00,
  "adjustments": [
    {"code": "PACKAGE_SURCHARGE", "description": "5% surcharge for wood package", "type": "INCREMENT", "amount": 100.00},
    {"code": "DESTINATION_SURCHARGE", "description": "18% surcharge for destination USA", "type": "INCREMENT", "amount": 378.00},
    {"code": "AIR_SHIPPING", "description": "Air shipping: 30 USD per unit", "type": "INCREMENT", "amount": 300.00}
  ],
  "total": 2778.00, "currency": "USD"
}
```

Errors are RFC 7807 problem responses (`detail`, plus `errors` per field for validation): 400 invalid input,
404 unknown duck or nothing in stock, 409 conflicts.

## Structure

```
backend/src/main/java/com/duckstore
  controller/   HTTP only: parse, delegate, map to DTOs
  dto/          request and response shapes (the API contract)
  service/      business logic (DuckService, DuckMerger, PriceResolver, OrderQuoteService, StockLookup port)
    event/      warehouse events, publisher, listeners (Observer)
    packaging/  package type + protection strategies
    pricing/    pricing rules, shipping charge strategies, BreakdownVerifier
  repository/   Spring Data interface and the StockLookup adapter
  entity/       the JPA entity
  common/       shared enums and limits
  util/         Money, Texts, ModeRegistry
  exceptions/   exception types and the single error handler
  config/       CORS, wiring of the configurable services
frontend/src
  api/          fetch client            hooks/        useDucks, useAsyncAction
  components/   DuckTable, DuckForm (add and edit), Field, Modal, ConfirmDialog
```

A change stays local: pricing rules never touch packaging or HTTP; the controller never touches the database; the
database is only known to the repository and the entity.

## Design patterns

| Pattern | Where | What it buys |
|---|---|---|
| Strategy | `ProtectionStrategy` and `ShippingChargeStrategy` (Air, Land, Sea each) | Behaviour that depends on the shipping mode lives in one class per mode, with no `switch`. A new mode is a new class. |
| Template Method | `PercentageRule` and `BulkDiscountRule`, `PackageRule`, `DestinationRule` | The "x% of the total, as a discount or an increment" algorithm is written once; subclasses supply the percent and the label. |
| Chain of responsibility (pipeline) | `PricingService` runs the `PricingRule`s in `@Order` | Each rule sees what earlier rules did. A new rule is a new class. |
| Decorator | `BreakdownVerifier` wraps any `PriceCalculator` | Checks that the itemised lines add up to the total to the cent and that a total is positive. A future rule that breaks the maths fails loudly instead of returning a wrong price, and the pricing code is untouched. |
| Observer | `WarehouseEventPublisher` (subject), `WarehouseListener` (observer), `AuditLogListener`, `LowStockListener` | The use cases publish an event and know nothing about who reacts. Listeners are notified after the transaction commits, and a failing listener cannot break a request. A new reaction is a new class. |
| Builder | `PricingContext` builds the immutable `PriceBreakdown` (rules add lines, `build()` returns the result). In tests, `DuckJson` and `OrderJson` build request bodies. | Construction is separated from use; tests state only the field they care about. |
| Command | `OrderCommand`, `DuckChange` | Use-case inputs are plain objects, independent of HTTP. |
| Registry | `ModeRegistry` | One strategy per shipping mode, checked at startup, written once for packaging and shipping. |
| Facade | `OrderQuoteService` | One entry point over price lookup, packaging and pricing. |
| Adapter | `DuckStockLookup` adapts the Spring Data repository to the `StockLookup` port | Pricing does not know about the database technology. |
| Repository | `DuckRepository` | Business code never sees SQL. |
| DTO | `dto/` | The API contract is independent of the entity and the domain objects. |
| Static factory | `Destination.from`, `Adjustment.increment/discount`, `Duck.activeKeyOf` | Parsing and construction rules sit with the type they belong to. |
| Dependency injection | constructor injection everywhere; `StoreConfig` wires the configurable services | No class creates its own collaborators. |
| Singleton (container-managed) | Every service, rule, strategy, registry and the publisher is a Spring singleton bean; enums are singletons by construction | One shared instance without global state. I did not hand-write a `getInstance()` singleton: it hides dependencies and makes classes hard to test, which works against the decoupling this project is meant to show. |

## SOLID and OOP

**Single responsibility**
* `DuckService` holds the warehouse use cases; `DuckMerger` alone owns the concurrency-safe add; `SortField` alone knows how to order.
* `PricingService` only runs rules and totals them; each rule knows one concern; `Money` does the arithmetic.
* `PackagingService` composes a package type (`PackageTypeSelector`) and a protection (`ProtectionStrategy`).
* Controllers only translate HTTP; the exception handler alone maps errors to responses.

**Open/closed** (extend by adding a class, not by editing one)
* New shipping mode: one `ProtectionStrategy` and one `ShippingChargeStrategy`; startup fails if either is missing.
* New pricing rule: one `PricingRule` class with an `@Order`.
* New error kind: one `ApiException` subclass; the handler does not change.
* New reaction to a warehouse change (an email, a metric): one `WarehouseListener`; `DuckService` does not change.
* New invariant on prices: wrap the calculator in another decorator.

**Liskov substitution**: every strategy and rule honours the interface contract (`mode()` matches what it handles, rules never mutate the context), so `PricingService` and the registries treat them uniformly. `PackagingServiceTest` checks all 15 size and mode combinations.

**Interface segregation**: `PricingRule`, `ProtectionStrategy` and `ShippingChargeStrategy` have one or two methods. Pricing needs one thing from the warehouse, so it sees only `StockLookup`, not the whole repository.

**Dependency inversion**: `StockLookup` is owned by the service layer and implemented by the repository layer (`DuckStockLookup`). `PriceResolver` depends on the abstraction, so its test uses a lambda. `PricingService` and `PriceResolver` are plain classes wired in `StoreConfig`, with no framework annotations.

**OOP concepts**
* *Encapsulation*: `Duck` has no setters; it exposes behaviour (`change`, `markDeleted`) and keeps its unique key consistent. Records are immutable; `PricingContext` hides the running total.
* *Abstraction*: callers use `ProtectionStrategy`, `PricingRule`, `StockLookup`, not implementations.
* *Inheritance*: `ApiException` with `NotFoundException`, `ConflictException`, `BadRequestException`; `PercentageRule` with its three rules.
* *Polymorphism*: the strategy and rule collections are used through their interfaces; `Labelled` enums share one parser.

## Concurrency

Rule: at most one active duck per (color, size, price); adding the same duck again adds to its quantity.

* The entity carries an `activeKey` ("RED|LARGE|12.50", or NULL once deleted) with a **unique constraint**, so the
  database itself refuses a second active duplicate. This is portable, unlike a partial unique index.
* `DuckMerger` adds: atomic `UPDATE ... SET quantity = quantity + n` on the active duck; if no row matched, insert it; if that
  insert loses a race (unique violation), retry, and the retry merges into the winner. No read-modify-write, no lost updates.
* `DuckConcurrencyTest` fires 32 simultaneous adds of the same duck and asserts one record with the exact total.

## Decisions where the requirements were ambiguous

| Ambiguity | Options considered | Decision | Change it |
|---|---|---|---|
| Price is a Double | Double; BigDecimal | `BigDecimal`, 2 decimals. Doubles make "same price" and money sums unreliable. A price like 10.505 is rejected, not rounded. | n/a |
| What "x% of the total cost" refers to | Each percentage on the running total, in the order the brief lists the rules; or every percentage on quantity x price | Running total, with flat shipping charges last so they are never multiplied. The brief's order reads like a pipeline. | `duckstore.pricing.percentage-base=SUBTOTAL` |
| Which price when several exist for a color and size | Cheapest; newest; highest; mix lots; reject | The cheapest lot that has stock. Lots are never mixed in one order. | `duckstore.store.enforce-stock=true` also requires one lot to hold the whole order (409 otherwise) |
| Does an order change stock | Yes; no | No. The brief asks for pricing and packaging; the endpoint is a quote. | n/a |
| Merge versus a deleted duck | Revive the old record; create a new one | Deleted ducks do not count; a new record is created. | n/a |
| Edit a price so it equals another active duck | Merge; reject; overwrite | 409. A merge on edit would be surprising. The unique key enforces it under concurrency. Edits are last-write-wins. | n/a |
| Edit request that includes color or size | Ignore; reject always; reject only if different | Accepted when equal to the stored value, 400 when different. Works for clients that echo the whole duck back. | n/a |
| "Sort by quantity" | Ascending; descending; fixed | Descending by default; field and direction are parameters; sizes sort by magnitude, not alphabetically. | `sort`, `dir` |
| Boundaries | "More than 100", "exceeds 1000" | Strict: 100 gets no discount, 101 does; 1000 gets no air reduction, 1001 does. | n/a |
| Rounding | Round at the end; round each line | Each line rounds half-up to the cent and the total is the sum of the lines, so the breakdown always adds up. | n/a |
| Air discount | Hidden in the air charge; a separate line | A separate itemised line, on the air charge only. | n/a |
| Destination | Free text; fixed list | Any non-blank text. Names and codes for USA, Bolivia, India match case-insensitively ("us", "United States", "BO"); everything else pays the "other" rate. | `Destination` enum |
| Quantity limits | None; a cap | 1 to 1,000,000,000 per add or order (an edit may set 0). A merge that would pass the cap is a 409, never an overflow. | `Limits.MAX_QUANTITY` |

### Pricing example

Red XLarge at 200 USD, 10 units to the USA by air: subtotal 2000.00; wood package +5% = +100.00 (2100.00);
USA +18% of 2100.00 = +378.00 (2478.00); air 30 x 10 = +300.00; total **2778.00**. With `percentage-base=SUBTOTAL` the
USA surcharge is 18% of 2000.00 = 360.00 and the total is 2760.00.

## Limitations

* The order endpoint prices an order; it does not authenticate, persist orders or manage stock.
* The listing is not paginated; sorting happens in the service. That is fine for a warehouse list of this size.
* No frontend automated tests; the business rules are in the backend and are covered there.
