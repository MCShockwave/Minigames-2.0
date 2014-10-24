package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Shop.ShopItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Dogtag implements IMinigame {

	HashMap<Item, String>		tag		= new HashMap<Item, String>();
	HashMap<String, BukkitTask>	slimes	= new HashMap<String, BukkitTask>();
	ArrayList<Slime>			spawned	= new ArrayList<Slime>();

	BukkitTask					comPo	= null;

	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);
		}

		comPo = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (Player p : Minigames.getOptedIn()) {
					if (Minigames.alivePlayers.contains(p.getName()) && Minigames.hasItem(p, ShopItem.Major)) {
						int id = p.getInventory().first(Material.COMPASS);
						if (id != -1) {
							Object[] os = getNearestDogtag(p);
							ItemStack com = p.getInventory().getItem(id);
							ItemMetaUtils.setItemName(com, "§rPointing to: §b" + os[0]);
							if (os[1] != null) {
								p.setCompassTarget((Location) os[1]);
							} else {
								p.setCompassTarget(p.getWorld().getSpawnLocation());
							}
						}
					}
				}
			}
		}, 10, 10);
	}

	public Object[] getNearestDogtag(Player p) {
		Item item = null;
		double dis = 0;
		for (Item i : tag.keySet()) {
			double distance = i.getLocation().distance(p.getLocation());
			if (dis == 0 || distance < dis) {
				item = i;
				dis = distance;
			}
		}
		if (item != null) {
			return new Object[] { tag.get(item) + "'s Dogtag", item.getLocation() };
		}
		return new Object[] { "Nothing", null };
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onGameEnd() {
		for (BukkitTask bt : slimes.values()) {
			bt.cancel();
		}
		slimes.clear();
		for (Slime s : spawned) {
			s.remove();
		}
		spawned.clear();
		tag.clear();
		comPo.cancel();

		for (Map.Entry<Block, GameTeam> e : mines.entrySet()){
			Block b = e.getKey();
			for (Player p : Bukkit.getOnlinePlayers()) {
				PacketUtils.setBlockFromPacket(p, b, b.getType(), b.getData());
			}
		}
		mines.clear();
	}

	@Override
	public void onPlayerDeath(final DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s killed themselves", "%s was murdered by %s");

		final Item i = e.p.getWorld().dropItem(e.p.getLocation(),
				new ItemStack(Material.WOOL, 1, Game.getWoolColor(e.gt)));
		Item sk = e.p.getWorld().dropItem(e.p.getLocation(), new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
		i.setPassenger(sk);
		slimes.put(e.p.getName(), Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				final Slime name = (Slime) e.p.getWorld().spawnEntity(i.getLocation().clone(), EntityType.SLIME);
				name.setSize(1);
				name.setCustomName(e.gt.color + e.p.getName());
				name.setCustomNameVisible(true);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						name.setHealth(0f);
					}
				}, 2);
			}
		}, 0, 18));

		tag.put(i, e.p.getName());

		e.p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0));
		Minigames.clearInv(e.p);

		Minigames.send(e.p, "You have been put in %s until your tag is picked up!", "holding");

		final GameTeam[] gts = Game.getInstance(this).teams;
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				if (getAliveCount(gts[0]) < 1) {
					Minigames.stop(gts[1].team);
				}
				if (getAliveCount(gts[1]) < 1) {
					Minigames.stop(gts[0].team);
				}
			}
		}, 2);

	}

	public int getAliveCount(GameTeam gt) {
		int dt = 0;
		for (Item i : tag.keySet()) {
			String s = tag.get(i);
			for (OfflinePlayer op : gt.team.getPlayers()) {
				if (i.isValid() && !i.isDead() && op.getName().equalsIgnoreCase(s)) {
					dt++;
				}
			}
		}
		return gt.team.getPlayers().size() - dt;
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		p.getInventory().addItem(
				ItemMetaUtils.addEnchantment(new ItemStack(Material.BOW), Enchantment.ARROW_INFINITE, 1));
		p.getInventory().setItem(27, new ItemStack(Material.ARROW));

		if (Minigames.hasItem(p, ShopItem.Colonel)) {
			p.getInventory().setItem(8,
					ItemMetaUtils.setItemName(new ItemStack(Material.SULPHUR, 3), "§rRight-click to set mine"));
		}
		if (Minigames.hasItem(p, ShopItem.Major)) {
			p.getInventory().setItem(8,
					ItemMetaUtils.setItemName(new ItemStack(Material.COMPASS), "§rPointing to: §bNothing"));
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		Item i = event.getItem();
		ItemStack it = i.getItemStack();
		GameTeam gt = Game.getTeam(p);

		if (!Minigames.alivePlayers.contains(p.getName()) || tag.values().contains(p.getName())
				&& !Minigames.hasItem(p, ShopItem.General) || gt == null) {
			event.setCancelled(true);
			return;
		}
		if (it.getType() == Material.WOOL) {
			short b = it.getDurability();
			String pla = tag.get(i);
			Player re = Bukkit.getPlayer(pla);
			if (re != null) {
				tag.remove(i);
				if (b == Game.getWoolColor(gt)) {
					if (re != p) {
						Minigames.send(gt.color, re, "Your dogtag was picked up by %s!", p.getName());
						Minigames.send(gt.color, p, "You picked up %s's dogtag! They will now respawn!", re.getName());
					} else {
						Minigames.send(gt.color, p, "You picked up %s dogtag! You will now respawn!", "your own");
					}
					for (Player se : Bukkit.getOnlinePlayers()) {
						if (se != re && se != p) {
							Minigames.send(gt.color, se, "%s's dogtag was recovered by their team!", re.getName());
						}
					}

					re.teleport(i.getLocation());
					re.removePotionEffect(PotionEffectType.INVISIBILITY);
					giveKit(re);
				} else {
					Minigames.send(gt.color, re, "Your dogtag was picked up by the %s team! You will not respawn!",
							gt.name);
					Minigames.send(Game.getTeam(re).color, p,
							"You picked up %s's dogtag! They will no longer respawn.", re.getName());
					for (Player se : Bukkit.getOnlinePlayers()) {
						if (se != re && se != p) {
							Minigames.send(Game.getTeam(re).color, se, "%s's dogtag was picked up by the %s team!",
									re.getName(), gt.color + ChatColor.ITALIC.toString() + gt.name);
						}
					}

					Minigames.setDead(re, false);
				}
			}
			if (slimes.containsKey(pla)) {
				slimes.get(pla).cancel();
				slimes.remove(pla);
			}
		}
		i.remove();
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			if (tag.values().contains(p.getName())) {
				event.setCancelled(true);
			}
		}

		if (event.getDamager() instanceof Arrow) {
			Arrow a = (Arrow) event.getDamager();
			if (a.getShooter() instanceof Player) {
				Player d = (Player) a.getShooter();

				if (Minigames.hasItem(d, ShopItem.Major)) {
					double dis = event.getEntity().getLocation().distance(d.getLocation());
					dis /= 15;
					if (dis > 1) {
						event.setDamage(event.getDamage() * dis);
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (tag.values().contains(p.getName())) {
				event.setCancelled(true);
			}
		}
	}

	HashMap<Block, GameTeam>	mines	= new HashMap<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();
		final Block b = event.getClickedBlock();

		if (it.getType() == Material.SULPHUR && a == Action.RIGHT_CLICK_BLOCK) {
			if (it.getAmount() > 1) {
				it.setAmount(it.getAmount() - 1);
			} else
				it.setType(Material.AIR);
			p.setItemInHand(it);

			mines.put(b, Game.getTeam(p));
			Minigames.send(p, "Placed %s at location!", "mine");
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		Block u = event.getTo().add(0, -1, 0).getBlock();

		if (Minigames.alivePlayers.contains(p.getName()) && Game.getTeam(p) != null && mines.containsKey(u)) {
			if (mines.get(u) != Game.getTeam(p)) {
				mines.remove(u);

				p.getWorld().playSound(p.getLocation(), Sound.EXPLODE, 1, 0.8f);
				p.damage(p.getHealth());
			}
		}
	}

}
