package com.thepaperraven.ai;

import com.thepaperraven.ai.events.VaultCreateEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

@Getter
public class VaultManager implements Listener {
    private final Plugin plugin;
    private final Map<Location, Vault> vaults;
    private final PlayerDataManager playerData;

    public VaultManager(Plugin plugin, PlayerDataManager playerData) {
        this.plugin = plugin;
        this.vaults = new HashMap<>();
        this.playerData = playerData;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (lines.length > 0 && lines[0].equalsIgnoreCase("[Resources]")) {
            Block block = event.getBlock();
            if (isValidVaultLocation(block)) {
                event.setLine(0, ChatColor.BOLD + "[Resources]");
                VaultCreateEvent vaultCreationEvent = new VaultCreateEvent(event.getPlayer(), block);
                Bukkit.getPluginManager().callEvent(vaultCreationEvent);
                if (!vaultCreationEvent.isCancelled()) {
                    Material allowedMaterial = getMaterial(lines);
                    Vault vault = Vault.fromEvent(vaultCreationEvent);
                    vaults.put(block.getLocation(), vault);
                    if (isDoubleChest(block)) {
                        vaults.put(getConnectedChest(block).getLocation(), vault);
                    }
                    vault.save();
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    private boolean isValidVaultLocation(Block block) {
        Location location = block.getLocation();
        if (vaults.containsKey(location)) {
            return false;
        }
        if (isDoubleChest(block)) {
            Location connectedChestLocation = getConnectedChest(block).getLocation();
            if (vaults.containsKey(connectedChestLocation)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDoubleChest(Block block) {
        return block.getBlockData() instanceof Chest && ((Chest) block.getBlockData()).getType() == Chest.Type.RIGHT;
    }

    private Chest getConnectedChest(Block block) {
        Chest chestBlock = (Chest) block.getState().getBlockData();
        BlockFace connectedFace = chestBlock.getFacing().getOppositeFace();
        Block connectedBlock = block.getRelative(connectedFace);
        return (Chest) connectedBlock.getState();
    }

    private Material getMaterial(String[] lines) {
        if (lines.length > 1 && Material.matchMaterial(lines[1]) != null) {
            return Material.matchMaterial(lines[1]);
        } else {
            return Material.WHEAT;
        }
    }

    public void removeVault(Location location) {
        vaults.remove(location);
    }

    public Vault getVault(Location location) {
        return vaults.get(location);
    }
}
