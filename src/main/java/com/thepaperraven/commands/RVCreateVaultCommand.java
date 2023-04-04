package com.thepaperraven.commands;

import com.thepaperraven.data.player.PlayerData;
import com.thepaperraven.data.vault.Vault;
import org.bukkit.ChatColor;
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

import static com.thepaperraven.data.vault.Vault.loadFromPDC;
import static com.thepaperraven.utils.InventoryUtil.isValidMaterial;

public class RVCreateVaultCommand implements CommandExecutor {
    private final Plugin plugin;

    public RVCreateVaultCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("Usage: /rvcreate <material>");
            return true;
        }
        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            player.sendMessage("Invalid material.");
            return true;
        }
        if (!isValidMaterial(material)){
            player.sendMessage(ChatColor.RED +"This is not a valid material!");
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

        Vault loadedFromPDC = loadFromPDC(block);
        if (loadedFromPDC != null){
            player.sendMessage("Cannot make a vault out of this, already part of a vault!");
            return true;
        }

        try {
            PlayerData playerData = PlayerData.get(player.getUniqueId());
            if (playerData.registerVault(block.getLocation(),material)) {
                player.sendMessage("Vault was registered as " + (playerData.getNextIndex() -1));
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
