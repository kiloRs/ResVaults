package com.thepaperraven.ai.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

public class ChestUtil {

    /**
     * Determines whether a chest block is part of a double chest.
     *
     * @param chestBlock the chest block to check
     * @return true if the chest block is part of a double chest, false otherwise
     */
    public static boolean isDoubleChest(Block chestBlock) {
        BlockFace[] facesToCheck = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : facesToCheck) {
            Block neighbor = chestBlock.getRelative(face);
            if (neighbor.getState() instanceof Chest neighborChest && neighborChest.getInventory().getLocation().equals(((Chest) chestBlock.getState()).getInventory().getLocation())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the neighbor chest of a double chest.
     *
     * @param chest the chest to get the neighbor of
     * @return the neighbor chest, or null if the chest is not part of a double chest
     */
    public static Chest getNeighborChest(Chest chest) {
        if (!isDoubleChest(chest.getBlock())) {
            return null;
        }
        BlockFace[] facesToCheck = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : facesToCheck) {
            Block neighbor = chest.getBlock().getRelative(face);
            if (neighbor.getState() instanceof Chest neighborChest && neighborChest.getInventory().getLocation().equals(chest.getInventory().getLocation())) {
                return neighborChest;
            }
        }
        return null;
    }

    /**
     * Gets the face of a chest block.
     *
     * @param chestBlock the chest block to get the face of
     * @return the face of the chest block
     */
    public static BlockFace getFaceOfChest(Block chestBlock) {
        if (isDoubleChest(chestBlock)) {
            return null;
        }
        for (BlockFace face : BlockFace.values()) {
            if (chestBlock.getRelative(face).getType() == Material.CHEST) {
                return face.getOppositeFace();
            }
        }
        return null;
    }

    /**
     * Gets the sign attached to a chest block.
     *
     * @param chestBlock the chest block to get the attached sign of
     * @param facing     the face of the chest block to check for the sign on
     * @return the attached sign, or null if there is no sign
     */
    public static Sign getSignAttachedTo(Block chestBlock, BlockFace facing) {
        for (BlockFace signFacing : new BlockFace[]{facing, BlockFace.DOWN}) {
            Block attached = chestBlock.getRelative(signFacing);
            if (attached.getState() instanceof Sign) {
                return (Sign) attached.getState();
            }
        }
        return null;
    }

}
