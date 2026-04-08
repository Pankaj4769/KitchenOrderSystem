package com.kos.dto;

import jakarta.persistence.*;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_item")
public class CartItem {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int qty;

    // ✅ FIX: renamed column
    @Column(name = "portion_type")
    private String portion;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_item_id")
    private List<Addon> addons;

    private String notes;
    private String image;
    private String category;

    @Column(name = "added_to_cart_status")
    private Boolean addedToCartStatus;

    public CartItem() {}

    public CartItem(String name, BigDecimal price, int qty) {
        this.name = name;
        this.price = price;
        this.qty = qty;
    }

    // Optional enum helpers
    public void setPortionEnum(Portion portionEnum) {
        this.portion = (portionEnum != null) ? portionEnum.name() : null;
    }

    public Portion getPortionEnum() {
        return (portion != null) ? Portion.valueOf(portion) : null;
    }

    // Getters and Setters

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getPortion() { return portion; }
    public void setPortion(String portion) { this.portion = portion; }

    public List<Addon> getAddons() { return addons; }
    public void setAddons(List<Addon> addons) { this.addons = addons; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getAddedToCartStatus() { return addedToCartStatus; }
    public void setAddedToCartStatus(Boolean addedToCartStatus) {
        this.addedToCartStatus = addedToCartStatus;
    }
}