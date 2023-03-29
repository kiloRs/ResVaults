package com.thepaperraven.ai.gui;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

@Getter
public class IconData {
    private final String name;
    private final String[] lore;
    private final int customModelData;
    private final ItemStack item;

    public IconData(String name, String[] lore, int customModelData, ItemStack item) {
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
        this.item = item;
    }

    public static IconData deserialize(String name, ConfigurationSection section) {
        String[] lore = section.getStringList("lore").toArray(new String[0]);
        int customModelData = section.getInt("customModelData");
        ItemStack item = serializeItem(section.getConfigurationSection("item"));

        return new IconData(name, lore, customModelData, item);
    }

    public static ItemStack serializeItem(ConfigurationSection section) {
        return section == null ? null : ItemStack.deserialize(section.getValues(true));
    }

    public static IconData deserialize(ConfigurationSection section) {
        String name = section.getString("name", "");
        List<String> lore = section.getStringList("lore");
        int customModelData = section.getInt("customModelData", 0);
        ItemStack item = deserializeItem(section.getConfigurationSection("item"));
        return new IconData(name, lore.toArray(new String[]{}), customModelData, item);
    }

    private static ItemStack deserializeItem(ConfigurationSection section) {
        Material material = Material.getMaterial(section.getString("material"));
        int amount = section.getInt("amount", 1);
        short durability = (short) section.getInt("durability", 0);
        ItemStack item = new ItemStack(material, amount, durability);
        if (section.contains("data")) {
            item.setDurability((short) section.getInt("data"));
        }
        if (section.contains("enchantments")) {
            List<Map<?, ?>> enchantmentList = section.getMapList("enchantments");
            for (Map<?, ?> enchantmentMap : enchantmentList) {
                Enchantment enchantment = Enchantment.getByName(enchantmentMap.get("type").toString());
                int level = (int) enchantmentMap.get("level");
                item.addEnchantment(enchantment, level);
            }
        }
        if (section.contains("itemMeta")) {
            ItemMeta meta = item.getItemMeta();
            ConfigurationSection metaSection = section.getConfigurationSection("itemMeta");
            if (metaSection.contains("displayName")) {
                meta.setDisplayName(metaSection.getString("displayName"));
            }
            if (metaSection.contains("lore")) {
                List<String> lore = metaSection.getStringList("lore");
                meta.setLore(lore);
            }
            if (metaSection.contains("customModelData")) {
                meta.setCustomModelData(metaSection.getInt("customModelData"));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void serialize(ConfigurationSection iconSection) {

    }
}
