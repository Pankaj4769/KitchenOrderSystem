package com.kos.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_categories")
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @Column(nullable = false)
    private String name;

    private String icon;

    @Column(nullable = false)
    private String restaurantId;

    public Integer getCategoryId()              { return categoryId; }
    public void   setCategoryId(Integer id)     { this.categoryId = id; }

    public String getName()                     { return name; }
    public void   setName(String name)          { this.name = name; }

    public String getIcon()                     { return icon; }
    public void   setIcon(String icon)          { this.icon = icon; }

    public String getRestaurantId()             { return restaurantId; }
    public void   setRestaurantId(String rid)   { this.restaurantId = rid; }
}
