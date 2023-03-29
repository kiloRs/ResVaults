package com.thepaperraven.ai;

import com.thepaperraven.ResourceVaults;
import lombok.Getter;
import org.bukkit.NamespacedKey;

/**
 * Contains all the NameSpacedKeys required in the ResourceVaults plugin!
 */
public class VaultKeys {
    /**
     * Used in the VaultGUI class, a list showing each of the Vaults of a PlayerData. The back key is the back page button's PDC storage key!
     */
    @Getter
    private static final NamespacedKey backKey= new NamespacedKey(ResourceVaults.getPlugin(), "back_icon");
    /**
     * Used in the VaultGUI class, a list showing each of the Vaults of a PlayerData. The back key is the next page button's PDC storage key!
     */
    @Getter
    private static final NamespacedKey nextKey= new NamespacedKey(ResourceVaults.getPlugin(), "next_icon");

    /**
     * The owning UUID key of the VaultInstance/Vault classes.
     */
    @Getter
    private static final NamespacedKey ownerKey= new NamespacedKey(ResourceVaults.getPlugin(), "owner_id");
    /**
     * The location key of the VaultInstance/Vault classes.
     */
    @Getter
    private static final NamespacedKey locationKey= new NamespacedKey(ResourceVaults.getPlugin(), "vault_location");
    @Getter
    private static final NamespacedKey doubleChestLocationKey = new NamespacedKey(ResourceVaults.getPlugin(),"vault_second_location");
    @Getter
    private static final NamespacedKey materialTypeKey= new NamespacedKey(ResourceVaults.getPlugin(), "vault_material_type");
    @Getter
    private static final NamespacedKey indexKey= new NamespacedKey(ResourceVaults.getPlugin(), "vault_index");
    @Getter
    private static final NamespacedKey vaultKey= new NamespacedKey(ResourceVaults.getPlugin(), "vault_create_date");
    @Getter
    private static final NamespacedKey locked = new NamespacedKey(ResourceVaults.getPlugin(), "vault_locked");

}