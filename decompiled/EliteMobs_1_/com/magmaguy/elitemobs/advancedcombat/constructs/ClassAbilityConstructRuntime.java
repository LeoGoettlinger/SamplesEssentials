package com.magmaguy.elitemobs.advancedcombat.constructs;

import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class ClassAbilityConstructRuntime implements AutoCloseable {
   private final ClassConstructVisualRegistry registry;
   private final PacketConstructService service;
   private final ConstructLifecycleTracker lifecycle = new ConstructLifecycleTracker();
   private final ClassAbilityConstructRuntime.LifecycleObserver observer;

   public ClassAbilityConstructRuntime(Plugin plugin) {
      this(plugin, ClassAbilityConstructRuntime.LifecycleObserver.NOOP);
   }

   public ClassAbilityConstructRuntime(Plugin plugin, ClassAbilityConstructRuntime.LifecycleObserver observer) {
      this(ClassConstructVisualRegistry.builtIns(), new PacketConstructService(plugin), observer);
   }

   ClassAbilityConstructRuntime(ClassConstructVisualRegistry registry, PacketConstructService service, ClassAbilityConstructRuntime.LifecycleObserver observer) {
      this.registry = Objects.requireNonNull(registry, "registry");
      this.service = Objects.requireNonNull(service, "service");
      this.observer = Objects.requireNonNull(observer, "observer");
   }

   public boolean present(Player caster, FixedAbilitySpec spec, Location origin) {
      Objects.requireNonNull(caster, "caster");
      Objects.requireNonNull(spec, "spec");
      Objects.requireNonNull(origin, "origin");
      ClassConstructVisualRegistry.Plan plan = this.registry.find(spec.id()).orElse(null);
      if (plan == null) {
         return false;
      } else {
         int durationTicks = Math.max(1, spec.tuning().durationTicks());

         Optional<PacketConstructService.Handle> spawned = switch (plan.anchorMode()) {
            case FIXED -> this.service.spawn(caster, origin, plan.definition(), durationTicks, this::cleared);
            case FOLLOW_CASTER -> this.service.spawnFollowing(caster, caster::getLocation, plan.definition(), durationTicks, this::cleared);
         };
         if (spawned.isEmpty()) {
            return false;
         } else {
            ConstructLifecycleTracker.Lease lease = this.lifecycle
               .open(spawned.orElseThrow().id(), caster.getUniqueId(), spec.id(), durationTicks, plan.anchorMode());
            this.observer.spawned(lease.casterId(), lease.abilityId(), lease.durationTicks(), lease.anchorMode());
            return true;
         }
      }
   }

   private void cleared(UUID constructId) {
      this.lifecycle.close(constructId).ifPresent(lease -> this.observer.cleared(lease.casterId(), lease.abilityId(), lease.anchorMode()));
   }

   public void deactivate(Player player) {
      this.service.deactivate(player);
   }

   @Override
   public void close() {
      this.service.close();
   }

   public interface LifecycleObserver {
      ClassAbilityConstructRuntime.LifecycleObserver NOOP = new ClassAbilityConstructRuntime.LifecycleObserver() {
         @Override
         public void spawned(UUID casterId, String abilityId, int durationTicks, ClassConstructVisualRegistry.AnchorMode anchorMode) {
         }

         @Override
         public void cleared(UUID casterId, String abilityId, ClassConstructVisualRegistry.AnchorMode anchorMode) {
         }
      };

      void spawned(UUID var1, String var2, int var3, ClassConstructVisualRegistry.AnchorMode var4);

      void cleared(UUID var1, String var2, ClassConstructVisualRegistry.AnchorMode var3);
   }
}
