package com.thepaperraven.ai.utils;

import com.thepaperraven.ai.Vault;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

public class SignUtil {

    public static void updateVaultSign(Vault vault){
        updateVaultSign(vault,false);
    }
    public static void updateVaultSign(Vault vault, boolean glow){
        updateVaultSign(vault, glow,ChatColor.WHITE,ChatColor.LIGHT_PURPLE,ChatColor.LIGHT_PURPLE,ChatColor.WHITE);
    }
    public static void updateVaultSign(Vault vault, boolean glow, ChatColor resourcesText, ChatColor materialText, ChatColor ownerText, ChatColor itemsText) {
        Sign sign = vault.getSign();
        if (sign == null) return;

        // Set the first line to "[Resources]" in red
        String firstLine = resourcesText + "[Resources]";
        // Set the second line to the material name in green
        String secondLine = materialText + vault.getMaterial().name();
        // Set the third line to the owner's name in blue
        String thirdLine = ownerText + vault.getOwner().getName();
        // Set the fourth line to the total items in yellow
        String fourthLine = itemsText + "Total Items: " + vault.getTotalItems();

        // Set each line of the sign
        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', firstLine));
        sign.setLine(1, ChatColor.translateAlternateColorCodes('&', secondLine));
        sign.setLine(2, ChatColor.translateAlternateColorCodes('&', thirdLine));
        sign.setLine(3, ChatColor.translateAlternateColorCodes('&', fourthLine));

        // Add glow effect to each line of the sign
        for (int i = 0; i < 4; i++) {
            sign.setGlowingText(glow);
        }
        sign.update();
    }

    public static void lockSignOfVault(Vault vault) {
        Sign sign = vault.getSign();
        if (sign == null) return;

        // Set the color of the owner's name to red to indicate it's locked
        String thirdLine = sign.getLine(2);
        sign.setLine(2, ChatColor.RED + thirdLine.substring(2));
        sign.update();
    }

    public static void unlockSignOfVault(Vault vault) {
        Sign sign = vault.getSign();
        if (sign == null) return;

        // Set the color of the owner's name to green to indicate it's unlocked
        String thirdLine = sign.getLine(2);
        sign.setLine(2, ChatColor.GREEN + thirdLine.substring(2));
        sign.update();
    }
}