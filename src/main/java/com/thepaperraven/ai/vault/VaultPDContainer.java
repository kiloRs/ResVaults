package com.thepaperraven.ai.vault;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.thepaperraven.config.VaultKeys.*;
import static com.thepaperraven.utils.LocationUtils.isDoubleChest;
@SuppressWarnings("deprecation")
@Getter
@SerializableAs("container")
public class VaultPDContainer implements ConfigurationSerializable, Invalidatable {
    private Container left;
    private Chest right = null;
    private InventoryHolder blockHolder;
    private int signState;
    private BlockFace signFace;
    private boolean valid = true;

    public static VaultPDContainer getDoubleChest(@NotNull DoubleChest doubleChest){
        return new VaultPDContainer(doubleChest);
    }
    public static VaultPDContainer getChest(@NotNull Chest chest){
        InventoryHolder holder = chest.getBlockInventory().getHolder();
        if (holder == null){
            return null;
        }
        if (holder instanceof DoubleChest doubleChest){
            return getDoubleChest(doubleChest);
        }
        return new VaultPDContainer(holder);
    }

    public VaultPDContainer(@NotNull InventoryHolder holder){
        if (holder instanceof DoubleChest doubleChest){
            this.blockHolder = doubleChest;
            this.left = ((Chest) doubleChest.getLeftSide().getInventory().getLocation().getBlock().getState());
            if (doubleChest.getRightSide() != null) {
                this.right = ((Chest) doubleChest.getRightSide().getInventory().getLocation().getBlock().getState());
            }
        }
        else if (holder instanceof Chest chestInventoryHolder){
            this.blockHolder = chestInventoryHolder;
            this.left = ((Chest) chestInventoryHolder.getBlock().getState());
        }
        else if (holder instanceof Container container){
            this.blockHolder = container;
            this.left = ((Container) container.getBlock().getState());
        }
        else {
            throw new RuntimeException("Invalid InventoryHolder for PDContainer!");
        }
        updateSignState();
    }

//    public VaultPDContainer(BlockState state) {
//        if (!(state instanceof Container)) {
//            throw new IllegalArgumentException("BlockState must be a Container");
//        }
//        this.left = ((Container) state);
//        if (state instanceof DoubleChest doubleChest) {
//            this.right = ((Chest) doubleChest.getRightSide().getInventory().getLocation().getBlock().getState());
//        }
//
//        updateSignState();
//    }

    public void setMaterialKey(Material material) {
        left.getPersistentDataContainer().set(getMaterialTypeKey(), PersistentDataType.STRING, material.name());
        if (hasSecondChest()) {
            right.getPersistentDataContainer().set(getMaterialTypeKey(), PersistentDataType.STRING, material.name());
        }
    }

