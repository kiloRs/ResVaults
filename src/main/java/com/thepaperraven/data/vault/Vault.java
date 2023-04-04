package com.thepaperraven.data.vault;

import com.jeff_media.jefflib.EnumUtils;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ResourceVaults;
import com.thepaperraven.config.PlayerConfiguration;
import com.thepaperraven.config.VaultKeys;
import com.thepaperraven.data.gui.VaultHolder;
import com.thepaperraven.data.player.PlayerData;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
public class Vault {

    private int index;
    private UUID ownerUUID;
    private Material material;
    private Chest chest;
    @Nullable
    private Chest second;
    private Inventory inventory;
    private Sign sign = null;
    public BlockFace signDirection;
    private List<Sign> signs = new ArrayList<>();


    public static final NamespacedKey SECOND_CHEST_LOC =new NamespacedKey(ResourceVaults.getInstance(),"VAULT_SECOND_CHEST");
    public static NamespacedKey INDEX = new NamespacedKey(ResourceVaults.getInstance(), "VAULT_INDEX");
    public static NamespacedKey UUID = new NamespacedKey(ResourceVaults.getInstance(), "VAULT_OWNER_UUID");
    public static NamespacedKey MATERIAL = new NamespacedKey(ResourceVaults.getInstance(), "VAULT_MATERIAL");
    public static NamespacedKey SIGN_FACE = new NamespacedKey(ResourceVaults.getInstance(), "VAULT_SIGN_FACE");
    public static NamespacedKey CHEST_FROM_SIGN = new NamespacedKey(ResourceVaults.getInstance(),"VAULT_CHEST_FROM_SIGN");
    private int capacity;
    private int slots;
    private int balance;
    private VaultHolder holder;
    private boolean inventoryState;

    public Vault(int index, UUID ownerUUID, Material material, Chest chest){
        this(index,ownerUUID, EnumUtils.getIfPresent(MaterialType.class,material.name()).orElseThrow(),chest,null);
    }
    public Vault(int index, UUID owner, Material material, DoubleChest doubleChest) {
        this.index = index;
        this.ownerUUID = owner;
        this.material = material;
        this.chest = ((Chest) doubleChest.getLeftSide().getInventory().getLocation().getBlock().getState());
        this.second = ((Chest)  doubleChest.getRightSide().getInventory().getLocation().getBlock().getState());
        if (notDoubleChest(this.chest, this.second)) {
            this.second = null;
        }
        this.inventory = this.chest.getInventory();
        this.signDirection = ((Directional) this.chest.getBlockData()).getFacing();
        this.slots = second!=null?54:27;

        this.capacity = slots * 64;
        this.holder = new VaultHolder(this);
        this.inventoryState = false;
    }

    public Vault(int index, UUID ownerUUID, MaterialType allowedType, Chest chest, @Nullable Chest otherChest) {
        this.index = index;
        this.ownerUUID = ownerUUID;
        this.material = allowedType.getMaterial();
        this.chest = chest;
        this.second = otherChest;
        if (this.second != null) {
            if (notDoubleChest(this.chest, this.second)) {
                this.second = null;
            }
        }
        this.inventory = chest.getInventory();
        if (!(chest.getBlockData() instanceof Directional directional)){
            throw new RuntimeException("Chest is not directional!");
        }
        this.signDirection = directional.getFacing();
        this.slots = second!=null?54:27;
        this.capacity = slots * 64;
        this.holder = new VaultHolder(this);
        this.inventoryState = false;

    }

    private static boolean notDoubleChest(Chest leftChest, Chest rightChest) {
        return !leftChest.getInventory().equals(rightChest.getInventory());
    }


    public void createVaultsSign(boolean checkIfRegistered) {
        if (checkIfRegistered && PlayerData.get(ownerUUID).getVaults().get(index)!=this){
            ResourceVaults.error("No Sign to Create: Not Registered and Forced to Verify!");
            return;
        }
        if (signDirection == null) {
            ResourceVaults.error("Invalid Sign Direction of Chest in Vault: " + index);
            return;
        }

        if (chest != null ){
            updateSign(chest,ResourceVaults.getConfiguration().doSignsGlow(),ResourceVaults.getConfiguration().getSignTextColor());
        }
        if (second != null){
            updateSign(second,ResourceVaults.getConfiguration().doSignsGlow(), ResourceVaults.getConfiguration().getSignTextColor());
        }
    }


