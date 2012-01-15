/**
* @author Jason Keeslar "LordJason"
* License: Give credit if you modify and/or redistribute any of my code.
*/
package com.foocraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

public class LsEntityListener extends EntityListener{
	public static LethalSnow plugin;
	
	public LsEntityListener(LethalSnow instance) {
		plugin = instance;
	}
	
	public void onEntityDamage(EntityDamageEvent The_Event){
		if(!The_Event.isCancelled() && The_Event.getCause() == DamageCause.PROJECTILE && The_Event instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent)The_Event;
			if(plugin.SHOW_DEBUG){plugin.logMessage("\nhit:\t" + The_Event.getEntity().getClass().getName() + " \ndamage:\t" + The_Event.getDamage() + "\n:::\t" + ((EntityDamageByEntityEvent)The_Event).getDamager().getClass().getName());}
			//if(((EntityDamageByEntityEvent)The_Event).getDamager().getClass().getName().equals("org.bukkit.craftbukkit.entity.CraftSnowball")){//Allright it really is a snowball.
			if( edbe.getDamager() instanceof  Snowball ){
				//if(The_Event.getEntity().getClass().getName().equals("org.bukkit.craftbukkit.entity.CraftPlayer")){
				if(The_Event.getEntity() instanceof Player){
					// Snowball hit a player.
					if(plugin.HURT_PLAYER){ // CAN_DAMAGE_PLAYER){
						The_Event.setDamage(AddDamage(The_Event.getDamage()));
					}
				}
				else{
					// it hit something besides a player
					if(plugin.HURT_MOBS){
						The_Event.setDamage(AddDamage(The_Event.getDamage()));
					}
				}
			}
		}
	}
	
	public void onEntityDeath(EntityDeathEvent e){
		if((e.getEntity().getClass().getName().equals("org.bukkit.craftbukkit.entity.CraftSnowman")) && plugin.maxSeedDrop > 0){
			Block block = e.getEntity().getLocation().getBlock();
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.PUMPKIN_SEEDS, plugin.rand.nextInt(plugin.maxSeedDrop) + 1));
		}
	}
	
	public int AddDamage(int damage){
		if(plugin.rand.nextInt(100 + 1) <= plugin.DamageChance){ // passed the chance thing.
			if(plugin.SHOW_DEBUG){plugin.logMessage("Passed chance check");}
			if(plugin.DamageMax > 0){
				int randomNum = plugin.rand.nextInt(plugin.DamageMax - plugin.DamageMin + 1) + plugin.DamageMin;
				damage = plugin.clampInt(randomNum);
			}
		}
		else{
			if(plugin.SHOW_DEBUG){plugin.logMessage("Failed chance check");}
			
		}
		if(plugin.SHOW_DEBUG){plugin.logMessage("Setting damage to " + damage);}
		return damage;
	}
}