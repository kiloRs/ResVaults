package com.thepaperraven.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;

public class VaultAmountListener implements Listener {

    private Sign getAttachedVaultSign(Chest chest) {
        for (BlockFace blockFace : BlockFace.values()) {
            Block attachedBlock = chest.getBlock().getRelative(blockFace);
            if (blockFace != BlockFace.NORTH && blockFace != BlockFace.SOUTH && BlockFace.EAST != blockFace && blockFace != BlockFace.WEST){
                continue;
            }
            if (attachedBlock.getState() instanceof Sign attachedSign) {
                if (attachedSign.getLine(0).equals("[Resources]")) {
                    return attachedSign;
                }
            }
        }
        return null;
    }

}
