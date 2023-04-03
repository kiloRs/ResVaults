package com.thepaperraven.commands;

import com.thepaperraven.ai.vault.Vault;
import com.thepaperraven.events.VaultCreateEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class RVCreateVaultCommand implements CommandExecutor {
    private final Plugin plugin;

    public RVCreateVaultCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage("Usage: /rvcreate <material>");
            return true;
        }
        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            player.sendMessage("Invalid material.");
            return true;
        }

        Block block = player.getTargetBlockExact(5);
        if (block == null || !(block.getState() instanceof InventoryHolder)) {
            player.sendMessage("You must be looking at a container block.");
            return true;
        }
        if (!(block.getState() instanceof Container container)){
            return true;
        }
        if (Vault.isVault(container)) {
            player.sendMessage("This container is already registered as a vault.");
            return true;
        }

        Vault vault = null;
        VaultCreateEvent event = new VaultCreateEvent(player, block.getLocation(), material);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return true;
        }

        vault = Vault.createVault(event);
        if (vault != null) {
            player.sendMessage("Vault created.");
        } else {
            player.sendMessage("Failed to register vault. Please try again later.");
        }
        return true;
    }
}
