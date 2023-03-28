package com.thepaperraven.ai;

import com.thepaperraven.commands.MyVaults;
import com.thepaperraven.commands.RSVaultsCommandExecuter;
import com.thepaperraven.listeners.VaultAmountListener;
import com.thepaperraven.listeners.VaultSignChangeListener;
import com.thepaperraven.config.resources.CurrencyPlaceholder;
import com.thepaperraven.config.resources.Placeholder;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * The main class of the RSVaults plugin!
 */
public class ResourceVaults extends JavaPlugin {

    @Getter
    private static Plugin plugin;


    public static void log(String s) {
        Logger.getLogger("Minecraft").info("[ResourceVaults]" + s);
    }

    public static void error(String error){
        Logger.getLogger("Minecraft").severe("[ResourceVaults] " + error);
    }

    public static VaultManager getVaultManager() {
        return new VaultManager((ResourceVaults.getPlugin()));
    }


    @Override
    public void onEnable() {
        plugin = this;
        //Register commands!
        PluginCommand rsvaults = getCommand("rsvaults");
        if (rsvaults!=null) {
            rsvaults.setExecutor(new RSVaultsCommandExecuter(this));
        }
        PluginCommand myvaults = getCommand("myvaults");
        if (myvaults!=null) {
            myvaults.setExecutor(new MyVaults(this));
        }
        //Register listeners!
        getServer().getPluginManager().registerEvents(new VaultSignChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new VaultAmountListener(this),this);
        getLogger().info("ResourceVaults plugin enabled!");

        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            log("PAPI not enabled?");
            return;
        }
        //Register PAPI Expansion!
        try {
            new CurrencyPlaceholder().register();
            new Placeholder().register();
            getLogger().info("Registered PAPI Placeholder : %vaults_'type'% && %vaults_all%" );
        } catch (Exception e) {
            throw new RuntimeException(e);
            }
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
    }


    public static PlayerData getPlayerData(UUID own){
        return new PlayerData(own);
    }
}