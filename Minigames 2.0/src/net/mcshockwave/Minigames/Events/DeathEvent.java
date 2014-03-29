package net.mcshockwave.Minigames.Events;

import net.mcshockwave.Minigames.Game.GameTeam;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scoreboard.Team;

public class DeathEvent {

	public Player		p;
	public Player		k;
	public DamageCause	dc;
	public Team			t	= null;
	public GameTeam		gt	= null;

	public DeathEvent(Player p, Player k, DamageCause dc, Team t, GameTeam gt) {
		this.p = p;
		this.k = k;
		this.dc = dc;
		this.t = t;
		this.gt = gt;
	}

	public DeathEvent(Player p, Player k, DamageCause dc) {
		this.p = p;
		this.k = k;
		this.dc = dc;
		t = null;
		gt = null;
	}

	public String getDamageCause() {
		return dc.name().toLowerCase().replace('_', ' ');
	}

}
