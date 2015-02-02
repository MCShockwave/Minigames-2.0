package net.mcshockwave.Minigames.Games;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.mcshockwave.MCS.Currency.PointsUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;

public class Target implements IMinigame {
	public String yellowTarget;
	public String greenTarget;

	@Override
	public void onGameStart() {
		for (GameTeam gt : Game.Target.teams) {
			selectTarget(gt);
		}
	}

	@Override
	public void onGameEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if(yellowTarget == e.p.getName() || greenTarget == e.p.getName()) {
			Minigames.broadcastDeath(e.p,e.k, "%s was killed by an unknown cause", "%s was brutally murdered by %s");
			PointsUtils.addPoints(e.k, 50, "§7Killing the §a§otarget§7!");
			Minigames.setDead(e.p, false);
		} else {
			giveKit(e.p);
		}
		if(e.p.getHealth() == 0) {
			if(yellowTarget == e.k.getName() || greenTarget == e.k.getName()) {
				if(e.k.getMaxHealth() < 20)
					e.k.setMaxHealth(e.k.getMaxHealth() + 2);
			}
		}
	}
	
	public void giveKit(Player p) {
		Minigames.clearInv(p);
		p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
	}
	
	
	public void selectTarget(GameTeam gt) {
		if (gt.team.getSize() > 0) {
			Player[] ps = gt.team.getPlayers().toArray(new Player[0]);
			Player p = ps[rand.nextInt(ps.length)];	
			p.setMaxHealth(10);
			p.setHealth(10);
			p.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
			Minigames.broadcast(gt.color, gt.color + p.getName() + " is %s's new target protect them, %s team.", gt.name);
			if(gt.name.equalsIgnoreCase("Yellow")) {
				yellowTarget = p.getName();
			} else {
				greenTarget = p.getName();
			}
		} 
	}
	
}
