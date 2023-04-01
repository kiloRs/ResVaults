package com.thepaperraven.ai.vault;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.events.VaultRegisterEvent;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;

@Getter
public class VaultInstance {
    private PlayerData ownerData = null;
    private VaultInventory inventory = null;
    private VaultPDContainer container = null;
    //All the stored data goes into the VaultMetadata, from the PDContainer.
    private VaultCommandMeta metadata = null;
    private final String vaultCreationMethod;
    private boolean saved = false;
    private boolean valid;

    public VaultInstance(VaultPDContainer container, VaultCommandMeta metadata, VaultInventory inventory, VaultRegisterEvent.Reason reason){
        this.container = container;
        this.metadata = metadata;
        this.inventory = inventory;
        this.ownerData = PlayerData.get(metadata.getOwnerUUID());
        this.valid = container.isValid()&&metadata.isValid()&&inventory.isValid();
        this.vaultCreationMethod = reason.name();
    }
    public VaultInstance(VaultPDContainer container, VaultCommandMeta vaultCommandMetaMetadata, VaultInventory inventory) {
        this(container, vaultCommandMetaMetadata,inventory, VaultRegisterEvent.Reason.CREATED);
    }

    public VaultInstance(VaultPDContainer container, VaultCommandMeta metadata) {
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

    public void saveToBlock(){
        container.saveToBlock(metadata);
    }
    public void removeFromBlock(){
        int x = 0;
        if (ownerData.hasVault(this.metadata.getVaultIndex())){
            ownerData.removeVault(this.metadata.getVaultIndex());
            x = 1;
        }
        else {
            this.container.removeFromBlock();
            this.metadata = null;
            this.container = null;
            this.saved = false;
            this.inventory = null;
            x = 2;
        }

        this.valid = false;

        ResourceVaults.error("Removed Vault Instance Type: " + x);
    }

    public static VaultInstance getExistingVaultFrom(Location containerLoc) {
        BlockState state = containerLoc.getBlock().getState();
        if (!(state instanceof Container container)) {
            ResourceVaults.error("Not a chest....");
            return null; // not a container
        }
//
//        BlockState signState = signLoc.getBlock().getState();
//        if (!(signState instanceof Sign sign)) {
//            return null; // not a sign
//        }

//        if (!sign.getLine(0).equals("[Resources]")) {
//            return null; // first line of sign doesn't match
//        }
        Material material = ResourceVaults.DEFAULT_MATERIAL;
        VaultPDContainer containerData = VaultPDContainer.get(container);
        if (!containerData.hasKeys()) {
            ResourceVaults.error("No Matching Keys on Container!");
            return null;
        }
        if (containerData.hasMaterialKey()){
            material = containerData.getMaterialKey();
        }


        PlayerData pd = PlayerData.get(containerData.getOwner());
        VaultInstance vault = pd.getVault(containerData.getVaultIndex());
        if (vault == null){
            ResourceVaults.error("Vault Error!");
            return null;
        }
        return vault;
    }
}