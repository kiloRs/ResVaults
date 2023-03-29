package com.thepaperraven.listeners;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class VaultSaveLoadListener implements Listener {
    public VaultSaveLoadListener(Plugin plugin) {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = new PlayerData(player.getUniqueId());
        playerData.getConfig().loadFromFile();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = ResourceVaults.getPlayerData(player.getUniqueId());
        playerData.getConfig().saveToFile();
    }
}
