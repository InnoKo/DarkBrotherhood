package me.iMint.DarkBrotherhood;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
	
	private final DarkBrotherhood plugin;
	private boolean usePerms = false;
	private int[] climbable = new int[1];
	private int poisonItem = 40;
	private static int maxEN;
	private static int addEN;
	private static int climbEN;
	private static int sneakEN;
	private static long tickEN;
	
	// Constructor
	PlayerListener (DarkBrotherhood aThis) {
		this.plugin = aThis;
		PlayerListener.maxEN = aThis.getConfig().getInt("MaximumEnergy");
		PlayerListener.addEN = aThis.getConfig().getInt("EnergyRestoreAmount");
		PlayerListener.tickEN = aThis.getConfig().getInt("EnergyRestoreTick") * 20;
		PlayerListener.sneakEN = aThis.getConfig().getInt("InvisibilityEnergyUsage");
		PlayerListener.climbEN = aThis.getConfig().getInt("ClimbingEnergyUsage");
	}
	
	// Sets the ID for the poisoning item (default mushroom)
	public void setPoisonItem(int i) {
		this.poisonItem = i;
	}
	
	// Turns Permission Usage on or off
	public void setPermissionsUsage(boolean b) {
		this.usePerms = b;
	}
	
	// Sets the climbable blocks
	public void setClimbable(int[] i) {
		this.climbable = i;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Shuriken throwing
		if ((event.getAction() == Action.LEFT_CLICK_AIR) && (event.getPlayer().getItemInHand().getTypeId() == 318) && (checkForPermission("DarkBrotherhood.use.shuriken", event.getPlayer()))) {
			ItemStack item = event.getItem();
			/**if (plugin.useSpout) {
				SpoutItemStack sItem = (SpoutItemStack) event.getItem();
				if (sItem.getMaterial() != plugin.shuriken) return;
			}**/
			if (item.getTypeId() != DarkBrotherhood.config.getInt("ShurikenId")) return;
			Arrow shuriken = event.getPlayer().launchProjectile(Arrow.class);
			//SpoutItemStack is = (SpoutItemStack) event.getPlayer().getItemInHand();
			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
			} else {
				event.getPlayer().getInventory().clear(event.getPlayer().getInventory().getHeldItemSlot());
			}
			
			this.plugin.entityListener.shuriken.add(shuriken);
		}
		
		// Climbing blocks
		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (event.getPlayer().getItemInHand().getTypeId() == 0)) {
			Block block = event.getClickedBlock();
			Player p = event.getPlayer();
			
			World w = p.getWorld();
			if (checkForPermission("DarkBrotherhood.climb", p)) {
				int energy = DarkBrotherhood.mana.get(p);
				if (energy < climbEN) {
					p.sendMessage(ChatColor.RED + "You don't have enough energy to climb!");
					return;
				}
				if (canClimb(block)) {
					if (event.getBlockFace() == BlockFace.DOWN) {
						Block playerBlock = w.getBlockAt(block.getX(), block.getY() - 1, block.getZ());
						climbBlock(playerBlock, p);
					} else if (event.getBlockFace() == BlockFace.UP) {
						Block playerBlock = block.getRelative(0, 2, 0);
						climbBlock(playerBlock, p);
					} else {
						int bx = block.getX();
						int bz = block.getZ();
						int px = p.getLocation().getBlockX();
						int pz = p.getLocation().getBlockZ();
						int xdifference = px - bx;
						int zdifference = pz - bz;
						if (Math.abs(xdifference) > Math.abs(zdifference)) {
							if (xdifference > 0) {
								climbBlock(block.getRelative(1, 0, 0), p);
							} else {
								climbBlock(block.getRelative(-1, 0, 0), p);
							}
							
						} else if (zdifference > 0) {
							climbBlock(block.getRelative(0, 0, 1), p);
						} else {
							climbBlock(block.getRelative(0, 0, -1), p);
						}
					}
					DarkBrotherhood.mana.put(p, energy - climbEN);
				}
				
			}
			
		}
		
		// Poisoning weapons
		if ((event.getAction() == Action.RIGHT_CLICK_AIR) && (event.getPlayer().getItemInHand().getTypeId() == this.poisonItem)
				&& (checkForPermission("DarkBrotherhood.use.poison", event.getPlayer()))) {
			Player p = event.getPlayer();
			if (this.plugin.entityListener.hasPoison.contains(p)) {
				p.sendMessage("Your weapon is already poisoned!");
			} else {
				p.sendMessage("You poison your weapon!");
				this.plugin.entityListener.hasPoison.add(p);
				ItemStack is = event.getPlayer().getItemInHand();
				if (is.getAmount() > 1) {
					is.setAmount(is.getAmount() - 1);
				} else event.getPlayer().getInventory().clear(event.getPlayer().getInventory().getHeldItemSlot());
			}
		}
	}
	
	// Invisible Sneaking
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player sneaker = event.getPlayer();
		Boolean sneaking = event.isSneaking();
		Player[] players = plugin.getServer().getOnlinePlayers();
		if (sneaking && checkForPermission("InvisibleSneak", sneaker)) {
			int energy = DarkBrotherhood.mana.get(sneaker);
			if (energy >= sneakEN) {
				DarkBrotherhood.mana.put(sneaker, energy - sneakEN);
				sneaker.sendMessage(ChatColor.GOLD + "You are now hidden in the shadows!");
				System.out.println(sneakEN + "," + climbEN + "," + addEN + "," + tickEN);
				for(int i = 0; i <= players.length; i++) {
					Player viewer = players[i];
					if (!checkForPermission("SeeInvisiblePlayers", viewer)) {
						sneaker.hidePlayer(viewer);
					}
				}
			} else {
				sneaker.sendMessage(ChatColor.RED + "Not enough energy to go invisible!");
			}
			
		} else { // Player stopped sneaking
			sneaker.sendMessage(ChatColor.GRAY + "You are no longer hidden in the shadows!");
			for(int i = 1; i <= players.length; i++) {
				Player viewer = players[i];
				sneaker.showPlayer(viewer);
			}
		}
	}
	
	// Starts the Energy schedulers
	@EventHandler
	public void startEnergy(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (!DarkBrotherhood.mana.containsKey(player)) DarkBrotherhood.mana.put(player, maxEN);
		
		// Restores Energy every 2 seconds, double restoration if the player has full health
		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable(){
			
			public void run() {
				int energy = DarkBrotherhood.mana.get(player);
				int addEN = PlayerListener.addEN;
				if (player.getHealth() == player.getMaxHealth()) addEN *= 2;
				int newEnergy = energy + addEN;
				if (newEnergy > maxEN) newEnergy = maxEN;
				DarkBrotherhood.mana.put(player, newEnergy);
				// If the energy actually increased, show the change!
				if (newEnergy != energy) showEnergy(player, newEnergy);
			}
		}, 0L, tickEN);
	}
	
	// Check if a Player has a certain permission
	public boolean checkForPermission(String permission, Player p) {
		if (this.usePerms) {
			return DarkBrotherhood.permission.has(p, permission);
		}
		return p.isOp();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		this.plugin.pTracker.players.add(event.getPlayer());
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.plugin.pTracker.players.remove(event.getPlayer());
	}
	
	private boolean canClimb(Block block) {
		int here = block.getTypeId();
		for(int i = 0; i < this.climbable.length; i++) {
			if (here == this.climbable[i]) {
				return true;
			}
		}
		return false;
	}
	
	// Climb a block
	public void climbBlock(Block b, Player p) {
		if (b.getTypeId() == 0) {
			b = b.getRelative(0, -1, 0);
			if (b.getTypeId() == 0) {
				b = b.getRelative(0, -1, 0);
				if (b.getTypeId() == 0) {
					//if (plugin.useSpout) SpoutManager.getMaterialManager().overrideBlock(b, plugin.grappleBlock);
					b.setTypeId(20);
					this.plugin.pTracker.setPlayerBlock(b, p);
				}
				
				Location loc = new Location(b.getWorld(), b.getX() + 0.5D, b.getY() + 1, b.getZ() + 0.5D);
				loc.setPitch(p.getLocation().getPitch());
				loc.setYaw(p.getLocation().getYaw());
				p.teleport(loc);
			}
		}
	}
	
	// Shows the energy bar to a player
	public static void showEnergy(Player p, int EN) {
		String energyBar = ChatColor.DARK_GREEN + "[";
		for(int i = 1; i <= maxEN / 3; i++) {
			if ((EN / 3) >= i) energyBar += ChatColor.GREEN + "|";
			else energyBar += ChatColor.GRAY + "|";
		}
		energyBar += ChatColor.DARK_GREEN + "]";
		p.sendMessage("Energy: " + energyBar);
	}
}