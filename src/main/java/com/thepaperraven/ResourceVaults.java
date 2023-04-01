package com.thepaperraven;

import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.player.PlayerDataFileHandler;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.ai.vault.VaultMetadata;
import com.thepaperraven.ai.vault.VaultPDContainer;
import com.thepaperraven.commands.Command;
import com.thepaperraven.config.Placeholder;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

    public static void reloadPlugin(boolean savePlayersFirst){
        Bukkit.savePlayers();

        plugin.reloadConfig();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (savePlayersFirst) {
                PlayerDataFileHandler.save(PlayerData.get(onlinePlayer.getUniqueId()));
            }
            PlayerData d = PlayerDataFileHandler.load(onlinePlayer.getUniqueId());

            if (d.getVaults().size()>0){
                ResourceVaults.log("Loaded " + d.getVaults().size() + " for " + onlinePlayer.getName());
            }
        }


    }

    @NotNull
    public static PlayerData getOwnerOf(@NotNull VaultInstance v) {
        if (!v.getContainer().hasOwner()){
            return new PlayerData(v.getMetadata().getOwnerUUID());
        }
        return new PlayerData(v.getContainer().getOwner());
    }

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();
        createPlayerDataFolder();

        registerConfigurationSerialization();
        // Register listeners!
        registerListeners(this);

        PluginCommand rv = Bukkit.getPluginCommand("rv");
        if (rv != null){
            rv.setExecutor(new Command());
        }
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
            PlayerDataFileHandler.save(playerData);
        }
    }

    private void registerConfigurationSerialization(){
        ConfigurationSerialization.registerClass(PlayerData.class);
        ConfigurationSerialization.registerClass(VaultMetadata.class);
        ConfigurationSerialization.registerClass(VaultPDContainer.class);
        ConfigurationSerialization.registerClass(VaultInstance.class);
        ConfigurationSerialization.registerClass(VaultInventory.class);

    }
    private void registerListeners(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new VaultCreationListener(),plugin);
        plugin.getServer().getPluginManager().registerEvents(new VaultInteractionListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDataFileHandler(plugin), plugin);
    }

    public static PlayerData getPlayerData(UUID own) {
        return new PlayerData(own);
    }
    public static void getLogger(int level, String text){
        RVLogger.getInstance().log(level, text);
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getLabel().equalsIgnoreCase("rv")){
            return new Command().onCommand(sender, command, label, args);
        }
        return super.onCommand(sender, command, label, args);
    }
}
