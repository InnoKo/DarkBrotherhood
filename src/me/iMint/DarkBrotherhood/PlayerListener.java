package me.iMint.DarkBrotherhood;

import java.util.List;
import net.minecraft.server.Material;
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
	private static DarkBrotherhood db;
	private final boolean usePerms;
	private final List<Integer> climbables;
	private final int energyItem;
	private final int poisonItem;
	private final int shurikenItem;
	private final int maxLight;
	private static int addEN;
	private static int climbEN;
	private static int healEN;
	private static int maxEN;
	private static int sneakEN;
	private static long tickEN;
	
	// Constructor
	PlayerListener (DarkBrotherhood db) {
		this.plugin = db;
		PlayerListener.db = db;
		usePerms = DarkBrotherhood.UsePermissions;
		climbables = DarkBrotherhood.ClimbableBlocks;
		energyItem = DarkBrotherhood.EnergyItem;
		poisonItem = DarkBrotherhood.PoisonItem;
		shurikenItem = DarkBrotherhood.ShurikenItem;
		maxLight = DarkBrotherhood.InvisibilityMaximumLightLevel;
		addEN = DarkBrotherhood.EnergyRestoreAmount;
		climbEN = DarkBrotherhood.ClimbingEnergyUsage;
		healEN = DarkBrotherhood.EnergyItemRestoreAmount;
		maxEN = DarkBrotherhood.MaximumEnergy;
		sneakEN = DarkBrotherhood.InvisibilityEnergyUsage;
		tickEN = DarkBrotherhood.EnergyRestoreTick * 20;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Variables
		Player player = event.getPlayer();
		Action action = event.getAction();
		
		// Shuriken throwing
		if (action.equals(Action.LEFT_CLICK_AIR) && player.getItemInHand().getTypeId() == shurikenItem && checkForPermission("DarkBrotherhood.use.shuriken", player)) {
			ItemStack item = event.getItem();
			/**if (plugin.useSpout) {
				SpoutItemStack sItem = (SpoutItemStack) event.getItem();
			n n				if (sItem.getMaterial() != plugin.shuriken) return;
			}**/
			Boolean critical = false;
			if (player.getWorld().getBlockAt(player.getLocation().subtract(0D, 1D, 0D)).getType().equals(Material.AIR)) {
				critical = true;
			}
			Arrow shuriken = player.launchProjectile(Arrow.class);
			//SpoutItemStack is = (SpoutItemStack) player.getItemInHand();
			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
			} else {
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
			}
			this.plugin.entityListener.shuriken.put(shuriken, critical);
		}
		
		// Climbing blocks
		if ((action == Action.RIGHT_CLICK_BLOCK) && (player.getItemInHand().getTypeId() == 0)) {
			Block block = event.getClickedBlock();
			World w = block.getWorld();
			if (checkForPermission("darkbrotherhood.climb", player)) {
				int energy = DarkBrotherhood.mana.get(player);
				if (energy < climbEN) {
					player.sendMessage(ChatColor.RED + "You don't have enough energy to climb!");
					return;
				}
				if (canClimb(block)) {
					if (event.getBlockFace() == BlockFace.DOWN) {
						Block playerBlock = w.getBlockAt(block.getX(), block.getY() - 1, block.getZ());
						climbBlock(playerBlock, player);
					} else if (event.getBlockFace() == BlockFace.UP) {
						Block playerBlock = block.getRelative(0, 2, 0);
						climbBlock(playerBlock, player);
					} else {
						int bx = block.getX();
						int bz = block.getZ();
						int px = player.getLocation().getBlockX();
						int pz = player.getLocation().getBlockZ();
						int xdifference = px - bx;
						int zdifference = pz - bz;
						if (Math.abs(xdifference) > Math.abs(zdifference)) {
							if (xdifference > 0) {
								climbBlock(block.getRelative(1, 0, 0), player);
							} else {
								climbBlock(block.getRelative(-1, 0, 0), player);
							}
							
						} else if (zdifference > 0) {
							climbBlock(block.getRelative(0, 0, 1), player);
						} else {
							climbBlock(block.getRelative(0, 0, -1), player);
						}
					}
					DarkBrotherhood.mana.put(player, energy - climbEN);
				}
				
			}
			
		}
		
		// Poisoning weapons
		if ((action.equals(Action.RIGHT_CLICK_AIR)) && (player.getItemInHand().getTypeId() == poisonItem) && (checkForPermission("DarkBrotherhood.use.poison", player))) {
			if (plugin.entityListener.hasPoison.contains(player)) {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Your weapon is already poisoned!");
			} else {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "You poison your weapon!");
				this.plugin.entityListener.hasPoison.add(player);
				ItemStack is = player.getItemInHand();
				if (is.getAmount() > 1) {
					is.setAmount(is.getAmount() - 1);
				} else {
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
			}
			return;
		}
		
		// Energizing
		if ((action.equals(Action.RIGHT_CLICK_AIR) && (player.getItemInHand().getTypeId() == energyItem))) {
			int energy = DarkBrotherhood.mana.get(player);
			int newEnergy = energy + healEN;
			if (newEnergy > maxEN) newEnergy = maxEN;
			DarkBrotherhood.mana.put(player, newEnergy);
			showEnergy(player, newEnergy);
			player.sendMessage("You recovered " + (newEnergy - energy) + " energy!");
			ItemStack i = player.getItemInHand();
			if (i.getAmount() > 1) {
				i.setAmount(i.getAmount() - 1);
			} else {
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
			}
			return;
		}
	}
	
	// Invisible Sneaking
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player sneaker = event.getPlayer();
		Boolean sneaking = event.isSneaking();
		Player[] players = plugin.getServer().getOnlinePlayers();
		if (sneaker.isFlying()) return;
		if (sneaking && checkForPermission("darkbrotherhood.invisibility", sneaker)) {
			int energy = DarkBrotherhood.mana.get(sneaker);
			if (energy >= sneakEN) {
				DarkBrotherhood.mana.put(sneaker, energy - sneakEN);
				if (sneaker.getLocation().getBlock().getLightLevel() <= maxLight) {
					DarkBrotherhood.hidden.add(sneaker);
					sneaker.sendMessage(ChatColor.GOLD + "You are now hidden in the shadows!");
					for(int i = 0; i < players.length; i++) {
						Player viewer = players[i];
						if (!checkForPermission("darkbrotherhood.seeinvisibleplayers", viewer)) {
							viewer.hidePlayer(sneaker);
						}
					}
				} else {
					sneaker.sendMessage(ChatColor.RED + "You can only hide in dark shadows!");
				}
			} else {
				sneaker.sendMessage(ChatColor.RED + "Not enough energy to go invisible!");
			}
			// Player stopped sneaking	
		} else if (!sneaking && DarkBrotherhood.hidden.contains(sneaker)) {
			DarkBrotherhood.hidden.remove(sneaker);
			sneaker.sendMessage(ChatColor.GRAY + "You are no longer hidden in the shadows!");
			for(int i = 0; i < players.length; i++) {
				Player viewer = players[i];
				viewer.showPlayer(sneaker);
			}
		}
	}
	
	// Starts the Energy schedulers
	@EventHandler
	public void startEnergy(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		startEnergy(player);
	}
	
	public static void startEnergy(final Player player) {
		if (!DarkBrotherhood.mana.containsKey(player)) DarkBrotherhood.mana.put(player, maxEN);
		// Restores Energy every (tickEN) seconds, double restoration if the player has full health
		db.getServer().getScheduler().scheduleAsyncRepeatingTask(db, new Runnable(){
			
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
	
	/** Commented out - this was my attempt to make door opening silent
	@EventHandler
	public void onOpenDoor(PlayerInteractEvent event) {
		Action action = event.getAction();
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		if ((action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_BLOCK)) && block instanceof Door && checkForPermission("darkbrotherhood.silent", player)) {
			Door door = (Door) block;
			event.setCancelled(true);
			Boolean isOpen = door.isOpen();
			door.setOpen(!isOpen);
			System.out.println("Door change");
		}
	}
	*/
	
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
		return climbables.contains(block.getTypeId());
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
		String energyBar = ChatColor.GOLD + "[";
		for(int i = 1; i <= maxEN / 3; i++) {
			if ((EN / 3) >= i) energyBar += ChatColor.YELLOW + "|";
			else energyBar += ChatColor.GRAY + "|";
		}
		energyBar += ChatColor.GOLD + "]";
		p.sendMessage("Energy: " + energyBar);
	}
}