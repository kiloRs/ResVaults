package com.thepaperraven.config;

import com.thepaperraven.data.vault.Vault;
import lombok.Getter;
import org.bukkit.NamespacedKey;

/**
 * Contains all the NameSpacedKeys required in the ResourceVaults plugin!
 */
public class VaultKeys {
//    /**
//     * Used in the VaultGUI class, a list showing each of the Vaults of a PlayerData. The back key is the back page button's PDC storage key!
//     */
//    @Getter
//    private static final NamespacedKey backKey= new NamespacedKey(ResourceVaults.getPlugin(), "back_icon");
//    /**
//     * Used in the VaultGUI class, a list showing each of the Vaults of a PlayerData. The back key is the next page button's PDC storage key!
//     */
//    @Getter
//    private static final NamespacedKey nextKey= new NamespacedKey(ResourceVaults.getPlugin(), "next_icon");

    /**
     * The owning UUID key of the VaultInstance/Vault classes.
     */
    @Getter
    private static final NamespacedKey ownerKey= Vault.UUID;
    @Getter
    private static final NamespacedKey materialTypeKey= Vault.MATERIAL;
    @Getter
    private static final NamespacedKey indexKey= Vault.INDEX;

}