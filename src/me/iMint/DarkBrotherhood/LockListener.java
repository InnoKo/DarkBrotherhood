package me.iMint.DarkBrotherhood;

import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LockListener implements Listener {
	
	DarkBrotherhood plugin;
	//	HashMap<Block, Player> locked;
	int lockpickItem;
	int lockItem;
	int chance;
	int failDamage;
	boolean usePerms;
	
	public LockListener (DarkBrotherhood db) {
		plugin = db;
		//		locked = DarkBrotherhood.locked;
		lockpickItem = DarkBrotherhood.LockpickItem;
		lockItem = DarkBrotherhood.LockItem;
		chance = DarkBrotherhood.LockpickSuccessChance;
		failDamage = DarkBrotherhood.LockpickFailureDamage;
		usePerms = DarkBrotherhood.UsePermissions;
	}
	
	// Locking
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		Action action = event.getAction();
		ItemStack item = event.getItem();
		if (!action.equals(Action.RIGHT_CLICK_BLOCK)) return;
		if (!block.getType().equals(Material.CHEST)) return;
		// Picking Locks
		if (item != null && item.getTypeId() == lockpickItem) {
			if (Util.hasPermission("darkbrotherhood.use.lockpick", player)) {
				if (!DarkBrotherhood.locked.containsKey(block)) return;
				Random rand = new Random();
				if (rand.nextInt(99) + 1 <= chance) { //Chance of successfully picking the lock
					player.sendMessage("You cunningly disengage the lock!");
					event.setCancelled(false);
				} else {
					player.damage(failDamage);
					player.sendMessage("The lock snaps back.");
					event.setCancelled(true);
				}
				return;
			} else {
				player.sendMessage(ChatColor.RED + "You don't have permission to pick locks!");
				event.setCancelled(true);
				return;
			}
		}
		// Opening Chests
		if (DarkBrotherhood.locked.containsKey(block)) {
			String owner = DarkBrotherhood.locked.get(block);
			if (player.getName().equalsIgnoreCase(owner)) return;
			player.sendMessage(ChatColor.RED + "This chest is locked by " + owner + ChatColor.RED + "!");
			event.setCancelled(true);
			return;
		}
		// Locking Chests
		if (item != null && item.getTypeId() == lockItem) {
			if (DarkBrotherhood.permission.has(player, "darkbrotherhood.use.lock")) {
				DarkBrotherhood.locked.put(block, player.getName());
				int amount = item.getAmount();
				if (amount > 1) {
					item.setAmount(amount - 1);
				} else {
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
				player.sendMessage(ChatColor.GOLD + "This chest is now locked!");
				plugin.saveData();
				event.setCancelled(true);
				return;
			} else {
				player.sendMessage(ChatColor.RED + "You don't have permission to lock chests!");
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onChestBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		String owner = DarkBrotherhood.locked.get(block);
		if (!block.getType().equals(Material.CHEST)) return;
		if (!DarkBrotherhood.locked.containsKey(block)) return;
		if (player.getName().equalsIgnoreCase(owner)) {
			DarkBrotherhood.locked.remove(block);
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.STONE_BUTTON, 1));
			return;
		} else {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "This chest is locked by " + owner + ChatColor.RED + "!");
			return;
		}
	}
}