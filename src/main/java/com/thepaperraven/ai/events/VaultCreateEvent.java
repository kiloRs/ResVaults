package com.thepaperraven.ai.events;

import com.thepaperraven.ai.Vault;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class VaultCreateEvent extends PlayerEvent implements Cancellable {
    @Getter
    private static final HandlerList handlerList = new HandlerList();
    private boolean cancelled = false;
    @Getter
    private final Vault vault;

    public VaultCreateEvent(@NotNull Vault vault) {
        super(Bukkit.getPlayer(vault.getOwnerUUID()));
        this.vault = vault;
    }

    public VaultCreateEvent(@NotNull Vault vault, boolean async) {
        super(Bukkit.getPlayer(vault.getOwnerUUID()), async);
        this.vault = vault;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static VaultCreateEvent generateNewEvent(UUID player, Chest chest){
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH,BlockFace.SOUTH,BlockFace.EAST,BlockFace.WEST};

        Location secondChest= null;
        Location signLocation = null;
        Material material = Material.WHEAT;
        for (BlockFace face : faces) {
            Block other = chest.getBlock().getRelative(face);
            if (other.getState() instanceof Chest second){
                if (chest.getBlockInventory().getHolder() instanceof DoubleChest doubleChest){
                    if (doubleChest.getInventory() == second.getInventory() && second.getInventory() == chest.getInventory()){
                        secondChest = second.getLocation().toBlockLocation();
                        continue;
                    }
                    if (second.getBlockInventory()==chest.getBlockInventory()){
                        secondChest = second.getLocation().toBlockLocation();
                        continue;
                    }
                    secondChest = null;
                }
                continue;
            }
            if (other.getState() instanceof Sign sign){
                if (sign.getLine(0).equalsIgnoreCase("[Resources]")){
                    signLocation = sign.getLocation();
                    String materialLine = sign.getLine(1);
                    Material m = Material.matchMaterial(materialLine);
                    if (m != null){
                        material = m;
                    }
                    continue;
                }
                else {
                    signLocation = null;
                }

            }
        }
        if (signLocation != null){
            Vault vault = null;
            if (secondChest!= null){
                vault = new Vault(player,signLocation,chest.getLocation().toBlockLocation(),secondChest,material);
            }
            vault = new Vault(player, signLocation, chest.getLocation().toBlockLocation(), material);
            return new VaultCreateEvent(vault);
        }
        return null;
    }
}
