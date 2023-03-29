package com.thepaperraven.config;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultInstance;
import com.thepaperraven.ai.VaultMetadata;
import com.thepaperraven.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerConfiguration extends YamlConfiguration {

    private final JavaPlugin plugin;
    private final UUID playerUUID;
    private final File configFile;

    public PlayerConfiguration(@NotNull UUID uuid){
        this((JavaPlugin) ResourceVaults.getPlugin(),uuid);
    }
    public PlayerConfiguration(JavaPlugin plugin,@NotNull UUID playerUUID) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
        this.configFile = new File(plugin.getDataFolder() + "/playerdata/" + playerUUID.toString() + ".yml");
    }

    public File getFile(){
        return new File(ResourceVaults.getPlugin().getDataFolder(),"/playerdata/" + playerUUID.toString() + ".yml");
    }
    public void save() {
        try {
            this.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!configFile.exists()) {
            plugin.saveResource("playerdata/" + playerUUID.toString() + ".yml", false);
        }
        try {
            this.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.load();
    }

    public void loadFromFile() {
        if (!getFile().exists()) {
            return;
        }
        FileConfiguration dataConfig = PlayerConfiguration.loadConfiguration(getFile());
        ConfigurationSection vaultsSection = dataConfig.getConfigurationSection("vaults");
        if (vaultsSection == null) {
            return;
        }
        for (String indexStr : vaultsSection.getKeys(false)) {
            int index;
            try {
                index = Integer.parseInt(indexStr);
            } catch (NumberFormatException e) {
                continue;
            }
            ConfigurationSection vaultSection = vaultsSection.getConfigurationSection(indexStr);
            if (vaultSection == null) {
                continue;
            }
            String chest1LocStr = vaultSection.getString("location.chest1");
            if (chest1LocStr == null) {
                continue;
            }
            Location chest1Loc = LocationUtils.getLocationFromString(chest1LocStr);
            if (chest1Loc == null) {
                ResourceVaults.error("Chest location error reading on load from " + index);
                continue;
            }
            Location chest2Loc = null;
            String chest2LocStr = vaultSection.getString("location.chest2");
            if (chest2LocStr != null) {
                chest2Loc = LocationUtils.getLocationFromString(chest2LocStr);

                if (chest2Loc == null){
                    ResourceVaults.error("Chest 2 location error reading on load from " + index);

                }
            }
            String signLocStr = vaultSection.getString("location.sign");
            if (signLocStr == null) {
                continue;
            }
            Location signLoc = LocationUtils.getLocationFromString(signLocStr);
            String materialKey = vaultSection.getString("material");
            if (materialKey == null) {
                continue;
            }
            Material material = Material.matchMaterial(materialKey);
            if (material == null) {
                continue;
            }
            String locked = vaultSection.getString("locked");
            VaultMetadata vaultMetadata = new VaultMetadata(material,playerUUID,index);
            List<Location> chestLocations = new ArrayList<>();
            chestLocations.add(chest1Loc);
            if (chest2Loc != null) {
                chestLocations.add(chest2Loc);
            }
            Vault vault = new Vault(vaultMetadata, chestLocations, signLoc);
            new PlayerData(playerUUID).getVaults().put(index,vault);
        }
    }
    public void saveToFile() {
        PlayerConfiguration config = this;
        // Clear existing vaults section
        config.set("vaults", null);

        // Save each vault to the config
        for (Map.Entry<Integer, VaultInstance> entry : ResourceVaults.getPlayerData(playerUUID).getVaults().entrySet()) {
            int index = entry.getKey();
            Vault vault = ((Vault) entry.getValue());
            VaultMetadata metadata = vault.getMetadata();

            // Create section for this vault
            ConfigurationSection vaultSection = config.createSection("vaults." + index);

            // Save location of chest(s) and sign
            vaultSection.set("location.chest1", LocationUtils.getStringOfLocation(vault.getChestLocations().get(0)));
            if (vault.getChestLocations().size() > 1) {
                vaultSection.set("location.chest2", LocationUtils.getStringOfLocation(vault.getChestLocations().get(1)));
            }
            vaultSection.set("location.sign", LocationUtils.getStringOfLocation(vault.getSignLocation()));

            // Save material and lock status
            vaultSection.set("material", metadata.getAllowedMaterial().getKey().getKey());
            vaultSection.set("locked", vault.isLocked() ? vault.getLockPassword() : null);
        }

        // Save the config to file
        try {
            config.save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
