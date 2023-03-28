package com.thepaperraven.events;

import com.thepaperraven.ai.Vault;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VaultUpdatePDCEvent extends VaultEvent {
    private static final HandlerList handlerList= new HandlerList();
    private List<NamespacedKey> updatedPDCKeys = new ArrayList<>();
    private UpdateReason reason;

    public VaultUpdatePDCEvent(Vault vault, List<NamespacedKey> changedKeys){
        super(vault);
        this.updatedPDCKeys = changedKeys;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public List<NamespacedKey> getUpdatedPDCKeys() {
        return updatedPDCKeys;
    }


    public static HandlerList getHandlerList(){
        return handlerList;
    }
    public UpdateReason getReason(){
        return reason;
    }
    public void setReason(UpdateReason reason){
        this.reason = reason;
    }
    public enum UpdateReason{
        CREATED,REMOVED,CHANGED;
    }
}
