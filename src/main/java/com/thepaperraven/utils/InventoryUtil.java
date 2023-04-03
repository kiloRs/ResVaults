package com.thepaperraven.utils;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.PDC;
import com.thepaperraven.ai.vault.Vault;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

public class InventoryUtil {

    public static boolean isValidMaterial(Material material) {
        for (Material validMaterial : ResourceVaults.getConfiguration().getAllowedMaterials()) {
            if (material == validMaterial) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVaultInventory(Inventory inventory) {
        return inventory.getHolder() instanceof VaultInventory vaultInventory;
    }

    public static boolean isContainerInventory(Inventory inventory) {
        return inventory.getHolder() instanceof Container container;
    }

    public static VaultInventory getVaultInventoryFrom(Location location) {
        if (location.toBlockLocation().getBlock().getState() instanceof Container container) {
            PDC p = new PDC(container);
            if (!p.hasKeys()) {
                ResourceVaults.log("No Inventory Found from : " + LocationUtils.getStringOfLocation(location).toUpperCase());
                return null;
            }
            Vault v = Vault.getExistingVaultFrom(location.toBlockLocation());
            if (v == null) {
                ResourceVaults.log("No Output of VaultInstance!");
                return null;
            }
            return v.getVaultInventory();
        }
        return null;
    }

    public static VaultInventory getVaultInventoryFrom(Inventory inventory) {
        if (inventory.getHolder() instanceof DoubleChest doubleChest) {
            ResourceVaults.log("DoubleChest!");
            return getVaultInventoryFrom(doubleChest.getLocation());
        } else if (inventory.getHolder() instanceof Container container) {
            ResourceVaults.log("Container!");
            return getVaultInventoryFrom(container.getInventory());
        }
        VaultInventory vaultInventoryFrom = getVaultInventoryFrom(inventory.getLocation());

        if (vaultInventoryFrom == null) {
            ResourceVaults.log("NO VAULT INVENTORY FROM INVENTORY!");
            return null;
        }
        return vaultInventoryFrom;

    }

    public static Vault getInstanceFrom(VaultInventory inventory) {
        if (!inventory.getContainer().hasKeys()) {
            ResourceVaults.log("VaultInv has no Keys!");
            return null;
        }
        Vault vault = PlayerData.get(inventory.getContainer().getOwner()).getVaults().get(inventory.getContainer().getVaultIndex());

        if (vault == null) {
            ResourceVaults.log("No Vault from VaultInv");
            return null;
        }
        return vault;
    }

    public static boolean acceptsMaterial(Container container, Material m) {
        return acceptsMaterial(getVaultInventoryFrom(container.getLocation()), m);
    }

    public static boolean acceptsMaterial(VaultInventory inventory, Material material) {
        Vault instanceFrom = getInstanceFrom(inventory);
        if (instanceFrom == null) {
            ResourceVaults.error("Bad Instance of Vault from " + inventory.getContainer().getVaultIndex());
            return true;
        }
        PDC c = instanceFrom.getContainer();
        if (c == null) {
            return true;
        }
        return c.hasKeys() && c.getMaterialKey() == material;
    }

    public static boolean hasKeys(Vault vault) {
        return vault.getContainer().hasKeys();
    }

    public static boolean hasKeys(VaultInventory inventory) {
        return inventory.getContainer().hasKeys();
    }

    public static @Nullable BlockFace getFacing(Block block) {
        if (block.getState() instanceof org.bukkit.material.Directional directional){
            ResourceVaults.log("Directional: " + directional.getClass());
            return directional.getFacing();
        }
        if (block.getBlockData() instanceof Directional directional) {
            ResourceVaults.log("Directional-2: " + directional.getClass());

            return directional.getFacing();
        }
        if (block.getState().getBlockData() instanceof Rotatable rotatable) {
            return rotatable.getRotation();
        }
        if (block.getType().name().endsWith("WALL_SIGN")) {
            BlockData data = block.getBlockData();
            if (data instanceof WallSign wallSign) {
                return wallSign.getFacing();
            }
        }
        return null;
    }

}

