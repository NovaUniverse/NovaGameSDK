package net.novauniverse.novagamesdk.team;

import org.bukkit.entity.Player;

import net.zeeraa.novacore.spigot.teams.TeamManager;

public class SDKTeamManager extends TeamManager {
	@Override
	public boolean requireTeamToJoin(Player player) {
		return true;
	}
}