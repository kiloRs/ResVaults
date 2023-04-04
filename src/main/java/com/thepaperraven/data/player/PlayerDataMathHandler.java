package com.thepaperraven.data.player;

import com.thepaperraven.data.vault.Vault;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.thepaperraven.utils.VaultUtil.getVaultsMatching;

public class PlayerDataMathHandler {
    /**
     * Deposit the specified amount of the given material into the player's vaults.
     *
     * @param player the player whose vaults to deposit to
     * @param material the material to deposit
     * @param amount the amount to deposit
     * @return true if the deposit was successful, false otherwise
     */
    public static boolean deposit(Player player, Material material, int amount) {
        Map<Integer, Vault> vaultsMatching = getVaultsMatching(material, player);
        if (vaultsMatching == null) {
            player.sendMessage(ChatColor.GOLD + "You don't have a vault for this material.");
            return false;
        }

        int remainingAmount = amount;
        boolean transactionSuccessful = false;

        // Loop through all the player's vaults that match the given material
        for (Vault vault : vaultsMatching.values()) {
            int availableSpace = vault.getRemainingSpace();
            if (availableSpace >= remainingAmount) {
                // If the current vault has enough available space, deposit the remaining amount and break out of the loop
                vault.deposit(remainingAmount);
                remainingAmount = 0;
                transactionSuccessful = true;
                break;
            } else if (availableSpace > 0) {
                // If the current vault doesn't have enough space for the remaining amount, deposit as much as possible and continue to the next vault
                vault.deposit(availableSpace);
                remainingAmount -= availableSpace;
            }
        }

        if (remainingAmount > 0) {
            player.sendMessage(ChatColor.GOLD + "All your vaults for this material are full.");
            return false;
        } else {
            player.sendMessage(ChatColor.GREEN + "Deposited " + amount + " " + material.name() + " to your vaults.");
            return true;
        }
    }

    /**
     * Withdraw the specified amount of the given material from the player's vaults.
     *
     * @param player the player whose vaults to withdraw from
     * @param material the material to withdraw
     * @param amount the amount to withdraw
     * @return true if the withdrawal was successful, false otherwise
     */
    public static boolean withdraw(Player player, Material material, int amount) {
        Map<Integer, Vault> vaultsMatching = getVaultsMatching(material, player);
        if (vaultsMatching == null) {
            player.sendMessage(ChatColor.RED + "You don't have a vault for this material.");
            return false;
        }

        int remainingAmount = amount;
        boolean transactionSuccessful = false;

        // Loop through all the player's vaults that match the given material
        for (Vault vault : vaultsMatching.values()) {
            int balance = vault.getBalance();
            if (balance >= remainingAmount) {
                // If the current vault has enough balance, withdraw the remaining amount and break out of the loop
                vault.withdraw(remainingAmount);
                remainingAmount = 0;
                transactionSuccessful = true;
                break;
            } else if (balance > 0) {
                // If the current vault doesn't have enough balance for the remaining amount, withdraw as much as possible and continue to the next vault
                vault.withdraw(balance);
                remainingAmount -= balance;
            }
        }

        if (remainingAmount > 0) {
            player.sendMessage(ChatColor.RED + "You don't have enough " + material.name() + " in your vaults.");
            return false;
        } else {
            player.sendMessage(ChatColor.GREEN + "Withdrew " + amount + " " + material.name() + " from your vaults.");
            return true;
        }
    }

}