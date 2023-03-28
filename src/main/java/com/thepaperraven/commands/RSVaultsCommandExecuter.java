package com.thepaperraven.commands;

import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultManager;
import com.thepaperraven.ai.gui.VaultIcon;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RSVaultsCommandExecuter implements CommandExecutor {

    private final ResourceVaults plugin;

    public RSVaultsCommandExecuter(ResourceVaults plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (args.length == 0) {
            showIndexMenu(player);
            return true;
        }

        if ("open".equals(args[0].toLowerCase())) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /rsvaults open <index>");
                return true;
            }

            int index;
            try {
                index = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid index number: " + args[1]);
                return true;
            }

            List<Vault> vaults = ResourceVaults.getVaultManager().getVaults(player.getUniqueId());

            if (vaults.isEmpty()) {
                return false;
            }
            if (vaults.get(index) == null) {
                player.sendRawMessage(ChatColor.RED + "WARNING: Index of Vault " + index + " missing or invalid.");
                return false;
            }
            Vault vault = vaults.get(index);
            if (vault == null) {
                player.sendMessage(ChatColor.RED + "You do not have a vault at index " + index);
                return true;
            }

            openVaultGUI(player, vault);
        } else {
            player.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
        }

        return true;
    }

    private void showIndexMenu(Player player) {
        VaultManager manager = ResourceVaults.getVaultManager();
        int numVaults = manager.getVaultCount(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "You have " + numVaults + " vaults:");

        for (int i = 1; i <= numVaults; i++) {
            player.sendMessage(ChatColor.YELLOW + "Vault - " + ChatColor.RESET + ChatColor.WHITE + i);
        }

        player.sendMessage(ChatColor.GREEN + "Use '/rsvaults open <index>' to open a vault");
    }

    private void openVaultGUI(Player player, Vault vault) {
        VaultIcon vaultIcon = VaultIcon.get(vault);
        player.openInventory(vaultIcon.getVault().getInventory());
    }
}
