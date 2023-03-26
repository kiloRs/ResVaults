package com.thepaperraven.ai;

package com.thepaperraven.ai;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Vault {
    private final VaultMetadata metadata;
    private final List<Location> chestLocations;
    private final Location signLocation;
    private final Material allowedMaterial;

    public Vault(VaultMetadata metadata, List<Location> chestLocations, Location signLocation, Material allowedMaterial) {
        this.metadata = metadata;
        this.chestLocations = chestLocations;
        this.signLocation = signLocation;
        this.allowedMaterial = allowedMaterial;
    }

    public int getAmount() {
        int amount = 0;
        for (Location chestLocation : chestLocations) {
            BlockState state = chestLocation.getBlock().getState();
            if (state instanceof Chest) {
                Inventory inventory = ((Chest) state).getBlockInventory();
                for (ItemStack stack : inventory.getContents()) {
                    if (stack != null && stack.getType() == allowedMaterial) {
                        amount += stack.getAmount();
                    }
                }
            }
        }
        return amount;
    }

    public List<Inventory> getChestInventories() {
        List<Inventory> inventories = new ArrayList<>();
        for (Location chestLocation : chestLocations) {
            BlockState state = chestLocation.getBlock().getState();
            if (state instanceof Chest) {
                Inventory inventory = ((Chest) state).getBlockInventory();
                inventories.add(inventory);
            }
        }
        return inventories;
    }
}
