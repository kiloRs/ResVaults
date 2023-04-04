package com.thepaperraven.events;

import com.thepaperraven.data.vault.Vault;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class VaultCreateEvent extends VaultChangeEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;
    private final Material material;
    private final boolean doesSignExist;
    private boolean cancelled = false;
    private final int index;

    public VaultCreateEvent(Player player, Location location, Material material, int actual) {
        super(new Vault(actual,player.getUniqueId(),material, ((Chest) location.getBlock().getState())));
        this.player = player;
        this.location = getVault().getChest().getLocation();
        this.material = getVault().getMaterial();
        this.index = getVault().getIndex();
        this.doesSignExist = getVault().hasSign();
    }

    public void setSignDirection(BlockFace face){
        getVault().signDirection = face;
    }

    public void createSign(){
        getVault().createVaultsSign(false);
    }
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
