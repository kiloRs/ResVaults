package com.thepaperraven.ai;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final String path;
    private final Map<Integer, Vault> dataMap;
    private int totalVaultAmount;
    private double totalVaultValue;
    private int totalItemsAmount;
    private final Map<Material, Double> totalValuePerMaterial;
    private final Map<Material, Integer> totalItemsPerMaterial;

    public PlayerData(Player player, String path) {
        this.uuid = player.getUniqueId();
        this.path = path;
        this.dataMap = new HashMap<>();
        this.totalVaultAmount = 0;
        this.totalVaultValue = 0;
        this.totalItemsAmount = 0;
        this.totalValuePerMaterial = new HashMap<>();
        this.totalItemsPerMaterial = new HashMap<>();
    }

    public void load() {
        File playerFile = new File(path + File.separator + uuid + ".yml");
        if (!playerFile.exists()) {
            return;
        }
        FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerFile);
        if (!playerDataConfig.isConfigurationSection("vaults")) {
            return;
        }
        for (String key : playerDataConfig.getConfigurationSection("vaults").getKeys(false)) {
            int id = Integer.parseInt(key);
            Location location = playerDataConfig.getLocation(key + ".location");
            if (location == null || location.getBlock().getType() != Material.CHEST) {
                continue;
            }
            String nameOfMaterial = playerDataConfig.getString(key + ".material", "WHEAT");
            Vault vault = new Vault(id, uuid, location.toBlockLocation(), Material.matchMaterial(nameOfMaterial));
            vault.load(playerDataConfig.getConfigurationSection("vaults." + key));
            dataMap.put(id, vault);
            totalVaultAmount += 1;
            totalVaultValue += vault.getTotalValue();
            for (ItemStack item : vault.getContents()) {
                Material material = item.getType();
                double value = Vault.getValue(material) * item.getAmount();
                totalValuePerMaterial.put(material, totalValuePerMaterial.getOrDefault(material, 0.0) + value);
                totalItemsAmount += item.getAmount();
                totalItemsPerMaterial.put(material, totalItemsPerMaterial.getOrDefault(material, 0) + item.getAmount());
            }
        }
    }

    public void save() {
        File playerFile = new File(path + File.separator + uuid + ".yml");
        FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerFile);
        playerDataConfig.set("vaults", null);
        for (Vault vault : dataMap.values()) {
            ConfigurationSection vaultSection = playerDataConfig.createSection("vaults." + vault.getId());
            vaultSection.set("location", vault.getLocation());
            vaultSection.set("material", vault.getMaterial().toString());
            vault.save(vaultSection);
        }
        try {
            playerDataConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public Vault getVault(int id) {
        return dataMap.get(id);
    }

    public Collection<Vault> getAllVaults() {
        return dataMap.values();
    }

    public int getTotalVaultAmount() {
        return totalVaultAmount;
    }

    public double getTotalVaultValue