    private void updateSign(Chest chest, boolean glowing, ChatColor colorUsed) {
        Block signBlock = chest.getBlock().getRelative(signDirection);
        signBlock.setType(ResourceVaults.getConfiguration().getDefaultSignType());
        Sign sign = (Sign) signBlock.getState();

        sign.setLine(0,colorUsed+ "[Resources]");
        sign.setLine(1,colorUsed + Bukkit.getPlayer(ownerUUID).getName());
        sign.setLine(2,colorUsed+ material.name());
        sign.setLine(3,colorUsed+ String.valueOf(index));
        sign.setGlowingText(glowing);
        sign.setEditable(false);
        sign.update(true);

        this.signs.add(sign);
    }

    public void setLocked(boolean locked){
        if (locked){
            chest.setLock(ownerUUID.toString());
            if (second!=null){
                this.second.setLock(ownerUUID.toString());
            }
        }
        else {
            chest.setLock(null);
            if (second != null){
                second.setLock(null);
        }
        }
    }
    public String getLock(){
        return chest.getLock();
    }
    public boolean isLocked(){
        return chest.isLocked() && (second == null || second.isLocked());
    }
    public boolean saveToBlock() {
        PersistentDataContainer container = chest.getPersistentDataContainer();
        container.set(INDEX, PersistentDataType.INTEGER, index);
        container.set(UUID, DataType.UUID, ownerUUID);
        container.set(MATERIAL, PersistentDataType.STRING, material.name());
        container.set(SIGN_FACE, PersistentDataType.STRING, signDirection.name());

        if (sign != null){
            container.set(INDEX, PersistentDataType.INTEGER, index);
            container.set(UUID, DataType.UUID, ownerUUID);
            container.set(MATERIAL, PersistentDataType.STRING, material.name());
            container.set(CHEST_FROM_SIGN,PersistentDataType.STRING,signDirection.getOppositeFace().name());
        }
        if (second != null){
            container.set(INDEX, PersistentDataType.INTEGER, index);
            container.set(UUID, DataType.UUID, ownerUUID);
            container.set(MATERIAL, PersistentDataType.STRING, material.name());
            container.set(SIGN_FACE, PersistentDataType.STRING, signDirection.name());
        }
        return true;
    }


    public boolean hasKeys() {
        PersistentDataContainer container = chest.getPersistentDataContainer();
        return container.has(INDEX, PersistentDataType.INTEGER)
                && container.has(UUID, DataType.UUID)
                && container.has(MATERIAL, PersistentDataType.STRING);
    }

    public boolean isRegisteredToMap(){
        return isRegistered(false);
    }
    public boolean isRegistered(boolean fileRequirement) {
        return hasKeys() && PlayerData.get(ownerUUID).isRegistered(this,fileRequirement);
    }

    public int getRemainingSpace() {
        return capacity - getBalance();
    }


    public int getBalance() {
        return Arrays.stream(inventory.getStorageContents())
                .filter(itemStack -> itemStack != null && itemStack.getType()==material)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }
    public boolean hasSign(){
        return sign!=null;
    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }
    public static Vault loadFromPDC(Block block) {
        if (block.getState() instanceof Chest chest) {
            // Get the block's PersistentDataContainer
            PersistentDataContainer dataContainer = chest.getPersistentDataContainer();
            // Check if the block has all the required data
            if (!dataContainer.has(UUID, DataType.UUID) ||
                    !dataContainer.has(MATERIAL, PersistentDataType.STRING) ||
                    !dataContainer.has(INDEX, PersistentDataType.INTEGER)) {
                return null;
            }
            // Read the owner UUID, material type, and index
            UUID ownerUuid = dataContainer.get(VaultKeys.getOwnerKey(), DataType.UUID);
            Material material = Material.matchMaterial(dataContainer.getOrDefault(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING, ResourceVaults.getConfiguration().getDefaultVaultMaterial().name()));
            int index = dataContainer.getOrDefault(VaultKeys.getIndexKey(), PersistentDataType.INTEGER, 0);
            if (index <= 0) {
                return null;
            }
            // Create the Vault object
            if (ownerUuid == null) {
                return null;
            }
            Player owner = Bukkit.getPlayer(ownerUuid);
            if (owner == null) {
                return null;
            }
            return new Vault(index, ownerUuid, material, chest);
        }
        return null;
    }

    public int getCapacity() {
        return capacity;
    }

    public void deposit(int amount) {
        balance += amount;
        chest.getInventory().addItem(new ItemStack(material, amount));
    }

    public void withdraw(int amount) {
        int available = chest.getInventory().all(material).values().stream().mapToInt(ItemStack::getAmount).sum();
        int toWithdraw = Math.min(amount, available);
        if (toWithdraw > 0) {
            ItemStack itemStack = new ItemStack(material, toWithdraw);
            chest.getInventory().removeItem(itemStack);
            balance -= toWithdraw;
        }
    }

