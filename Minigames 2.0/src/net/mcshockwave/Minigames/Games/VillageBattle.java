package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;
import java.util.HashMap;

import net.mcshockwave.MCS.Utils.FireworkLaunchUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Utils.LocUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class VillageBattle implements IMinigame {

	Location					gspawn	= new Location(w, 577, 103, 5);
	Location					yspawn	= new Location(w, 667, 103, -166);

	Score						gs, ys;
	Objective					o;

	HashMap<Player, Profession>	types	= new HashMap<Player, Profession>();

	ArrayList<Villager>			greVil	= new ArrayList<Villager>();
	ArrayList<Villager>			yelVil	= new ArrayList<Villager>();

	HashMap<Player, Long>		cool	= new HashMap<Player, Long>();

	BukkitTask					bt		= null;

	@Override
	public void onGameStart() {
		int gvc = Game.getInstance(this).teams[0].team.getSize();
		int yvc = Game.getInstance(this).teams[1].team.getSize();

		gvc *= 3;
		yvc *= 3;

		for (int i = 0; i < gvc; i++) {
			spawnVillager(true);
		}
		for (int i = 0; i < yvc; i++) {
			spawnVillager(false);
		}

		Scoreboard s = Bukkit.getScoreboardManager().getMainScoreboard();
		o = s.registerNewObjective("Villagers", "dummy");
		o.setDisplayName(ChatColor.AQUA + "Villagers");
		gs = o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Green"));
		gs.setScore(gvc);
		ys = o.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + "Yellow"));
		ys.setScore(yvc);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (Player p : Minigames.getOptedIn()) {
			startPlayer(p, Game.getTeam(p));
		}

		final VillageBattle t = this;
		bt = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (Villager v : greVil) {
					if (!v.isValid() || v.isDead()) {
						greVil.remove(v);
					}
				}
				for (Villager v : yelVil) {
					if (!v.isValid() || v.isDead()) {
						yelVil.remove(v);
					}
				}

				for (GameTeam gt : Game.getInstance(t).teams) {
					setCount(gt, getTotalCountOnTeam(gt));
				}
			}
		}, 10, 100);

	}

	public void startPlayer(Player p, GameTeam gt) {
		p.setLevel(0);
		Minigames.milkPlayer(p);
		Minigames.clearInv(p);
		types.remove(p);
		p.teleport(gt.spawn);
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 0));
	}

	public void spawnVillager(boolean isGreen) {
		Location l = getValidSpawnPoint(isGreen);
		Villager v = (Villager) w.spawnEntity(l, EntityType.VILLAGER);
		Profession p = Profession.values()[rand.nextInt(Profession.values().length)];
		v.setProfession(p);
		String cusname = (isGreen ? ChatColor.GREEN : ChatColor.YELLOW).toString();
		switch (p) {
			case BLACKSMITH:
				cusname += "Blacksmith";
				break;
			case BUTCHER:
				cusname += "Butcher";
				break;
			case FARMER:
				cusname += "Farmer";
				break;
			case LIBRARIAN:
				cusname += "Librarian";
				break;
			case PRIEST:
				cusname += "Priest";
				break;
		}
		v.setCustomName(cusname);
		v.setCustomNameVisible(true);
		(isGreen ? greVil : yelVil).add(v);
	}

	public Location getValidSpawnPoint(boolean isGreen) {
		boolean notDone = true;
		int times = 0;
		while (notDone || times > 100) {
			Location l = LocUtils.addRand(isGreen ? gspawn.clone() : yspawn.clone(), 25, 1, 25);
			l.setY(gspawn.getBlockY());
			if (l.getBlock().getType() == Material.AIR) {
				notDone = false;
				return l;
			}
			times++;
		}
		return isGreen ? gspawn : yspawn;
	}

	@Override
	public void onGameEnd() {
		o.unregister();

		for (Villager v : greVil) {
			v.remove();
		}
		for (Villager v : yelVil) {
			v.remove();
		}
		greVil.clear();
		yelVil.clear();

		for (Player p : Minigames.getOptedIn()) {
			p.setLevel(0);
		}

		bt.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		startPlayer(e.p, e.gt);
		for (Villager v : greVil) {
			if (!v.isValid() || v.isDead()) {
				greVil.remove(v);
			}
		}
		for (Villager v : yelVil) {
			if (!v.isValid() || v.isDead()) {
				yelVil.remove(v);
			}
		}
		if (!types.containsKey(e.p))
			return;
		addCount(e.gt, -1);
		setCount(e.gt, getTotalCountOnTeam(e.gt));
		Minigames.broadcastDeath(e.p, e.k, "%s was killed", "%s was murdered by %s");
	}

	public void giveKit(Player p, Profession pro) {
		p.setLevel(100);
		p.getInventory().setHelmet(
				ItemMetaUtils.setItemName(
						ItemMetaUtils.setHeadName(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "MHF_Villager"),
						ChatColor.RESET + "Villager Head"));
		ProfessionKit pk = ProfessionKit.valueOf(pro.name());
		if (pk != null) {
			String[] desc = pk.desc;
			for (int i = 0; i < desc.length; i++) {
				desc[i] = ChatColor.AQUA + desc[i];
			}
			p.getInventory().setItem(
					1,
					ItemMetaUtils.setLore(
							ItemMetaUtils.setItemName(new ItemStack(Material.ENCHANTED_BOOK), ChatColor.AQUA
									+ pk.specAbilName), desc));
			p.getInventory().addItem(pk.main);
			if (pk.main.getType() == Material.BOW) {
				p.getInventory().setItem(27, new ItemStack(Material.ARROW));
			}
			p.addPotionEffect(new PotionEffect(pk.pos, 10000000, 0));
			p.addPotionEffect(new PotionEffect(pk.neg, 10000000,
					(pk.neg == PotionEffectType.DAMAGE_RESISTANCE ? -2 : 0)));
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.VILLAGER) {
			if (event.getDamager() instanceof Player) {
				Player d = (Player) event.getDamager();
				if (!Minigames.alivePlayers.contains(d.getName())) {
					event.setCancelled(true);
				}
				Villager v = (Villager) event.getEntity();
				GameTeam gt = Game.getTeam(d);

				String name = v.getCustomName();
				ChatColor cc = ChatColor.getByChar(name.charAt(1));

				if (gt.color == cc) {
					event.setCancelled(true);
				}
			}
		}

		if (event.getDamager().getType() == EntityType.PLAYER) {
			Player d = (Player) event.getDamager();

			if (event.getEntityType() == EntityType.PLAYER && !types.containsKey((Player) event.getEntity())
					|| !types.containsKey(d)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Villager) {
			Villager v = (Villager) event.getEntity();

			GameTeam gt = null;
			if (greVil.contains(v)) {
				gt = Game.getInstance(this).teams[0];
				greVil.remove(v);
			}
			if (yelVil.contains(v)) {
				gt = Game.getInstance(this).teams[1];
				greVil.remove(v);
			}

			if (gt != null) {
				addCount(gt, -1);
				final GameTeam gt2 = gt;
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						setCount(gt2, getTotalCountOnTeam(gt2));
					}
				}, 1);
			}
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();

			if (Minigames.alivePlayers.contains(p.getName()) && !types.containsKey(p)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();
		Entity rc = event.getRightClicked();
		
		event.setCancelled(true);

		if (Minigames.alivePlayers.contains(p.getName()) && !types.containsKey(p) && rc instanceof Villager) {
			Villager v = (Villager) rc;
			GameTeam gt = Game.getTeam(p);

			String name = v.getCustomName();
			ChatColor cc = ChatColor.getByChar(name.charAt(1));

			if (gt.color == cc) {
				Minigames.milkPlayer(p);
				Minigames.clearInv(p);
				Minigames.send(gt.color, p, "You are now a %s villager!", ChatColor.stripColor(v.getCustomName()));
				giveKit(p, v.getProfession());
				types.put(p, v.getProfession());
				p.teleport(v.getLocation());
				(gt.color == ChatColor.GREEN ? greVil : yelVil).remove(v);
				setCount(gt, getTotalCountOnTeam(gt));
				v.remove();
			}
		}
	}

	public int getAllCurrentPlayersOnTeam(GameTeam gt) {
		int cou = 0;
		for (Player p : types.keySet()) {
			if (Game.getTeam(p) == gt && !p.hasPotionEffect(PotionEffectType.INVISIBILITY) && types.get(p) != null) {
				cou++;
			}
		}
		return cou;
	}

	public int getAllVillagersOnTeam(GameTeam gt) {
		if (gt.color == ChatColor.GREEN) {
			return greVil.size();
		} else {
			return yelVil.size();
		}
	}

	public int getTotalCountOnTeam(GameTeam gt) {
		return getAllCurrentPlayersOnTeam(gt) + getAllVillagersOnTeam(gt);
	}

	public void addCount(GameTeam gt, int add) {
		setCount(gt, getTotalCountOnTeam(gt) + add);
	}

	public void setCount(GameTeam gt, int set) {
		if (!Minigames.started) {
			return;
		}
		for (Villager v : greVil) {
			if (!v.isValid() || v.isDead()) {
				greVil.remove(v);
			}
		}
		for (Villager v : yelVil) {
			if (!v.isValid() || v.isDead()) {
				yelVil.remove(v);
			}
		}
		if (gt.color == ChatColor.GREEN) {
			if (set <= 0) {
				Minigames.stop(Game.getInstance(this).teams[1].team);
			}
			if (gs != null && Minigames.started)
				gs.setScore(set);
		}
		if (gt.color == ChatColor.YELLOW) {
			if (set <= 0) {
				Minigames.stop(Game.getInstance(this).teams[0].team);
			}
			if (ys != null && Minigames.started)
				ys.setScore(set);
		}
	}

	public int getLvlReq(Profession p) {
		switch (p) {
			case BLACKSMITH:
				return 50;
			case BUTCHER:
				return 10;
			case FARMER:
				return 20;
			case LIBRARIAN:
				return 20;
			case PRIEST:
				return 10;
		}
		return 0;
	}

	public boolean canUseAbil(Player p, int lvl) {
		if (cool.containsKey(p) && cool.get(p) >= System.currentTimeMillis()) {
			return false;
		}
		return p.getLevel() >= lvl;
	}

	public void subLvl(Player p, int lvl, int ctime) {
		int lc = p.getLevel();
		lc -= lvl;
		if (lc <= 0) {
			lc = 0;
		}
		p.setLevel(lc);

		cool.remove(p);
		cool.put(p, System.currentTimeMillis() + ctime * 1000);
	}

	HashMap<FallingBlock, Player>	anvil	= new HashMap<FallingBlock, Player>();

	@EventHandler
	public void onAnvilLand(EntityChangeBlockEvent event) {
		if (event.getTo() == Material.ANVIL) {
			event.setCancelled(true);
			Player p = null;

			if (anvil.containsKey(event.getEntity())) {
				p = anvil.get(event.getEntity());
			} else
				return;

			Block b = event.getBlock();
			b.getWorld().playSound(b.getLocation(), Sound.ANVIL_LAND, 2, 0);
			PacketUtils.sendPacketGlobally(b.getLocation(), 50,
					PacketUtils.generateBlockParticles(Material.ANVIL, 0, b.getLocation()));

			for (Entity e : event.getEntity().getNearbyEntities(5, 5, 5)) {
				if (e instanceof Player) {
					Player p2 = (Player) e;

					if (p2 != p && Game.getTeam(p2) != Game.getTeam(p) && Minigames.alivePlayers.contains(p2.getName())
							&& types.containsKey(p2)) {
						p2.damage(10);
					}
				}

				if (e instanceof Villager) {
					Villager v = (Villager) e;

					String name = v.getCustomName();
					ChatColor cc = ChatColor.getByChar(name.charAt(1));

					if (Game.getTeam(p).color != cc) {
						v.damage(8);
					}
				}
			}

			event.getEntity().remove();
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Action a = event.getAction();
		final ItemStack it = p.getItemInHand();

		if (it != null && a.name().contains("RIGHT_CLICK") && types.containsKey(p)) {
			Profession pro = types.get(p);
			GameTeam gt = Game.getTeam(p);

			final int slot = p.getInventory().getHeldItemSlot();

			if (it.getType() == Material.STONE_HOE && pro == Profession.FARMER) {
				p.setItemInHand(null);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						if (types.containsKey(p)) {
							p.getInventory().setItem(slot, it);
						}
					}
				}, 60);

				Arrow ar = p.launchProjectile(Arrow.class);
				ar.setVelocity(ar.getVelocity().multiply(3));
			}

			if (it.getType() == Material.ANVIL && pro == Profession.BLACKSMITH) {
				p.setItemInHand(null);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						if (types.containsKey(p)) {
							p.getInventory().setItem(slot, it);
						}
					}
				}, 100);

				FallingBlock fb = p.getWorld().spawnFallingBlock(p.getEyeLocation(), Material.ANVIL, (byte) 0);
				fb.setVelocity(p.getLocation().getDirection().multiply(2));
				anvil.put(fb, p);
			}

			if (it.getType() == Material.ENCHANTED_BOOK) {

				if (!canUseAbil(p, getLvlReq(pro))) {
					return;
				}

				if (pro == Profession.LIBRARIAN) {
					PacketUtils
							.sendPacketGlobally(p.getLocation(), 50, PacketUtils.generateParticles(
									ParticleEffect.ENCHANTMENT_TABLE, p.getLocation(), 0, 1, 100));
					p.getWorld().playSound(p.getLocation(), Sound.DIG_GRASS, 1, 0);

					for (Entity e : p.getNearbyEntities(5, 5, 5)) {
						if (e instanceof Player) {
							Player p2 = (Player) e;

							if (p2 != p && Minigames.alivePlayers.contains(p2.getName()) && Game.getTeam(p2) != gt) {
								p2.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
								p2.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
							}
						}
					}
				}

				if (pro == Profession.FARMER) {
					p.getWorld().playSound(p.getLocation(), Sound.GHAST_FIREBALL, 1, 0);

					for (Entity e : p.getNearbyEntities(5, 5, 5)) {
						if (e instanceof Player) {
							Player p2 = (Player) e;

							if (p2 != p && Minigames.alivePlayers.contains(p2.getName()) && Game.getTeam(p2) != gt) {
								p2.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
							}
						}
					}
				}

				if (pro == Profession.PRIEST) {
					PacketUtils.sendPacketGlobally(p.getLocation(), 50,
							PacketUtils.generateParticles(ParticleEffect.HEART, p.getLocation(), 3, 1, 100));
					p.getWorld().playSound(p.getLocation(), Sound.BURP, 1, 1);
					p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1));

					for (Entity e : p.getNearbyEntities(5, 5, 5)) {
						if (e instanceof Player) {
							Player p2 = (Player) e;

							if (Minigames.alivePlayers.contains(p2.getName()) && Game.getTeam(p2) == gt) {
								p2.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1));
							}
						}
					}
				}

				if (pro == Profession.BLACKSMITH) {
					FireworkLaunchUtils.playFirework(p.getLocation(), Color.RED);
					p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 2));
				}

				if (pro == Profession.BUTCHER) {
					p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().multiply(2).setY(0.5)));
					p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 1, 0);
				}

				subLvl(p, getLvlReq(pro), 5);

			}
		}
	}

	public enum ProfessionKit {
		LIBRARIAN(
			new ItemStack(Material.STONE_SWORD),
			"Stun",
			PotionEffectType.SPEED,
			PotionEffectType.WEAKNESS,
			"Gives nearby enemies blindness and",
			"nausea for 10 seconds"),
		FARMER(
			new ItemStack(Material.STONE_HOE),
			"Slow",
			PotionEffectType.REGENERATION,
			PotionEffectType.DAMAGE_RESISTANCE,
			"Gives nearby enemies slowness",
			"for 5 seconds"),
		PRIEST(
			ItemMetaUtils.addEnchantment(new ItemStack(Material.BOW), Enchantment.ARROW_INFINITE, 1),
			"Heal",
			PotionEffectType.INCREASE_DAMAGE,
			PotionEffectType.DAMAGE_RESISTANCE,
			"Gives nearby allies and yourself",
			"regeneration II"),
		BLACKSMITH(
			new ItemStack(Material.ANVIL),
			"Berserk",
			PotionEffectType.DAMAGE_RESISTANCE,
			PotionEffectType.SLOW,
			"Gives yourself Strength III for 5 seconds"),
		BUTCHER(
			new ItemStack(Material.IRON_SWORD),
			"Evade",
			PotionEffectType.SPEED,
			PotionEffectType.DAMAGE_RESISTANCE,
			"Launches you in the direction you are pointing");

		ItemStack			main;
		String				specAbilName;
		PotionEffectType	pos;
		PotionEffectType	neg;
		String[]			desc;

		private ProfessionKit(ItemStack main, String specAbilName, PotionEffectType pos, PotionEffectType neg,
				String... desc) {
			this.main = main;
			this.specAbilName = specAbilName;
			this.pos = pos;
			this.neg = neg;
			this.desc = desc;
		}
	}

}
