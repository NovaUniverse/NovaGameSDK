package net.novauniverse.novagamesdk.team;

import net.md_5.bungee.api.ChatColor;
import net.zeeraa.novacore.spigot.teams.Team;

public class SDKTeam extends Team {
	private String name;

	public SDKTeam(String name) {
		this.name = name;
	}

	@Override
	public ChatColor getTeamColor() {
		return ChatColor.GREEN;
	}

	@Override
	public String getDisplayName() {
		return name;
	}
}