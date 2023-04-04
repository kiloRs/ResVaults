package com.thepaperraven.config;

import com.thepaperraven.data.player.PlayerData;
import com.thepaperraven.ResourceVaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "rs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "KiloBitez";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return null;
        }
        if (identifier.isEmpty()){
            return null;
        }

        // Get the player's data
        PlayerData playerData = ResourceVaults.getPlayerData(player.getUniqueId());

        // Placeholder: %myplugin_total_vaults%
        if (identifier.equals("total_vaults")) {
            int totalVaults = playerData.getTotalHandler().getTotalVaults();
            return Integer.toString(totalVaults);
//            return ResourceVaults.getVaultManager().getTotalVaults(player.getUniqueId()) + "";
        }

        // Placeholder: %myplugin_total_vaults_[material]%
        if (identifier.startsWith("total_vaults_")) {
            String material = identifier.substring(14).toUpperCase();
            Material mats = Material.matchMaterial(material);
            if (mats != null){
                int total = playerData.getTotalHandler().getTotalVaults(mats);
                return Integer.toString(total);
            }
        }

        // Placeholder: %myplugin_total_items%
        if (identifier.equals("total_items")) {
            return Integer.toString(playerData.getTotalHandler().getTotalItems());
        }

        // Placeholder: %myplugin_total_[material]%
        if (identifier.startsWith("total_")) {
            String material = identifier.substring(6).toUpperCase();
            Material mats = Material.matchMaterial(material);
            if (mats != null){
                return String.valueOf(playerData.getTotalHandler().getTotalItems(mats));
            }
            else {
                ResourceVaults.log("Placeholder has invalid Material name: " + material);
            }
        }

        return null;
    }
}
