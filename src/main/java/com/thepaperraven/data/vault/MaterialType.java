package com.thepaperraven.data.vault;

import lombok.Getter;
import org.bukkit.Material;

public enum MaterialType {
    STONE(Material.STONE),
    LEATHER(Material.LEATHER),
    WHEAT(Material.WHEAT);

    @Getter
    private final Material material;

    MaterialType(Material material) {
        this.material = material;
    }

}
