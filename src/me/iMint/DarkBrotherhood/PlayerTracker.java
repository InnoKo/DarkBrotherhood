package me.iMint.DarkBrotherhood;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerTracker implements Runnable {
	
	DarkBrotherhood plugin;
	Server server;
	boolean usePerms;
	int runtimes = 0;
	int duration;
	int poisonDamage;
	HashMap<Player, Block> playerBlocks = new HashMap<Player, Block>();
	HashMap<Player, Boolean> hasBlock = new HashMap<Player, Boolean>();
	ArrayList<Player> players = new ArrayList<Player>();
	ArrayList<LivingEntity> poisoned = new ArrayList<LivingEntity>();
	HashMap<LivingEntity, Integer> poisonDurations = new HashMap<LivingEntity, Integer>();
	
	public PlayerTracker (DarkBrotherhood db) {
		plugin = db;
		server = db.getServer();
		usePerms = DarkBrotherhood.UsePermissions;
		duration = DarkBrotherhood.PoisonDuration;
		poisonDamage = DarkBrotherhood.PoisonDamage;
	}
	
	// Poison a player
	public void addPoisonedPlayer(Player player) {
		poisoned.add(player);
	}
	
	// Damaged poisoned players
	public void applyPoison() {
		for(int i = 0; i < poisoned.size(); i++) {
			LivingEntity p = poisoned.get(i);
			if (poisonDurations.containsKey(p)) {
				if (poisonDurations.get(p).intValue() > 1) {
					p.damage(this.poisonDamage);
					poisonDurations.put(p, Integer.valueOf(poisonDurations.get(p).intValue() - 1));
				} else {
					p.damage(this.poisonDamage);
					this.poisoned.remove(i);
					this.poisonDurations.remove(p);
				}
			} else {
				poisonDurations.put(p, duration);
			}
		}
	}
	
	// Runnable
	public void run() {
		runtimes += 1;
		applyPoison();
		for(int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			
			if (this.playerBlocks.containsKey(p)) {
				Location loc = playerBlocks.get(p).getLocation();
				int px = p.getLocation().getBlockX();
				int py = p.getLocation().getBlockY();
				int pz = p.getLocation().getBlockZ();
				int lx = loc.getBlockX();
				int ly = loc.getBlockY();
				int lz = loc.getBlockZ();
				
				int r = 2;
				if ((px > lx + r) || (px < lx - r) || (py > ly + r) || (py < ly - r) || (pz > lz + r) || (pz < lz - r) || (!p.isSneaking())) {
					if (hasBlock.containsKey(p)) {
						if (hasBlock.get(p).booleanValue()) {
							if (playerBlocks.containsKey(p)) {
								playerBlocks.get(p).setTypeId(0);
								hasBlock.put(p, Boolean.valueOf(false));
							} else {
								playerBlocks.put(p, p.getWorld().getBlockAt(px, py - 1, pz));
								hasBlock.put(p, Boolean.valueOf(false));
							}
						}
					} else {
						hasBlock.put(p, Boolean.FALSE);
					}
				}
				
			} else {
				this.playerBlocks.put(p, p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
				this.hasBlock.put(p, Boolean.valueOf(false));
			}
		}
		if (this.runtimes == 60) {
			ArrayList<Player> updated = new ArrayList<Player>();
			for(int i = 0; i < this.players.size(); i++) {
				if (!updated.contains(this.players.get(i))) {
					updated.add(this.players.get(i));
				}
			}
			
			this.players = updated;
		}
	}
	
	public void setPlayerBlock(Block block, Player p) {
		//if(plugin.useSpout) SpoutManager.getMaterialManager().overrideBlock(block, plugin.grappleBlock);
		if (this.hasBlock.containsKey(p)) {
			if (this.hasBlock.get(p).booleanValue()) {
				if (this.playerBlocks.containsKey(p)) {
					Block b = this.playerBlocks.get(p);
					b.setTypeId(0);
					this.playerBlocks.put(p, block);
					this.hasBlock.put(p, Boolean.valueOf(true));
				} else {
					this.playerBlocks.put(p, block);
					this.hasBlock.put(p, Boolean.valueOf(true));
				}
			} else {
				this.playerBlocks.put(p, block);
				this.hasBlock.put(p, Boolean.valueOf(true));
			}
		} else {
			this.playerBlocks.put(p, block);
			this.hasBlock.put(p, Boolean.valueOf(true));
		}
	}
}