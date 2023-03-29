package com.thepaperraven;

import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultInstance;
import com.thepaperraven.ai.VaultManager;
import com.thepaperraven.commands.Command;
import com.thepaperraven.config.Placeholder;
import com.thepaperraven.listeners.VaultBlockListener;
import com.thepaperraven.listeners.VaultInventoryListener;
import com.thepaperraven.listeners.VaultSaveLoadListener;
import com.thepaperraven.listeners.VaultSignChangeListener;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * The main class of the RSVaults plugin!
 */
public class ResourceVaults extends JavaPlugin {

    @Getter
    private static Plugin plugin;
//    private static IconManager iconManager;

    public static void log(String s) {
        Logger.getLogger("Minecraft").info("[ResourceVaults]" + s);
    }

    public static void error(String error) {
        Logger.getLogger("Minecraft").severe("[ResourceVaults] " + error);
    }

    public static VaultManager getVaultManager() {
        return new VaultManager((ResourceVaults.getPlugin()));
    }

    public static void reloadPlugin(boolean savePlayersFirst){
        Bukkit.savePlayers();

        plugin.reloadConfig();

//        iconManager.load();


        if (savePlayersFirst){
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = getPlayerData(onlinePlayer.getUniqueId());
                playerData.getVaults().forEach((integer, vaultInstance) -> {
                    playerData.saveVault(((Vault) vaultInstance));});
            }
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            getPlayerData(onlinePlayer.getUniqueId()).getVaults().forEach((integer, vaultInstance) -> getPlayerData(onlinePlayer.getUniqueId()).loadVault(integer));
        }

    }
    @Override
    public void onEnable() {
        plugin = this;

//        iconManager = new IconManager(new File(plugin.getDataFolder(),"icons.yml"));
        // Register commands!
        getCommand("rv").setExecutor(new Command());

        saveDefaultConfig();
        saveResourceFile();
        // Register listeners!
        registerListeners(this);

        getLogger().info("ResourceVaults plugin enabled!");

        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            log("PAPI not enabled?");
            return;
        }

        // Register PAPI Expansion!
        try {
            new Placeholder().register();
            getLogger().info("Registered PAPI Placeholder : %vaults_'type'% && %vaults_all%");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        saveConfig();
    }

    @NotNull
    public static String replacePlaceholders(Player player, String message) {
        if (player == null) {
            return message;
        }

        return PlaceholderAPI.setPlaceholders(player, message);
    }

    @Override
    public void onDisable() {
        getLogger().info("ResourceVaults plugin disabled!");

        savePlayers();
    }

    private static void savePlayers() {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            PlayerData playerData = PlayerData.get(onlinePlayer.getUniqueId());
            for (Map.Entry<Integer, VaultInstance> entry : playerData.getVaults().entrySet()) {
                Integer integer = entry.getKey();
                VaultInstance vaultInstance = entry.getValue();
                playerData.saveVault(((Vault) vaultInstance));
            }
        }
    }

    private void registerListeners(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new VaultBlockListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new VaultInventoryListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new VaultSignChangeListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new VaultSaveLoadListener(this), plugin);
    }

    public static PlayerData getPlayerData(UUID own) {
        return new PlayerData(own);
    }
    public static void getLogger(int level, String text){
        RVLogger.getInstance().log(level, text);
    }
    public void saveResourceFile() {
        // Get the plugin's data folder
        File dataFolder = getDataFolder();

        // Create the plugin folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        // Get the resource file as an input stream
        InputStream inputStream = getResource("icons.yaml");

        if (inputStream != null) {
            // Create the file object to save the resource to
            File outputFile = new File(dataFolder, "icons.yaml");

            try {
                // Create the output stream to write the file
                OutputStream outputStream = new FileOutputStream(outputFile);

                // Copy the resource file to the output stream
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                // Close the input and output streams
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public FileConfiguration loadFile(){
        return YamlConfiguration.loadConfiguration(new File(this.getDataFolder(),"icons.yml"));
    }

}
