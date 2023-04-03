package com.thepaperraven.events;

import com.thepaperraven.ai.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VaultCreateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;
    private final Material material;
    private boolean cancelled = false;
    private final int nextIndex;

    public VaultCreateEvent(Player player, Location location, Material material) {
        this.player = player;
        this.location = location;
        this.material = material;
        this.nextIndex = PlayerData.get(player.getUniqueId()).getVaults().size() + 1;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
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

    public int getNextIndex() {
        return nextIndex;
    }
}
