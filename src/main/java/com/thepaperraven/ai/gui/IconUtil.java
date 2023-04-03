package com.thepaperraven.ai.gui;

import com.thepaperraven.ResourceVaults;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class IconUtil {

    public static ItemStack getIcon(String iconName) {
        File file = new File(ResourceVaults.getPlugin().getDataFolder(), "icons.yml");
        if (!file.exists()){
            ResourceVaults.error("No File Icons.yml");
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection iconSection = config.getConfigurationSection(iconName);

        if (iconSection == null) {
            ResourceVaults.getPlugin().getLogger().warning("Icon section for " + iconName + " not found in config.");
            return null;
        }

        Material material = Material.getMaterial(iconSection.getString("material"));
        if (material == null) {
            ResourceVaults.getPlugin().getLogger().warning("Invalid material for icon " + iconName + ": " + iconSection.getString("material"));
            return null;
        }

        String displayName = ChatColor.translateAlternateColorCodes('&', iconSection.getString("display-name",iconName));
        int customModelData = iconSection.getInt("custom-model-data", 0);

        ItemStack icon = new ItemStack(material);
        ItemMeta iconMeta = icon.getItemMeta();
        iconMeta.setDisplayName(displayName);
        if (customModelData > 0) {
            iconMeta.setCustomModelData(customModelData);
        }
        icon.setItemMeta(iconMeta);

        return icon;
    }

}
