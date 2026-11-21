package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.magmacore.location.LocationQueryRegistry;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public final class ScriptNativeProjectiles implements Listener, AutoCloseable {
   private static final String PROTECTED = "nightbreak_script_projectile_protection";
   private static final ThreadLocal<ScriptNativeProjectiles.Impact> CURRENT_IMPACT = new ThreadLocal<>();
   private final Map<UUID, ScriptNativeProjectiles.Binding> bindings = new HashMap<>();
   private final Plugin plugin;

   public ScriptNativeProjectiles(Plugin plugin) {
      this.plugin = plugin;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   public static boolean isProtectionAware(Entity entity) {
      return entity != null && entity.hasMetadata("nightbreak_script_projectile_protection");
   }

   public static void applyAttributedDamage(LivingEntity target, double amount, Player actor) {
      ScriptNativeProjectiles.Impact impact = CURRENT_IMPACT.get();
      if (impact != null && !impact.applying && impact.target == target && impact.projectile.getShooter() == actor) {
         impact.applying = true;
         impact.observed = false;

         try {
            target.damage(amount, impact.source);
         } finally {
            impact.applying = false;
         }
      } else {
         target.damage(amount, actor);
      }
   }

   public boolean bind(Projectile projectile, ScriptInstance instance, BooleanSupplier active, LuaFunction damage, boolean protect) {
      if (projectile.isValid() && !this.bindings.containsKey(projectile.getUniqueId())) {
         ScriptNativeProjectiles.Binding binding = new ScriptNativeProjectiles.Binding(instance, active, damage, protect);
         this.bindings.put(projectile.getUniqueId(), binding);
         if (protect) {
            projectile.setMetadata("nightbreak_script_projectile_protection", new FixedMetadataValue(this.plugin, true));
         }

         instance.ownCleanup(() -> {
            this.bindings.remove(projectile.getUniqueId(), binding);
            if (protect) {
               projectile.removeMetadata("nightbreak_script_projectile_protection", this.plugin);
            }
         });
         return true;
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void damage(EntityDamageByEntityEvent event) {
      ScriptNativeProjectiles.Binding binding = this.bindings.get(event.getDamager().getUniqueId());
      if (binding != null) {
         ScriptNativeProjectiles.Impact current = CURRENT_IMPACT.get();
         if (current != null && current.applying && !current.observed && current.projectile == event.getDamager() && current.target == event.getEntity()) {
            current.observed = true;
         } else {
            boolean accepted = !event.isCancelled();
            event.setCancelled(true);
            if (accepted && binding.active.getAsBoolean() && event.getEntity() instanceof LivingEntity target) {
               double amount = event.getDamage();
               if (Double.isFinite(amount) && !(amount <= 0.0)) {
                  ScriptNativeProjectiles.Impact impact = new ScriptNativeProjectiles.Impact((Projectile)event.getDamager(), target, event.getDamageSource());
                  binding.instance
                     .ownLater(
                        1,
                        () -> {
                           if (binding.active.getAsBoolean() && target.isValid() && !target.isDead()) {
                              ScriptNativeProjectiles.Impact previous = CURRENT_IMPACT.get();
                              CURRENT_IMPACT.set(impact);

                              try {
                                 binding.instance
                                    .invokeOwnedCallback(
                                       "native projectile damage", binding.damage, LuaLivingEntityTable.build(target), LuaValue.valueOf(amount)
                                    );
                              } finally {
                                 if (previous == null) {
                                    CURRENT_IMPACT.remove();
                                 } else {
                                    CURRENT_IMPACT.set(previous);
                                 }
                              }
                           }
                        }
                     );
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void explode(EntityExplodeEvent event) {
      ScriptNativeProjectiles.Binding binding = this.bindings.get(event.getEntity().getUniqueId());
      if (binding != null && binding.protect) {
         if (binding.active.getAsBoolean() && !LocationQueryRegistry.isInAnyProtectedRegion(event.getLocation())) {
            event.blockList().removeIf(block -> LocationQueryRegistry.isInAnyProtectedRegion(block.getLocation()));
         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void ignite(BlockIgniteEvent event) {
      if (event.getIgnitingEntity() != null) {
         ScriptNativeProjectiles.Binding binding = this.bindings.get(event.getIgnitingEntity().getUniqueId());
         if (binding != null
            && binding.protect
            && (!binding.active.getAsBoolean() || LocationQueryRegistry.isInAnyProtectedRegion(event.getBlock().getLocation()))) {
            event.setCancelled(true);
         }
      }
   }

   @Override
   public void close() {
      HandlerList.unregisterAll(this);
      this.bindings.clear();
   }

   private record Binding(ScriptInstance instance, BooleanSupplier active, LuaFunction damage, boolean protect) {
   }

   private static final class Impact {
      private final Projectile projectile;
      private final LivingEntity target;
      private final DamageSource source;
      private boolean applying;
      private boolean observed;

      private Impact(Projectile projectile, LivingEntity target, DamageSource source) {
         this.projectile = projectile;
         this.target = target;
         this.source = source;
      }
   }
}
