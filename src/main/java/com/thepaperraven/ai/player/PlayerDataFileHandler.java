package com.thepaperraven.ai.player;

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

        playerData.getVaults().forEach((integer, vaultInstance) -> vaultInstance.save());
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        PlayerData playerData = new PlayerData(e.getPlayer().getUniqueId());

        if (playerData.getVaults().isEmpty()){
            playerData.getPlayer().sendRawMessage("No Vaults Loaded");
        }
        else {
            playerData.getPlayer().sendRawMessage("Vaults Loaded: " + playerData.getVaults().size());
        }
    }



}
