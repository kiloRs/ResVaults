package com.thepaperraven.ai;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.config.resources.Resource;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.thepaperraven.ai.VaultKeys.*;
import static com.thepaperraven.config.resources.Resource.WHEAT;

public class Vault {

    private final UUID ownerId;
    private final Location location;
    private Date date;
    @Getter
    private final PersistentDataContainer container;
    private final int index;
    @Getter
    private final Chest chest;
    @Getter
    private final Sign sign;
    @Getter
    private final Resource resource;
    private boolean locked = true;


    public Vault(int index, UUID uuid, Location location, Resource resource){
        this.resource = resource;
        this.ownerId = uuid;
        this.location = location;
        this.index = index;
        this.chest = ((Chest) location.getBlock().getState());
        this.sign = ((Sign) location.getBlock().getRelative(BlockFace.UP).getState());

        container = chest.getPersistentDataContainer();
        if (save()) {
            ResourceVaults.log("Saving Vault: " + location + " - " + Bukkit.getPlayer(ownerId).getName());
            return;
        }

        ResourceVaults.log("Vault Instance LocateD!");
    }

    public Vault(int index, UUID ownerId, Location location, Material materialType) {
        this.ownerId = ownerId;
        this.location = location;
        this.index = index;
        this.chest = ((Chest) location.getBlock().getState());
        this.sign = ((Sign) location.getBlock().getRelative(BlockFace.UP).getState());
        this.resource = Resource.get(materialType);


        container = chest.getPersistentDataContainer();

        save();
    }


    @NotNull
    public Date getCreationDate(){
        return container.getOrDefault(getVaultKey(), DataType.DATE,new Date());
    }
    @NotNull
    public UUID getOwnerId() {
        return container.getOrDefault(getOwnerKey(), DataType.UUID,ownerId);
    }

    @NotNull
    public Location getLocation() {
        @Nullable Location locationString = container.getOrDefault(getLocationKey(), DataType.LOCATION,location);
        return locationString;
    }

    @NotNull
    public Material getMaterialType() {
        String materialTypeString = container.getOrDefault(getMaterialTypeKey(), PersistentDataType.STRING,WHEAT.getMaterial().getKey().getKey());
        Material material = Material.matchMaterial(materialTypeString);

        if (material == null){
            container.set(getMaterialTypeKey(),PersistentDataType.STRING,"WHEAT");
            material = Material.matchMaterial(materialTypeString);
            if (material == null){
                throw  new RuntimeException("Error with material for " + location.toString());
            }
        }
        return material;
    }

    public int getIndex() {
        return container.getOrDefault(getIndexKey(), PersistentDataType.INTEGER, this.index);}

    public void setIndex(int index) {
        container.set(getIndexKey(), PersistentDataType.INTEGER, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Vault vault)) return false;


