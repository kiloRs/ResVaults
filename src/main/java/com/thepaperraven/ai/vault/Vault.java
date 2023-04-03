package com.thepaperraven.ai.vault;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.VaultManager;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.events.VaultCreateEvent;
import com.thepaperraven.utils.LocationUtils;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Vault {

    private List<Location> blockLocations = new ArrayList<>();
    private final UUID ownerUUID;
    private final int vaultIndex;
    private final Material material;
    private final VaultInventory vaultInventory;
    private int totalItems;
    private final int totalSlots;
    private final PDC container;
    private int freeSpace;
    private long createdOn;
    private final int capacity;
    private int amount;
    private boolean registered;

    public static boolean isVault(Container container){
        return PDC.get(container).hasKeys();
    }
    public Vault(UUID ownerUUID, int vaultIndex, Material materialType, PDC pdc) {
        this.ownerUUID = ownerUUID;
        this.vaultIndex = vaultIndex;
        this.material = materialType;
        this.container = pdc;
        this.vaultInventory = new VaultInventory(this);
        this.totalSlots = vaultInventory.getInventory().getSize();
        this.capacity = vaultInventory.getInventory().getSize() * material.getMaxStackSize();
        this.amount = capacity - freeSpace;
        this.freeSpace = updateFreeSpace();
        this.blockLocations = new ArrayList<>();

        this.blockLocations.add(pdc.getLeft().getLocation().toBlockLocation());
        if (pdc.hasSecondChest()){
            this.blockLocations.add(pdc.getRight().getLocation().toBlockLocation());
        }
        else {
            if (pdc.getLeft().getBlock().getBlockData() instanceof Directional directional) {
                BlockFace facing = directional.getFacing();

                Block front = pdc.getLeft().getBlock().getRelative(facing);

                if (front.getState() instanceof Sign sign && sign.getLine(0).equalsIgnoreCase("[Resources]")){
                    this.blockLocations.add(sign.getLocation().toBlockLocation());
                }
            }
            return;
        }
        if (pdc.hasSigns()){
            Block left = pdc.getLeft().getBlock();
            Block right = pdc.getRight().getBlock();

            if (left.getBlockData() instanceof Directional directional && right.getBlockData() instanceof Directional otherDirectional){
                BlockFace facing = directional.getFacing();
                if (facing ==otherDirectional.getFacing()) {
                    Block frontLeft = left.getRelative(facing);
                    Block frontRight = right.getRelative(facing);
                    if (frontLeft.getState() instanceof Sign sign && sign.getLine(0).contains("[Resources]")){
                        this.blockLocations.add(frontLeft.getLocation().toBlockLocation());
                    }
                    if (frontRight.getState() instanceof Sign sign && sign.getLine(0).contains("[Resources]")){
                        this.blockLocations.add(frontRight.getLocation().toBlockLocation());
                    }
                }
            }
        }
    }

    private int updateFreeSpace() {
        int freeSpace =0;
        for (int i = 0; i < getVaultInventory().getInventory().getSize(); i++) {
            ItemStack item = vaultInventory.getInventory().getItem(i);
            if (item == null || item.getType()==Material.AIR) {
                freeSpace += vaultInventory.getInventory().getMaxStackSize();
                continue;
            } else if (item.getType()==material) {
                freeSpace += (vaultInventory.getInventory().getMaxStackSize() - item.getAmount());
                continue;
            }
        }
        return freeSpace;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }


    public static Vault getExistingVaultFrom(Location containerLoc) {
        BlockState state = containerLoc.getBlock().getState();
        if (!(state instanceof Container container)) {
            ResourceVaults.error("Not a chest....");
            return null; // not a container
        }
        Material material = ResourceVaults.getConfiguration().getDefaultVaultMaterial();
        PDC containerData = PDC.get(container);
        if (!containerData.hasKeys()) {
            ResourceVaults.error("No Matching Keys on Container!");
            return null;
        }
        if (containerData.hasMaterialKey()){
            material = containerData.getMaterialKey();
        }


        PlayerData pd = PlayerData.get(containerData.getOwner());
        Vault vault = pd.getVaults().getOrDefault(containerData.getVaultIndex(),null);
        if (vault == null){
            ResourceVaults.error("No Existing Vault " + LocationUtils.getStringOfLocation(containerLoc.toBlockLocation()));
            return null;
        }
        return vault;
    }
    public void setCreatedOnTime(long time){
        this.createdOn = time;
    }
    /**
     * Returns the amount of the material stored in this vault.
     *
     * @return the amount of the material stored in this vault
     */
    public int getAmount() {
        int amount = 0;
        for (int i = 0; i < vaultInventory.getInventory().getContents().length; i++) {
            if (vaultInventory.getInventory().getContents()[i] != null && vaultInventory.getInventory().getContents()[i].getType() == material) {
                amount += vaultInventory.getInventory().getContents()[i].getAmount();
            }
        }
        return amount;
    }
       public int getRemainingSpace() {
        return capacity - amount;
    }

    //Testing these!
    public int take(int amount) {
        int taken = Math.min(amount, this.amount);
        this.amount -= taken;
        return taken;
    }

    //Testing these!
    public int add(int amount) {
        int added = Math.min(amount, capacity - this.amount);
        this.amount += added;
        return added;
    }


    public void removeBlocks(){
        for (Block block : getBlocks()) {
            if (block.getState() instanceof Container x){
                x.getBlock().breakNaturally();
                continue;
            }
            if (block.getState() instanceof Sign sign){
                sign.getBlock().setType(Material.AIR);
                continue;
            }

        }
    }
    public List<Block> getBlocks(){
        return blockLocations.stream().map(Location::getBlock).collect(Collectors.toList());
    }
    public void registerInternal() {
        if (!registered) {
            if (createdOn == 0){
                setCreatedOnTime(System.currentTimeMillis());
            }
            Location chestLoc = container.getLeft().getLocation().toBlockLocation();
            Location chestLoc2 = null;
            if (getContainer().hasSecondChest()) {
                chestLoc2 = getContainer().getRight().getLocation().toBlockLocation();
            }
            Block chestBlock = chestLoc.getBlock();
            if (chestBlock.getState() instanceof Chest chest && chest.getBlockData() instanceof Directional directional && (!getContainer().hasSecondChest() || chestLoc2.getBlock().getState() instanceof Chest chest1 && chest.getInventory().equals(chest1.getInventory()))) {
                // Assign the sign to the front face of the chest
                BlockFace facing = directional.getFacing();
                Player player = Bukkit.getPlayer(ownerUUID);
                // Save PDC to the chest
                VaultInventory inv = vaultInventory;
                PlayerData playerData = PlayerData.get(player.getUniqueId());

                container.setVaultIndex(vaultIndex);
                container.setOwner(ownerUUID);
                container.setMaterialKey(material);
                container.setCreatedDate(createdOn);
                container.update();
                registered = true;


                if (getContainer().hasSecondChest()){
                    Block signBlock2 = chestBlock.getRelative(facing);
                    assignSign(player, signBlock2);
                }
                Block signBlock = chestBlock.getRelative(facing);
                assignSign(player, signBlock);


            }
        }
    }

    private void assignSign(Player player, Block signBlock) {
        Material dType = ResourceVaults.getConfiguration().getDefaultSignType();
        if (dType == null){
            throw new RuntimeException("Invalid Sign Type from Configuration (general)");
        }
        signBlock.setType(dType);
        Sign signState = (Sign) signBlock.getState();
        signState.setLine(0, ResourceVaults.getConfiguration().getSignTextColor() +"[Resources]");
        signState.setLine(1, ResourceVaults.getConfiguration().getSignTextColor() + material.name().toUpperCase());
        signState.setLine(2, ResourceVaults.getConfiguration().getSignTextColor() + player.getName());
        signState.setLine(3,ResourceVaults.getConfiguration().getSignTextColor() + "Index: " + vaultIndex);
        signState.setLine(4, ResourceVaults.getConfiguration().getSignTextColor() + String.valueOf(amount) + "/" + capacity);
        signState.setGlowingText(true);
        signState.setEditable(false);
        signState.update();
    }

    public static Vault createVault(VaultCreateEvent event) {
        if (!(event.getLocation().getBlock().getState() instanceof Container container)){
            ResourceVaults.log("No Container on Vault Creation!");
            return null;
        }
        Vault vault = new Vault(event.getPlayer().getUniqueId(),event.getNextIndex(),event.getMaterial(),PDC.get(container));
        boolean success = VaultManager.registerVault(event.getPlayer(), vault);
        if (success) {
            vault.setCreatedOnTime(System.currentTimeMillis());
            vault.registerInternal();
            VaultManager.savePlayerVaults(event.getPlayer());
            return vault;
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Vault vault = (Vault) o;

        return new EqualsBuilder().append(vaultIndex, vault.vaultIndex).append(createdOn, vault.createdOn).append(ownerUUID, vault.ownerUUID).append(material, vault.material).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(ownerUUID).append(vaultIndex).append(material).append(createdOn).toHashCode();
    }
}