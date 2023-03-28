package com.thepaperraven.events;

import com.thepaperraven.ai.Vault;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VaultAddBalanceEvent extends VaultBalanceEvent {
    private static final HandlerList handlerList = new HandlerList();
    private boolean cancelled;

    public VaultAddBalanceEvent(Vault vault, int addBalance) {
        super(vault,addBalance);
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}

