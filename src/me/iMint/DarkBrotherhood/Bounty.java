package me.iMint.DarkBrotherhood;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Bounty {
	
	private HashMap<Player, Double> bounties;
	private Double reward;
	private final Player target;
	private int bountyAmount;
	private boolean mostWanted;
	
	public Bounty (Player player) {
		this.target = player;
		this.reward = 0D;
		this.bountyAmount = 0;
	}
	
	public void placeBounty(Player player, Double reward) {
		bounties.put(player, reward);
		this.reward += reward;
		Bukkit.getServer().broadcastMessage(ChatColor.GOLD + target.getName() + " now has a bounty of " + reward + "!");
		bountyAmount = bounties.size();
		if (bountyAmount >= 5) {
			String dname = target.getDisplayName();
			target.setDisplayName(ChatColor.DARK_RED + "[Most Wanted] " + dname);
			mostWanted = true;
		}
	}
	
	public Player getTarget() {
		return target;
	}
	
	public Double getReward() {
		return reward;
	}
	
	public HashMap<Player, Double> getBounties() {
		return bounties;
	}
}