    public void toConfig(ConfigurationSection config) {
        // Set the material of the vault
        config.set("material", material.name());


        if (second != null){
            config.set("location.left.world", chest.getWorld().getName());
            config.set("location.left.x", chest.getX());
            config.set("location.left.y", chest.getY());
            config.set("location.left.z", chest.getZ());
            config.set("location.right.world", chest.getWorld().getName());
            config.set("location.right.x", chest.getX());
            config.set("location.right.y", chest.getY());
            config.set("location.right.z", chest.getZ());
        }
        else {
            // Set the location of the chest block
            config.set("location.chest.world", chest.getWorld().getName());
            config.set("location.chest.x", chest.getX());
            config.set("location.chest.y", chest.getY());
            config.set("location.chest.z", chest.getZ());
        }

        // Set the sign of the vault, if it exists

        int count =0;
        for (Sign sign1 : signs) {
            ++count;
            config.set("location.signs." + count+ ".world", sign1.getWorld().getName());
            config.set("location.signs." + count+ ".x", sign1.getX());
            config.set("location.signs." + count+ ".y", sign1.getY());
            config.set("location.signs." + count+ ".z", sign1.getZ());
            // Set the facing direction of the sign
            config.set("location.signs." + count + ".direction", signDirection.name());
        }
    }
    public static Vault fromConfigSection(PlayerConfiguration configuration, int index) {
        ConfigurationSection mainSection = configuration.getConfigurationSection("vaults");
        ConfigurationSection section = mainSection != null ? mainSection.getConfigurationSection(String.valueOf(index)) : null;
        if (section == null){
            throw new RuntimeException("Missing Configuration Section Trying to Load Error");
        }
        Material material = Material.getMaterial(section.getString("material",""));
        java.util.UUID owner = configuration.getPlayerUUID();

        World world = Bukkit.getWorld(section.getString("location.chest.world","world"));
        int x = section.getInt("location.chest.x");
        int y = section.getInt("location.chest.y");
        int z = section.getInt("location.chest.z");
        Location chestLocation = new Location(world, x, y, z);

        Location leftLocation = null;
        Location rightLocation = null;
        if (section.contains("location.left") && section.contains("location.right")) {
            World leftWorld = Bukkit.getWorld(section.getString("location.left.world","world"));
            int leftX = section.getInt("location.left.x");
            int leftY = section.getInt("location.left.y");
            int leftZ = section.getInt("location.left.z");
            leftLocation = new Location(leftWorld, leftX, leftY, leftZ);

            World rightWorld = Bukkit.getWorld(section.getString("location.right.world","world"));
            int rightX = section.getInt("location.right.x");
            int rightY = section.getInt("location.right.y");
            int rightZ = section.getInt("location.right.z");
            rightLocation = new Location(rightWorld, rightX, rightY, rightZ);
        }

        List<Sign> signs = new ArrayList<>();
        Location signLocation;
        List<String> signKeys = new ArrayList<>(section.getConfigurationSection("location.signs").getKeys(false));
        for (String signKey : signKeys) {
            ConfigurationSection signSection = section.getConfigurationSection("location.signs." + signKey);

            if (signSection == null){
                continue;
            }
            World signWorld = Bukkit.getWorld(signSection.getString("world","world"));
            int signX = signSection.getInt("x");
            int signY = signSection.getInt("y");
            int signZ = signSection.getInt("z");
            signLocation = new Location(signWorld, signX, signY, signZ);

            signs.add(((Sign) signLocation.getBlock().getState()));
            Block signBlock = signLocation.getBlock();
            if (signBlock.getState() instanceof Sign sign) {
                Optional<BlockFace> direction = EnumUtils.getIfPresent(BlockFace.class, signSection.getString("direction"));
                if (direction.isPresent()) {
                    ((WallSign) sign.getBlockData()).setFacing(direction.get());
                    sign.update();
                }
                signs.add(sign);
            }
        }

        Vault vault;
        BlockState state = chestLocation.getBlock().getState();
        if (state instanceof Chest chest) {
            if (chest.getInventory().getHolder() instanceof DoubleChest doubleChest) {
                vault = new Vault(index, owner, material, doubleChest);
            }
            if (material == null){
                throw new RuntimeException("Loading Material of Vault is null for " + index + " attempted to pass invalid name");
            }

            vault = new Vault(index, owner, material, ((Chest) state));


            return vault;
        }
        return null;
    }

    public void setInventoryState(boolean inventoryState) {
        this.inventoryState = inventoryState;
    }

    public boolean isStateOfInventoryOpen() {
        return inventoryState;
    }
}

