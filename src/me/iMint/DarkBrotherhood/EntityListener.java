package me.iMint.DarkBrotherhood;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EntityListener implements Listener {
	
	DarkBrotherhood plugin;
	private boolean usePerms;
	private int multiplier = 2;
	private final int rollEN;
	private final int assassinEN;
	private final int shurikenDMG;
	private final int rollChance;
	private final String goodRollMsg;
	private final String badRollMsg;
	
	ArrayList<Entity> shuriken = new ArrayList<Entity>();
	ArrayList<Player> hasPoison = new ArrayList<Player>();
	
	public void setMultiplier(int m) {
		this.multiplier = m;
	}
	
	public void setPermissionUsage(boolean b) {
		this.usePerms = b;
	}
	
	public EntityListener (DarkBrotherhood plugin) {
		this.plugin = plugin;
		rollEN = plugin.getConfig().getInt("L.O.F.-EnergyUsage");
		assassinEN = plugin.getConfig().getInt("AssassinateEnergyUsage");
		shurikenDMG = plugin.getConfig().getInt("ShurikenDamage");
		rollChance = plugin.getConfig().getInt("L.O.F.-Chance");
		goodRollMsg = plugin.getConfig().getString("L.O.F.-success-message");
		badRollMsg = plugin.getConfig().getString("L.O.F.-fail-message");
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (this.shuriken.contains(event.getEntity())) {
			event.getEntity().remove();
			this.shuriken.remove(event.getEntity());
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		boolean wasShuriken = false;
		if ((e.getCause() == EntityDamageEvent.DamageCause.FALL) && ((e.getEntity() instanceof Player))) {
			Player player = (Player) e.getEntity();
			int energy = DarkBrotherhood.mana.get(player);
			if ((checkForPermission("DarkBrotherhood.roll", player)) && (player.isSneaking())) {
				Random success = new Random();
				if (success.nextInt(99) + 1 <= rollChance) {
					if (energy >= rollEN) {
						player.sendMessage(goodRollMsg);
						DarkBrotherhood.mana.put(player, energy - rollEN);
						e.setCancelled(true);
					} else {
						player.sendMessage(ChatColor.RED + "Not enough energy to perform a Leap of Faith!");
					}
				} else {
					if (energy >= rollEN) player.sendMessage(badRollMsg);
					else player.sendMessage(ChatColor.RED + "Not enough energy to perform a Leap of Faith!");
				}
			}
			
		}
		
		if ((e instanceof EntityDamageByEntityEvent)) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
			
			// Shuriken Damage
			if (this.shuriken.contains(event.getDamager())) {
				e.setDamage(shurikenDMG);
				this.shuriken.remove(event.getDamager());
				wasShuriken = true;
			}
			
			Entity en = event.getDamager();
			if (event.isCancelled()) return;
			if (!(en instanceof Player)) return;
			Player assassin = (Player) en;
			int energy = DarkBrotherhood.mana.get(assassin);
			
			if (!(event.getEntity() instanceof LivingEntity)) return;
			if (!checkForPermission("DarkBrotherhood.assassinate", assassin)) return;
			LivingEntity target = (LivingEntity) event.getEntity();
			int base = event.getDamage();
			
			// Poisoning
			if (this.hasPoison.contains(assassin)) {
				this.hasPoison.remove(assassin);
				this.plugin.pTracker.poisonDurations.remove(target);
				this.plugin.pTracker.poisoned.add(target);
				assassin.sendMessage("Target has been poisoned!");
			}
			if (!assassin.isSneaking()) return;
			
			// Assassination
			double q = target.getLocation().getDirection().dot(assassin.getLocation().getDirection());
			if (q > 0.0D) {
				int bonus = (int) (q * this.multiplier * base);
				if (bonus == 0) return;
				if (wasShuriken) bonus *= 2;
				System.out.println(assassinEN);
				if (energy < assassinEN) {
					assassin.sendMessage(ChatColor.RED + "You don't have enough energy to assassinate!");
					return;
				} else {
					assassin.sendMessage(ChatColor.GOLD + "Sneak attack for " + bonus + " extra damage!");
					event.setDamage(base + bonus);
					DarkBrotherhood.mana.put(assassin, energy - assassinEN);
					PlayerListener.showEnergy(assassin, energy - assassinEN);
				}
			}
		}
	}
	
	// Check if a Player has a certain permission
	public boolean checkForPermission(String permission, Player p) {
		if (this.usePerms) {
			return DarkBrotherhood.permission.has(p, permission);
		}
		return p.isOp();
	}
}