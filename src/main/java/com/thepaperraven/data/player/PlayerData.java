package com.thepaperraven.data.player;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.data.vault.Vault;
import com.thepaperraven.config.PlayerConfiguration;
import com.thepaperraven.events.VaultCreateEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;

@Getter
public class PlayerData {

    private static final Map<UUID,PlayerData> loadedPlayerData = new LinkedHashMap<>();
    private final UUID playerUUID;
    private final TotalHandler totalHandler;
    private final Map<Integer, Vault> vaults;
    private final PlayerConfiguration config;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.vaults = new HashMap<>();
        this.config = new PlayerConfiguration(this.playerUUID);
        this.totalHandler = new TotalHandler() {
            @Override
            public int getTotalItems() {
                int total = 0;
                for (Map.Entry<Integer, Vault> entry : vaults.entrySet()) {
                    Vault vault = entry.getValue();
                    total = +vault.getBalance();
                }
                return total;
            }

            @Override
            public int getTotalItems(Material material) {
                int total = 0;
                for (Map.Entry<Integer, Vault> entry : vaults.entrySet()) {
                    if (entry.getValue().getMaterial()==material) {
                        Vault vault = entry.getValue();
                        total = +vault.getBalance();
                    }
                }
                return total;
            }

            @Override
            public int getTotalVaults() {
                return vaults.size();
            }

            @Override
            public int getTotalVaults(Material material) {
                int total = getTotalVaults();
                for (Map.Entry<Integer, Vault> entry : vaults.entrySet()) {
                    Vault vault = entry.getValue();
                    if (vault.getMaterial() == material) {
                        continue;
                    }
                    total--;
                }
                return total;
            }
        };
    }
    public boolean doesConfigurationContain(int x) {
        return config.isConfigurationSection("vaults." + x);
    }
    public boolean registerVault(Location location,Material material){
        if (location.getBlock().getState() instanceof Chest chest){

            if (doesConfigurationContain(getNextIndex())){
                ResourceVaults.error("Vault is already saved!");
                return false;
            }
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null){
                ResourceVaults.log("Player not valid: " + playerUUID);
                return false;
            }
            PlayerData playerData = PlayerData.get(playerUUID);
            if (playerData.getVaults().containsKey(getNextIndex())){
                ResourceVaults.log("Invalid Vault Key Number " + getNextIndex());
                return false;
            }
            VaultCreateEvent createEvent = new VaultCreateEvent(player,location,material,getNextIndex());

            if (createEvent.isCancelled()){
                ResourceVaults.error("Did not register vault after event was cancelled for " + player.getName());
                return false;
            }


            vaults.put(createEvent.getIndex(),createEvent.getVault());

            Date from = Date.from(Instant.now());
            ResourceVaults.getLogger("Vault was registered at " + from);
            boolean isSaved = doesConfigurationContain(createEvent.getIndex());
            ResourceVaults.getLogger("Was it saved? " + isSaved);

            return true;
        }
        return false;
    }

    public static PlayerData get(UUID owner) {
        return new PlayerData(owner);
    }

    public int getNextIndex() {
        return vaults.size() +1;

    }

    public void unregisterVault(int index) {
        vaults.remove(index);
    }


    public boolean isRegistered(Vault vault, boolean isSavedToFile) {

        if (isSavedToFile){
            if (config.containsVaultIndex(vault.getIndex())) {
                return vaults.containsKey(vault.getIndex()) && vaults.get(vault.getIndex()).equals(vault);
            }
            return false;
        }

        return vaults.containsKey(vault.getIndex()) && vaults.get(vault.getIndex()).equals(vault);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }
}
