package com.thepaperraven.ai.events;

import com.thepaperraven.ai.Vault;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class VaultCreateEvent extends PlayerEvent implements Cancellable {
    @Getter
    private static final HandlerList handlerList = new HandlerList();
    private boolean cancelled = false;
    @Getter
    private final Vault vault;

    public VaultCreateEvent(@NotNull Vault vault) {
        super(Bukkit.getPlayer(vault.getOwnerId()));
        this.vault = vault;
    }

    public VaultCreateEvent(@NotNull Vault vault, boolean async) {
        super(Bukkit.getPlayer(vault.getOwnerId()), async);
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
}
