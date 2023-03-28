package com.thepaperraven.events;

import com.thepaperraven.ai.Vault;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VaultDeleteEvent extends VaultEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    @Getter
    private DeleteMethod deleteMethod = DeleteMethod.SERVER;

    public VaultDeleteEvent(Vault vault){
        super(vault);
    }
    public VaultDeleteEvent(Vault vault, DeleteMethod deleteMethod) {
        super(vault);
        this.deleteMethod = deleteMethod;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
    public enum DeleteMethod{
        OWNER,SERVER,OP,TIMER,OTHER;
    }
}

