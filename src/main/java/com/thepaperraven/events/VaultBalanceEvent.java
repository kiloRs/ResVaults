package com.thepaperraven.events;

import com.thepaperraven.ai.Vault;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class VaultBalanceEvent extends VaultEvent {
    private static final HandlerList handlerList = new HandlerList();
    @Getter
    @Setter
    private int amount;

    public VaultBalanceEvent(Vault vault, int amount) {
        super(vault);
        this.amount = amount;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}

