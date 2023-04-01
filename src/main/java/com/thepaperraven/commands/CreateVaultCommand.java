package com.thepaperraven.commands;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultCommandMeta;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.ai.vault.VaultPDContainer;
import com.thepaperraven.events.VaultRegisterEvent;
import com.thepaperraven.utils.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import static com.thepaperraven.utils.InventoryUtil.getFacing;

public class CreateVaultCommand {

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player!");
            return true;
        }
        String materialType = ResourceVaults.DEFAULT_MATERIAL.name();

        if (args.length > 1){
            materialType = args[1];
        }

        Block block = player.getTargetBlock(null, 10);

        Material vM = Material.matchMaterial(materialType);
        if (vM == null || !InventoryUtil.isValidMaterial(vM)){
            player.sendMessage("Wrong Material!");
            return false;
        }
        if (!(block.getState() instanceof Container container)) {
            player.sendMessage("You must be looking at a Chest or a Double Chest to create a Vault!");
            return true;
        }

        InventoryHolder holder = container.getInventory().getHolder();

        if (!(holder instanceof Container chest)) {
            player.sendMessage("You must be looking at a Chest or a Double Chest to create a Vault!");
            return true;
        }


        VaultPDContainer p = new VaultPDContainer(container);
        if (p.hasKeys()){
            player.sendMessage("Already a Vault!");
            return true;
        }


        VaultCommandMeta m = VaultCommandMeta.getMeta(materialType, player.getUniqueId());
        VaultInstance vaultInstance = new VaultInstance(p, m);


        BlockFace face = getFacing(chest.getBlock());

        if (face == null){
            player.sendMessage("Error with Chest Direction?");
            return true;
        }

        Chest left = (Chest) p.getLeft();

        if (!p.hasKeys()){
            ResourceVaults.log("Vault Preparation has No Keys! Successful So far...");
        }

        Block other = left.getBlock().getRelative(face);

        if (other.getType()!=Material.AIR ){
            ResourceVaults.error("Chest must not have a block on the front of it to be a Vault.");
            player.sendMessage("Error with Sign location (front of chest) not being AIR.");
            return true;
        }

        int vaultIndex = PlayerData.get(player.getUniqueId()).getNextIndex();

        if (m == null){
            player.sendMessage("Error w Meta!");
            return true;
        }
        VaultInstance instance = new VaultInstance(p, m);
        VaultRegisterEvent event = new VaultRegisterEvent(instance, VaultRegisterEvent.Reason.COMMAND);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return true;
        }

        ResourceVaults.log("Registering?...");


        if (PlayerData.registerVault(instance)) {
            ResourceVaults.log("Created Vault of " +((instance.getContainer().hasSecondChest()) ? 2 : 1 )+ " Chest(s)");

        }

        player.sendMessage("Vault created with index " + instance.getContainer().getVaultIndex() + "!");
        return true;
    }


}
