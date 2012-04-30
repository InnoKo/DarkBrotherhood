/**
 * FONT: purisa 11
 */
package me.iMint.DarkBrotherhood;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DarkBrotherhood extends JavaPlugin {
	
	// Utility fields
	public static final Logger log = Logger.getLogger("Minecraft");
	public static Permission permission;
	public PlayerTracker pTracker;
	public EntityListener entityListener;
	public LockListener lockListener;
	public PlayerListener playerlistener;
	
	// File fields
	public static final String mainDirectory = "plugins/DarkBrotherhood";
	public static FileConfiguration config;
	public static File chestData;
	public static File manaData;
	public File configFile;
	
	// Config fields
	public static boolean UsePermissions;
	public static List<Integer> ClimbableBlocks;
	public static int Multiplier;
	public static int EnergyItem;
	public static int LockpickItem;
	public static int LockItem;
	public static int PoisonItem;
	public static int ShurikenItem;
	public static int LockpickSuccessChance;
	public static int LockpickFailureDamage;
	public static int PoisonDuration;
	public static int PoisonDamage;
	public static int ShurikenDamage;
	public static int InvisibilityMaximumLightLevel;
	public static int InvisibilityDistance;
	public static int LeapOfFaithSuccessChance;
	public static String LeapOfFaithSuccessMessage;
	public static String LeapOfFaithFailureMessage;
	public static int MaximumEnergy;
	public static int EnergyRestoreTick;
	public static int EnergyRestoreAmount;
	public static int EnergyItemRestoreAmount;
	public static int AssassinationEnergyUsage;
	public static int LeapOfFaithEnergyUsage;
	public static int ClimbingEnergyUsage;
	public static int InvisibilityEnergyUsage;
	
	/** 
	// Spout fields
	public Shuriken shuriken = new Shuriken(this); // "http://i.imgur.com/0E0t3.png");
	public GrappleBlock grappleBlock;
	public ItemStack shurikenStack = new SpoutItemStack(shuriken, 4);
	public Boolean useSpout;
	private final String shurikenURL = "http://i.imgur.com/0E0t3.png";
	**/
	
	// Other fields
	public static HashMap<Player, Bounty> bounties = new HashMap<Player, Bounty>();
	public static HashMap<Player, Integer> mana = new HashMap<Player, Integer>();
	public static HashMap<Player, Integer> taskIDs = new HashMap<Player, Integer>();
	public static HashMap<Block, String> locked = new HashMap<Block, String>();
	public static List<Player> hidden = new ArrayList<Player>();
	
	// Disabling
	@Override
	public void onDisable() {
		saveData();
		log.info("DarkBrotherhood is now disabled!");
	}
	
	// Enabling
	@Override
	public void onEnable() {
		// Config
		configFile = new File(mainDirectory, "config.yml");
		if (!configFile.exists()) {
			(new File(mainDirectory)).mkdir();
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			copy(getResource("config.yml"), configFile);
		}
		config = this.getConfig();
		loadConfig();
		
		// Events
		PluginManager pm = getServer().getPluginManager();
		entityListener = new EntityListener(this);
		lockListener = new LockListener(this);
		playerlistener = new PlayerListener(this);
		pTracker = new PlayerTracker(this);
		pm.registerEvents(entityListener, this);
		pm.registerEvents(lockListener, this);
		pm.registerEvents(playerlistener, this);
		
		/**
		// Spout
		useSpout = getConfig().getBoolean("spout-enabled");
		if (useSpout) {
			SpoutShapedRecipe shurikenRecipe = new SpoutShapedRecipe(shurikenStack);
			shurikenRecipe.shape(" A ", "ABA", " A ");
			shurikenRecipe.setIngredient('A', MaterialData.flint);
			shurikenRecipe.setIngredient('B', MaterialData.ironIngot);
			SpoutManager.getMaterialManager().registerSpoutRecipe(shurikenRecipe);
			grappleBlock = new GrappleBlock(this);
			SpoutManager.getFileManager().addToPreLoginCache(this, shurikenURL);
		}
		**/
		
		// Load Everything
		loadData();
		loadProcedure();
		setupPermissions();
		
		// Scheduler
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
			
			public void run() {
				Iterator<Player> it = hidden.iterator();
				while (it.hasNext()) {
					Player sneaker = it.next();
					Player[] viewers = getServer().getOnlinePlayers();
					for(int i = 0; i < viewers.length; i++) {
						Player viewer = viewers[i];
						Location sneak = sneaker.getLocation();
						Location view = viewer.getLocation();
						// If the viewer is too close to the sneaker, he can be seen!
						if (view.distance(sneak) < InvisibilityDistance) {
							viewer.showPlayer(sneaker);
						}
					}
				}
			}
		}, 0L, 80L);
		
		// If there are already players online (i.e. from a server reload) load energy for them
		Player[] players = getServer().getOnlinePlayers();
		for(int i = 0; i < players.length; i++) {
			PlayerListener.startEnergy(players[i]);
		}
		
		// Enabling Message
		PluginDescriptionFile pdf = this.getDescription();
		log.info(pdf.getName() + " " + pdf.getVersion() + " by iMint is now enabled!");
	}
	
	private void loadConfig() {
		config.addDefault("General Settings.Use Permissions", false);
		config.addDefault("General Settings.Climbable Blocks", "[4,5,24,43,44,45,47,48,85,101]");
		config.addDefault("General Settings.Multiplier", 2);
		config.addDefault("Item Settings.Energy Item", 353);
		config.addDefault("Item Settings.Lockpick Item", 287);
		config.addDefault("Item Settings.Lock Item", 77);
		config.addDefault("Item Settings.Poison Item", 40);
		config.addDefault("Item Settings.Shuriken Item", 318);
		config.addDefault("Stat Settings.Lockpick Success Chance", 25);
		config.addDefault("Stat Settings.Lockpick Failure Damage", 5);
		config.addDefault("Stat Settings.Poison Duration", 5);
		config.addDefault("Stat Settings.Poison Damage", 1);
		config.addDefault("Stat Settings.Shuriken Damage", 2);
		config.addDefault("Stat Settings.Invisibility Maximum Light Level", 14);
		config.addDefault("Stat Settings.Invisibility Distance", 1);
		config.addDefault("Leap of Faith Settings.Success Chance", 50);
		config.addDefault("Leap of Faith Settings.Success Message", "You successfully rolled to avoid fall damage!");
		config.addDefault("Leap of Faith Settings.Failure Message", "You failed to roll, badly injuring yourself!");
		config.addDefault("Energy Settings.Maximum Energy", 100);
		config.addDefault("Energy Settings.Energy Restore Tick", 2);
		config.addDefault("Energy Settings.Energy Restore Amount", 10);
		config.addDefault("Energy Settings.Energy Item Restore Amount", 15);
		config.addDefault("Energy Settings.Assassination Energy Usage", 40);
		config.addDefault("Energy Settings.Leap of Faith Energy Usage", 40);
		config.addDefault("Energy Settings.Climbing Energy Usage", 15);
		config.addDefault("Energy Settings.Invisibility Energy Usage", 20);
		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveConfig();
		//█████████████████████████████████████████████████████████████████████████████████████████████████████████████████
		UsePermissions = getConfig().getBoolean("General Settings.Use Permissions");
		ClimbableBlocks = getConfig().getIntegerList("General Settings.Climbable Blocks");
		Multiplier = getConfig().getInt("General Settings.Multiplier");
		EnergyItem = getConfig().getInt("Item Settings.Energy Item");
		LockpickItem = getConfig().getInt("Item Settings.Lockpick Item");
		LockItem = getConfig().getInt("Item Settings.Lock Item");
		PoisonItem = getConfig().getInt("Item Settings.Poison Item");
		ShurikenItem = getConfig().getInt("Item Settings.Shuriken Item");
		LockpickSuccessChance = getConfig().getInt("Stat Settings.Lockpick Success Chance");
		LockpickFailureDamage = getConfig().getInt("Stat Settings.Lockpick Failure Damage");
		PoisonDuration = 5;//getConfig().getInt("Stat Settings.Poison Duration");
		PoisonDamage = getConfig().getInt("Stat Settings.Poison Damage");
		ShurikenDamage = getConfig().getInt("Stat Settings.Shuriken Damage");
		InvisibilityMaximumLightLevel = getConfig().getInt("Stat Settings.Invisibility Maximum Light Level");
		InvisibilityDistance = getConfig().getInt("Stat Settings.Invisibility Distance");
		LeapOfFaithSuccessChance = getConfig().getInt("Leap of Faith Settings.Success Chance");
		LeapOfFaithSuccessMessage = getConfig().getString("Leap of Faith Settings.Success Message");
		LeapOfFaithFailureMessage = getConfig().getString("Leap of Faith Settings.Failure Message");
		MaximumEnergy = getConfig().getInt("Energy Settings.Maximum Energy");
		EnergyRestoreTick = getConfig().getInt("Energy Settings.Energy Restore Tick");
		EnergyRestoreAmount = getConfig().getInt("Energy Settings.Energy Restore Amount");
		EnergyItemRestoreAmount = getConfig().getInt("Energy Settings.Energy Item Restore Amount");
		AssassinationEnergyUsage = getConfig().getInt("Energy Settings.Assassination Energy Usage");
		LeapOfFaithEnergyUsage = getConfig().getInt("Energy Settings.Leap of Faith Energy Usage");
		ClimbingEnergyUsage = getConfig().getInt("Energy Settings.Climbing Energy Usage");
		InvisibilityEnergyUsage = getConfig().getInt("Energy Settings.Invisibility Energy Usage");
	}
	
	// Loads schedulers
	private void loadProcedure() {
		// Scheduler
		BukkitScheduler bs = getServer().getScheduler();
		bs.scheduleAsyncRepeatingTask(this, this.pTracker, 1L, 20L);
	}
	
	// Set up Vault Permissions
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
	
	// Copy the default config.yml
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Save data files
	public void saveData() {
		// Save Energy
		try {
			FileOutputStream os = new FileOutputStream(manaData);
			PrintStream printer = new PrintStream(os);
			for(Entry<Player, Integer> entry : mana.entrySet()) {
				Player player = entry.getKey();
				int mana = entry.getValue();
				String paper = player.getName() + ":" + mana;
				printer.println(paper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Save Locked Chests
		try {
			FileOutputStream os = new FileOutputStream(chestData);
			PrintStream printer = new PrintStream(os);
			for(Entry<Block, String> entry : locked.entrySet()) {
				Block block = entry.getKey();
				String player = entry.getValue();
				String w = block.getWorld().getName();
				int x = block.getX();
				int y = block.getY();
				int z = block.getZ();
				String paper = w + "," + x + "," + y + "," + z + ":" + player;
				printer.println(paper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Load data files
	public void loadData() {
		// Load Energy
		try {
			manaData = new File(mainDirectory + "/mana.dat");
			if (!manaData.exists()) {
				(new File(mainDirectory)).mkdir();
				manaData.createNewFile();
			}
			FileInputStream in = new FileInputStream(manaData);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(in)));
			String s;
			while ((s = reader.readLine()) != null) {
				String temp[] = s.split(":");
				Player player = getServer().getPlayer(temp[0]);
				Integer mana = Integer.parseInt(temp[1]);
				DarkBrotherhood.mana.put(player, mana);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Load Locked Chests
		try {
			chestData = new File(mainDirectory + "/chests.dat");
			if (!chestData.exists()) {
				(new File(mainDirectory)).mkdir();
				chestData.createNewFile();
			}
			FileInputStream in = new FileInputStream(chestData);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(in)));
			String s;
			while ((s = reader.readLine()) != null) {
				String temp[] = s.split(":");
				String _coords = temp[0];
				String[] coords = _coords.split(",");
				int x = Integer.parseInt(coords[1]);
				int y = Integer.parseInt(coords[2]);
				int z = Integer.parseInt(coords[3]);
				World world = getServer().getWorld(coords[0]);
				Block block = world.getBlockAt(x, y, z);
				String player = temp[1];
				DarkBrotherhood.locked.put(block, player);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("darkbrotherhood") || label.equalsIgnoreCase("db")) {
			// Show Command Usage
			if (args.length == 0) {
				sender.sendMessage("/darkbrotherhood reload");
				return true;
			}
			
			// Commands
			if (args.length == 1) {
				
				// Reload Config
				if (args[0].equalsIgnoreCase("reload")) {
					// If the command was from the console
					if (!(sender instanceof Player)) {
						reloadConfig();
						loadConfig();
						sender.sendMessage("DarkBrotherhood reloaded.");
						return true;
					} else {
						Player player = (Player) sender;
						if (config.getBoolean("usePermissions") && !permission.has(player, "darkbrotherhood.reload")) {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						} else {
							reloadConfig();
							loadConfig();
							sender.sendMessage(ChatColor.GREEN + "DarkBrotherhood reloaded.");
							return true;
						}
					}
				}
				
				// Show Energy Bar
				if (args[0].equalsIgnoreCase("energy")) {
					if (!(sender instanceof Player)) return true;
					Player player = (Player) sender;
					int energy = DarkBrotherhood.mana.get(player);
					PlayerListener.showEnergy(player, energy);
					return true;
				}
				
				// Place Bounty
				if (args[0].equalsIgnoreCase("bounty")) {
					if (!(sender instanceof Player)) return true;
					Player player = (Player) sender;
					Player target = getServer().getPlayer(args[1]);
					Double reward = Double.parseDouble(args[2]);
					if (args.length < 2) {
						player.sendMessage(ChatColor.RED + "You must specify a player to put your bounty on!");
						return true;
					}
					if (args.length < 3) {
						player.sendMessage(ChatColor.RED + "You must specify a bounty reward!");
						return true;
					}
					if (target == null) {
						player.sendMessage(ChatColor.RED + "That player doesn't exist or is not online!");
						return true;
					}
					if (bounties.containsKey(target)) {
						Bounty bounty = bounties.get(target);
						if (bounty.getBounties().containsKey(player)) {
							Double oldReward = bounty.getBounties().get(player);
							Double newReward = oldReward + reward;
							bounty.placeBounty(player, newReward);
							if (newReward >= oldReward) player.sendMessage(ChatColor.GOLD + "You have increased your bounty on this player to " + newReward + "!");
							else player.sendMessage(ChatColor.GOLD + "You have decreased your bounty on this player to " + newReward + ".");
							return true;
						} else {
							bounty.placeBounty(player, reward);
							player.sendMessage(ChatColor.GOLD + "You have placed a bounty of " + reward + " on this player!");
							return true;
						}
					} else {
						Bounty bounty = new Bounty(target);
						bounties.put(target, bounty);
						bounty.placeBounty(player, reward);
						player.sendMessage(ChatColor.GOLD + "You have placed a bounty of " + reward + " on this player!");
						return true;
					}
				}
			}
		}
		return false;
	}
}