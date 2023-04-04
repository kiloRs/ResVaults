package com.thepaperraven.events;

import com.thepaperraven.data.vault.Vault;
import lombok.Getter;

@Getter
public class VaultDeleteEvent extends VaultChangeEvent{
    private boolean notify = false;
    private String notification = "";

    public VaultDeleteEvent(Vault vault) {
        super(vault);

    }

    public VaultDeleteEvent(Vault vault, boolean async) {
        super(vault, async);

    }
    public void setNotification(String reason){
        this.notification = reason;

        if (this.notification.length()>0){
            this.notify = true;
        }
    }


}
