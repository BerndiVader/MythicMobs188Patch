package com.gmail.berndivader.mm188patch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.compatibility.CompatibilityManager;
import io.lumine.xikage.mythicmobs.drops.MythicDropTable;
import io.lumine.xikage.mythicmobs.io.ConfigManager;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.SkillString;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import io.lumine.xikage.mythicmobs.skills.TriggeredSkill;

public class MythicMobs188 
implements
Listener {

	public MythicMobs188() {
		Core.pluginmanager.registerEvents(this, Core.plugin);
	}
	
	@EventHandler
	public void CustomMythicMobsSpawnEvent(CreatureSpawnEvent e) {
		if (!e.isCancelled() && e.getSpawnReason().equals(SpawnReason.CUSTOM)) {
			Entity bukkitEntity = e.getEntity();
			new BukkitRunnable() {
				@Override
				public void run() {
					if (Core.mythicmobs.getAPIHelper().isMythicMob(bukkitEntity.getUniqueId())) return;
					ActiveMob am = Core.mythicmobs.getAPIHelper().getMythicMobInstance(bukkitEntity);
					Core.mythicmobs.getVolatileCodeHandler().setAttackDamage(bukkitEntity, am.getDamage());
				}
			}.runTaskLater(Core.plugin, 5L);
		}
	}

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onInteractTrigger(PlayerInteractAtEntityEvent e) {
    	if (!e.isCancelled()) {
            Entity t = e.getRightClicked();
            if (!Core.mythicmobs.getMobManager().isActiveMob(t.getUniqueId())) return;
            ActiveMob am = Core.mythicmobs.getMobManager().getMythicMobInstance(BukkitAdapter.adapt(t));
			TriggeredSkill ts = new TriggeredSkill(SkillTrigger.INTERACT, am, (AbstractEntity)BukkitAdapter.adapt(e.getPlayer()), true, new Pair[0]);
            if (ts.getCancelled()) e.setCancelled(true);
            if (!am.getType().getIsInteractable()) e.setCancelled(true);
    	}
    }
    
    @SuppressWarnings("unchecked")
	@EventHandler
    public void onDeathTrigger(EntityDeathEvent e) {
        boolean good = true;
        AbstractEntity killedEntity = BukkitAdapter.adapt((Entity)e.getEntity());
        ActiveMob am = Core.mythicmobs.getMobManager().getMythicMobInstance(killedEntity);
        if (am != null) {
            LivingEntity killerLE;
            MythicMob mm = am.getType();
            Player killer = MythicMobs188.getPlayerKiller(e);
            if (killer == null 
            		&& (killerLE = MythicMobs188.getKiller(e)) != null 
            		&& Core.mythicmobs.getMobManager().isActiveMob(killerLE.getUniqueId())) {
                ActiveMob amkiller = Core.mythicmobs.getMobManager().getMythicMobInstance(BukkitAdapter.adapt((Entity)killerLE));
                if (!killedEntity.isPlayer() && amkiller.getType().preventMobKillDrops) {
                    e.getDrops().clear();
                    good = false;
                }
            }
            new TriggeredSkill(SkillTrigger.DEATH, am, (AbstractEntity)BukkitAdapter.adapt(killer), new Pair[0]);
            if (mm.preventOtherDrops) {
                e.getDrops().clear();
                e.setDroppedExp(0);
            }
            ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
            int exp = 0;
            int cexp = 0;
            int hexp = 0;
            int sexp = 0;
            int mexp = 0;
            boolean modExp = false;
            double money = 0.0;
            if (mm.getDrops() == null) {
                good = false;
            }
            if (good) {
                for (String s : mm.getDrops()) {
                    MythicDropTable dt;
                    Optional<MythicDropTable> maybeDT = Core.mythicmobs.getDropManager().getDropTable(s);
                    if (maybeDT.isPresent()) {
                        dt = maybeDT.get();
                    } else {
                        List<String> baseEquipmentList = Arrays.asList(s);
                        dt = new MythicDropTable(baseEquipmentList, null, null, null, null);
                    }
                    dt.parseTable(am, BukkitAdapter.adapt(killer));
                    for (ItemStack iS : dt.getDrops()) {
                        loot.add(iS);
                    }
                    if (dt.modifiesExperience()) {
                        modExp = true;
                    }
                    exp += dt.getExp();
                    cexp += dt.getChampionsExp();
                    hexp += dt.getHeroesExp();
                    sexp += dt.getSkillAPIExp();
                    mexp += dt.getMcMMOExp();
                    money += dt.getMoney();
                }
                if (mm.getDropsPerLevel() != null) {
                    for (int i = 0; i < am.getLevel(); ++i) {
                        for (String s : mm.getDropsPerLevel()) {
                            MythicDropTable dt;
                            Optional<MythicDropTable> maybeDT = Core.mythicmobs.getDropManager().getDropTable(s);
                            if (maybeDT.isPresent()) {
                                dt = maybeDT.get();
                            } else {
                                List<String> baseEquipmentList = Arrays.asList(s);
                                dt = new MythicDropTable(baseEquipmentList, null, null, null, null);
                            }
                            dt.parseTable(am, BukkitAdapter.adapt(killer));
                            for (ItemStack iS : dt.getDrops()) {
                                loot.add(iS);
                            }
                            if (dt.modifiesExperience()) {
                                modExp = true;
                            }
                            exp += dt.getExp();
                            cexp += dt.getChampionsExp();
                            hexp += dt.getHeroesExp();
                            sexp += dt.getSkillAPIExp();
                            mexp += dt.getMcMMOExp();
                            money += dt.getMoney();
                        }
                    }
                }
            }
            Player eKiller = killer == null ? e.getEntity().getKiller() : killer;
            AbstractEntity aKiller = BukkitAdapter.adapt((Entity)eKiller);
            MythicMobDeathEvent event = new MythicMobDeathEvent(am, (LivingEntity)eKiller, loot, exp, money);
            Core.pluginmanager.callEvent((Event)event);
            for (ItemStack iS : event.getDrops()) {
                e.getDrops().add(iS);
            }
            if (modExp) {
                e.setDroppedExp(event.getExp());
            }
            if (killer != null) {
                String message;
                if (CompatibilityManager.Champions != null && cexp > 0) {
                    CompatibilityManager.Champions.giveExp(BukkitAdapter.adapt(killer), cexp);
                }
                if (CompatibilityManager.Heroes != null && hexp > 0) {
                    CompatibilityManager.Heroes.giveHeroesExp(am, killer, hexp);
                }
                if (CompatibilityManager.SkillAPI != null && sexp > 0) {
                    CompatibilityManager.SkillAPI.giveExp(killer, sexp);
                    if (ConfigManager.compatSkillAPIShowXPMessage) {
                        message = ConfigManager.compatSkillAPIXPMessageFormat;
                        message = SkillString.parseMobVariables(message, am, null, aKiller);
                        message = message.replace("<drops.xp>", String.valueOf(sexp));
                        killer.sendMessage(message);
                    }
                }
                if (Core.mythicmobs.getCompatibility().getVault().isPresent() && money > 0.0) {
                    Core.mythicmobs.getCompatibility().getVault().get().giveMoney(killer, event.getCurrency());
                    if (ConfigManager.compatVaultShowMoneyMessage) {
                        message = ConfigManager.compatVaultMoneyMessageFormat;
                        message = SkillString.parseMobVariables(message, am, null, aKiller);
                        message = message.replace("<drops.money>", String.valueOf(event.getCurrency()));
                        killer.sendMessage(message);
                    }
                }
                if (CompatibilityManager.mcMMO != null && mexp > 0) {
                    String skilltype = "unarmed";
                    if (e.getEntity().getKiller() instanceof Projectile) {
                        skilltype = "archery";
                    } else {
                        Material m = killer.getEquipment().getItemInHand().getType();
                        if (m == Material.STONE_SWORD || m == Material.WOOD_SWORD || m == Material.IRON_SWORD || m == Material.DIAMOND_SWORD) {
                            skilltype = "swords";
                        }
                    }
                    CompatibilityManager.mcMMO.giveExp(killer, mexp, skilltype);
                    if (ConfigManager.compatMcMMOShowXPMessage) {
                        String message2 = ConfigManager.compatMcMMOXPMessageFormat;
                        message2 = SkillString.parseMobVariables(message2, am, null, aKiller);
                        message2 = message2.replace("<drops.mcmmo>", String.valueOf(money));
                        killer.sendMessage(message2);
                    }
                }
            }
            if (!good) {
                e.setDroppedExp(0);
                e.getDrops().clear();
            }
            if (mm.getDisplayName() != null) {
                String display = SkillString.parseMobVariables(mm.getDisplayName(), am, null, aKiller);
                killedEntity.getBukkitEntity().setCustomName(display);
            }
            am.setDead();
            MobManager.QueuedMobCleanup mclup = new MobManager.QueuedMobCleanup(am);
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)MythicMobs.inst(), (Runnable)mclup, 200);
        } else {
            LivingEntity killerLE = MythicMobs188.getKiller(e);
            if (killerLE != null 
            		&& Core.mythicmobs.getMobManager().isActiveMob(killerLE.getUniqueId())) {
                ActiveMob amkiller = Core.mythicmobs.getMobManager().getMythicMobInstance(BukkitAdapter.adapt((Entity)killerLE));
                if (!killedEntity.isPlayer() 
                		&& amkiller.getType().preventMobKillDrops) {
                    e.getDrops().clear();
                    e.setDroppedExp(0);
                }
            }
        }
        Core.mythicmobs.getMobManager().getActiveMobsInCombat().forEach(mob -> {
            if (mob.getEntity().isValid() && mob.getType().usesThreatTable()) {
                mob.getThreatTable().observeDeath(killedEntity);
            }
        }
        );
    }

    private static Player getPlayerKiller(EntityDeathEvent event) {
        EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
        if (entityDamageEvent != null && !entityDamageEvent.isCancelled() && entityDamageEvent instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent)entityDamageEvent).getDamager();
            if (damager instanceof Projectile) {
                if (((Projectile)damager).getShooter() instanceof LivingEntity) {
                    LivingEntity shooter = (LivingEntity)((Projectile)damager).getShooter();
                    if (shooter != null && shooter instanceof Player) {
                        return (Player)shooter;
                    }
                } else {
                    return null;
                }
            }
            if (damager instanceof Player) {
                return (Player)damager;
            }
        }
        return null;
    }

    private static LivingEntity getKiller(EntityDeathEvent event) {
        EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
        if (entityDamageEvent != null && !entityDamageEvent.isCancelled() && entityDamageEvent instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent)entityDamageEvent).getDamager();
            if (damager instanceof Projectile) {
                if (((Projectile)damager).getShooter() instanceof LivingEntity) {
                    LivingEntity shooter = (LivingEntity)((Projectile)damager).getShooter();
                    if (shooter != null && shooter instanceof LivingEntity) {
                        return shooter;
                    }
                } else {
                    return null;
                }
            }
            if (damager instanceof LivingEntity) {
                return (LivingEntity)damager;
            }
        }
        return null;
    }    
	
}
