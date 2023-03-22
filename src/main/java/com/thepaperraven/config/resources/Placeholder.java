package com.thepaperraven.config.resources;

import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.ResourceVaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "rs";
    }

    @Override
    public String getAuthor() {
        return "KiloBytez";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // Get the player's data
        PlayerData playerData = ResourceVaults.getPlayerData(player);

        // Placeholder: %myplugin_total_vaults%
        if (identifier.equals("total_vaults")) {
            return Integer.toString(playerData.getTotalVaults());
        }

        // Placeholder: %myplugin_total_vaults_[material]%
        if (identifier.startsWith("total_vaults_")) {
            String material = identifier.substring(14).toUpperCase();
            int total = playerData.getTotalVaultsByMaterial(Material.getMaterial(material));
            return Integer.toString(total);
        }

        // Placeholder: %myplugin_total_items%
        if (identifier.equals("total_items")) {
            return Integer.toString(playerData.getTotalItems());
        }

        // Placeholder: %myplugin_total_[material]%
        if (identifier.startsWith("total_")) {
            String material = identifier.substring(6).toUpperCase();
            int total = playerData.getTotalMaterial(Material.getMaterial(material));
            return Integer.toString(total);
        }

        return null;
    }
}
