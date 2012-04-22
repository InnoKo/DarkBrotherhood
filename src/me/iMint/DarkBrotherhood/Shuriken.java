package me.iMint.DarkBrotherhood;

import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.material.item.GenericCustomItem;

public class Shuriken extends GenericCustomItem {

	public Shuriken(Plugin plugin){
		super(plugin, "Shuriken", "http://i.imgur.com/0E0t3.png");
	}

	public Shuriken(Plugin plugin, String name, String texture){
		super(plugin, name, texture);
		// super(plugin, "Shuriken", "http://i.imgur.com/0E0t3.png");
	}

}