        return new EqualsBuilder().append(getIndex(), vault.getIndex()).append(getOwnerId(), vault.getOwnerId()).append(getLocation(), vault.getLocation()).append(getMaterialType(), vault.getMaterialType()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getOwnerId()).append(getLocation()).append(getMaterialType()).append(getIndex()).toHashCode();
    }

    public boolean isActive() {
        return container.has(VaultKeys.getOwnerKey())&&container.has(VaultKeys.getVaultKey())&&container.has(VaultKeys.getLocationKey())&&container.has(VaultKeys.getIndexKey())&&container.has(VaultKeys.getMaterialTypeKey());
    }
    public boolean isOwner(Player player) {
        return ownerId.equals(player.getUniqueId());
    }

    public void remove() {
        container.remove(getOwnerKey());
        container.remove(getLocationKey());
        container.remove(getMaterialTypeKey());
        container.remove(getIndexKey());

        Block block = chest.getBlock();

        block.setType(Material.AIR);

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getState() instanceof Sign) {
                relative.setType(Material.AIR);
            }
        }
    }

    public Sign getSign() {
        return sign;
    }

    public void open() {
        Player player = Bukkit.getPlayer(ownerId);
        if (player == null){
            throw new RuntimeException("Player not found!");
        }
        player.openInventory(getInventory());
    }

    @NotNull
    protected Inventory getInventory() {
        return chest.getBlockInventory();
    }

    @Nullable
    public static Vault fromBlock(Block block) {
        if (block.getState() instanceof Chest chest) {
            if (chest.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID) &&
                    chest.getPersistentDataContainer().has(getLocationKey(), DataType.LOCATION) &&
                    chest.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING) &&
                    chest.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER)) {
                return fromPDC(chest);
            }
        } else if (block.getState() instanceof Sign sign) {
            BlockFace oppositeFace = ((Directional) sign.getBlockData()).getFacing().getOppositeFace();
            Block attachedBlock = sign.getBlock().getRelative(oppositeFace);
            if (attachedBlock.getState() instanceof Chest chest) {
                if (chest.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID) &&
                        chest.getPersistentDataContainer().has(getLocationKey(), DataType.LOCATION) &&
                        chest.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING) &&
                        chest.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER)) {
                    return fromPDC(chest);
                }
            }
        }
        return null;
    }


    @Nullable
    public static Vault fromIndex(Player player, int index){
        return VaultManager.getVaultById(index,player);
    }
    @Nullable
    public static Vault fromLocation(Location location){
        if (location.getBlock().getState() instanceof Chest chest){
            return fromPDC(chest);
        }
        return null;
    }

    public static Vault fromPDC(Chest chest){
        return fromPDC(chest.getPersistentDataContainer());
    }

    public static Vault fromPDC(PersistentDataContainer container){
        return new Vault(container.getOrDefault(VaultKeys.getIndexKey(),PersistentDataType.INTEGER,1),container.getOrDefault(VaultKeys.getOwnerKey(),DataType.UUID,null),container.getOrDefault(VaultKeys.getLocationKey(),DataType.LOCATION,null),Material.matchMaterial(container.getOrDefault(VaultKeys.getMaterialTypeKey(),PersistentDataType.STRING,"WHEAT")));
    }

    public boolean save(){
        return this.save(false);
    }
    public boolean save(boolean logSave) {

        if (logSave){
            ResourceVaults.log("Saving Vault: " + location.toString());
        }
        boolean changed = false;
        if (!container.has(getOwnerKey())) {
            container.set(getOwnerKey(), DataType.UUID, ownerId);
            changed = true;
        }
        if (container.has(getLocationKey())) {
            container.set(getLocationKey(), DataType.LOCATION, location);
            changed = true;
        }
        if (!container.has(getMaterialTypeKey())) {
            container.set(getMaterialTypeKey(), PersistentDataType.STRING, resource.getMaterial().getKey().getKey());
            changed = true;
        }
        if (!container.has(getIndexKey())) {
            container.set(getIndexKey(), PersistentDataType.INTEGER, index);
            changed = true;
        }
        if (!container.has(getVaultKey())) {
            //Set the current time as time of creation, if latest.
            container.set(VaultKeys.getVaultKey(), DataType.DATE, new Date(System.currentTimeMillis()));
            changed = true;
        }
        return changed;
    }


    public void withdraw(Player player, int amount) {
        Inventory vaultInventory = getInventory();
        Inventory playerInventory = player.getInventory();

        for (int i = 0; i < vaultInventory.getSize(); i++) {
            ItemStack item = vaultInventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            int stackSize = item.getAmount();
            if (amount <= stackSize) {
                ItemStack clone = item.clone();
                clone.setAmount(amount);
                vaultInventory.removeItem(clone);

                HashMap<Integer, ItemStack> overflow = playerInventory.addItem(clone);
                for (ItemStack stack : overflow.values()) {
                    vaultInventory.addItem(stack);
                }
                break;
            } else {
                ItemStack clone = item.clone();
                vaultInventory.removeItem(item);

                HashMap<Integer, ItemStack> overflow = playerInventory.addItem(clone);
                for (ItemStack stack : overflow.values()) {
                    vaultInventory.addItem(stack);
                }

                amount -= stackSize;
            }
        }
    }
    public void add(ItemStack stack) {
        chest.getInventory().addItem(stack);
    }

    public boolean hasSpace(ItemStack stack) {
        Inventory inv = chest.getInventory();

        HashMap<Integer, ItemStack> remaining = inv.addItem(stack);

        return remaining.isEmpty();
    }

    public int getCount() {
        int count = 0;
        for (ItemStack stack : chest.getInventory().getContents()) {
            if (stack != null) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    public int getSpace() {
        int space = 0;
        for (ItemStack stack : chest.getInventory().getContents()) {
            if (stack == null) {
                space += 64;
            } else if (stack.getType() == resource.getMaterial() && stack.getAmount() < 64) {
                space += 64 - stack.getAmount();
            }
        }
        return space;
    }

    public List<ItemStack> remove(int amount) {
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack stack : chest.getInventory().getContents()) {
            if (stack != null && stack.getType() == resource.getMaterial()) {
                int count = stack.getAmount();
                int toRemove = Math.min(amount, count);

                ItemStack clone = stack.clone();
                clone.setAmount(toRemove);
                items.add(clone);

                stack.setAmount(count - toRemove);
                amount -= toRemove;
                if (amount <= 0) {
                    break;
                }
            }
        }
        return items;
    }

    public void open(Player player) {
        player.openInventory(chest.getInventory());
    }


    public void lock(Player player) {
        sign.setLine(0, ChatColor.WHITE + "[Resources]");
        sign.setLine(1, ChatColor.GREEN+resource.getName().toUpperCase());
        sign.setLine(2, ChatColor.DARK_GREEN + player.getName());
        sign.setLine(3, ChatColor.RED + "LOCKED");
        sign.update();

        chest.setLock(player.getUniqueId().toString());


        chest.update(locked);

    }

    public void unlock() {
        sign.setLine(0, ChatColor.WHITE + "[Resources]");
        sign.setLine(1, ChatColor.GREEN+resource.getName().toUpperCase());
        sign.setLine(2, ChatColor.GREEN + Bukkit.getPlayer(ownerId).getName());
        sign.setLine(3, ChatColor.GREEN + "UNLOCKED");
        sign.update();

        if (chest.isLocked()) {
            chest.setLock(null);
            locked = false;
        }
        chest.update();


    }
    public boolean isDoubleChest() {
        Block block = chest.getBlock();

        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            BlockFace[] facesToCheck = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

            for (BlockFace face : facesToCheck) {
                Block neighbor = block.getRelative(face);
                if (neighbor.getType() == Material.CHEST && chest.getInventory().equals(((Chest) neighbor.getState()).getInventory())) {
                    return true; // Found a double chest
                }
            }
        }

        return false; // Single chest or not a chest block
    }

        @Override
    public String toString() {
        return "Vault{" +
                "ownerId=" + ownerId +
                ", location=" + location +
                ", materialType=" + resource.getMaterial().getKey().getKey() +
                ", index=" + index +
                ", resource=" + resource +
                '}';
    }
}
