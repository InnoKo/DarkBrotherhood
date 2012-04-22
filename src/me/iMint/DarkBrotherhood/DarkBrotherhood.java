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
import java.util.Map.Entry;
import java.util.logging.Logger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
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
	public PlayerTracker pTracker = new PlayerTracker(this);
	public EntityListener entityListener;
	public LockpickListener lockListener;
	public PlayerListener playerlistener;
	public static Permission permission;
	
	// File fields
	public static final String mainDirectory = "plugins/Darkbrotherhood";
	public static FileConfiguration config;
	public static File manaData;
	public File configFile;
	
	/** 
	// Spout fields
	public Shuriken shuriken = new Shuriken(this); // "http://i.imgur.com/0E0t3.png");
	public GrappleBlock grappleBlock;
	public ItemStack shurikenStack = new SpoutItemStack(shuriken, 4);
	public Boolean useSpout;
	private final String shurikenURL = "http://i.imgur.com/0E0t3.png";
	**/
	
	// Other fields
	public static HashMap<Player, Integer> mana = new HashMap<Player, Integer>();
	
	// Disabling
	@Override
	public void onDisable() {
		saveData();
		log.info("DarkBrotherhood is now Disabled!");
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
		
		// Events
		PluginManager pm = getServer().getPluginManager();
		entityListener = new EntityListener(this);
		lockListener = new LockpickListener(this);
		playerlistener = new PlayerListener(this);
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
		loadConfig();
		loadData();
		loadProcedure();
		setupPermissions();
		
		// Enabling Message
		PluginDescriptionFile pdf = this.getDescription();
		log.info(pdf.getName() + " " + pdf.getVersion() + " is now Enabled!");
	}
	
	// Loads the Config
	private void loadConfig() {
		config.addDefault("usePermissions", "false");
		config.addDefault("climbableBlocks", "4,5,43,44,45,47,48,85");
		config.addDefault("Multiplier", "2");
		config.addDefault("LockPickId", "287");
		config.addDefault("LockPickChance", "25");
		config.addDefault("FailedLockpickDamage", "5");
		config.addDefault("PoisonDuration", "5");
		config.addDefault("PoisonDamage", "1");
		config.addDefault("PoisonItemID", "40");
		config.addDefault("L.O.F.-Chance", "50");
		config.addDefault("L.O.F.-success-message", "You successfully rolled to avoid fall damage!");
		config.addDefault("L.O.F.-fail-message", "You failed to roll, badly injuring yourself!");
		config.addDefault("ShurikenItemID", "318");
		config.addDefault("ShurikenDamage", "2");
		config.addDefault("MaximumEnergy", "100");
		config.addDefault("EnergyRestoreTick", "5");
		config.addDefault("EnergyRestoreAmount", "5");
		config.addDefault("AssassinateEnergyUsage", "60");
		config.addDefault("L.O.F.-EnergyUsage", "40");
		config.addDefault("ClimbingEnergyUsage", "5");
		config.addDefault("InvisibilityEnergyUsage", "20");
		config.options().copyDefaults(true);
		saveConfig();
	}
	
	// Reloads the config via /darkbrotherhood reload
	private void reLoadConfig() {
		try {
			config.load(configFile);
			saveConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Loads config values into memory
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadProcedure() {
		// Scheduler
		BukkitScheduler bs = getServer().getScheduler();
		bs.scheduleAsyncRepeatingTask(this, this.pTracker, 1L, 20L);
		
		// Permission Usage
		boolean b = config.getBoolean("usePermissions");
		entityListener.setPermissionUsage(b);
		playerlistener.setPermissionsUsage(b);
		lockListener.setPermissionUsage(b);
		
		// Climbable Blocks
		String s = config.getString("climbableBlocks");
		String[] climbablesString = s.split(",");
		ArrayList climbables = new ArrayList();
		for(int i = 0; i < climbablesString.length; i++) {
			climbables.add(Integer.valueOf(Integer.parseInt(climbablesString[i])));
		}
		int[] finished = new int[climbables.size()];
		for(int i = 0; i < climbables.size(); i++) {
			Integer current = (Integer) climbables.get(i);
			finished[i] = current.intValue();
		}
		this.playerlistener.setClimbable(finished);
		
		// Load Config Values
		this.entityListener.setMultiplier(config.getInt("Multiplier"));
		this.lockListener.setFailedDamage(config.getInt("failedLockpickDamage"));
		this.lockListener.setLockPickId(config.getInt("LockPickId"));
		this.lockListener.setLockPickChance(config.getInt("LockPickChance"));
		this.pTracker.setDuration(config.getInt("poisonDuration"));
		this.playerlistener.setPoisonItem(config.getInt("poisonItem"));
		this.pTracker.setPoisonDamage(config.getInt("poisonDamage"));
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
	}
	
	// Load data files
	public void loadData() {
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
						reLoadConfig();
						sender.sendMessage("DarkBrotherhood reloaded.");
						return true;
					} else {
						Player player = (Player) sender;
						if (config.getBoolean("usePermissions") && !permission.has(player, "darkbrotherhood.reload")) {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						} else {
							reLoadConfig();
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
			}
		}
		return false;
	}
}