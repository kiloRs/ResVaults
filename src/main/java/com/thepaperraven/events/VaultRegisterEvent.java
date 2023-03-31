package com.thepaperraven.events;

import com.thepaperraven.ai.vault.VaultInstance;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
public class VaultRegisterEvent extends Event  implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    @Getter
    private final VaultInstance vault;
    private boolean cancel = false;

    public VaultRegisterEvent(VaultInstance instance) {
        vault = instance;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}