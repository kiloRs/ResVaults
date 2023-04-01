package com.thepaperraven.events;

import com.thepaperraven.ai.vault.VaultInstance;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
public class VaultRegisterEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    @Getter
    private final VaultInstance vault;
    @Getter
    @Setter
    private Reason reason;
    private boolean cancel = false;

    public VaultRegisterEvent(VaultInstance instance) {
        this(instance,Reason.CREATED);
    }
    public VaultRegisterEvent(VaultInstance instance,Reason reason) {
        super(Bukkit.getPlayer(instance.getMetadata().getOwnerUUID()));
        this.vault = instance;
        this.reason =reason;
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
    public enum Reason {
        CREATED,EDITED,COMMAND,PLUGIN,ERROR;
    }
}
