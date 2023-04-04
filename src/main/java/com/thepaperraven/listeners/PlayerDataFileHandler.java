package com.thepaperraven.listeners;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.data.player.PlayerData;
import com.thepaperraven.events.VaultCreateEvent;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataFileHandler implements Listener {

    @Getter
    private final Plugin vaults;

    public PlayerDataFileHandler(JavaPlugin vaults) {
        this.vaults = vaults;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e){
        PlayerData playerData = new PlayerData(e.getPlayer().getUniqueId());
        ResourceVaults.getPlayerDataManager().savePlayerData(playerData);
        ResourceVaults.log("LEAVE: Saving Player Data...");
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        PlayerData playerData = new PlayerData(e.getPlayer().getUniqueId());
        playerData.getConfig().save();
        ResourceVaults.getPlayerDataManager().loadPlayer(e.getPlayer());
        ResourceVaults.error("JOIN: Loading Player Data....");
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = false)
    public void onCreatePre(VaultCreateEvent e){
        if (e.isCancelled()){
            ResourceVaults.error("Failed to accept vault creation metadata!");
            return;
        }
        if (!e.getPlayer().hasPermission("rv.create") || !e.getPlayer().isOp()){
            e.getPlayer().sendMessage("Insufficient Permissions!");
            e.setCancelled(true);
            return;
        }
    }
    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = false)
    public void onCreatePost(VaultCreateEvent e){
        if (e.isCancelled()){
            ResourceVaults.error("Failed to accept vault creation metadata!");
            return;
        }
        ResourceVaults.error("Creating Vault for " + e.getPlayer().getName() + " as " + e.getIndex());
    }



}
