package net.novauniverse.novagamesdk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.novauniverse.novagamesdk.team.SDKTeam;
import net.novauniverse.novagamesdk.team.SDKTeamManager;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.utils.JSONFileType;
import net.zeeraa.novacore.commons.utils.JSONFileUtils;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerEliminationReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.GameBeginEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.GameEndEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.GameStartEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.GameStartFailureEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.messages.PlayerEliminationMessage;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.messages.TeamEliminationMessage;
import net.zeeraa.novacore.spigot.gameengine.module.modules.gamelobby.GameLobby;
import net.zeeraa.novacore.spigot.gameengine.module.modules.gamelobby.mapselector.selectors.RandomLobbyMapSelector;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;

public class NovaGameSDK extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getDataFolder().mkdir();

		File teamFile = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + File.separator + "teams.json");
		if (!teamFile.exists()) {
			try {
				JSONFileUtils.createEmpty(teamFile, JSONFileType.JSONObject);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		JSONObject teamData;
		try {
			teamData = JSONFileUtils.readJSONObjectFromFile(teamFile);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			return;
		}

		List<Team> teams = new ArrayList<>();
		teamData.keySet().forEach(name -> {
			JSONArray uuids = teamData.getJSONArray(name);
			SDKTeam team = new SDKTeam(name);
			for (int i = 0; i < uuids.length(); i++) {
				team.addPlayer(UUID.fromString(uuids.getString(i)));
			}
			teams.add(team);
		});

		SDKTeamManager teamManager = new SDKTeamManager();
		NovaCore.getInstance().setTeamManager(teamManager);
		teamManager.getTeams().addAll(teams);

		Log.info("GameSDK", teamManager.getTeams().size() + " teams loaded from teams.json");

		Bukkit.getPluginManager().registerEvents(this, this);

		GameManager.getInstance().setTeamEliminationMessage(new TeamEliminationMessage() {
			@Override
			public void showTeamEliminatedMessage(Team team, int placement) {
				log("Team eliminated: " + team.getDisplayName() + ". Placement: " + placement);
			}
		});

		GameManager.getInstance().setPlayerEliminationMessage(new PlayerEliminationMessage() {
			@Override
			public void showPlayerEliminatedMessage(OfflinePlayer player, Entity killer, PlayerEliminationReason reason, int placement) {
				log("Player eliminated: " + player.getName() + ". Killer: " + killer + ". Reason: " + reason.name() + ". Placement: " + placement);
			}
		});

		File gameLobbyDataDirectory = new File(getDataFolder().getAbsolutePath() + File.separator + "lobby_map_data");
		File gameLobbyWorldDirectory = new File(getDataFolder().getAbsolutePath() + File.separator + "lobby_map_worlds");

		gameLobbyDataDirectory.mkdir();
		gameLobbyWorldDirectory.mkdir();

		GameLobby.getInstance().getMapReader().loadAll(gameLobbyDataDirectory, gameLobbyWorldDirectory);
		GameLobby.getInstance().setMapSelector(new RandomLobbyMapSelector());
		
		ModuleManager.enable(GameLobby.class);

		Log.info("GameSDK", "Ready");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Team team = TeamManager.getTeamManager().getPlayerTeam(player);
		if (team == null) {
			log(player.getName() + " joined with no team");
		} else {
			log(player.getName() + " joined with team: " + team.getDisplayName() + ". Color: " + team.getTeamColor() + team.getTeamColor().name());
		}
	}

	public static final void log(String message) {
		Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[GameSDK]: " + ChatColor.RESET + message);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameEnd(GameEndEvent e) {
		log("GameEndEvent. Reason: " + e.getReason());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStart(GameStartEvent e) {
		log("GameStartEvent");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameBegin(GameBeginEvent e) {
		log("GameBeginEvent");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStartFailure(GameStartFailureEvent e) {
		log("GameStartFailureEvent. " + e.getException().getClass().getName() + " " + e.getException().getMessage());
	}
}