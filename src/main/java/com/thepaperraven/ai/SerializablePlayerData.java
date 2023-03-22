package com.thepaperraven.ai;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vault {
    private final int id;
    private final UUID owner;
    private final String path;
    @Getter
    private final Map<Integer, ItemStack> items;
    @Getter
    @Setter
    private boolean active;
    private double totalValue;
    private int totalItems;
    private final Map<Material, Integer> itemsPerMaterial;
    private final Map<Material, Double> valuePerMaterial;
    private final Plugin plugin = ResourceVaults.getPlugin();
    private final PlayerData playerData;

    public Vault(int id, UUID owner, PlayerData playerData) {
        this.id = id;
        this.owner = owner;
        this.path = "vaults." + id;
        this.items = new HashMap<>();
        this.active = false;
        this.totalValue = 0;
        this.totalItems = 0;
        this.itemsPerMaterial = new HashMap<>();
        this.valuePerMaterial = new HashMap<>();
        this.playerData = playerData;
    }

    public void load(ConfigurationSection section) {
        active = section.getBoolean("active");
        totalValue = section.getDouble("totalValue");
        totalItems = section.getInt("totalItems");
        items.clear();
        for (String key : section.getKeys(false)) {
            if (key.startsWith("item_")) {
                int slot = Integer.parseInt(key.split("_")[1]);
                ItemStack item = section.getItemStack(key);
                items.put(slot, item);
                Material material = item.getType();
                int amount = item.getAmount();
                double value = calculateValue(material, amount);
                if (itemsPerMaterial.containsKey(material)) {
                    int currentAmount = itemsPerMaterial.get(material);
                    itemsPerMaterial.put(material, currentAmount + amount);
                    double currentValue = valuePerMaterial.get(material);
                    valuePerMaterial.put(material, currentValue + value);
                } else {
                    itemsPerMaterial.put(material, amount);
                    valuePerMaterial.put(material, value);
                }
            }
        }
    }

    public void save(ConfigurationSection section) {
        section.set("active", active);
        section.set("totalValue", totalValue);
        section.set("totalItems", totalItems);
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item != null) {
                section.set("item_" + i, item);
            }
        }
    }
}