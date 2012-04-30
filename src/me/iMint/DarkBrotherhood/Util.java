package me.iMint.DarkBrotherhood;

import org.bukkit.entity.Player;

public class Util {
	
	private static Boolean usePerms = DarkBrotherhood.UsePermissions;
	
	public static boolean hasPermission(String permission, Player player) {
		if (usePerms) {
			return DarkBrotherhood.permission.has(player, permission);
		}
		return player.isOp();
	}
}
