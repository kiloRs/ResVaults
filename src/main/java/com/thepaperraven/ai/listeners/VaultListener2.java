import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.events.VaultCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

public class VaultListener implements Listener {

    private final VaultManager vaultManager;
    private final PlayerDataManager playerDataManager;

    public VaultListener(VaultManager vaultManager, PlayerDataManager playerDataManager) {
        this.vaultManager = vaultManager;
        this.playerDataManager = playerDataManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!"[Resources]".equals(event.getLine(0))) {
            return;
        }

        Material material = Material.WHEAT;
        if (!event.getLine(1).isEmpty()) {
            try {
                material = Material.valueOf(event.getLine(1).toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Block block = event.getBlock().getRelative(BlockFace.DOWN);
        if (!block.getType().name().contains("CHEST")) {
            return;
        }

        Vault vault = vaultManager.getVaultFromLocation(block.getLocation());
        if (vault != null) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int totalVaults = vaultManager.getTotalVaults(uuid);

        if (totalVaults >= VaultManager.MAX_VAULTS) {
            player.sendMessage("You have reached the maximum amount of vaults.");
            event.setCancelled(true);
            return;
        }

        event.setLine(0, "[Resources]");
        event.setLine(1, material.name());
        event.setLine(2, "Vault #" + (totalVaults + 1));

        VaultCreateEvent vaultCreateEvent = new VaultCreateEvent(player, block.getLocation(), material);
        ResourceVaults.getPlugin().getServer().getPluginManager().callEvent(vaultCreateEvent);

        if (!vaultCreateEvent.isCancelled()) {
            Vault newVault = new Vault(uuid, vaultCreateEvent.getLocation(), vaultCreateEvent.getMaterial(), totalVaults + 1);
            vaultManager.addVault(newVault);

            PlayerData playerData = ResourceVaults.getPlayerData(uuid);
            playerData.addVault(newVault);

            player.sendMessage("Vault created successfully!");
        } else {
            player.sendMessage("Failed to create vault.");
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        // Only process if the placed block is a chest
        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        // Check if the chest has the [Resources] sign on the front
        if (!VaultUtils.isVault(chest)) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if the player has permission to create a vault
        if (!player.hasPermission("vaults.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to create a vault.");
            event.setCancelled(true);
            return;
        }

        // Get the material type of the chest from the sign
        Material material = VaultUtils.getVaultMaterial(chest);

        // Check if the material is valid
        if (!VaultUtils.isValidMaterial(material)) {
            player.sendMessage(ChatColor.RED + "Invalid vault material type.");
            event.setCancelled(true);
            return;
        }

        // Create the Vault instance
        Vault vault = new Vault(playerId, block.getLocation(), material);

        // Fire a VaultCreateEvent
        VaultCreateEvent createEvent = new VaultCreateEvent(vault);
        Bukkit.getPluginManager().callEvent(createEvent);

        // Check if the event was cancelled
        if (createEvent.isCancelled()) {
            player.sendMessage(ChatColor.RED + "Failed to create vault.");
            event.setCancelled(true);
            return;
        }

        // Save the Vault to the player's PlayerData
        PlayerData playerData = VaultManager.getPlayerData(playerId);
        int index = playerData.getTotalAmountOfVaults() + 1;
        playerData.setVault(index, vault);

        // Save the Vault to the chest's PersistentDataContainer
        VaultUtils.saveVaultToPDC(chest, vault);

        // Set the sign text to [Resources] and the material type
        VaultUtils.setVaultSign(chest, material);

        player.sendMessage(ChatColor.GREEN + "Vault created successfully.");
    }
}