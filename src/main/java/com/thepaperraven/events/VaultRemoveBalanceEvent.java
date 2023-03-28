package com.thepaperraven.events;

import com.thepaperraven.ai.Vault;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VaultRemoveBalanceEvent  extends VaultBalanceEvent{

    private static final HandlerList handlerList = new HandlerList();

    public VaultRemoveBalanceEvent(Vault vault, int amount) {
        super(vault, amount);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
