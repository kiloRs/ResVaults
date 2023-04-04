package com.thepaperraven.data.player;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.config.PlayerConfiguration;
import com.thepaperraven.data.vault.Vault;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Data
public class PlayerDataManager {

    private final JavaPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> loadedPlayerData;

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.loadedPlayerData = new HashMap<>();
    }

    public PlayerData getPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = loadedPlayerData.get(uuid);
        if (playerData == null) {
            playerData = loadPlayerData(uuid);
            loadedPlayerData.put(uuid, playerData);
        }
        return playerData;
    }
    public void saveVault(UUID ownerUUID, Vault vault) {
        int vaultIndex = vault.getIndex();
        PlayerConfiguration playerConfig = new PlayerConfiguration(ownerUUID);
        ConfigurationSection vaultSection = playerConfig.getConfigurationSection("vaults");
        if (vaultSection == null) {
            vaultSection = playerConfig.createSection("vaults");
        }
        ConfigurationSection indexConfiguration = vaultSection.getConfigurationSection(String.valueOf(vaultIndex));
        if (indexConfiguration == null) {
            indexConfiguration = vaultSection.createSection(String.valueOf(vaultIndex));
        }
        vault.toConfig(vaultSection);
        playerConfig.save();
    }
    public void loadPlayer(Player player){
        ResourceVaults.getLogger("Attempting to load playerdata....");
        PlayerData playerData = loadPlayerData(player.getUniqueId());
        loadedPlayerData.put(playerData.getPlayerUUID(),playerData);
    }
    private PlayerData loadPlayerData(UUID uuid) {
        File playerDataFile = new File(dataFolder, uuid + ".yml");
        if (!playerDataFile.exists()) {
            return new PlayerData(uuid);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerDataFile);
        PlayerData playerData = new PlayerData(uuid);

        ConfigurationSection vaultsSection = yaml.getConfigurationSection("vaults");
        if (vaultsSection != null) {
            for (String vaultIndex : vaultsSection.getKeys(false)) {
                ConfigurationSection vaultSection = vaultsSection.getConfigurationSection(vaultIndex);
                if (vaultSection != null) {
                    Vault vault = Vault.fromConfigSection(playerData.getConfig(),Integer.parseInt(vaultIndex));
                    if (vault == null){
                        ResourceVaults.error("Major Error while loading Vault in loadingPlayerData!");
                        continue;
                    }
                    playerData.getVaults().put(vault.getIndex(),vault);
                }
            }
        }

        return playerData;
    }

    public void savePlayerData(PlayerData playerData) {
        UUID uuid = playerData.getPlayerUUID();
        for (int i = 0; i < playerData.getVaults().size() ;i++) {
            Vault vault = playerData.getVaults().get(i);
            saveVault(uuid,vault);
        }

        try {
            playerData.getConfig().save();
        } catch (Exception e) {
            throw new RuntimeException("Hard Save Fail for PlayerData: " + playerData.getPlayer());
        }
    }

}
