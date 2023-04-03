package com.thepaperraven;

import org.bukkit.block.*;
import org.bukkit.Material;

public enum ContainerType {
    CHEST(Material.CHEST, Chest.class),
    DOUBLE_CHEST(Material.CHEST,Chest.class),
    TRAPPED_CHEST(Material.TRAPPED_CHEST, Chest.class),
    BARREL(Material.BARREL, Barrel.class),
    HOPPER(Material.HOPPER, Hopper.class),
    SHULKER_BOX(Material.SHULKER_BOX, ShulkerBox.class),
    DROPPER(Material.DROPPER, Dropper.class),
    DISPENSER(Material.DISPENSER,Dispenser.class);


    private final Material material;
    private final Class<? extends Container> containerClass;

    ContainerType(Material material, Class<? extends Container> containerClass) {
        this.material = material;
        this.containerClass = containerClass;
    }

    public Material getMaterial() {
        return material;
    }

    public Class<? extends Container> getContainerClass() {
        return containerClass;
    }

    public static ContainerType fromString(String str) {
        for (ContainerType type : values()) {
            if (str.equalsIgnoreCase(type.getMaterial().name())){
                return type;
            }
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        return null;
    }
}
