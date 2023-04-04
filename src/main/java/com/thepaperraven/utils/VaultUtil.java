package com.thepaperraven.utils;


import com.jeff_media.jefflib.EnumUtils;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ResourceVaults;
import com.thepaperraven.data.player.PlayerData;
import com.thepaperraven.data.vault.Vault;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.*;
import java.util.stream.Stream;

import static com.thepaperraven.data.vault.Vault.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VaultUtil {
    public static void takeFromPlayer(Player player, Material m, int amount){
        if (hasEnoughInPlayer(player,m,amount)){
            Stream<@Nullable ItemStack> stream = Arrays.stream(player.getInventory().getStorageContents()).filter(itemStack -> itemStack != null && itemStack.getType() == m);
            int total = stream.filter(Objects::nonNull).mapToInt(ItemStack::getAmount).sum();
            for (ItemStack itemStack : player.getInventory()) {
                if (amount - itemStack.getAmount() < 0){
                    itemStack.setAmount(itemStack.getAmount()-amount);
                }
            }
        }
    }

    public static boolean hasEnoughInPlayer(Player player, Material material, int amountToTake){
        return getTotalFromPlayersInventory(player,material)>=amountToTake;
    }
    public static int getTotalFromPlayersInventory(Player player, Material m){
        return Arrays.stream(player.getInventory().getStorageContents()).filter(itemStack -> itemStack!=null&&itemStack.getType()==m).mapToInt(ItemStack::getAmount).sum();
    }
    public static boolean hasEnoughInVaults(Player player, Material material, int amount) {
        int total = getAmountOf(player, material);
        return total >= amount;
    }
    public static int getAmountOfVaults(Player player, Material material){
        Map<Integer, Vault> m = getVaultsMatching(material, player);
        return  m==null?0:m.size();
    }
    public static int getAmountOf(Player player, Material material){
        Map<Integer, Vault> vaultsMatching = getVaultsMatching(material, player);
        if (vaultsMatching == null){
            return 0;
        }
        Collection<Vault> values = vaultsMatching.values();
        if (values.isEmpty()){
            return 0;
        }
        return values.stream().mapToInt(Vault::getBalance).sum();
    }
    public static Map<Integer,Vault> getVaultsMatching(Material material, Player player){
        Map<Material, Map<Integer, Vault>> vaultsByMaterial = getVaultsByMaterial(player);
        if (vaultsByMaterial.containsKey(material)){
            return vaultsByMaterial.get(material);
        }
        return null;
    }
    public static Map<Material,Map<Integer,Vault>> getVaultsByMaterial(Player player){
        Map<Material,Map<Integer,Vault>> mappedByMaterial = new LinkedHashMap<>();
        mappedByMaterial.put(Material.LEATHER,new LinkedHashMap<>());
        mappedByMaterial.put(Material.STONE,new LinkedHashMap<>());
        mappedByMaterial.put(Material.WHEAT,new LinkedHashMap<>());

        mappedByMaterial.forEach((material, integerVaultMap) -> {
            Map<Integer, Vault> vaults = getPlayer(player).getVaults();
            vaults.forEach((integer, vault) -> {
                if (vault.getMaterial()==material){
                    integerVaultMap.put(integer,vault);
                }
            });
        });

        return mappedByMaterial;
    }
    public static PlayerData getPlayer(Player player){
        return PlayerData.get(player.getUniqueId());
    }
    public static Optional<Vault> getVault(Chest chest) {
        UUID ownerUuid = getOwnerUuid(chest);
        int index = getIndex(chest);
        Material material = getMaterial(chest);

        if (ownerUuid == null || index <= 0 || material == null) {
            return Optional.empty();
        }

        return Optional.of(new Vault(index, ownerUuid, material, chest));
    }


    public static UUID getOwnerUuid(TileState chest) {
        return chest.getPersistentDataContainer().get(Vault.UUID, DataType.UUID);
    }

    public static int getIndex(TileState chest) {
        return chest.getPersistentDataContainer().getOrDefault(INDEX, PersistentDataType.INTEGER, 0);
    }

    public static Material getMaterial(Chest chest) {
        String materialName = chest.getPersistentDataContainer().getOrDefault(MATERIAL, PersistentDataType.STRING, ResourceVaults.getConfiguration().getDefaultVaultMaterial().name().toUpperCase());

        Material material = Material.getMaterial(materialName);
        if (material == null) {
            throw new RuntimeException("Invalid Material Name: " + materialName);
        }
        return material;
    }

    /**
     * @param location THe location of the chest.
     *
     * @return only try if the SIGN_FACE key is saved into the chest, which is the direction the sign generates onto the sign.
     */
    public static boolean isVault(Location location){
        return isVault(location,true);
    }
    public static boolean isVault(Location location, boolean requireSignData){
        if (location.getBlock().getState() instanceof Chest chest){
            return isVault(chest,requireSignData);
        }
        return false;
    }
    public static boolean isVault(Chest chest, boolean lookForSign){
        PersistentDataContainer container = chest.getPersistentDataContainer();
        return container.has(INDEX, PersistentDataType.INTEGER)
                && container.has(Vault.UUID, DataType.UUID)
                && container.has(MATERIAL, PersistentDataType.STRING)
                && (!lookForSign || container.has(SIGN_FACE, PersistentDataType.STRING));

    }


    public static Optional<BlockFace> getSignBackFace(Sign sign){
        PersistentDataContainer pdc = sign.getPersistentDataContainer();
        if (pdc.has(CHEST_FROM_SIGN,PersistentDataType.STRING)){
            String s = pdc.get(CHEST_FROM_SIGN, PersistentDataType.STRING);
            if (s == null){
                return Optional.empty();
            }
            return EnumUtils.getIfPresent(BlockFace.class,s);
        }
        return Optional.empty();
    }


    public static boolean isVaultType(TileState tile, Material material) {
        if (!(tile instanceof Container container)) {
            return false;
        }

        PersistentDataContainer pdc = container.getPersistentDataContainer();

        if (!pdc.has(MATERIAL, PersistentDataType.STRING) || !pdc.has(INDEX,PersistentDataType.INTEGER) || !pdc.has(UUID,DataType.UUID)){
            return false;
        }

        String name = pdc.get(MATERIAL, PersistentDataType.STRING);
        if (name == null){
            ResourceVaults.getLogger("Invalid Material Name in 154: isVaultType()");
            return false;
        }
        Material vaultMaterial = Material.matchMaterial(name);
        return vaultMaterial != null && vaultMaterial == material;
    }

    public static boolean isVaultOwner(TileState tile, Player player) {
        if (!(tile instanceof Chest container)) {
            if (tile instanceof Sign sign){
                PersistentDataContainer pdc = sign.getPersistentDataContainer();
                if (!pdc.has(UUID,DataType.UUID)) {
                    return false;
                }

                UUID ownerUUID = getOwnerUuid((tile));
                return Objects.equals(ownerUUID, player.getUniqueId());

            }

            return false;
        }

        PersistentDataContainer pdc = container.getPersistentDataContainer();
        if (!pdc.has(UUID,DataType.UUID)) {
            return false;
        }

        UUID ownerUUID = getOwnerUuid(tile);
        return Objects.equals(ownerUUID, player.getUniqueId());
    }

    public static boolean isVaultIndex(TileState tile, int indexChecking) {
        if (!(tile instanceof Container container)) {
            if (tile instanceof Sign sign){
                PersistentDataContainer pdc = sign.getPersistentDataContainer();
                if (!pdc.has(INDEX, PersistentDataType.INTEGER)) {
                    return false;
                }

                int index = getIndex(tile);
                return index == indexChecking;
            }
            return false;
        }

        PersistentDataContainer pdc = container.getPersistentDataContainer();
        if (!pdc.has(INDEX, PersistentDataType.INTEGER)) {
            return false;
        }

        int index = getIndex(tile);
        return index == indexChecking;
    }

}