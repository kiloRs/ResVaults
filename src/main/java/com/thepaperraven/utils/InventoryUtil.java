package com.thepaperraven.utils;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.ai.vault.VaultPDContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryUtil {

    public static boolean isValidMaterial(Material material) {
        for (Material validMaterial : ResourceVaults.validMaterials) {
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
            VaultPDContainer p = new VaultPDContainer(container);
            if (!p.hasKeys()) {
                ResourceVaults.log("No Inventory Found from : " + LocationUtils.getStringOfLocation(location).toUpperCase());
                return null;
            }
            VaultInstance v = VaultInstance.getExistingVaultFrom(location.toBlockLocation());
            if (v == null) {
                ResourceVaults.log("No Output of VaultInstance!");
                return null;
            }
            return v.getInventory();
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

    public static VaultInstance getInstanceFrom(VaultInventory inventory) {
        if (!inventory.getContainer().hasKeys()) {
            ResourceVaults.log("VaultInv has no Keys!");
            return null;
        }
        VaultInstance vault = PlayerData.get(inventory.getContainer().getOwner()).getVault(inventory.getContainer().getVaultIndex());

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
        VaultInstance instanceFrom = getInstanceFrom(inventory);
        if (instanceFrom == null) {
            ResourceVaults.error("Bad Instance of Vault from " + inventory.getMetadata().getVaultIndex());
            return true;
        }
        VaultPDContainer c = instanceFrom.getContainer();
        if (c == null) {
            return true;
        }
        return c.hasKeys() && c.getMaterialKey() == material;
    }

    public static boolean hasKeys(VaultInstance vaultInstance) {
        return vaultInstance.getContainer().hasKeys();
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

    public static void createSign(Block blockInstance, String materialName, String playerName, int vaultIndex,@NotNull BlockFace face) {
        blockInstance.setBlockData(Bukkit.createBlockData(Material.BIRCH_WALL_SIGN));
        if (blockInstance.getState() instanceof Sign sign) {
            ResourceVaults.log("Sign Creation Started!");
            if (!(sign.getBlockData() instanceof WallSign wallSign)) {
                throw new RuntimeException("Invalid Sign");
            }
            wallSign.setFacing(face);

            if (Material.matchMaterial(materialName) == null) {
                        throw new RuntimeException("Material problem or null");
                    }
                    sign.setLine(0, ChatColor.BLACK + "[Resources]");
                    sign.setLine(1,ChatColor.BLACK + materialName.toUpperCase());
                    sign.setLine(2, ChatColor.BLACK +playerName.toUpperCase());
                    sign.setLine(3, ChatColor.BLACK +String.valueOf(vaultIndex).toUpperCase());
                    sign.setGlowingText(true);
                    sign.setEditable(false);
                    if (sign.update(true)) {
                        ResourceVaults.log("Sign Created!");
                    } else {
                        ResourceVaults.log("No Sign Creation?");
                    }
                }

    }
}

