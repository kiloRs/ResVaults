package com.thepaperraven.events;

import com.thepaperraven.data.vault.Vault;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class VaultChangeEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private boolean cancelled;
    private final Vault vault;

    public VaultChangeEvent(Vault vault) {
        super(Objects.requireNonNull(Bukkit.getOfflinePlayer(vault.getOwnerUUID()).getPlayer(),"Player cannot be null for Vault Change Event! This means no owner!"));
        this.vault = vault;
    }

    public VaultChangeEvent(Vault vault, boolean async) {
        super(Objects.requireNonNull(Bukkit.getOfflinePlayer(vault.getOwnerUUID()).getPlayer(),"Player cannot be null for Vault Change Event! This means no owner! Async:"  + async));
        this.vault = vault;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
