package com.thepaperraven.ai;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ai.utils.LocationUtils;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Vault implements VaultInstance {
    private final UUID ownerUUID;
    private final Location signLocation;
    private final Location chestLocation1;
    private final Location chestLocation2;
    private final Material allowedMaterial;
    private final PlayerData playerData;
    private boolean isDoubleChest = false;
    private final InventoryHolder holder;
    private final VaultMetadata vaultMetadata;

    /**
     * @param ownerUUID       Owning Player
     * @param signLocation    Location of Resources Sign
     * @param chestLocation1  Chest Location
     * @param allowedMaterial Material to be used within the Vault
     *                        This constructor is for the use of a Single chest only, not for use with a DoubleChest!
     */
    public Vault(UUID ownerUUID, Location signLocation, Location chestLocation1, Material allowedMaterial) {
        this(ownerUUID, signLocation, chestLocation1, null, allowedMaterial);
    }

    /**
     * @param ownerUUID       Owning Player
     * @param signLocation    Location of Resources Sign
     * @param chestLocation1  Chest Location
     * @param allowedMaterial Material to be used within the Vault
     *                        This constructor is for the use of a DoubleChest only! Use the other constructor for the single chest instance.
     */
    public Vault(UUID ownerUUID, Location signLocation, Location chestLocation1, @Nullable Location chestLocation2, Material allowedMaterial) {
        this.ownerUUID = ownerUUID;
        this.playerData = new PlayerData(ownerUUID);
        this.signLocation = signLocation.toBlockLocation();
        this.chestLocation1 = chestLocation1.toBlockLocation();
        if (chestLocation2 != null) {
            this.chestLocation2 = chestLocation2.toBlockLocation();
            this.isDoubleChest = true;
            this.holder = chestLocation1.toBlockLocation().getBlock().getState() instanceof Chest chest ? chest.getBlockInventory().getHolder() instanceof DoubleChest doubleChest ? doubleChest : chest.getInventory().getHolder() : null;
        } else {
            this.chestLocation2 = null;
            this.holder = chestLocation1.toBlockLocation().getBlock().getState() instanceof Chest chest ? chest.getBlockInventory().getHolder() instanceof DoubleChest doubleChest ? doubleChest : chest.getInventory().getHolder() : null;
        }
        this.allowedMaterial = allowedMaterial;
        VaultMetadata meta = VaultMetadata.loadFromPDC(chestLocation1.getBlock());
        this.vaultMetadata = meta == null ? new VaultMetadata(allowedMaterial.getKey().getKey(), ownerUUID, playerData.getIndexOf(this)):meta;

        if (this.holder == null) {
            throw new RuntimeException("Invalid Holder of Vault: " + playerData.getIndexOf(this));
        }

        this.vaultMetadata.saveToPDC(chestLocation1.getBlock());
        this.vaultMetadata.saveToPDC(signLocation.getBlock());
    }

    @Override
    public boolean isValid() {
        return ownerUUID != null
                && signLocation != null
                && chestLocation1 != null
                && allowedMaterial != null
                && playerData != null
                && holder != null
                && vaultMetadata != null;
    }
    public boolean isActive() {
        List<Block> blocks = new ArrayList<>();
        Block chest = chestLocation1.getBlock();
        Block sign = signLocation.getBlock();
        blocks.add(chest);
        blocks.add(sign);
        if (this.isDoubleChest()) {
            Block relative = chestLocation2.getBlock();
            if (relative.getState() instanceof Chest) {
                blocks.add(relative);
            } else {
                return false;
            }
        }
        for (Block block : blocks) {
            if (!(block.getState() instanceof TileState tileState)) {
                return false;
            }
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            if (!pdc.has(VaultKeys.getOwnerKey(), PersistentDataType.STRING)) {
                return false;
            }
            UUID ownerUUID = pdc.get(VaultKeys.getOwnerKey(), DataType.UUID);
            if (ownerUUID == null){
                return false;
            }
            if (!ownerUUID.equals(vaultMetadata.getOwnerUUID())) {
                return false;
            }
            if (!pdc.has(VaultKeys.getIndexKey(), PersistentDataType.INTEGER)) {
                return false;
            }
            int vaultIndex = pdc.getOrDefault(VaultKeys.getIndexKey(), PersistentDataType.INTEGER,0);
            if (vaultIndex != vaultMetadata.getVaultIndex()) {
                return false;
            }
        }
        return true;
    }


    /**
     * Serializes the Vault object to a configuration section.
     *
     * @param config The file configuration to save to.
     * @param path   The path to save the configuration to.
     */
    public void toConfig(FileConfiguration config, String path) {
        ConfigurationSection section = config.createSection(path);
        section.set("ownerUUID", ownerUUID.toString());
        section.set("signLocation", this.signLocation.toString());
        section.set("chestLocation1", this.chestLocation1.toString());
        if (this.chestLocation2 != null) {
            section.set("chestLocation2", this.getChestLocation2().toString());
        }
        section.set("allowedMaterial", allowedMaterial.getKey().getKey());
        section.set("playerDataIndex", playerData.getIndexOf(this));
    }

    /**
     * Deserializes the Vault object from a configuration section.
     *
     * @param config The file configuration to load from.
     * @param path   The path to load the configuration from.
     * @return The deserialized Vault object.
     */
    public static Vault fromConfig(FileConfiguration config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        UUID ownerUUID = UUID.fromString(section.getString("ownerUUID"));
        Material allowedMaterial = Material.getMaterial(section.getString("allowedMaterial"));
        Location signLocation = LocationUtils.getLocationFromString(section.getString("signLocation"));
        Location chestLocation1 = LocationUtils.getLocationFromString(section.getString("chestLocation1"));
        Location chestLocation2 = null;
        if (section.isString("chestLocation2")) {
            chestLocation2 = LocationUtils.getLocationFromString(section.getString("chestLocation2"));
        }
        int playerDataIndex = section.getInt("playerDataIndex");
        return new Vault(ownerUUID, signLocation, chestLocation1, chestLocation2, allowedMaterial);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Vault vault)) return false;

        return new EqualsBuilder().append(getOwnerUUID(), vault.getOwnerUUID()).append(getSignLocation(), vault.getSignLocation()).append(getChestLocation1(), vault.getChestLocation1()).append(getChestLocation2(), vault.getChestLocation2()).append(getAllowedMaterial(), vault.getAllowedMaterial()).append(getPlayerData(), vault.getPlayerData()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getOwnerUUID()).append(getSignLocation()).append(getChestLocation1()).append(getChestLocation2()).append(getAllowedMaterial()).append(getPlayerData()).toHashCode();
    }
}
@Getter
class VaultMetadata{
    private final String allowedMaterial;
    private final UUID ownerUUID;
    private final int vaultIndex;

