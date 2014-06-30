package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Tiers implements IMinigame {

	private static final int			KILLS_NEEDED	= 1;

	public HashMap<GameTeam, GameScore>	tiers			= new HashMap<>();
	public HashMap<GameTeam, Integer>	buffer			= new HashMap<>();

	@Override
	public void onGameStart() {
		for (GameTeam gt : Game.Tiers.teams) {
			tiers.put(gt, Sidebar.getNewScore(gt.color + gt.name, 1));
			buffer.put(gt, 0);
			giveKit(gt);
		}
	}

	public void giveKit(GameTeam t) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (Game.getTeam(p) == t) {
				giveKit(p);
			}
		}
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		Minigames.milkPlayer(p);
		GameTeam gt = Game.getTeam(p);
		if (gt == null) {
			return;
		}

		Tier t = Tier.getFromId(tiers.get(gt).getVal() - 1);
		p.getInventory().setArmorContents(t.acons);
		for (ItemStack it : t.cons) {
			p.getInventory().addItem(it);
		}
	}

	@Override
	public void onGameEnd() {
		buffer.clear();
		tiers.clear();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		GameTeam gt = e.gt;

		if (gt != null) {
			Tier t = Tier.getFromId(tiers.get(gt).getVal() - 1);

			GameTeam kgt = Game.getTeam(e.k);

			Minigames.broadcastDeath(p, e.k, "%s [" + t.getId() + "] was killed", "%s [" + t.getId()
					+ "] was killed by %s [" + Tier.getFromId(tiers.get(kgt).getVal() - 1).getId() + "]");

			if (e.k != null) {
				addKill(kgt);
			}
		}

		giveKit(p);
	}

	public void addKill(GameTeam gt) {
		int bf = buffer.get(gt);
		bf++;
		if (bf >= KILLS_NEEDED) {
			bf = 0;

			int ti = tiers.get(gt).getVal();
			ti++;
			tiers.get(gt).setVal(ti);
			if (ti >= Tier.values().length) {
				Minigames.stop(gt.team);
				return;
			}

			Minigames.broadcast(gt.color, "Team %s was upgraded to Tier %s", gt.name, Tier.getFromId(ti).getId());
			giveKit(gt);
		}
		buffer.remove(gt);
		buffer.put(gt, bf);
	}

	public enum Tier {
		t0(
			Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.DIAMOND_BOOTS,
			Material.DIAMOND_SWORD,
			Enchantment.PROTECTION_ENVIRONMENTAL,
			3,
			Enchantment.DAMAGE_ALL,
			4),
		t1(
			Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.DIAMOND_BOOTS,
			Material.DIAMOND_SWORD,
			Enchantment.PROTECTION_ENVIRONMENTAL,
			1,
			Enchantment.DAMAGE_ALL,
			3),
		t2(
			Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.DIAMOND_BOOTS,
			Material.DIAMOND_SWORD,
			null,
			0,
			Enchantment.DAMAGE_ALL,
			1),
		t3(
			Material.IRON_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.IRON_BOOTS,
			Material.DIAMOND_SWORD,
			null,
			0,
			null,
			0),
		t4(
			Material.IRON_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.IRON_LEGGINGS,
			Material.IRON_BOOTS,
			Material.IRON_SWORD,
			null,
			0,
			null,
			0),
		t5(
			Material.IRON_HELMET,
			Material.IRON_CHESTPLATE,
			Material.IRON_LEGGINGS,
			Material.IRON_BOOTS,
			Material.IRON_SWORD,
			null,
			0,
			null,
			0),
		t6(
			Material.CHAINMAIL_HELMET,
			Material.IRON_CHESTPLATE,
			Material.IRON_LEGGINGS,
			Material.CHAINMAIL_BOOTS,
			Material.IRON_SWORD,
			null,
			0,
			null,
			0),
		t7(
			Material.CHAINMAIL_HELMET,
			Material.IRON_CHESTPLATE,
			Material.CHAINMAIL_LEGGINGS,
			Material.CHAINMAIL_BOOTS,
			Material.IRON_SWORD,
			null,
			0,
			null,
			0),
		t8(
			Material.CHAINMAIL_HELMET,
			Material.CHAINMAIL_CHESTPLATE,
			Material.CHAINMAIL_LEGGINGS,
			Material.CHAINMAIL_BOOTS,
			Material.STONE_SWORD,
			null,
			0,
			null,
			0),
		t9(
			Material.LEATHER_HELMET,
			Material.CHAINMAIL_CHESTPLATE,
			Material.CHAINMAIL_LEGGINGS,
			Material.LEATHER_BOOTS,
			Material.STONE_SWORD,
			null,
			0,
			null,
			0),
		t10(
			Material.LEATHER_HELMET,
			Material.CHAINMAIL_CHESTPLATE,
			Material.LEATHER_LEGGINGS,
			Material.LEATHER_BOOTS,
			Material.STONE_SWORD,
			null,
			0,
			null,
			0),
		t11(
			Material.LEATHER_HELMET,
			Material.LEATHER_CHESTPLATE,
			Material.LEATHER_LEGGINGS,
			Material.LEATHER_BOOTS,
			Material.WOOD_SWORD,
			null,
			0,
			null,
			0),
		t12(
			Material.AIR,
			Material.LEATHER_CHESTPLATE,
			Material.LEATHER_LEGGINGS,
			Material.AIR,
			Material.WOOD_SWORD,
			null,
			0,
			null,
			0),
		t13(
			Material.AIR,
			Material.LEATHER_CHESTPLATE,
			Material.AIR,
			Material.AIR,
			Material.WOOD_SWORD,
			null,
			0,
			null,
			0),
		t14(
			Material.AIR,
			Material.AIR,
			Material.AIR,
			Material.AIR,
			Material.WOOD_SWORD,
			null,
			0,
			null,
			0), ;

		public ItemStack[]	acons	= new ItemStack[4];
		public ItemStack[]	cons	= new ItemStack[] {};

		private Tier(Material h, Material c, Material l, Material b, Material s, Enchantment aenc, int aencl,
				Enchantment senc, int sencl) {
			acons = new ItemStack[] { i(b, aenc, aencl), i(l, aenc, aencl), i(c, aenc, aencl), i(h, aenc, aencl) };
			cons = new ItemStack[] { i(s, senc, sencl) };
		}

		public static Tier getFromId(int id) {
			return values()[id];
		}

		public int getId() {
			return ordinal() + 1;
		}
	}

	private static ItemStack i(Material m, Enchantment e, int l) {
		ItemStack it = new ItemStack(m);
		if (e != null) {
			ItemMetaUtils.addEnchantment(it, e, l);
		}
		return it;
	}

}
