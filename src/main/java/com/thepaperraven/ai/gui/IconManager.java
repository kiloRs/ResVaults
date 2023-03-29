package com.thepaperraven.ai.gui;

import com.thepaperraven.ResourceVaults;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IconManager {
    private final Map<Integer, IconData> iconDataMap;
    private final File file;

    public IconManager(){
        this(new File(ResourceVaults.getPlugin().getDataFolder(),"icons.yml"));
    }
    public IconManager(File file) {
        this.iconDataMap = new HashMap<>();
        this.file = file;
    }

    public void addIconData(int slot, IconData iconData) {
        iconDataMap.put(slot, iconData);
    }

    public void removeIconData(int slot) {
        iconDataMap.remove(slot);
    }

    public void updateIcons(Inventory inventory) {
        for (Map.Entry<Integer, IconData> entry : iconDataMap.entrySet()) {
            int slot = entry.getKey();
            IconData iconData = entry.getValue();
            ItemStack item = iconData.getItem().clone();
            item.setAmount(1);
            inventory.setItem(slot, item);
        }
    }

    public void deserialize(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            int slot = Integer.parseInt(key);
            IconData iconData = IconData.deserialize(section.getConfigurationSection(key));
            addIconData(slot, iconData);
        }
    }

    public void serialize(ConfigurationSection section) {
        for (Map.Entry<Integer, IconData> entry : iconDataMap.entrySet()) {
            int slot = entry.getKey();
            IconData iconData = entry.getValue();
            ConfigurationSection iconSection = section.createSection(String.valueOf(slot));
            iconData.serialize(iconSection);
        }
    }
}
