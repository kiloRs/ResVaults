package com.thepaperraven.listeners;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.ai.vault.VaultMetadata;
import com.thepaperraven.ai.vault.VaultPDContainer;
import com.thepaperraven.events.VaultRegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.thepaperraven.utils.LocationUtils.getStringOfLocation;

public class VaultRegistrationListener implements Listener {

    private final Plugin resourceVaults;

    public VaultRegistrationListener(Plugin resourceVaults) {

        this.resourceVaults = resourceVaults;
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onRegisterStage(VaultRegisterEvent e){
        VaultInstance vault = e.getVault();
        logValidation(vault, "Valid Vault on Staging!", "Invalid Vault on Staging!");
        ResourceVaults.log("Staging VaultRegisterEvent!");
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegister(VaultRegisterEvent e){
        VaultInstance vault = e.getVault();

        logValidation(vault, "Valid Vault on Register!", "Invalid Vault on Register!");
        ResourceVaults.log("VaultRegisterEvent -> :" + vault.getMetadata().getOwnerUUID() + " : " + vault.getMetadata().getVaultIndex());
        ResourceVaults.log("VaultRegisterEvent --> :" + (vault.getContainer().hasSecondChest()?getStringOfLocation(vault.getContainer().getLeft().getLocation().toBlockLocation()) + " " + getStringOfLocation(vault.getContainer().getRight().getLocation().toBlockLocation()):getStringOfLocation(vault.getContainer().getLeft().getLocation().toBlockLocation())));

        if (vault.getInventory() == null || vault.getInventory().getInventory().getLocation() == null){
            return;
        }
        ResourceVaults.log("VaultRegisterEvent ---> :" + vault.getInventory().getInventory().getLocation().toBlockLocation().toString());
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        BlockState state = block.getState();
        if (state instanceof Sign sign && sign.getBlockData() instanceof WallSign wallSign && sign.getLine(0).equalsIgnoreCase("[Resources]")){
            Block other = sign.getBlock().getRelative(wallSign.getFacing().getOppositeFace());
            if (other == null){
                return;
            }
            if ( other.getState() instanceof InventoryHolder holder ){
                VaultPDContainer vaultPDContainer = new VaultPDContainer(holder);
                if (!vaultPDContainer.hasKeys()){
                    return;
                }
                if (vaultPDContainer.hasOwner() && vaultPDContainer.getOwner().equals(player.getUniqueId())){
                    PlayerData playerData = PlayerData.get(player.getUniqueId());
                    if (playerData.hasVault(vaultPDContainer.getVaultIndex())){
                        VaultInstance vault = playerData.getVault(vaultPDContainer.getVaultIndex());
                        if (vault.getContainer() != vaultPDContainer){
                            player.sendMessage("Mismatched Data? Error!");
                            return;
                        }
                    }
                    vaultPDContainer.updateSignText(true);
                    return;
                }
                else if (!vaultPDContainer.getOwner().equals(player.getUniqueId())){
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Cannot change other players Vault's sign.");
                }
            }
        }
//        if (state instanceof Sign sign &&  sign.line(0) != null && !sign.getLine(0).isEmpty() && sign.getLine(0).equalsIgnoreCase("[Resources]") && sign.getBlockData() instanceof WallSign wallSign) {
//            Block b = sign.getBlock().getRelative(wallSign.getFacing().getOppositeFace());
//            if (b.getState() instanceof InventoryHolder invHolder) {
//                UUID uuid = player.getUniqueId();
//                Material material = getMaterial(sign.getLines());
//                int index = PlayerData.get(player.getUniqueId()).getNextIndex();
//                VaultPDContainer c = new VaultPDContainer(invHolder);
//                VaultMetadata m = new VaultMetadata(material, uuid, index);
//                VaultInventory i = new VaultInventory(c, m);
//                VaultInstance vaultInstance = new VaultInstance(c, m, i);
//
//                VaultRegisterEvent vaultRegisterEvent = new VaultRegisterEvent(vaultInstance);
//                Bukkit.getPluginManager().callEvent(vaultRegisterEvent);
//
//                if (vaultRegisterEvent.isCancelled()){
//                    player.sendRawMessage("Vault registration was cancelled on " + vaultInstance.getMetadata().getVaultIndex() + " of " + event.getPlayer().getName());
//                    vaultInstance.removeFromBlock(true);
//                    c = null;
//                    m = null;
//                    i = null;
//                    vaultInstance = null;
//                    return;
//                }
//                logValidation(vaultInstance, "Valid Vault on Sign Change!", "Invalid Vault on Sign Change!");
//
//                try {
//                    PlayerData.registerVault(vaultInstance);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//                player.sendMessage("Vault registered with index " + index);
//            }
        }


    private static void logValidation(VaultInstance vaultInstance, String s, String error) {
        if (vaultInstance.isValid()){
            ResourceVaults.log(s);
        }
        else {
            ResourceVaults.error(error);
        }
    }

    public static Material getMaterial(String[] lines){
        String lineTwo = lines[1];
        Material material = Material.matchMaterial(lineTwo);
        return material !=null?material:Material.WHEAT;
    }

    public Plugin getResourceVaults() {
        return resourceVaults;
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            Block block = event.getClickedBlock();
            if (block == null){
                return;
            }
            if (block.getState() instanceof Sign sign && sign.getBlockData() instanceof WallSign wallSign) {
                if (sign == null || sign.getLines().length == 0){
                    return;
                }
                if (sign.getLine(0).equalsIgnoreCase("[Resources]")) {
                    Block attachedBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
                    if (attachedBlock.getState() instanceof DoubleChest doubleChest) {
                        InventoryHolder invHolder = doubleChest.getInventory().getHolder();
                        try {
                            register(player, sign, invHolder);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else if (attachedBlock.getState() instanceof InventoryHolder invHolder) {
                        try {
                            register(player, sign, invHolder);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

    }

    private void register(Player player, @NotNull Sign sign, InventoryHolder invHolder) throws Exception {
        UUID uuid = player.getUniqueId();
        Material m = Material.matchMaterial(sign.getLine(1).toUpperCase());
        Material material = m == null?Material.WHEAT:m;
        int index = PlayerData.get(uuid).getNextIndex();
        VaultInstance vaultInstance = new VaultInstance(new VaultPDContainer(invHolder), new VaultMetadata(material, uuid, index));
        if (vaultInstance.getContainer().hasKeys()){
            if (vaultInstance.getContainer().getOwner() != uuid){
                player.sendMessage("Someone else owns this Vault!");
                return;
            }
            else {
                ResourceVaults.log("Attempt to register owned Vault of " + player.getName());
                return;
            }
        }
        if (vaultInstance.isValid()) {
            player.sendMessage("Vault registered with index " + index);
            VaultRegisterEvent e = new VaultRegisterEvent(vaultInstance);
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()){
                player.sendMessage("Cancelled Vault Creation!");
                return;
            }
            player.sendMessage("Registering Vault...");
            PlayerData.registerVault(vaultInstance);

        }
        else {
            player.sendMessage(ChatColor.RED + "Error! Registering Invalid Vault!");
        }
    }
}
