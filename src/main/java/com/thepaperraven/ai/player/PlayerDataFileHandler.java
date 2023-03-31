package com.thepaperraven.ai.player;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.config.PlayerConfiguration;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataFileHandler implements Listener {

    @Getter
    private final Plugin vaults;

    public PlayerDataFileHandler(ResourceVaults vaults) {
        this.vaults = vaults;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent e){
        PlayerData playerData = new PlayerData(e.getPlayer().getUniqueId());

        try {
            save(playerData);
            playerData.getPlayer().sendRawMessage("Saved Player Data!");
            ResourceVaults.log("Saved PlayerData for " + playerData.getPlayer().getName());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e){
        PlayerData playerData = load(e.getPlayer().getUniqueId());

        if (playerData.getVaults().isEmpty()){
            playerData.getPlayer().sendRawMessage("No Vaults Loaded");
        }
        else {
            playerData.getPlayer().sendRawMessage("Vaults Loaded: " + playerData.getVaults().size());
        }
    }

    public static Map<String, Object> serializeVaultInstances(Map<Integer, VaultInstance> vaultInstances) {
        Map<String, Object> serializedVaultInstances = new HashMap<>();
        for (Map.Entry<Integer, VaultInstance> entry : vaultInstances.entrySet()) {
            String key = "vault" + entry.getKey();
            VaultInstance value = entry.getValue();
            serializedVaultInstances.put(key, value.serialize());
        }
        return serializedVaultInstances;
    }

    public static Map<Integer, VaultInstance> deserializeVaultInstances(Map<String, Object> serializedVaultInstances) {
        Map<Integer, VaultInstance> vaultInstances = new HashMap<>();
        for (Map.Entry<String, Object> entry : serializedVaultInstances.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("vault")) {
                Integer index = Integer.parseInt(key.substring(5));
                ConfigurationSection section = (ConfigurationSection) entry.getValue();
                VaultInstance vaultInstance = (VaultInstance) ConfigurationSerialization.deserializeObject(section.getValues(false),VaultInstance.class);
                vaultInstances.put(index, vaultInstance);
            }
        }
        return vaultInstances;
    }

    public static void save(PlayerData playerData) {
        Map<Integer, VaultInstance> vaults = playerData.getVaults();
        PlayerConfiguration config = playerData.getConfig();
        // Serialize vault instances to map
        Map<String, Object> serializedVaultInstances = serializeVaultInstances(vaults);

        // Set serialized vault instances map in config
        config.set("vaults", serializedVaultInstances);

        // Save the config to file
        try {
            config.save(config.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayerData load(UUID uuid) {
        PlayerData playerData = new PlayerData(uuid);
        File file = playerData.getFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return playerData;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.isConfigurationSection("vaults")){
            // Deserialize vault instances from map in config
            Map<String, Object> serializedVaultInstances = config.getConfigurationSection("vaults").getValues(false);
            Map<Integer, VaultInstance> vaultInstances = deserializeVaultInstances(serializedVaultInstances);

            // Set deserialized vault instances map in player data
            playerData.getVaults().putAll(vaultInstances);
        }
        return playerData;
    }


}
