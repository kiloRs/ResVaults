package com.thepaperraven.utils;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VaultUtil {
    /**
     * Checks if the given player is the owner of the given vault.
     * @param vault the vault to check ownership of
     * @param player the player to check ownership against
     * @return true if the player is the owner of the vault, false otherwise
     */
    public static boolean isOwner(Vault vault, Player player) {
        return vault.getMetadata().getOwnerUUID().equals(player.getUniqueId());
    }
    /**
     * Returns the {@link VaultInstance} with the given index number owned by the given player.
     * @param owner the player who owns the vault
     * @param indexNumber the index number of the vault to get
     * @return the VaultInstance with the given index number owned by the given player, or null if no such vault exists
     */
    public static VaultInstance getVault(Player owner, int indexNumber){
        return new PlayerData(owner.getUniqueId()).getVault(indexNumber);
    }
    /**
     * Checks if the given location is part of a vault.
     * @param location the location to check
     * @return true if the location is part of a vault, false otherwise
     */
    public static boolean isVault(Location location) {
        if (!(location.getBlock().getState() instanceof TileState tileState)) {
            return false;
        }
        return hasIndex(tileState) && hasOwner(tileState) && hasMaterial(tileState)&& new PlayerData(getOwner(location.getBlock())).hasVault(getIndex(location.getBlock()));
    }
    /**
     * Gets the owner UUID of the vault that the given block is a part of.
     * @param block the block to get the owner of
     * @return the UUID of the owner of the vault that the block is a part of, or null if the block is not part of a vault
     */
    public static UUID getOwner(Block block) {
        if (block.getState() instanceof TileState tileState){
            if (hasOwner(tileState)){
                return tileState.getPersistentDataContainer().getOrDefault(VaultKeys.getOwnerKey(), DataType.UUID,null);
            }
        }
        return null;
    }
    /**
     * Checks if the given location is part of a vault.
     * @param block the location to check
     * @return true if the location is part of a vault, false otherwise
     */
    public static boolean isVault(Block block) {
        if (!(block.getState() instanceof TileState tileState)) {
            return false;
        }
        return hasIndex(tileState) && hasOwner(tileState) && hasMaterial(tileState);
    }
    /**
     * Checks if the given chest has a vault sign adjacent to it.
     * @param chest the chest to check
     * @return true if the chest has a vault sign adjacent to it, false otherwise
     */
    public static boolean hasVaultSign(Chest chest) {
        for (BlockFace blockFace : BlockFace.values()) {
            Block signBlock = chest.getBlock().getRelative(blockFace);
            if (signBlock.getState() instanceof Sign sign) {
                if (sign.getLine(0).equals("[Resources]")) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Checks if the given secondary chest is part of the same double chest as the given main chest.
     * @param main the main chest to check against
     * @param secondary the secondary chest to check
     * @return true if the secondary chest is part of the same double chest as the main chest, false otherwise
     */
    public static boolean isSecondaryDoubleChest(Chest main, Chest secondary) {
        if (!(main.getInventory().getHolder() instanceof DoubleChest doubleChest)) {
            return false;
        }
        return doubleChest.getRightSide().getInventory().getLocation().equals(secondary.getBlock().getLocation())
                || doubleChest.getLeftSide().getInventory().getLocation().equals(secondary.getBlock().getLocation());
    }
    /**
     * Checks if the given double chest has a vault sign on either of its sides.
     * @param doubleChest the double chest to check
     * @return true if the double chest has a vault sign on either of its sides, false otherwise
     */
    public static boolean doesDoubleChestHaveSign(DoubleChest doubleChest) {
        return hasVaultSign((Chest) doubleChest.getLeftSide().getInventory().getHolder())
                || hasVaultSign((Chest) doubleChest.getRightSide().getInventory().getHolder());
    }
    /**
     * Checks if the given TileState has an index value in its {@link PersistentDataContainer}.
     * @param tileState the TileState to check
     * @return true if the TileState has an index value, false otherwise
     */
    public static boolean hasIndex(TileState tileState) {
        PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
        return dataContainer.has(VaultKeys.getIndexKey(), PersistentDataType.INTEGER);
    }
    /**
     * Checks if the given TileState has an owner UUID value in its {@link PersistentDataContainer}.
     * @param tileState the TileState to check
     * @return true if the TileState has an owner UUID value, false otherwise
     */
    public static boolean hasOwner(TileState tileState) {
        PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
        return dataContainer.has(VaultKeys.getOwnerKey(), PersistentDataType.STRING);
    }
    /**
     * Checks if the given TileState has a material type value in its {@link PersistentDataContainer}.
     * @param tileState the TileState to check
     * @return true if the TileState has a material type value, false otherwise
     */
    public static boolean hasMaterial(TileState tileState) {
        PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
        return dataContainer.has(VaultKeys.getMaterialTypeKey()) && Material.matchMaterial(dataContainer.getOrDefault(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING, "WHEAT")) != null;
    }
    /**
     * Checks if the given vault is registered with the owning player's {@link PlayerData}.
     * @param vault the vault to check
     * @return true if the vault is registered with the owning player's PlayerData, false otherwise
     */
    public static boolean isRegistered(Vault vault) {
        Map<Integer, VaultInstance> vaults = new PlayerData(vault.getMetadata().getOwnerUUID()).getVaults();

        return vaults.containsKey(vault.getMetadata().getVaultIndex()) && ((vaults.get(vault.getMetadata().getVaultIndex()) == vault || vaults.get(vault.getMetadata().getVaultIndex()).equals(vault)));
    }

    public static boolean isSaved(Vault vault) {
        if (!vault.isActive()) {
            ResourceVaults.error("Vault is not active!");
        }
            return new PlayerData(vault.getMetadata().getOwnerUUID()).hasVault(vault.getMetadata().getVaultIndex()) && vault == new PlayerData(vault.getMetadata().getOwnerUUID()).getVault(vault.getMetadata().getVaultIndex()) && vault.isActive();
    }

    public static int getIndex(Vault vault) {
        return vault.getMetadata().getVaultIndex();
    }

    public static int getIndex(Block block){
        if (block.getState() instanceof Chest chest){
            return getIndex(chest);
        }
        if (block.getState() instanceof Sign sign){
            return sign.getPersistentDataContainer().getOrDefault(VaultKeys.getIndexKey(),PersistentDataType.INTEGER,0);
        }
        return 0;
    }
    public static int getIndex(Chest chest){
        return chest.getPersistentDataContainer().getOrDefault(VaultKeys.getIndexKey(),PersistentDataType.INTEGER,0);
    }

    public static Material getMaterial(Vault vault) {
        TileState tileState = vault.getMainChest();
        PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();

        if (!dataContainer.has(VaultKeys.getMaterialTypeKey())) {
            return vault.getMetadata().getAllowedMaterial();
        }
        String s = dataContainer.get(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING);
        if (s == null) {
            s = "WHEAT";
        }

        return dataContainer.has(VaultKeys.getMaterialTypeKey()) ? Material.matchMaterial(s) : vault.getMetadata().getAllowedMaterial();

    }

    /**
     * Reads the "[Resources]" text from the given Sign instance.
     *
     * @param sign the Sign instance to read from
     * @return the value of the "[Resources]" text, or null if it was not found
     */
    public static String getResourcesFrom(Sign sign) {
        return sign.getLine(0).equalsIgnoreCase("[Resources]") ? sign.getLine(0) : null;
    }
    /**
     * Reads the Material value from the given Sign instance.
     *
     * @param sign the Sign instance to read from
     * @return the Material value, or null if it was not found or could not be parsed
     */
    public static Material getMaterialFrom(Sign sign) {
        String line = sign.getLine(1);
        try {
            return Material.valueOf(line.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    /**
     * Reads the Player name from the given Sign instance.
     *
     * @param sign the Sign instance to read from
     * @return the Player name, or null if it was not found
     */
    public static String getPlayerNameFrom(Sign sign) {
        return sign.getLine(2);
    }
    /**
     * Reads the Vault index from the given Sign instance.
     *
     * @param sign the Sign instance to read from
     * @return the Vault index, or null if it was not found or could not be parsed
     */
    public static Integer getVaultIndexFrom(Sign sign) {
        String line = sign.getLine(3);
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    /**
     * Checks if the given Sign instance matches the inputted Material, Player name, and Vault index.
     *
     * @param sign the Sign instance to check
     * @param material the Material to compare against
     * @param playerName the Player name to compare against
     * @param vaultIndex the Vault index to compare against
     * @return true if all fields match, false otherwise
     */
    public static boolean matches(Sign sign, Material material, String playerName, int vaultIndex) {
        Integer vaultIndexFrom = getVaultIndexFrom(sign);
        return getResourcesFrom(sign) != null
                && getMaterialFrom(sign) == material
                && getPlayerNameFrom(sign).equalsIgnoreCase(playerName)
                && vaultIndexFrom != null && vaultIndexFrom == vaultIndex;
    }
    /**
     * Retrieves the connected chest of a given sign.
     * @param sign The sign to retrieve the connected chest from.
     * @return The connected chest of the given sign, or null if there is no connected chest.
     */
    public static Chest getConnectedChest(Sign sign) {
        Block block = sign.getBlock();
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
        for (BlockFace face : faces) {
            Block chestBlock = block.getRelative(face);
            if (chestBlock.getState() instanceof Chest) {
                return (Chest) chestBlock.getState();
            }
        }
        return null;
    }

    /**
     * Retrieves the sign of a given chest, and optionally checks for the "[Resources]" text.
     * @param chest The chest to retrieve the sign from.
     * @param checkForText Whether to check for the "[Resources]" text in the sign.
     * @return The sign of the given chest, or null if there is no connected sign, or if checkForText is true and the sign does not contain the "[Resources]" text.
     */
    public static Sign getConnectedResourcesSign(Chest chest, boolean checkForText) {
        Block block = chest.getBlock();
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getState() instanceof Sign sign) {
                if (!checkForText || getResourcesFrom(sign) != null) {
                    return sign;
                }
            }
        }
        return null;
    }
    /**
     * Checks if the given sign is connected to a double chest.
     *
     * @param sign the sign to check
     * @return true if the sign is connected to a double chest, false otherwise
     */
    public static boolean isConnectedToDoubleChest(Sign sign) {
        Block attachedBlock = sign.getBlock().getRelative(((Attachable) sign.getBlock().getBlockData()).getAttachedFace());
        if (attachedBlock.getState() instanceof Chest chest) {
            return chest.getInventory().getHolder() instanceof DoubleChest;
        }
        return false;
    }
    /**
     * Returns a list of chests the given sign is connected to, with the first block added to the list always being the left-most sign.
     *
     * @param sign the sign to check for connected chests
     * @return a list of connected chests
     */
    public static List<Chest> getConnectedChests(Sign sign) {
        List<Chest> chests = new ArrayList<>();
        BlockState state = sign.getBlock().getState();
        if (state instanceof Container container) {
            chests.add(container.getInventory().getHolder() instanceof Chest ? (Chest) container.getInventory().getHolder() : null);
            if (container.getInventory().getHolder() instanceof DoubleChest doubleChest) {
                Chest leftChest = (Chest) doubleChest.getLeftSide();
                Chest rightChest = (Chest) doubleChest.getRightSide();
                if (leftChest.getLocation().getX() < rightChest.getLocation().getX()) {
                    chests.add(leftChest);
                    chests.add(rightChest);
                } else {
                    chests.add(rightChest);
                    chests.add(leftChest);
                }
            }
        }
        return chests;
    }


    /**
     * Returns the next available vault index for the specified player.
     *
     * @param player the player whose vault index is being requested
     * @return the next available vault index for the player
     */
    public static int getPlayersVaultIndex(Player player) {
        return new PlayerData(player.getUniqueId()).getNextIndex();
    }

    // Helper method to get the double chest if the given chest is part of a double chest
    private Block getDoubleChest(Chest chest) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : faces) {
            Block adjacent = chest.getBlock().getRelative(face);
            if (adjacent.getState() instanceof Chest && ((Chest) adjacent.getState()).getInventory().getHolder() == chest.getInventory().getHolder()) {
                return adjacent;
            }
        }
        return null;
    }

}
