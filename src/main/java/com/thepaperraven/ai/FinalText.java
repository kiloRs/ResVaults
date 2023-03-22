package com.thepaperraven.ai;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FinalText extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "rs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "kilo";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        UUID uniqueId = player.getUniqueId();
        List<Vault> vaults = ResourceVaults.getVaultManager().getVaults(uniqueId);
        int count = 0;

        if (params.equalsIgnoreCase("total")){
        if (vaults.isEmpty()){
            return count + "";
        }
        for (Vault vault : vaults) {
            if (vault.getInventory().getHolder() instanceof DoubleChest doubleChest) {
                for (ItemStack itemStack : doubleChest.getInventory()) {
                    if (itemStack == null || itemStack.getType() != vault.getMaterialType()) {
                        continue;
                    }
                    count += itemStack.getAmount();
                }
            } else {
                for (ItemStack itemStack : vault.getInventory()) {
                    if (itemStack == null) {
                        continue;
                    }
                    if (itemStack.getType() != vault.getMaterialType()) {
                        continue;
                    }
                    count += itemStack.getAmount();
                }
            }
            if (count > 0) {
                return count + "";
            }
        }
        }
        else {
            Material material = Material.matchMaterial(params);

            if (material != null && material != Material.AIR){
                for (Vault vault : vaults) {
                    if (vault.getMaterialType()==material){
                        if (vault.getInventory().getHolder() instanceof DoubleChest doubleChest) {
                            for (ItemStack itemStack : doubleChest.getInventory()) {
                                if (itemStack == null || itemStack.getType()!=material){
                                    continue;
                                }
                                count += itemStack.getAmount();
                            }
                        }
                        else {
                            for (ItemStack itemStack : vault.getInventory()) {
                                if (itemStack == null || itemStack.getType()!=material){
                                    continue;
                                }
                                count += itemStack.getAmount();
                            }
                        }
                    }
                }
            }
            return count + "";
        }
        return super.onPlaceholderRequest(player,params);
    }
}
