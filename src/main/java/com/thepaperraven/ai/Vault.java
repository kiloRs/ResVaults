package com.thepaperraven.ai;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Vault implements VaultInstance {

    private int amount;
    @Getter
    private final List<Location> chestLocations;
    @Getter
    private final VaultMetadata metadata;
    @Getter
    private final Location signLocation;
    @Getter
    private boolean active = false;
    @Getter
    private final Chest mainChest;
    @Getter
    private final Sign sign;
    private boolean locked;
    @Getter
    private final VaultInventory vaultInventory;
    @Getter
    private String lockPassword = "VAULT_";
    @Getter
    private final String LOCKED_METADATA_KEY = "vault_locked";
    @Getter
    private ArrayList<Block> lockedBlocks = new ArrayList<>();

    public Vault(VaultMetadata metadata, List<Location> chestLocations, Location signLocation) {
        this.metadata = metadata;
        this.signLocation = signLocation;
        this.amount = 0;
        this.chestLocations = chestLocations;
        this.mainChest = chestLocations.size() > 0 ? chestLocations.get(0).getBlock().getState() instanceof Chest chest ? chest : null : null;
        this.sign = signLocation.getBlock().getState() instanceof Sign aSign ? aSign : null;
        this.active = chestLocations.size() > 0 && chestLocations.size() < 3 && metadata != null && mainChest != null && sign != null && sign.getLine(0).equalsIgnoreCase("[Resources]") && metadata.getAllowedMaterial() != null && metadata.getVaultIndex() > 0 && new PlayerData(metadata.getOwnerUUID()).hasVault(metadata.getVaultIndex()) && new PlayerData(metadata.getOwnerUUID()).getVault(metadata.getVaultIndex()) == this && new PlayerData(metadata.getOwnerUUID()).getUuid() == getMetadata().getOwnerUUID();
        this.vaultInventory = new VaultInventory(this);

        if (this.metadata == null){
            throw new RuntimeException("Invalid Metadata for Vault!");
        }

        this.lockPassword = lockPassword + metadata.getVaultIndex();

        if (this.amount == 0){
            findAmount();
        }

        this.lockedBlocks = getBlocks().stream()
                .filter(block -> block.hasMetadata(LOCKED_METADATA_KEY) && block.getMetadata(LOCKED_METADATA_KEY).get(0).asBoolean())
                .collect(Collectors.toCollection(ArrayList::new));

    }

    private void findAmount() {
        for (ItemStack itemStack : vaultInventory.getInventory()) {
            if (itemStack.getType()== metadata.getAllowedMaterial()){
                this.amount = itemStack.getAmount() + this.amount;
            }
        }
    }

    // Returns the maximum amount of the allowed material that can be stored in the vault
    public int getMaxAmount() {
        int maxAmount = 0;
        for (Location location : chestLocations) {
            BlockState state = location.getBlock().getState();
            if (state instanceof Chest chest) {
                Inventory inventory = chest.getInventory();
                maxAmount += inventory.getSize() * metadata.getAllowedMaterial().getMaxStackSize();
            }
        }
        return maxAmount;
    }
    public void updatePDC() {
        // Loop through all chest locations and update their PDC
        for (Location loc : chestLocations) {
            Block block = loc.getBlock();
            if (block.getState() instanceof Chest chest) {
                if (chest.getInventory().getType() == InventoryType.CHEST) {
                    PersistentDataContainer pdc = chest.getPersistentDataContainer();
                    setData(pdc);
                }
            }
        }

        // If there are two chest locations, update the PDC of the second chest
        if (chestLocations.size() > 1) {
            Location secondLoc = chestLocations.get(1);
            Block secondBlock = secondLoc.getBlock();
            if (secondBlock.getState() instanceof Chest secondChest) {
                if (secondChest.getInventory().getType() == InventoryType.CHEST) {
                    PersistentDataContainer secondPDC = secondChest.getPersistentDataContainer();
                    setData(secondPDC);
                }
            }
        }

        // Update the PDC of the sign block
        if (signLocation != null) {
            Block signBlock = signLocation.getBlock();
            if (signBlock.getState() instanceof Sign aSign) {
                PersistentDataContainer pdc = sign.getPersistentDataContainer();
                setData(pdc);

                // Update the sign text
                String[] lines = aSign.getLines();
                lines[0] = "[Resources]";
                lines[1] = metadata.getAllowedMaterial().name();
                lines[2] = Bukkit.getPlayer(getMetadata().getOwnerUUID()).getName();
                lines[3] = ChatColor.WHITE + "- - - - " + metadata.getVaultIndex() + " - - - -";
                if (locked) {
                    for (int i = 0; i < lines.length; i++) {
                        lines[i] = ChatColor.RED + lines[i];
                        aSign.lines().set(i, Component.text(lines[i]));
                    }
                } else {
                    for (int i = 0; i < lines.length; i++) {
                        lines[i] = ChatColor.GREEN + lines[i];
                        aSign.setLine(i,lines[i]);
                    }
                }
                aSign.setGlowingText(true);
                aSign.setEditable(locked);
                sign.update();

                try {
                    new PlayerData(metadata.getOwnerUUID()).saveVault(this);
                    ResourceVaults.log("Saving Vault to File: " + metadata.getVaultIndex() + " of " + Bukkit.getPlayer(metadata.getOwnerUUID()).getName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void setData(PersistentDataContainer pdc) {
        pdc.set(VaultKeys.getOwnerKey(), DataType.UUID, getMetadata().getOwnerUUID());
        pdc.set(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING,metadata.getAllowedMaterial().name());
        pdc.set(VaultKeys.getIndexKey(), PersistentDataType.INTEGER, metadata.getVaultIndex());
        pdc.set(VaultKeys.getLocked(), PersistentDataType.STRING, lockPassword);


    }

    @Override
    public boolean hasSign() {
        return signLocation.getBlock().getState() instanceof Sign sign;
    }

    @Override
    public boolean hasOwner() {
        return Bukkit.getPlayer(metadata.getOwnerUUID()) != null;
    }
    /**
     * Adds the specified amount of items to the VaultInventory, creating new ItemStack(vault.getMetadata().getAllowedMaterial)
     * and adding them to the first not-max-size slots, until the inventory is full.
     *
     * @param amount the amount of items to add
     * @return the amount of items overflowing from the inventory
     */
    @Override
    public int add(int amount) {
        return vaultInventory.add(amount);
    }

    public void setLocked(boolean lock) {
        this.locked = lock;
    }

    @Override
    public void lock() {
        for (Location chestLocation : chestLocations) {
            if (chestLocation.getBlock().getState() instanceof Chest chest) {
                chest.setLock(getLockPassword());
                chest.setMetadata(LOCKED_METADATA_KEY,new FixedMetadataValue(ResourceVaults.getPlugin(),getLockPassword()));
                lockedBlocks.add(chestLocation.getBlock());
            }
        }
        if (signLocation != null){
            if (signLocation.getBlock().getState() instanceof Sign a) {
                a.setEditable(false);
                a.setMetadata(LOCKED_METADATA_KEY,new FixedMetadataValue(ResourceVaults.getPlugin(),getLockPassword()));
                lockedBlocks.add(a.getBlock());
            }
        }
    }

    @Override
    public void unlock() {
        for (Location chestLocation : chestLocations) {
            if (chestLocation.getBlock().getState() instanceof Chest chest) {
                chest.setLock(null);
                chest.removeMetadata(LOCKED_METADATA_KEY,ResourceVaults.getPlugin());
                lockedBlocks.remove(chest.getBlock());
            }
        }
        if (signLocation != null){
            if (signLocation.getBlock().getState() instanceof Sign ourSign) {
                ourSign.setEditable(true);
                ourSign.removeMetadata("vault_locked",ResourceVaults.getPlugin());
                lockedBlocks.remove(signLocation.getBlock());
            }
        }
    }
    /**
     * Gets the exact amount of items within the Vault (count the amount of itemstacks per slot).
     *
     * @return the amount of items within the Vault
     */
    public int getAmount() {
        return amount;
    }

    public List<Block> getBlocks(){
        ArrayList<Block> b = new ArrayList<>();
        for (Location chestLocation : chestLocations) {
            b.add(chestLocation.getBlock());
        }
        b.add(sign.getBlock());

        return b;
    }

    @Override
    public List<Block> lockedBlocks() {
        return lockedBlocks;
    }

    public boolean isLocked(){
        return locked;
    }

    public boolean save() {
        // Save the PlayerData object
        PlayerData d = PlayerData.get(getMetadata().getOwnerUUID());
        d.saveVault(this);

        d.loadVault(metadata.getVaultIndex());

        int change = 0;
        // Set data for each block in chestLocations
        for (Location location : chestLocations) {
            Block block = location.getBlock();
            // Check if the block is a chest
            if (block.getState() instanceof Chest chest) {
                change++;
                setData(chest.getPersistentDataContainer());
            }
        }

        // Set data for the sign block
        Block signBlock = signLocation.getBlock();
        // Check if the block is a sign
        if (signBlock.getState() instanceof Sign a) {
            setData(a.getPersistentDataContainer());
            change++;
        }
        return change>=2 && change < 4;
    }


    public static List<Block> getBlocksFromInventory(Inventory inventory){
        return inventory.getHolder() instanceof VaultInventory vaultInventory1 ? vaultInventory1.getVault().getBlocks():new ArrayList<>();
    }
    public static Vault getVaultFromInventory(Inventory inventory) {
        if (inventory.getHolder() instanceof VaultInventory) {
            return ((VaultInventory) inventory.getHolder()).getVault();
        }
        return null;
    }

}
