package com.thepaperraven.config;

import com.thepaperraven.ai.ResourceVaults;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerConfiguration extends YamlConfiguration {

    private final JavaPlugin plugin;
    private final UUID playerUUID;
    private final File configFile;

    public PlayerConfiguration(UUID uuid){
        this((JavaPlugin) ResourceVaults.getPlugin(),uuid);
    }
    public PlayerConfiguration(JavaPlugin plugin, UUID playerUUID) {
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
}
