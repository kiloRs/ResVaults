package com.thepaperraven.ai;

import com.thepaperraven.ai.commands.MyVaults;
import com.thepaperraven.ai.commands.RSVaultsCommandExecuter;
import com.thepaperraven.config.ConfigurationFile;
import com.thepaperraven.config.resources.CurrencyPlaceholder;
import com.thepaperraven.config.resources.Placeholder;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
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
        getServer().getPluginManager().registerEvents(new com.thepaperraven.ai.VaultListener(), this);
        getLogger().info("ResourceVaults plugin enabled!");

        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            log("PAPI not enabled?");
            return;
        }
        //Register PAPI Expansion!
        try {
            new FinalText().register();
            getLogger().info("Registered PAPI Placeholder : %rs_total% and %rs_'material'%");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    @NotNull
    public static ConfigurationFile getFileOfPlayer(Player player){
        return new ConfigurationFile(player);
    }
    @NotNull
    public static ConfigurationFile getFileOfPlayer(UUID uuid){
        return new ConfigurationFile(Bukkit.getPlayer(uuid));
    }
    @NotNull
    public static ConfigurationFile getResourcesConfiguration(){
        return new ConfigurationFile("resources");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("MyVaults")){
            if (sender.isOp()){
                if (args.length > 0){
                    String text = args[0];
                    List<Player> players = Bukkit.matchPlayer(text);
                    if (!players.isEmpty()){
                        Player player = players.get(0);
                        Map<Integer, Vault> vaults = getPlayerData(player).getVaults();

                        sender.sendMessage("Vaults of Player: " + player.getName());
                        for (Map.Entry<Integer,Vault> vault : vaults.entrySet()) {
                            if (vault == null){
                                continue;
                            }
                            sender.sendMessage("" + vault.getValue().getVaultMetadata().getIndex() + " - " + vault.getValue().getVaultMetadata().getMaterial().getKey().getKey() + " - " + vault.getValue().getChestLocation1().toBlockLocation() + " - " + vault.getValue().getVaultMetadata().getOwner().toString());
                        }
                    }
                }
            }
            return new MyVaults(this).onCommand(sender, command, label, args);
        }
        else if (label.equalsIgnoreCase("RSVaults")){
            return new RSVaultsCommandExecuter(this).onCommand(sender, command, label, args);
        }
        else if (label.equalsIgnoreCase("RS")){
            if (args.length == 1&&args[0].equalsIgnoreCase("reload")){
                this.reloadConfig();
                log("Reloading Configurations of RSVaults plugin!");
                return true;
            }
            else if (args.length == 0){
                sender.sendMessage("RSVaults");
                sender.sendMessage("MyVaults - Open a GUI of the owning players vaults!");
                return true;
            }
        }
        return false;
    }

    public static PlayerData getPlayerDataManager(UUID own){
        return new PlayerData(own);
    }
}