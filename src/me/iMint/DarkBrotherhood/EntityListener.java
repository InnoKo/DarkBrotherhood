package me.iMint.DarkBrotherhood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EntityListener implements Listener {
	
	DarkBrotherhood plugin;
	private final int multiplier;
	private final int rollEN;
	private final int assassinEN;
	private final int shurikenDMG;
	private final int rollChance;
	private final String goodRollMsg;
	private final String badRollMsg;
	
	HashMap<Entity, Boolean> shuriken = new HashMap<Entity, Boolean>();
	ArrayList<Player> hasPoison = new ArrayList<Player>();
	
	public EntityListener (DarkBrotherhood plugin) {
		this.plugin = plugin;
		multiplier = DarkBrotherhood.Multiplier;
		rollEN = DarkBrotherhood.LeapOfFaithEnergyUsage;
		assassinEN = DarkBrotherhood.AssassinationEnergyUsage;
		shurikenDMG = DarkBrotherhood.ShurikenDamage;
		rollChance = DarkBrotherhood.LeapOfFaithSuccessChance;
		goodRollMsg = DarkBrotherhood.LeapOfFaithSuccessMessage;
		badRollMsg = DarkBrotherhood.LeapOfFaithFailureMessage;
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (this.shuriken.containsKey(event.getEntity())) {
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
			if ((Util.hasPermission("DarkBrotherhood.roll", player)) && (player.isSneaking())) {
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
			if (this.shuriken.containsKey(event.getDamager())) {
				e.setDamage(shurikenDMG);
				if (shuriken.get(event.getDamager())) {
					e.setDamage(shurikenDMG * 2);
				}
				this.shuriken.remove(event.getDamager());
				wasShuriken = true;
			}
			
			Entity en = event.getDamager();
			if (event.isCancelled()) return;
			if (!(en instanceof Player)) return;
			Player assassin = (Player) en;
			int energy = DarkBrotherhood.mana.get(assassin);
			
			if (!(event.getEntity() instanceof LivingEntity)) return;
			if (!Util.hasPermission("DarkBrotherhood.assassinate", assassin)) return;
			LivingEntity target = (LivingEntity) event.getEntity();
			int base = event.getDamage();
			
			// Poisoning
			if (this.hasPoison.contains(assassin)) {
				this.hasPoison.remove(assassin);
				this.plugin.pTracker.poisonDurations.remove(target);
				this.plugin.pTracker.poisoned.add(target);
				if (target instanceof Player) ((Player) target).sendMessage(ChatColor.LIGHT_PURPLE + "You have been poisoned!");
				assassin.sendMessage(ChatColor.LIGHT_PURPLE + "You have poisoned your target!");
			}
			if (!assassin.isSneaking()) return;
			
			// Assassination
			double q = target.getLocation().getDirection().dot(assassin.getLocation().getDirection());
			if (q > 0.0D) {
				int bonus = (int) (q * this.multiplier * base);
				if (bonus == 0) return;
				if (wasShuriken) bonus *= 2;
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
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		Entity target = event.getTarget();
		// Can't see hidden players
		if (target instanceof Player) {
			Player player = (Player) target;
			if (DarkBrotherhood.hidden.contains(player)) {
				event.setCancelled(true);
			}
		}
	}
}