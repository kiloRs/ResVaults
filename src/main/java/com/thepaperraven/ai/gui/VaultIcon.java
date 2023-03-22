package com.thepaperraven.ai.gui;

import com.thepaperraven.ai.Vault;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class VaultIcon {
    @Getter
    private final ItemStack itemStack;
    @Getter
    private final Vault vault;
    @Getter
    @Setter
    private boolean active = true;

    public VaultIcon(Vault vault){
        this.itemStack = new ItemStack(vault.getMaterialType(),1);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName( "&c" + vault.getMaterialType().key().asString() + " Vault " + vault.getIndex());
        this.itemStack.setItemMeta(itemMeta);
        this.vault = vault;
    }

    @NotNull
    public static VaultIcon get(Vault vault) {
        return new VaultIcon(vault);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof VaultIcon vaultIcon)) return false;
        if (!itemStack.isSimilar(vaultIcon.itemStack)){
            return false;
        }

        return new EqualsBuilder().append(vault, vaultIcon.vault).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(itemStack).append(vault).toHashCode();
    }
}
