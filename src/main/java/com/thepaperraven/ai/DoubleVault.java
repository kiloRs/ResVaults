package com.thepaperraven.ai;

import com.thepaperraven.config.resources.Resource;
import io.lumine.mythic.lib.api.util.EnumUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

public class DoubleVault extends Vault {

    private final int index;
    private final UUID uuid;
    private final Location location;
    private final Location otherLocation;
    private final Resource resource;

    public DoubleVault(int index, UUID uuid, Location location, Location secondLocation, Resource resource) {
        super(index, uuid, location, resource);
        this.index = index;
        this.uuid = uuid;
        this.location = location;
        otherLocation = secondLocation;
        this.resource = resource;
    }

    public DoubleVault(int index, UUID ownerId, Location location, Location secondLocation, Material materialType) {
        super(index, ownerId, location, materialType);
        this.index = index;
        this.uuid = ownerId;
        this.location = location;
        this.resource = EnumUtils.getIfPresent(Resource.class,materialType.getKey().getKey()).orElseThrow(() -> new RuntimeException("No Resource Type: " + materialType.getKey().getKey()));
        this.otherLocation = secondLocation;
    }

    public InventoryHolder getChests(){
        if (getInventory().getHolder() instanceof Chest chest && !(getInventory().getHolder() instanceof DoubleChest doubleChest)){
            return chest;
        }
        return getInventory().getHolder() instanceof DoubleChest doubleChest? doubleChest:null;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return getChest().getBlockInventory();
    }

    @Override
    public @NotNull Block getBlock() {
        return super.getLocation().getBlock();
    }

    @Override
    public @NotNull UUID getOwner() {
        return super.getOwner();
    }

    @Override
    public @NotNull Material getType() {
        return super.getMaterialType();
    }

    @Override
    public @NotNull Date getTimeCreated() {
        return super.getCreationDate();
    }

    @Override
    public Chest getChest() {
        return super.getChest();
    }

    @Override
    public Sign getSign() {
        return super.getSign();
    }

    @Override
    public int getIndex() {
        return super.getIndex();
    }
}
