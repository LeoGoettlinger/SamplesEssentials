package com.earth2me.essentials;

import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.MaterialUtil;
import java.util.Locale;
import net.ess3.provider.PersistentDataProvider;
import net.ess3.provider.SpawnerItemProvider;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class EssentialsBlockListener implements Listener {
   private final transient net.ess3.api.IEssentials ess;

   public EssentialsBlockListener(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPlace(final BlockPlaceEvent event) {
      ItemStack is = event.getItemInHand();
      if (is.getType() == MaterialUtil.SPAWNER && this.ess.provider(PersistentDataProvider.class).getString(is, "convert") != null) {
         BlockState blockState = event.getBlockPlaced().getState();
         if (blockState instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner)blockState;
            EntityType type = this.ess.provider(SpawnerItemProvider.class).getEntityType(event.getItemInHand());
            if (type != null
               && Mob.fromBukkitType(type) != null
               && this.ess.getUser(event.getPlayer()).isAuthorized("essentials.spawnerconvert." + Mob.fromBukkitType(type).name().toLowerCase(Locale.ENGLISH))) {
               spawner.setSpawnedType(type);
               spawner.update();
            }
         }
      }

      User user = this.ess.getUser(event.getPlayer());
      if (user.hasUnlimited(is) && user.getBase().getGameMode() == GameMode.SURVIVAL) {
         this.ess.scheduleSyncDelayedTask(() -> {
            if (is != null && is.getType() != null && !MaterialUtil.isAir(is.getType())) {
               ItemStack cloneIs = is.clone();
               cloneIs.setAmount(1);
               Inventories.addItem(user.getBase(), cloneIs);
               user.getBase().updateInventory();
            }
         });
      }
   }
}
