package com.thepaperraven.ai.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

import static com.thepaperraven.ai.utils.ChestUtil.getDoubleChest;

public class BlockFaceUtil {

    /**
     * Returns the BlockFace of a sign attached to a presumed chest block.
     *
     * @param chestBlock the presumed chest block to check for an attached sign
     * @return the BlockFace of the chest block where the sign is attached, or null if no sign is found
     */
    public static BlockFace getFaceOfSign(Block chestBlock) {
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;
            Block relative = chestBlock.getRelative(face);
            if (relative.getState() instanceof org.bukkit.block.Sign sign) {
                if (sign.getLine(0).equalsIgnoreCase("[Resources]")) {
                    return face.getOppositeFace();
                }
            }
        }
        return null;
    }

    /**
     * Gets the BlockFace of the chest connected to the given chest block, if it is part of a double chest.
     * Returns null if the given chest block is not part of a double chest or the other chest cannot be found.
     *
     * @param chestBlock The chest block to check for a connected double chest.
     * @return The BlockFace of the connected chest, or null if no double chest is found.
     */
    public static BlockFace getFaceOfDoubleChest(Block chestBlock) {
        // Check if the given block is a chest
        if (!(chestBlock.getBlockData() instanceof Chest chest)) {
            return null;
        }

        // Check if the chest is part of a double chest
        DoubleChest doubleChest = getDoubleChest(chest);
        if (doubleChest == null) {
            return null;
        }

        // Get the location of one of the chests in the double chest
        Location chestLocation = doubleChest.getLocation().getBlock().getLocation();

        // Find the other chest in the double chest and get its facing direction
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF || face == BlockFace.UP || face == BlockFace.DOWN) {
                // Skip faces that are not adjacent to the chest block
                continue;
            }

            // Get the block at the adjacent face
            Block adjacentBlock = chestLocation.getBlock().getRelative(face);

            // Check if the adjacent block is a chest and is part of the same double chest
            if (adjacentBlock.getBlockData() instanceof Chest adjacentChest) {
                DoubleChest adjacentDoubleChest = getDoubleChest(adjacentChest);
                if (doubleChest.equals(adjacentDoubleChest)) {
                    // Return the face that the adjacent chest is on
                    return face.getOppositeFace();
                }
            }
        }

        // The other chest could not be found
        return null;
    }



//    private static BlockFace getFace(org.bukkit.Location loc1, org.bukkit.Location loc2) {
//        if (loc1.getBlockX() < loc2.getBlockX()) {
//            return BlockFace.EAST;
//        } else if (loc1.getBlockX() > loc2.getBlockX()) {
//            return BlockFace.WEST;
//        } else if (loc1.getBlockZ() < loc2.getBlockZ()) {
//            return BlockFace.SOUTH;
//        } else {
//            return BlockFace.NORTH;
//        }
//    }
}
