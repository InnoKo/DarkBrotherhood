package me.iMint.DarkBrotherhood;

import java.util.HashMap;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LockListener implements Listener {
	
	DarkBrotherhood plugin;
	HashMap<Block, Player> locked;
	int lockpickItem;
	int lockItem;
	int chance;
	int failDamage;
	boolean usePerms;
	
	public LockListener (DarkBrotherhood db) {
		plugin = db;
		locked = DarkBrotherhood.locked;
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
		if (locked.containsKey(block)) {
			Player owner = locked.get(block);
			if (player.equals(owner)) return;
			player.sendMessage(ChatColor.RED + "This chest is locked by " + locked.get(block).getDisplayName() + ChatColor.RED + "!");
			event.setCancelled(true);
			return;
			// Locking Chests
		}
		if (item.getTypeId() == lockItem) {
			if (DarkBrotherhood.permission.has(player, "darkbrotherhood.use.lock")) {
				locked.put(block, player);
				int amount = item.getAmount();
				if (amount > 1) {
					item.setAmount(amount - 1);
				} else {
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
				player.sendMessage(ChatColor.GOLD + "This chest is now locked!");
				event.setCancelled(true);
				return;
			} else {
				player.sendMessage(ChatColor.RED + "You don't have permission to lock chests!");
				event.setCancelled(true);
				return;
			}
		}
		// Picking Locks
		if (item.getTypeId() == lockpickItem) {
			if (checkForPermission("darkbrotherhood.use.lockpick", player)) {
				if (!locked.containsKey(block)) return;
				Random rand = new Random();
				if (rand.nextInt(99) + 1 <= chance) { //Chance of successfully picking the lock
					player.sendMessage("You cunningly disengage the lock!");
					event.setCancelled(false);
				} else {
					player.damage(failDamage);
					player.sendMessage("The lock snaps back.");
				}
				return;
			} else {
				player.sendMessage(ChatColor.RED + "You don't have permission to pick locks!");
				event.setCancelled(true);
				return;
			}
		}
	}
	
	public boolean checkForPermission(String permission, Player p) {
		if (this.usePerms) {
			return DarkBrotherhood.permission.has(p, permission);
		}
		return p.isOp();
	}
}