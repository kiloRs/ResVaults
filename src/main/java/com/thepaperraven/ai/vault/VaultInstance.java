package com.thepaperraven.ai.vault;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.events.VaultRegisterEvent;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@Getter
@SerializableAs("vault")
public class VaultInstance implements ConfigurationSerializable, Invalidatable {
    private PlayerData ownerData = null;
    private VaultInventory inventory = null;
    private VaultPDContainer container = null;
    //All the stored data goes into the VaultMetadata, from the PDContainer.
    private VaultMetadata metadata = null;
    private final String vaultCreationMethod;
    private boolean saved = false;
    private boolean valid;

    public VaultInstance(VaultPDContainer container, VaultMetadata metadata, VaultInventory inventory, VaultRegisterEvent.Reason reason){
        this.container = container;
        this.metadata = metadata;
        this.inventory = inventory;
        this.ownerData = PlayerData.get(metadata.getOwnerUUID());
        this.valid = container.isValid()&&metadata.isValid()&&inventory.isValid();
        this.vaultCreationMethod = reason.name();
    }
    public VaultInstance(VaultPDContainer container, VaultMetadata vaultMetadata, VaultInventory inventory) {
        this(container,vaultMetadata,inventory, VaultRegisterEvent.Reason.CREATED);
    }

    public VaultInstance(VaultPDContainer container, VaultMetadata metadata) {
        this(container,metadata,new VaultInventory(container,metadata));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof VaultInstance that)) return false;

        return new EqualsBuilder().append(getContainer(), that.getContainer()).append(getMetadata(), that.getMetadata()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getContainer()).append(getMetadata()).toHashCode();
    }


    public VaultInventory getInventory() {
        return inventory;
    }


    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
/*
        data.put("ownerData", ownerData);
*/
        data.put("inventory", inventory);
        data.put("container", container);
        data.put("metadata", metadata);
        data.put("saved", saved);
        data.put("valid", valid);
        return data;
    }

    public static VaultInstance deserialize(Map<String, Object> data) {
//        PlayerData ownerData = (PlayerData) data.get("ownerData");
        VaultInventory inventory = (VaultInventory) data.get("inventory");
        VaultPDContainer container = (VaultPDContainer) data.get("container");
        VaultMetadata metadata = (VaultMetadata) data.get("metadata");
        boolean saved = (boolean) data.get("saved");
        boolean valid = (boolean) data.get("valid");
        VaultInstance vault = new VaultInstance(container, metadata, inventory);
        vault.ownerData = new PlayerData(metadata.getOwnerUUID());
        vault.saved = saved;
        vault.valid = valid;
        return vault;
    }

    public boolean save(){
        if (ownerData.register(this)) {
            saveToBlock();
            this.saved = true;
            ResourceVaults.log("Saved Valid VaultInstance to " + ownerData.getPlayer().getName() + " as " + this.metadata.getVaultIndex());
            return true;
        }
        if (ownerData.hasVault(this.metadata.getVaultIndex()) && ownerData.getVault(this.metadata.getVaultIndex())==this && ownerData.hasVault(this)){
            this.saved = true;
            ResourceVaults.log("No Need to Save, Already Exists in PlayerData!");
            return true;
        }
        return false;
    }
    public void saveToBlock(){
        container.saveToBlock(metadata);
    }
    public void removeFromBlock(boolean breakSignAlso, boolean i){
        int x = 0;
        if (ownerData.hasVault(this.metadata.getVaultIndex())){
            ownerData.removeVault(this.metadata.getVaultIndex(),i);
            x = 1;
        }
        else {
            this.container.removeFromBlock(breakSignAlso);
            this.metadata = null;
            this.container = null;
            this.saved = false;
            this.inventory = null;
            x = 2;
        }

        this.valid = false;

        ResourceVaults.error("Removed Vault Instance Type: " + x);
    }

    @Override
    public void invalidate() {
        this.metadata.invalidate();
        this.container.invalidate();
        this.inventory.invalidate();

        this.metadata = null;
        this.container = null;
        this.saved = false;
        this.inventory = null;
        this.valid = false;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
}