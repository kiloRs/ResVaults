package com.thepaperraven.events;

import com.thepaperraven.ai.Vault;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VaultCreateEvent extends VaultEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Player creatingPlayer;

    private boolean cancelled = false;

    public VaultCreateEvent(Vault vault, Player playerCreatingVault) {
        super(vault);
        creatingPlayer = playerCreatingVault;
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
    public CreatedBy getCreatedByState(){
        if (getVault().hasOwner()){
            if (!getVault().getMetadata().isOwner(creatingPlayer)){
                if (creatingPlayer.isOp()){
                    return CreatedBy.OP;
                }
                return CreatedBy.INVALID;
            }
            return CreatedBy.OWNER;
        }
        return CreatedBy.SERVER;
    }
    public enum CreatedBy{
        OWNER,OP,SERVER,INVALID;

        CreatedBy(){

        }
    }
}
