package com.duckstore.entity;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * A lot of ducks sharing one color, size and unit price.
 *
 * <p>The rule "one active record per color, size and price" is enforced by the database through
 * {@code activeKey}: it holds "RED|LARGE|12.50" while the duck is active and NULL once it is deleted.
 * A unique constraint on that column rejects a second active duplicate whichever database is used
 * (it relies on the database allowing several NULLs in a unique column, as H2, PostgreSQL, MySQL,
 * MariaDB and Oracle do).
 */
@Entity
@Table(name = "ducks", uniqueConstraints = @UniqueConstraint(name = "uk_ducks_active_key", columnNames = "active_key"))
public class Duck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "duck_color", nullable = false, length = 10)
    private Color color;

    @Enumerated(EnumType.STRING)
    @Column(name = "duck_size", nullable = false, length = 10)
    private Size size;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "active_key", length = 40)
    private String activeKey;

    protected Duck() {
        // required by JPA
    }

    public Duck(Color color, Size size, BigDecimal price, int quantity) {
        this.color = color;
        this.size = size;
        this.price = price;
        this.quantity = quantity;
        this.activeKey = activeKeyOf(color, size, price);
    }

    public static String activeKeyOf(Color color, Size size, BigDecimal price) {
        return color.name() + "|" + size.name() + "|" + price.toPlainString();
    }

    /** Only price and quantity can change; color and size are fixed for the life of a record. */
    public void change(BigDecimal newPrice, int newQuantity) {
        this.price = newPrice;
        this.quantity = newQuantity;
        this.activeKey = activeKeyOf(color, size, newPrice);
    }

    /** Logical delete: the row stays, and the unique key is released so the same duck can be added again. */
    public void markDeleted() {
        this.deleted = true;
        this.activeKey = null;
    }

    public Integer getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public Size getSize() {
        return size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
