package com.thepaperraven.config.resources;

import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import io.lumine.mythic.lib.api.util.EnumUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CurrencyPlaceholder extends PlaceholderExpansion {
    private int count;

    @Override
    public @NotNull String getIdentifier() {
        return "vaults";
    }

    @Override
    public @NotNull String getAuthor() {
        return "kilo";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0-BETA";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return super.onRequest(player, params);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        Optional<Resource> isHavePH = EnumUtils.getIfPresent(Resource.class, params);

        if (params.equalsIgnoreCase("size")){
            return ResourceVaults.getVaultManager().getVaultCount(player.getUniqueId()) + "";
        }

        List<Vault> vaults = ResourceVaults.getVaultManager().getVaults(player.getUniqueId());
        if (params.equalsIgnoreCase("all")){
            for (Vault vault : vaults) {
                List<ItemStack> items = ResourceVaults.getVaultManager().getItemsFromVault(vault);
                for (ItemStack item : items) {
                    if (item == null || item.getType()== Material.AIR){
                        continue;
                    }
                    count += item.getAmount();
                }
            }
            return count + "";
        }

        Resource orNone = isHavePH.orElse(Resource.NONE);

        if (orNone== Resource.NONE){
            ResourceVaults.getVaultManager().getVaultCount(player.getUniqueId());
            return "Total: (" + count + ")";
        }

        List<Vault> all = vaults;

        for (Vault vault : all) {
            if (vault.getMaterialType() != orNone.getMaterial()){
                continue;
            }
            for (ItemStack itemStack : vault.getInventory()) {
                if (itemStack == null || itemStack.getType()!=orNone.getMaterial()){
                    continue;
                }
                count += itemStack.getAmount();
            }
        }

        return "" + count;
    }
}
