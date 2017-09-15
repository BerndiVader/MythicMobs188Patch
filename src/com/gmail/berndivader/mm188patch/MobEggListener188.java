package com.gmail.berndivader.mm188patch;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.EggManager;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public class MobEggListener188
implements Listener {
	
	public MobEggListener188() {
		Core.pluginmanager.registerEvents(this, Core.plugin);
	}
	
	private static String checkMonsterEgg(ItemStack i) {
        if (i==null
        		||i.getType()!=Material.MONSTER_EGG
        		||!i.getItemMeta().hasLore()) return null;
        List<String>list=i.getItemMeta().getLore();
        if (list.get(0).contains("A Mythical Egg that can")) return list.get(2);
        return null;
	}
	
    @EventHandler(priority=EventPriority.HIGH)
    public void EggEvent(PlayerInteractEvent e) {
        if (e.getAction()==Action.RIGHT_CLICK_AIR
        		||e.getAction()==Action.RIGHT_CLICK_BLOCK) {
            ItemStack i=e.getItem();
            String iN=MobEggListener188.checkMonsterEgg(i);
            if (iN!=null) {
                final MythicMob mm = EggManager.getMythicMobFromEgg(iN);
                if (mm==null) return;
                ItemStack eggItem = i.clone();
                if (i.getAmount()==1) {
                    i=new ItemStack(Material.AIR,1);
                    e.getPlayer().setItemInHand(i);
                } else {
                    i.setAmount(i.getAmount()-1);
                }
                if (e.getAction()==Action.RIGHT_CLICK_BLOCK) {
                    Location location = e.getClickedBlock().getLocation().clone();
                    location.setY(location.getY() + 1.0);
                    Core.mythicmobs.getMobManager().spawnMob(mm.getInternalName(), location);
                } else {
                    Player player = e.getPlayer();
                    final Item egg = e.getPlayer().getWorld().dropItem(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY() + 1.4, player.getLocation().getZ()), eggItem);
                    Vector dir = player.getLocation().getDirection();
                    Vector vec = new Vector(dir.getX(), dir.getY(), dir.getZ()).multiply(1);
                    egg.setPickupDelay(32767);
                    egg.setTicksLived(6400);
                    egg.setVelocity(vec);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Core.plugin, new Runnable(){
                        @Override
                        public void run() {
                            Core.mythicmobs.getMobManager().spawnMob(mm.getInternalName(), egg.getLocation());
                            egg.remove();
                        }
                    },60L);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGH)
    public void DispenseEggEvent(BlockDispenseEvent e) {
        if (e.getBlock().getType()==Material.DROPPER) return;
        ItemStack i=e.getItem();
        String iN=MobEggListener188.checkMonsterEgg(i);
        if (iN!=null) {
            final MythicMob mm = EggManager.getMythicMobFromEgg(iN);
            if (mm==null) return;
            Location location = e.getBlock().getLocation().clone();
            if (e.getBlock().getData()==(byte)8) {
                location.add(0,-1.0D,0);
            } else if (e.getBlock().getData()==(byte)9) {
                location.add(0,1.0D,0);
            } else if (e.getBlock().getData()==(byte)10) {
                location.add(0,0,-1.0D);
            } else if (e.getBlock().getData()==(byte)11) {
                location.add(0,0,1.0D);
            } else if (e.getBlock().getData()==(byte)12) {
                location.add(-1.0D,0,0);
            } else if (e.getBlock().getData()==(byte)13) {
                location.add(1.0D,0,0);
            } else {
                location.add(0,1.0D,0);
            }
            MythicMobs.inst().getMobManager().spawnMob(mm.getInternalName(),location);
        }
    }

}