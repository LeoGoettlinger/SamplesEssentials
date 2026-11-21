package me.chancesd.pvpmanager.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.projectiles.ProjectileSource;

public class EntityListener1_9 implements Listener {
   private final PlayerManager ph;
   private final Cache<UUID, Set<AreaEffectCloud>> potionMessageCache = CacheBuilder.newBuilder().expireAfterAccess(3L, TimeUnit.SECONDS).build();
   private final Cache<UUID, AtomicInteger> elytraSpamCache = CacheBuilder.newBuilder().expireAfterWrite(2L, TimeUnit.SECONDS).build();
   private static final int ELYTRA_SPAM_THRESHOLD = 3;

   public EntityListener1_9(PlayerManager ph) {
      this.ph = ph;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onEntityGlide(EntityToggleGlideEvent event) {
      if (Conf.BLOCK_GLIDE_IN_COMBAT.asBool() && event.isGliding()) {
         Player player = (Player)event.getEntity();
         CombatPlayer combatPlayer = this.ph.get(player);
         if (combatPlayer.isInCombat()) {
            combatPlayer.message(Lang.ELYTRA_BLOCKED_IN_COMBAT, new Object[0]);
            event.setCancelled(true);
            UUID playerId = player.getUniqueId();
            AtomicInteger attemptCount = (AtomicInteger)this.elytraSpamCache.getIfPresent(playerId);
            if (attemptCount == null) {
               attemptCount = new AtomicInteger(1);
               this.elytraSpamCache.put(playerId, attemptCount);
            } else {
               int attempts = attemptCount.incrementAndGet();
               if (attempts >= 3) {
                  CombatUtils.removeElytra(player);
                  this.elytraSpamCache.invalidate(playerId);
               }
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onLingeringPotionSplash(AreaEffectCloudApplyEvent event) {
      if (!CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName())) {
         AreaEffectCloud areaCloud = event.getEntity();
         if (!event.getAffectedEntities().isEmpty() && areaCloud.getSource() instanceof Player player && CombatUtils.hasHarmfulPotion(areaCloud)) {
            List<Entity> toRemove = new ArrayList<>();

            for (LivingEntity e : event.getAffectedEntities()) {
               if (e.getType() == EntityType.PLAYER && !e.equals(player)) {
                  Player attacked = (Player)e;
                  ProtectionResult result = this.ph.checkProtection(player, attacked);
                  if (result.isProtected()) {
                     toRemove.add(e);
                     Set<AreaEffectCloud> clouds = (Set<AreaEffectCloud>)this.potionMessageCache.getIfPresent(player.getUniqueId());
                     if (clouds == null || !clouds.contains(areaCloud)) {
                        Lang.messageProtection(result, player, attacked);
                        Set<AreaEffectCloud> newClouds = new HashSet<>();
                        newClouds.add(areaCloud);
                        if (clouds != null) {
                           newClouds.addAll(clouds);
                        }

                        this.potionMessageCache.put(player.getUniqueId(), newClouds);
                     }
                  }
               }
            }

            event.getAffectedEntities().removeAll(toRemove);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onLingeringPotionSplashMon(AreaEffectCloudApplyEvent event) {
      if (!CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName())) {
         AreaEffectCloud areaCloud = event.getEntity();
         ProjectileSource areaCloudSource = areaCloud.getSource();
         if (!event.getAffectedEntities().isEmpty() && areaCloudSource instanceof Player) {
            if (CombatUtils.hasHarmfulPotion(areaCloud)) {
               Player player = (Player)areaCloudSource;

               for (LivingEntity e : event.getAffectedEntities()) {
                  if (e.getType() == EntityType.PLAYER && !e.equals(player)) {
                     Player attacked = (Player)e;
                     ProtectionResult result = this.ph.checkProtection(player, attacked);
                     if (result.isVulnerable()) {
                        this.ph.getPlugin().getEntityListener().processDamage(player, attacked);
                     }
                  }
               }
            }
         }
      }
   }
}
