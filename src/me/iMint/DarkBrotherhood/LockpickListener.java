package me.iMint.DarkBrotherhood;

import java.util.Random;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class LockpickListener implements Listener {
	
	int lockpickid = 287;
	int percentChance = 25;
	int failedChanceDamage = 5;
	boolean usePerms = false;
	public DarkBrotherhood plugin;
	
	public LockpickListener (DarkBrotherhood instance) {
		this.plugin = instance;
	}
	
	public void setLockPickId(int i) {
		this.lockpickid = i;
	}
	
	public void setLockPickChance(int i) {
		this.percentChance = i;
	}
	
	public void setPermissionUsage(boolean b) {
		this.usePerms = b;
	}
	
	public void setFailedDamage(int i) {
		this.failedChanceDamage = i;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		int i = event.getClickedBlock().getTypeId();
		if ((i != 54) && (i != 61) && (i != 23) && (i != 324)) return;
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		if (!checkForPermission("DarkBrotherhood.use.lockpick", event.getPlayer())) return;
		Player p = event.getPlayer();
		Random rand = new Random();
		if (p.getItemInHand().getTypeId() != this.lockpickid) return;
		if (rand.nextInt(99) + 1 <= this.percentChance) { //Chance of successfully picking the lock
			p.sendMessage("You cunningly disengage the lock!");
			event.setCancelled(false);
		} else {
			p.damage(this.failedChanceDamage);
			p.sendMessage("The lock snaps back.");
		}
	}
	
	public boolean checkForPermission(String permission, Player p) {
		if (this.usePerms) {
			return p.hasPermission(permission);
		}
		return p.isOp();
	}
}