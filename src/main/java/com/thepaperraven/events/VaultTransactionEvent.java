package com.thepaperraven.events;

import com.thepaperraven.data.vault.Vault;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class VaultTransactionEvent extends VaultChangeEvent{

    private static HandlerList handlerList = new HandlerList();
    private TransactionType transactionType;
    private int amount;

    public VaultTransactionEvent(Vault vault,TransactionType transactionType, int amount) {
        super(vault);
        this.transactionType = transactionType;
        this.amount = amount;

    }

    public VaultTransactionEvent(Vault vault, boolean async) {
        super(vault, async);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }

    enum TransactionType{
        REMOVE,WITHDRAW,DEPOSIT,ADD;
    }
}
