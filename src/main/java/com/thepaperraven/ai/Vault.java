package com.thepaperraven.ai;

import com.jeff_media.morepersistentdatatypes.DataType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class Vault {

    private final int index;
    private final Location chestLocation;
    private final Chest chest;
    @Getter
    @Setter
    private Location doubleChestLocation;
    @Getter
    @Setter
    private final Material material;
    @Getter
    private final Player owner;
    @Getter
    private boolean locked;
    @Getter
    private final @NotNull Date creationTime;
    @Getter
    private final Inventory inventory;
    @Getter
    @Setter
    private final boolean isDouble;

    @NotNull
    public static Vault getNewVaultInstance(PlayerData data,Location chestLocation, Material material,boolean doubleChest){
        Vault vault = new Vault(data.getVaults().size()+1,chestLocation,material,data.getPlayer(),false,doubleChest);

        if (doubleChest) {
            vault.setDoubleChestLocation(vault.chestLocation.getBlock().getRelative(vault.getSecondChestOrNull()).getLocation());
        }
        return vault;
    }

    @Nullable
    public static Vault getSavedVault(PlayerData data, int index){
        if (!data.getVaults().containsKey(index)){
            return null;
        }
        return data.getVaults().get(index);
    }

    public Vault(int index, Location location, Material material, Player owner){
        this(index,location,material,owner,true,false);
    }

    public Vault(int index, Location chestLocation, Material material, Player owner, boolean locked, boolean isDouble) {
        this.index = index;
        this.chestLocation = chestLocation;
        this.material = material == null ? Material.WHEAT : material;
        this.owner = owner;
        this.chest = ((Chest) chestLocation.getBlock().getState());
        this.locked = locked;
        this.creationTime = chest.getPersistentDataContainer().getOrDefault(VaultKeys.getVaultKey(), DataType.DATE,new Date(System.currentTimeMillis()));
        this.isDouble = isDouble;
        this.inventory = ((Chest) chestLocation.getBlock().getState()).getInventory();
        BlockFace face = getSecondChestOrNull();
        this.doubleChestLocation = getSecondChestOrNull()==null?null:chestLocation.getBlock().getRelative(face).getLocation();
    }

    public int getIndex() {
        return index;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public Material getMaterial() {
        return material;
    }

    public Player getOwner() {
        return owner;
    }

    public BlockFace getSecondChestOrNull() {
        if (!isDouble) {
            return null;
        }
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : faces) {
            if (chestLocation.getBlock().getRelative(face).getState() instanceof Chest chest) {
                chest = (Chest) chestLocation.getBlock().getRelative(face).getState();
                if (chest.getBlockInventory().equals(inventory)) {
                    return face;
                }
            }
        }
        return null;
    }

    public void lock() {
        locked = true;
        updateSign();
    }

    public void unlock() {
        locked = false;
        updateSign();
    }

    public void updateSign() {
        Sign sign = getSign();
        if (sign == null) {
            return;
        }
        String[] lines = sign.getLines();
        lines[0] = "[RESOURCES]";
        lines[1] = material.toString();
        lines[2] = owner.getName();
        lines[3] = locked ? "LOCKED" : "";
        new BukkitRunnable() {
            @Override
            public void run() {
                sign.setLine(0, lines[0]);
                sign.setLine(1, lines[1]);
                sign.setLine(2, lines[2]);
                sign.setLine(3, lines[3]);
                sign.update();
            }
        }.runTask(ResourceVaults.getPlugin());
    }

    public Sign getSign() {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : faces) {
            Sign sign = getSign(face);
            if (sign != null) {
                return sign;
            }
        }
        return null;
    }


}