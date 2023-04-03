package com.thepaperraven.listeners;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.VaultManager;
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
        VaultManager.savePlayerVaults(e.getPlayer());
        ResourceVaults.log("LEAVE: Saving Player Data...");
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        VaultManager.loadPlayerVaults(e.getPlayer());
        ResourceVaults.error("JOIN: Loading Player Data....");
    }



}
