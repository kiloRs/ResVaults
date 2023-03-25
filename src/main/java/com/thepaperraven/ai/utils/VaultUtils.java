package com.thepaperraven.ai.utils;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultKeys;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

import static com.thepaperraven.ai.VaultKeys.*;

public class VaultUtils {
    /**
     * Returns the Vault object corresponding to the block that was placed
     * @param block The block that was placed
     * @return The Vault object, or null if the block isn't a chest or the player is not the owner
     */
    public static Vault getVaultFromBlock(Block block) {
        // Make sure the block is a chest
        if (!isChestOrSign(block)) {
            return null;
        }

        // Make sure the player is the owner
        return fromBlock(block);
    }
    private static Vault fromBlock(Block block) {
        if (block.getState() instanceof Chest chest){
            return chest.getPersistentDataContainer().has(VaultKeys.getVaultKey()) && chest.getPersistentDataContainer().has(VaultKeys.getIndexKey()) && chest.getPersistentDataContainer().has(VaultKeys.getOwnerKey()) && chest.getPersistentDataContainer().has(VaultKeys.getMaterialTypeKey()) && chest.getPersistentDataContainer().has(VaultKeys.getLocationKey())?new Vault(chest.getPersistentDataContainer().getOrDefault(VaultKeys.getIndexKey(), PersistentDataType.INTEGER,-1).intValue(),chest.getPersistentDataContainer().getOrDefault(VaultKeys.getLocationKey(),DataType.LOCATION,block.getLocation()).toBlockLocation(),Material.matchMaterial(chest.getPersistentDataContainer().getOrDefault(VaultKeys.getMaterialTypeKey(),PersistentDataType.STRING,"WEHAT")), Bukkit.getPlayer(chest.getPersistentDataContainer().getOrDefault(VaultKeys.getOwnerKey(), DataType.UUID, null))):null;
        }
        if (block.getState() instanceof Sign sign){
            if (!((TextComponent) sign.line(0).asComponent()).content().equalsIgnoreCase("[Resources]")){
                return null;
            }
            Block signBlock = sign.getBlock();

            if (signBlock.getBlockData() instanceof WallSign wallSign){
                BlockFace backSide = wallSign.getFacing().getOppositeFace();

                Block possible = signBlock.getRelative(backSide);
                if (possible.getState() instanceof Chest chest){
                    return chest.getPersistentDataContainer().has(VaultKeys.getVaultKey()) && chest.getPersistentDataContainer().has(VaultKeys.getIndexKey()) && chest.getPersistentDataContainer().has(VaultKeys.getOwnerKey()) && chest.getPersistentDataContainer().has(VaultKeys.getMaterialTypeKey()) && chest.getPersistentDataContainer().has(VaultKeys.getLocationKey())?new Vault(chest.getPersistentDataContainer().getOrDefault(VaultKeys.getIndexKey(), PersistentDataType.INTEGER,-1).intValue(),chest.getPersistentDataContainer().getOrDefault(VaultKeys.getLocationKey(),DataType.LOCATION,block.getLocation()).toBlockLocation(),Material.matchMaterial(chest.getPersistentDataContainer().getOrDefault(VaultKeys.getMaterialTypeKey(),PersistentDataType.STRING,"WEHAT")), Bukkit.getPlayer(chest.getPersistentDataContainer().getOrDefault(VaultKeys.getOwnerKey(), DataType.UUID, null))):null;

                }
            }
        }
        return null;
    }

    /**
     * Checks if a block is a chest
     * @param block The block to check
     * @return true if the block is a chest, false otherwise
     */
    private static boolean isChestOrSign(Block block) {
        return block.getState() instanceof Chest chest||block.getState() instanceof Sign sign;
    }

    /**
     * Teleports a player to a vault
     * @param player The player to teleport
     * @param vault The vault to teleport to
     */
    public static void teleportToVault(Player player, Vault vault) {
        Location location = vault.getChestLocation();
        location.setX(location.getX() + 0.5);
        location.setY(location.getY() + 0.5);
        location.setZ(location.getZ() + 0.5);
        player.teleport(location);
    }

    public static void removeItemsFrom(Player player, Material type, int amount) {
        PlayerData playerData = ResourceVaults.getPlayerData(player);

        Map<Integer, Vault> map = playerData.getVaults();

        if (map.isEmpty()) {
            return;
        }

        int count = 0;
        for (Map.Entry<Integer, Vault> entry : map.entrySet()) {
            Vault vault = entry.getValue();

            if (vault.getMaterial() == type) {
                Inventory inventory = vault.getChest().getBlockInventory();

                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() == type) {
                        int itemAmount = item.getAmount();
                        if (count + itemAmount <= amount) {
                            inventory.removeItem(item);
                            count += itemAmount;
                        } else {
                            item.setAmount(amount - count);
                            count = amount;
                            break;
                        }
                    }
                    if (count == amount) {
                        return;
                    }
                }
            }
        }
    }public static Vault getVault(Player player, int index, Location chestLocation) {
        Block block = chestLocation.getBlock();
        if (!(block.getState() instanceof Chest || block.getState() instanceof DoubleChest)) {
            return null;
        }
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getBlockData() instanceof WallSign || relative.getState() instanceof Sign sign) {
                Sign sign = (Sign) relative.getState();
                if (!sign.getLine(1).equals("[RESOURCES]")) {
                    continue;
                }
                Material material = Material.getMaterial(sign.getLine(2));
                if (material == null) {
                    material = Material.WHEAT;
                }
                String playerName = sign.getLine(3);
                if (!player.getName().equals(playerName)) {
                    return null;
                }
                PlayerData playerData = ResourceVaults.getPlayerData(player);
                Map<Integer, Vault> vaults = playerData.getVaults();
                if (!vaults.containsKey(index)) {
                    return null;
                }
                Vault vault = vaults.get(index);
                if (vault.getChest().getLocation().toBlockLocation().equals(chestLocation.toBlockLocation())) {
                    return vault;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean isVault(Block block) {
        if (block.getState() instanceof Chest chest) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            for (BlockFace face : faces) {
                Block relative = block.getRelative(face);
                if (relative.getState() instanceof Sign sign) {
                    if (sign.getLine(0).equals("[Resources]")) {
                        if (chest.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID) &&
                                chest.getPersistentDataContainer().has(getLocationKey(), DataType.LOCATION) &&
                                chest.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING) &&
                                chest.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER)) {
                            return true;
                        }
                    }
                }
            }
        } else if (block.getState() instanceof Sign sign) {
            if (sign.getLine(0).equals("[Resources]")) {
                BlockFace oppositeFace = ((Directional) block.getBlockData()).getFacing().getOppositeFace();
                Block attachedBlock = block.getRelative(oppositeFace);
                if (attachedBlock.getState() instanceof Chest chest) {
                    return chest.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID) &&
                            chest.getPersistentDataContainer().has(getLocationKey(), DataType.LOCATION) &&
                            chest.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING) &&
                            chest.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER);
                }
            }
        }
        return false;
    }
}