    public VaultMetadata(Material allowedMaterial, UUID ownerUUID, int vaultIndex) {
        this.allowedMaterial = allowedMaterial.getKey().getKey();
        this.ownerUUID = ownerUUID;
        this.vaultIndex = vaultIndex;
    }

    public void saveToPDC(Block block) {
        PersistentDataContainer pdc = block.getState() instanceof TileState tileState? tileState.getPersistentDataContainer():null;
        if (pdc == null){
            ResourceVaults.log("No Container to Save: " + block.getLocation().toBlockLocation());
            return;
        }
        pdc.set(VaultKeys.getOwnerKey(), DataType.UUID, ownerUUID);
        pdc.set(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING, allowedMaterial);
        pdc.set(VaultKeys.getIndexKey(), PersistentDataType.INTEGER, vaultIndex);
    }

    public static VaultMetadata loadFromPDC(Block block) {
        PersistentDataContainer pdc = block.getState() instanceof TileState tileState? tileState.getPersistentDataContainer():null;
        if ( pdc == null){
            return null;
        }
        @Nullable UUID owner = pdc.get(VaultKeys.getOwnerKey(), DataType.UUID);
        Material material = Material.matchMaterial(pdc.get(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING));
        Integer index = pdc.get(VaultKeys.getIndexKey(), PersistentDataType.INTEGER);
        if (owner != null && material != null && index != null) {
            return new VaultMetadata(material, owner, index);
        } else {
            return null;
        }
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static VaultMetadata fromJson(String json) {
        return gson.fromJson(json, VaultMetadata.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof VaultMetadata that)) return false;

        return new EqualsBuilder().append(getVaultIndex(), that.getVaultIndex()).append(getAllowedMaterial(), that.getAllowedMaterial()).append(getOwnerUUID(), that.getOwnerUUID()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getAllowedMaterial()).append(getOwnerUUID()).append(getVaultIndex()).toHashCode();
    }

}
