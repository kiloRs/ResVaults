package com.thepaperraven.listeners;

import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultMetadata;
import com.thepaperraven.ai.VaultEventFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Attachable;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class VaultSignChangeListener implements Listener {
    private final Plugin plugin;


    public VaultSignChangeListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation().toBlockLocation();

        // Check if the sign text matches the vault creation format
        String line = event.getLine(0);
        if (line == null){
            return;
        }
        if (!line.equalsIgnoreCase("[Resources]")) {
            return;
        }

        // Get the material of the chest connected to the sign
        Block attachedBlock;
        if (block.getBlockData() instanceof Attachable) {
            BlockFace attachedFace = ((Attachable) block.getBlockData()).getAttachedFace();
            attachedBlock = block.getRelative(attachedFace);
        } else {
            attachedBlock = null;
        }
        if (attachedBlock == null || !(attachedBlock.getState() instanceof Chest chest)) {
            player.sendMessage(ChatColor.RED + "Error: The sign must be attached to a chest.");
            return;
        }

        Material material = Material.WHEAT;

        String second = event.getLine(1);
        if (second != null) {
            if (Material.matchMaterial(second) != null){
                material = Material.matchMaterial(second);
            }
        }
        // Get the player data and create a new vault
        PlayerData playerData = ResourceVaults.getPlayerData(player.getUniqueId());

        int index = playerData.getNextIndex();
        VaultMetadata metadata = new VaultMetadata( material,playerData, index);
        ArrayList<Location> chestLocations = new ArrayList<>();

        if (((Chest) attachedBlock.getState()).getBlockInventory() instanceof DoubleChest doubleChest ){
            chestLocations.add(doubleChest.getLeftSide().getInventory().getLocation().toBlockLocation());
            chestLocations.add(doubleChest.getRightSide().getInventory().getLocation().toBlockLocation());
        }
        else {
            chestLocations.add(attachedBlock.getLocation().toBlockLocation());
        }

        Vault vault = new Vault(metadata, chestLocations, location);

        // Fire the VaultCreateEvent
        if (!VaultEventFactory.fireCreateEvent(player,vault)) {
            player.sendMessage(ChatColor.RED + "Vault creation cancelled.");
            return;
        }


        // Add the new vault to the player data and update the PDCs of the blocks involved
        playerData.addVault(vault);
        vault.updatePDC();


        //Sign update is handled in vault.updatePDC() based on locked state.
    }
}