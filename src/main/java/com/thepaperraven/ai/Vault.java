package com.thepaperraven.ai;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.material.Attachable;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Getter
public class Vault {
    private static final String SIGN_HEADER = "[Resources]";

    private final int index;
    private final UUID ownersUUID;
    private final PlayerData owningData;
    private Material material;
    private final Location chestLocation;
    private final Location signLocation;
    private Location doubleChestLocation;

    public Vault(int index, PlayerData owningData, Location chestLocation){
        this(index,owningData,chestLocation,getFaceOrNull(chestLocation),getVaultMaterial(chestLocation));
    }

    public Vault(int index, PlayerData owner, Location chestLocation, BlockFace doubleChestFace, Material type){
        this(index,owner.getUuid(),chestLocation,getSignLocation(chestLocation).toBlockLocation(),getDoubleChest(doubleChestFace,chestLocation),type);
    }


    public Vault(int index, UUID owner, Location chest, Location sign, @Nullable Location doubleChestLocation, Material type){
        this.index = index;
        this.ownersUUID = owner;
        this.owningData = new PlayerData(ownersUUID);
        this.material = type;
        this.chestLocation = chest;
        this.signLocation = sign;
        this.doubleChestLocation = doubleChestLocation;

        if (this.material == null){
            this.material = Material.WHEAT;
        }
        if (this.doubleChestLocation == chestLocation){
            this.doubleChestLocation = null;
        }
        if (this.doubleChestLocation == null){
            Location doubleChestTry = getDoubleChestLocation(chestLocation);

            if (doubleChestTry == null || (doubleChestTry.equals(chestLocation)||doubleChestTry==chestLocation)){
                return;
            }
            this.doubleChestLocation = doubleChestTry;
        }
    }

    public static Vault getInstance(int index, UUID ownersUUID, Location chestLocation) {
        Location signLocation = getSignLocation(chestLocation);
        Location doubleChestLocation = getDoubleChestLocation(chestLocation);

        return new Vault(index, ownersUUID, chestLocation, signLocation, doubleChestLocation, getVaultMaterial(chestLocation));
    }

    @Nullable
    private static Location getSignLocation(Location chestLocation) {
        for (BlockFace face : BlockFace.values()) {
            Block block = chestLocation.getBlock().getRelative(face);
            if (block.getState() instanceof Sign sign && Objects.equals(sign.getLine(0), SIGN_HEADER)) {
                return block.getLocation();
            }
        }
        return null;
    }

    private static Location getDoubleChestLocation(Location chestLocation) {
        Chest chest = (Chest) chestLocation.getBlock().getState();
        BlockFace facing = ((Attachable) chest.getBlockData()).getAttachedFace();
        Block block = chestLocation.getBlock().getRelative(facing);

        if (block.getState() instanceof Chest doubleChest) {
            DoubleChest doubleChestInventory = (DoubleChest) doubleChest.getInventory().getHolder();
            return doubleChest.getLocation();
        }
        return null;
    }

    private static Material getVaultMaterial(Location chestLocation) {
        for (BlockFace face : BlockFace.values()) {
            Block block = chestLocation.getBlock().getRelative(face);
            if (block.getState() instanceof Sign sign && Objects.equals(sign.getLine(0), SIGN_HEADER)) {
                String line2 = sign.getLine(1).toUpperCase().replace(" ", "_");
                Material material = Material.matchMaterial(line2);
                if (material != null) {
                    return material;
                }
            }
        }
        return Material.WHEAT;
    }
    private static BlockFace getFaceOrNull(Location chestLocation) {
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};

        for (BlockFace face : faces) {
            Block other = chestLocation.getBlock().getRelative(face);
            if (chestLocation.getBlock().getState() instanceof Chest chest){
                if (other.getState() instanceof Chest chest2){
                    if (chest.getBlockInventory().getHolder() instanceof DoubleChest doubleChest && chest2.getBlockInventory().getHolder() instanceof DoubleChest doubleChest1){
                        return doubleChest.getLeftSide()==doubleChest1.getLeftSide()&&doubleChest.getRightSide()==doubleChest1.getRightSide()||doubleChest.getInventory()==doubleChest1.getInventory()?face:null;
                    }
                }
            }
        }
        return null;
    }

    private static Location getDoubleChest(BlockFace faceConnected, Location chestLocation) {
        if (chestLocation.toBlockLocation().getBlock().getState() instanceof Chest chest) {
            Block connectedBlock = chest.getBlock().getRelative(faceConnected);

            if (connectedBlock.getState() instanceof Chest secondChest){
                if (chest.getBlockInventory().getHolder() instanceof DoubleChest doubleChest){
                    if ((chest.getBlockInventory()==doubleChest.getInventory()&&secondChest.getBlockInventory()==doubleChest.getInventory())){
                        return secondChest.getLocation().toBlockLocation();
                    }
                }
            }
        }
        return null;
    }
}
