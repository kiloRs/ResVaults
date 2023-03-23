package com.thepaperraven.ai;

import com.comphenix.protocol.PacketType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    @Getter
    private final Player player;
    private final UUID uuid;
    private final File dataFile;
    private final YamlConfiguration dataConfig;
    @Getter
    private final Map<Integer, Vault> vaults;
    private int totalVaults;
    private int totalVaultItems;

    private PlayerData(UUID uuid, File dataFile, YamlConfiguration dataConfig) {
        this.uuid = uuid;
        this.dataFile = dataFile;
        this.dataConfig = dataConfig;
        this.vaults = new HashMap<>();
        this.totalVaults = 0;
        this.totalVaultItems = 0;
        this.player = Bukkit.getPlayer(uuid);
    }

    public static PlayerData getPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerDataMap.containsKey(uuid)) {
            File dataFile = new File(ResourceVaults.getPlugin().getDataFolder() + File.separator + "players", uuid + ".yml");
            YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            PlayerData playerData = new PlayerData(uuid, dataFile, dataConfig);
            playerDataMap.put(uuid, playerData);
        }
        return playerDataMap.get(uuid);
    }

    public void save() throws IOException {
        dataConfig.set("Total.Vaults.Accounted", totalVaults);
        dataConfig.set("Total.Vaults.Items", totalVaultItems);

        ConfigurationSection vaultsSection = dataConfig.createSection("Vaults");
        for (Map.Entry<Integer, Vault> entry : vaults.entrySet()) {
            int vaultIndex = entry.getKey();
            Vault vault = entry.getValue();
            ConfigurationSection vaultSection = vaultsSection.createSection(Integer.toString(vaultIndex));
            Location location = vault.getLocation();
            vaultSection.set("Location.World", location.getWorld().getName());
            vaultSection.set("Location.X", location.getBlockX());
            vaultSection.set("Location.Y", location.getBlockY());
            vaultSection.set("Location.Z", location.getBlockZ());
            vaultSection.set("Double", vault.isDouble());

            if (vault.isDouble()) {
                vaultSection.set("DoubleDirection", vault.getDoubleDirection().name());
            }

            vaultSection.set("Material-Type", vault.getMaterialType().name());
            vaultSection.set("Current-Amount", vault.getCurrentAmount());
            vaultSection.set("Creation-Time", vault.getCreationTime());
            vaultSection.set("Locked", vault.isLocked());
            vault.savePDC();
        }

        dataConfig.save(dataFile);
    }

    public void load() {
        if (!file.exists()) {
            return;
        }

        try {
            ConfigurationSection config = YamlConfiguration.loadConfiguration(file);

            // Load the vaults
            if (config.isConfigurationSection("Vaults")) {
                ConfigurationSection vaultsConfig = config.getConfigurationSection("Vaults");

                for (String indexString : vaultsConfig.getKeys(false)) {
                    int index = Integer.parseInt(indexString);
                    ConfigurationSection vaultConfig = vaultsConfig.getConfigurationSection(indexString);

                    // Load the vault's data
                    String worldName = vaultConfig.getString("Location.World");
                    double x = vaultConfig.getDouble("Location.X");
                    double y = vaultConfig.getDouble("Location.Y");
                    double z = vaultConfig.getDouble("Location.Z");
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        continue; // World not loaded, skip
                    }
                    Location location = new Location(world, x, y, z);
                    boolean isDouble = vaultConfig.getBoolean("Double");
                    BlockFace doubleFace = null;
                    if (isDouble) {
                        String doubleFaceString = vaultConfig.getString("DoubleFace");
                        if (doubleFaceString != null) {
                            doubleFace = BlockFace.valueOf(doubleFaceString);
                        }
                    }
                    Material material = Material.valueOf(vaultConfig.getString("MaterialType"));
                    long creationTime = vaultConfig.getLong("CreationTime");
                    double currentAmount = vaultConfig.getDouble("CurrentAmount");
                    boolean locked = vaultConfig.getBoolean("Locked");

                    // Load the vault's PDC data
                    PersistentDataContainer pdc = null;
                    if (location.getBlock().getState() instanceof Chest chest) {
                        if (isDouble) {
                            BlockFace oppositeFace = doubleFace.getOppositeFace();
                            BlockFace leftFace = BlockFaceUtil.rotateBlockFace(oppositeFace, BlockFaceUtil.getRotation(chest.getBlockData()));
                            Location secondLocation = chest.getRelative(leftFace).getLocation();
                            if (secondLocation.getBlock().getState() instanceof Chest secondChest) {
                                pdc = secondChest.getInventory().getPersistentDataContainer();
                            }
                        } else {
                            pdc = chest.getInventory().getPersistentDataContainer();
                        }
                    }

                    Vault vault = new Vault(index, location, isDouble, doubleFace, material, creationTime, currentAmount, locked, pdc);
                    vaults.put(index, vault);
                }
            }

            // Load the total vaults data
            if (config.contains("Total.Vaults.Accounted")) {
                totalVaults = config.getInt("Total.Vaults.Accounted");
            }
            if (config.contains("Total.Vaults.Items")) {
                totalVaultItems = config.getInt("Total.Vaults.Items");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}