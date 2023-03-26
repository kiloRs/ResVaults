package com.thepaperraven.ai;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

public interface VaultManagerSystem {

    /**
     * Creates a new vault for the specified player with the given material.
     *
     * @param ownerUUID the UUID of the player who owns the vault
     * @param material the material type of the vault
     * @param signLocation the location of the sign block
     * @param chestLocations the locations of the chest blocks
     * @return the newly created Vault instance
     */
    Vault createVault(UUID ownerUUID, Material material, Location signLocation, List<Location> chestLocations);

    /**
     * Retrieves the Vault instance with the specified index and material type.
     *
     * @param ownerUUID the UUID of the player who owns the vault
     * @param index the index number of the vault
     * @param material the material type of the vault
     * @return the Vault instance, or null if not found
     */
    Vault getVault(UUID ownerUUID, int index, Material material);

    /**
     * Deletes the specified Vault instance.
     *
     * @param vault the Vault instance to delete
     */
    void deleteVault(Vault vault);

    /**
     * Retrieves all the Vault instances owned by the specified player.
     *
     * @param ownerUUID the UUID of the player
     * @return a list of Vault instances
     */
    List<Vault> getVaults(UUID ownerUUID);

    /**
     * Retrieves all the Vault instances with the specified material type.
     *
     * @param material the material type
     * @return a list of Vault instances
     */
    List<Vault> getVaults(Material material);

    /**
     * Retrieves the total balance of all the vaults owned by the specified player.
     *
     * @param ownerUUID the UUID of the player
     * @return the total balance
     */
    int getBalance(UUID ownerUUID);

    /**
     * Retrieves the total balance of all the vaults with the specified material type.
     *
     * @param material the material type
     * @return the total balance
     */
    int getBalance(Material material);

    /**
     * Retrieves the total number of vaults owned by the specified player.
     *
     * @param ownerUUID the UUID of the player
     * @return the total number of vaults
     */
    int getTotalVaults(UUID ownerUUID);

    /**
     * Retrieves the total number of vaults with the specified material type.
     *
     * @param material the material type
     * @return the total number of vaults
     */
    int getTotalVaults(Material material);

    boolean isGreaterThan(int amount, Material material);
    boolean isEqualTo(int amount, Material material);
    boolean isTotalAmountGreaterThan(int amount);
    boolean isTotalAmountLessThan(int amount);

}
