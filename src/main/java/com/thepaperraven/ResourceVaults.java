package com.thepaperraven;

import com.thepaperraven.data.player.PlayerData;
import com.thepaperraven.commands.RVCreateVaultCommand;
import com.thepaperraven.commands.RVDepositCommand;
import com.thepaperraven.commands.RVReloadCommand;
import com.thepaperraven.commands.RVWithdrawCommand;
import com.thepaperraven.config.GeneralConfiguration;
import com.thepaperraven.config.Placeholder;
import com.thepaperraven.data.player.PlayerDataManager;
import com.thepaperraven.listeners.PlayerDataFileHandler;
import com.thepaperraven.listeners.VaultInventoryListener;
import com.thepaperraven.listeners.VaultSignProtectionListener;
import com.thepaperraven.utils.RVLogger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * The main class of the RSVaults plugin!
 */
public class ResourceVaults extends JavaPlugin {


//    public static Material DEFAULT_MATERIAL = Material.WHEAT;
//    @Getter
//    public static Material[] validMaterials = new Material[]{Material.WHEAT,Material.LEATHER,Material.STONE};

    public static String PREFIX;
    private static PlayerDataManager playerDataManager;

    public static GeneralConfiguration getConfiguration() {
        return new GeneralConfiguration(getPlugin().getConfig());
    }

    @Getter
    private static Plugin plugin;
//    private static IconManager iconManager;

    public static void log(String s) {
        Logger.getLogger("Minecraft").info("[ResourceVaults]" + s);
    }

    public static void error(String error) {
        Logger.getLogger("Minecraft").severe("[ResourceVaults] " + error);
    }

    public static void reloadPlugin(boolean savePlayersFirst) {
        Bukkit.savePlayers();

        plugin.reloadConfig();
    }

    public static Plugin getInstance() {
        return plugin;
    }

    public static RVLogger getSpecialLogger() {
        return new RVLogger();
    }

    public static PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    @Override
    public void onEnable() {
        plugin = this;

        playerDataManager = new PlayerDataManager(this);
        saveDefaultConfig();
        createPlayerDataFolder();
        // Register listeners!
        registerListeners(this);
        registerCommands();
        getLogger().info("ResourceVaults plugin enabled!");

        PREFIX = getConfiguration().getPREFIX() != null ? getConfiguration().getPREFIX() : ChatColor.GRAY + "[" + ChatColor.YELLOW + "ResourceVaults" + ChatColor.GRAY + "]";
        saveConfig();
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
    @Override
    public void onDisable() {
        getLogger().info("ResourceVaults plugin disabled!");
        //Save PlayerData here!

        //todo Add the required save methods here!
    }

    private void registerCommands() {
        PluginCommand create = getCommand("rvcreate");
        PluginCommand command = getCommand("rvdeposit");
        PluginCommand withdraw = getCommand("rvwithdraw");
        PluginCommand reload = getCommand("rvreload");

        if (reload != null) {
            reload.setExecutor(new RVReloadCommand());
        }
        if (create != null) {
            create.setExecutor(new RVCreateVaultCommand(this));
        }
        if (command != null) {
            command.setExecutor(new RVDepositCommand());
        }
        if (withdraw != null) {
            withdraw.setExecutor(new RVWithdrawCommand());
        }
    }

    private void registerListeners(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new VaultInventoryListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDataFileHandler(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new VaultSignProtectionListener(plugin), plugin);

        ResourceVaults.error("Registered all known listeners of RV!");
    }

    public static PlayerData getPlayerData(UUID own) {
        return new PlayerData(own);
    }

    public static void getLogger(String text) {
        getSpecialLogger().log(text);
    }

    private void createPlayerDataFolder() {
        // Get the plugin's directory
        File pluginDirectory = getDataFolder();

        // Create the playerdata folder if it doesn't exist
        File playerDataFolder = new File(pluginDirectory, "playerdata");
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
            playerDataFolder.mkdirs();
            log("Creating PlayerData Folder");
        }
    }
}