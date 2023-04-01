package com.thepaperraven.commands;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeleteVaultCommand {

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player!");
            return true;
        }

        if (args.length<2){
            return false;
        }

        VaultInstance vault = PlayerData.get(player.getUniqueId()).getVault(Integer.parseInt(args[1]));

        if (vault == null) {
            player.sendMessage("You are not looking at a Vault sign!");
            return true;
        }

        if (!vault.getContainer().getOwner().equals(player.getUniqueId())) {
            player.sendMessage("You are not the owner of this Vault!");
            return true;
        }

        if (!vault.getOwnerData().hasVault(vault.getContainer().getVaultIndex())){
            player.sendMessage("No vault by index " + args[1]);
            return true;
        }
        vault.getOwnerData().removeVault(vault.getContainer().getVaultIndex());

        ResourceVaults.log("Deleted!");
        return true;
    }
}
