# MapStruct Mappers - Advanced Features Documentation

## OVERVIEW
MapStruct mappers demonstrating advanced mapping capabilities with lifecycle callbacks, custom methods, and context objects.

## WHERE TO LOOK

| Task | Location | Description |
|------|----------|-------------|
| Advanced mapping features | `OrderMapper.java` | Complete example with @Before/@AfterMapping, @Named, @Context |
| Currency formatting | `OrderItemMapper.java` | Custom format/parse methods with @Named |
| Expression mapping | `AddressMapper.java` | Java expression for fullAddress combination |
| Orika mappers | `mapper/orika/` | Alternative mapping framework mappers |

## CONVENTIONS

### Mapper Configuration
All mappers MUST use `@Mapper(componentModel = "spring")` to enable Spring dependency injection:
```java
@Mapper(componentModel = "spring", uses = {OtherMapper.class})
public interface OrderMapper { }
```

### Advanced Features

**Lifecycle Callbacks** - Use `@BeforeMapping` and `@AfterMapping` for validation and enrichment:
```java
@BeforeMapping
protected void validateOrder(Order order) { }

@AfterMapping
protected void enrichOrderDto(Order order, @MappingTarget OrderDto dto) { }
```

**Named Methods** - Create reusable mapping logic with `@Named`:
```java
@Named("formatCurrency")
default String formatCurrency(Double amount) {
    return String.format("$%.2f", amount);
}

// Usage in mapping
@Mapping(target = "priceDisplay", source = "price", qualifiedByName = "formatCurrency")
```

**Context Objects** - Track correlation ID and depth across nested mappings:
```java
public abstract OrderDto toOrderDtoWithContext(Order order, @Context MappingContext context);

public static class MappingContext {
    private final String correlationId;
    private int depth = 0;
    // @BeforeMapping methods can modify context
}
```

**Inherit Configuration** - Reuse mapping configs for collections:
```java
@InheritConfiguration(name = "toOrderDto")
public abstract List<OrderDto> toOrderDtoList(List<Order> orders);
```

### Custom Mapping Methods
- `formatDate` / `parseDate` - DateTime ↔ String conversion
- `formatCurrency` / `parseCurrency` - Currency string formatting
- `statusToDisplay` - Enum to display text transformation

## ANTI-PATTERNS

1. ❌ **DO NOT modify generated code** - All `*Impl.java` files in target/ are auto-generated; changes will be lost on recompile
2. ❌ **DO NOT name conflicts** - Use distinct names for `@Named` methods (e.g., `formatOrderCurrency` vs `formatCurrency`)
3. ❌ **DO NOT mix frameworks** - Keep MapStruct and Orika mappers separate in `mapper/orika/` subdirectory