    public boolean hasMaterialKey() {
        boolean hasMaterial = left.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING);
        if (hasSecondChest()) {
            hasMaterial &= right.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING)
                    && right.getPersistentDataContainer().get(getMaterialTypeKey(), PersistentDataType.STRING).equals(left.getPersistentDataContainer().get(getMaterialTypeKey(), PersistentDataType.STRING));
        }
        return hasMaterial;
    }

    public Material getMaterialKey() {
        if (!hasMaterialKey()) {
            return null;
        }
        return Material.getMaterial(left.getPersistentDataContainer().getOrDefault(getMaterialTypeKey(),
                PersistentDataType.STRING,"WHEAT"));
    }

    public void setOwner(UUID owner) {
        left.getPersistentDataContainer().set(getOwnerKey(), DataType.UUID, owner);
        if (hasSecondChest()) {
            right.getPersistentDataContainer().set(getOwnerKey(), DataType.UUID, owner);
        }
    }

    public boolean hasOwner() {
        boolean hasOwner = left.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID);
        if (hasSecondChest()) {
            hasOwner &= right.getPersistentDataContainer().has(getOwnerKey(), PersistentDataType.STRING)
                    && right.getPersistentDataContainer().get(getOwnerKey(), PersistentDataType.STRING).equals(left.getPersistentDataContainer().get(getOwnerKey(), PersistentDataType.STRING));
        }
        return hasOwner;
    }

    public UUID getOwner() {
        return left.getPersistentDataContainer().get(getOwnerKey(), DataType.UUID);
    }
    public void setVaultIndex(int index) {
        left.getPersistentDataContainer().set(getIndexKey(), PersistentDataType.INTEGER, index);
        if (hasSecondChest()) {
            right.getPersistentDataContainer().set(getIndexKey(), PersistentDataType.INTEGER, index);
        }
    }

    public boolean hasVaultIndex() {
        boolean hasIndex = left.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER);
        if (hasSecondChest()) {
            hasIndex &= right.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER)
                    && right.getPersistentDataContainer().get(getIndexKey(), PersistentDataType.INTEGER).intValue() == left.getPersistentDataContainer().get(getIndexKey(), PersistentDataType.INTEGER).intValue();
        }
        return hasIndex;
    }


    public int getVaultIndex() {
        if (!hasVaultIndex()) {
            return -1;
        }
        Integer leftIndex = left.getPersistentDataContainer().getOrDefault(getIndexKey(), PersistentDataType.INTEGER, 0);
        Integer rightIndex = right == null ? leftIndex : right.getPersistentDataContainer().getOrDefault(getIndexKey(), PersistentDataType.INTEGER, 0);
        return rightIndex.intValue()==leftIndex.intValue() ?leftIndex:0;
    }
    public Inventory getInventory() {
        return left.getInventory();
    }
    public InventoryHolder getInventoryHolder(){
        return blockHolder;
    }

    public boolean hasSecondChest(){
        return right!=null&&right.getBlock().getState() instanceof Chest chest;
    }
    public void updateSignState() {
        signState = 0;
        signFace = null;

        Block leftBlock = left.getBlock();
        Block rightBlock = right != null ? right.getBlock() : null;

        for (BlockFace face : BlockFace.values()) {
            Block signBlock = leftBlock.getRelative(face);
            if (signBlock.getState() instanceof Sign sign) {
                if (sign.getLine(0).equals("[Resources]")) {
                    signState = 1;
                    signFace = face;
                    return;
                }
            }

            if (rightBlock != null && signState == 0 && signFace ==null) {
                signBlock = rightBlock.getRelative(face);
                if (signBlock.getState() instanceof Sign sign) {
                    if (sign.getLine(0).equals("[Resources]")) {
                        signState = 2;
                        signFace = face;
                        return;
                    }
                }
            }
        }
    }
    public Sign getSign(){
        if (signState==0){
            return null;
        }
        else if (signState == 1){
            BlockState state = left.getBlock().getRelative(signFace).getState();
            return state instanceof Sign sign?sign:null;
        }
        else if (signState == 2){
            BlockState state = right.getBlock().getRelative(signFace).getState();
            return state instanceof Sign sign?sign:null;
        }
        return null;
    }
    public void updateFromSign(){
        updateSignState();
        Block relative = null;
        if (signState>0 && signFace != null){
            if (signState == 1){
                relative = left.getBlock().getRelative(signFace);
                if (relative.getState() instanceof Sign sign) {
                    if (sign.getLines().length>3) {
                        UUID playerUniqueId = Bukkit.getPlayerUniqueId(sign.getLine(2));
                        if (playerUniqueId != null) {
                            setOwner(playerUniqueId);
                            String line = sign.getLine(3);
                            int number = Integer.parseInt(line);
                            setVaultIndex(number);
                            String matLine = sign.getLine(1);
                            Material material = Material.matchMaterial(matLine);

                            if (material == null){
                                material = Material.WHEAT;
                            }
                            setMaterialKey(material);
                        }
                    }
                }
            }
        }
    }
    public boolean hasKeys(){
        return hasKeys(isDoubleChest(left.getBlock()));
    }
    public boolean hasKeys(boolean requireDoubleChest){
        if (hasSecondChest() && requireDoubleChest){
            return hasOwner() && hasMaterialKey() && hasVaultIndex();
        }
        return hasMaterialKey() && hasOwner() && hasVaultIndex();
    }

    public Location getLocationOfSign(){
        updateSignState();

        if (signState>0){
            if (signState == 1) {
                Block b = left.getBlock().getRelative(signFace);
                return b.getState() instanceof Sign aSign?aSign.getLocation().toBlockLocation():null;
            }
            if (signState == 2){
                Block b = right.getBlock().getRelative(signFace);
                return b.getState() instanceof Sign aSign?aSign.getLocation().toBlockLocation():null;
            }
        }
        return null;
    }
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> mapping = new HashMap<>();
        if (isDoubleChest(left.getBlock()) && hasSecondChest()){
            mapping.put("chest.left.location",left.getLocation().toBlockLocation().serialize());
            mapping.put("chest.right.location",right.getLocation().toBlockLocation().serialize());
            mapping.put("sign.face",signFace);
            mapping.put("sign.state",signState);
            mapping.put("sign.location",getLocationOfSign().toBlockLocation().serialize());
            if (hasOwner()){
                mapping.put("owner",getOwner().toString());
            }
            if (hasMaterialKey()){
                mapping.put("material",getMaterialKey().name());
            }
            if (hasVaultIndex()){
                mapping.put("index",getVaultIndex());
            }

            return mapping;
        }
        mapping.put("chest.left.location",left.getLocation().toBlockLocation().serialize());
        mapping.put("chest.right.location",hasSecondChest()?right.getLocation().toBlockLocation().serialize():null);
        mapping.put("sign.face",signFace);
        mapping.put("sign.state",signState);
        mapping.put("sign.location",getLocationOfSign().toBlockLocation().serialize());
        if (hasOwner()){
            mapping.put("owner",getOwner().toString());
        }
        if (hasMaterialKey()){
            mapping.put("material",getMaterialKey().name());
        }
        if (hasVaultIndex()){
            mapping.put("index",getVaultIndex());
        }

        return mapping;
    }
    public static VaultPDContainer getVaultContainerByBlock(Player player, Block block, boolean checkRegistered) {
        if (block.getState() instanceof InventoryHolder holder && !checkRegistered){
            return new VaultPDContainer(holder);
        }
        else if (checkRegistered){
            for (VaultInstance vaultInstance : PlayerData.get(player.getUniqueId()).getVaults().values()) {
                if (block.getState() instanceof Container container){
                    if (vaultInstance.getContainer().hasSecondChest() && !(container.getInventory().getHolder() instanceof DoubleChest doubleChest)){
                        continue;
                    }
                    if (container.getInventory().equals(vaultInstance.getContainer().getInventory())){
                        return vaultInstance.getContainer();
                    }
                    continue;
                }
            }
        }
        return null;
    }
    public void updateSignText(boolean lock){
        if (hasSign()) {
            if (getSign() == null){
                ResourceVaults.error("Problem with Sign instance of " + getVaultIndex() + " in " + Bukkit.getPlayer(getOwner()).getName());
                return;
            }
            getSign().setLine(0, ChatColor.GREEN + "[Resources]");
            getSign().setLine(1,ChatColor.GREEN + getMaterialKey().name());
            getSign().setLine(2, ChatColor.GREEN + Bukkit.getPlayer(getOwner()).getName());
            getSign().setLine(3, ChatColor.GREEN + String.valueOf(getVaultIndex()));
            getSign().setEditable(!lock);
            getSign().setGlowingText(lock);
        }
    }

    public void saveToBlock(VaultMetadata vaultMetadata){
        setVaultIndex(vaultMetadata.getVaultIndex());
        setOwner(vaultMetadata.getOwnerUUID());
        setMaterialKey(vaultMetadata.getAllowedMaterial());

        updateSignText(true);
    }

    public boolean hasSign() {
        updateSignState();
        return getSign()!=null;
    }

    public void removeFromBlock(boolean breakSign) {
        left.getPersistentDataContainer().remove(getOwnerKey());
        left.getPersistentDataContainer().remove(getIndexKey());
        left.getPersistentDataContainer().remove(getMaterialTypeKey());
        if (hasSecondChest()){
            right.getPersistentDataContainer().remove(getOwnerKey());
            right.getPersistentDataContainer().remove(getIndexKey());
            right.getPersistentDataContainer().remove(getMaterialTypeKey());
        }

        if (breakSign){
            if (hasSign()){
                if (getSign().getBlock().breakNaturally()) {
                    ResourceVaults.log("Sign Broke by Server - PDC Removed");
                }
            }
        }
        invalidate();


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VaultPDContainer that = (VaultPDContainer) o;
        if (signState != that.signState || signFace != that.signFace){
            updateSignState();
            that.updateSignState();
        }
        if (signState != that.signState || signFace != that.signFace){
            ResourceVaults.error("Invalid Match of PDContainer!");
            return false;
        }

        return new EqualsBuilder().append(signState, that.signState).append(left, that.left).append(right, that.right).append(blockHolder, that.blockHolder).append(signFace, that.signFace).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(left).append(right).append(blockHolder).toHashCode();
    }
    @Override
    public void invalidate() {
        this.left = null;
        this.right = null;;
        this.blockHolder = null;
        this.valid = false;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
}