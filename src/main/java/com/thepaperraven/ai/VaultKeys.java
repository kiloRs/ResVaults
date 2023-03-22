package com.thepaperraven.ai;

import lombok.Getter;
import org.bukkit.NamespacedKey;

public class VaultKeys {
    @Getter
    private static final NamespacedKey backKey= new NamespacedKey(ResourceVaults.getPlugin(), "back_icon");
    @Getter
    private static final NamespacedKey nextKey= new NamespacedKey(ResourceVaults.getPlugin(), "next_icon");

    @Getter
    private static final NamespacedKey ownerKey= new NamespacedKey(ResourceVaults.getPlugin(), "owner_id");
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

}