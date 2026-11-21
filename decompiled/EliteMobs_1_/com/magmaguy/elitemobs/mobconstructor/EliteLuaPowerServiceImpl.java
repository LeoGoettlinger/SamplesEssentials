package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerActivity;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerProgram;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerService;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerType;
import com.magmaguy.elitemobs.api.power.ElitePowerActionHandler;
import com.magmaguy.elitemobs.api.power.ElitePowerActionRequest;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

final class EliteLuaPowerServiceImpl implements EliteLuaPowerService, Listener {
   private final EliteLuaPowerCatalog catalog = new EliteLuaPowerCatalog();
   private final Map<NamespacedKey, LinkedHashMap<NamespacedKey, EliteLuaPowerServiceImpl.ActionRegistration>> actionHandlers = new LinkedHashMap<>();
   private boolean closed;

   @Override
   public EliteLuaPowerProgram registerLuaPower(Plugin owner, NamespacedKey key, long revision, String source, EliteLuaPowerType type, String effect) {
      this.requireAvailable();
      requireServerThread();
      requireEnabledOwner(owner);
      return this.catalog.register(owner, key, revision, source, type, effect);
   }

   @Override
   public List<EliteLuaPowerProgram> setPowers(Plugin owner, EliteEntity eliteEntity, List<NamespacedKey> orderedPowerKeys) {
      this.requireAvailable();
      requireServerThread();
      requireEnabledOwner(owner);
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      if (EntityTracker.getEliteMobEntities().get(eliteEntity.getEliteUUID()) != eliteEntity) {
         throw new IllegalStateException("Elite actor is not registered with EliteMobs");
      } else {
         return this.setPowersInternal(owner, eliteEntity, orderedPowerKeys, false);
      }
   }

   List<EliteLuaPowerProgram> setPreparedPowers(Plugin owner, EliteEntity eliteEntity, List<NamespacedKey> orderedPowerKeys) {
      this.requireAvailable();
      requireServerThread();
      requireEnabledOwner(owner);
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      return this.setPowersInternal(owner, eliteEntity, orderedPowerKeys, true);
   }

   @Override
   public List<EliteLuaPowerProgram> inspectPowers(EliteEntity eliteEntity) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
      if (binding == null) {
         return List.of();
      } else if (!binding.matchesAttachmentOrder(eliteEntity.copyElitePowersInAttachmentOrder())) {
         clearBindingQuietly(eliteEntity, "managed power collection drift", true);
         return List.of();
      } else {
         return binding.descriptors();
      }
   }

   @Override
   public List<EliteLuaPowerActivity> inspectPowerActivity(EliteEntity eliteEntity) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
      if (binding == null) {
         return List.of();
      } else if (!binding.matchesAttachmentOrder(eliteEntity.copyElitePowersInAttachmentOrder())) {
         clearBindingQuietly(eliteEntity, "managed power collection drift", true);
         return List.of();
      } else {
         return binding.activity();
      }
   }

   @Override
   public void registerActionHandler(Plugin owner, NamespacedKey powerKey, NamespacedKey actionKey, ElitePowerActionHandler handler) {
      this.requireAvailable();
      requireServerThread();
      requireEnabledOwner(owner);
      Objects.requireNonNull(powerKey, "powerKey");
      Objects.requireNonNull(actionKey, "actionKey");
      Objects.requireNonNull(handler, "handler");
      this.catalog.resolveOwned(owner, List.of(powerKey));
      requireOwnerNamespace(owner, actionKey, "Power action");
      LinkedHashMap<NamespacedKey, EliteLuaPowerServiceImpl.ActionRegistration> handlers = this.actionHandlers
         .computeIfAbsent(actionKey, ignored -> new LinkedHashMap<>());

      for (EliteLuaPowerServiceImpl.ActionRegistration registration : handlers.values()) {
         if (registration.owner() != owner) {
            throw new IllegalArgumentException("Power action " + actionKey + " is already owned by another plugin");
         }
      }

      EliteLuaPowerServiceImpl.ActionRegistration previous = handlers.get(powerKey);
      if (previous == null) {
         handlers.put(powerKey, new EliteLuaPowerServiceImpl.ActionRegistration(owner, powerKey, handler));
      } else if (previous.owner() != owner || !previous.powerKey().equals(powerKey) || previous.handler() != handler) {
         throw new IllegalArgumentException("Power action " + actionKey + " is already registered for " + powerKey);
      }
   }

   @Override
   public void unregisterOwner(Plugin owner) {
      this.requireAvailable();
      requireServerThread();
      this.unregisterOwnerInternal(Objects.requireNonNull(owner, "owner"));
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobRemove(EliteMobRemoveEvent event) {
      clearBindingQuietly(event.getEliteMobEntity(), "removed elite", false);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginDisable(PluginDisableEvent event) {
      if (!this.closed && event.getPlugin() != MetadataHandler.PLUGIN) {
         this.unregisterOwnerInternal(event.getPlugin());
      }
   }

   void shutdown() {
      if (!this.closed) {
         requireServerThread();
         this.closed = true;

         for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
            clearBindingQuietly(eliteEntity, "EliteMobs shutdown", true);
         }

         this.actionHandlers.clear();
         this.catalog.clear();
      }
   }

   ElitePowerActionResult dispatchMindAction(
      UUID logicalOwner, UUID physicalEntity, NamespacedKey actionKey, Map<String, Object> payload, long gameTick, long generation
   ) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(logicalOwner, "logicalOwner");
      Objects.requireNonNull(physicalEntity, "physicalEntity");
      Objects.requireNonNull(actionKey, "actionKey");
      Objects.requireNonNull(payload, "payload");
      EliteEntity eliteEntity = EntityTracker.getEliteMobEntities().get(logicalOwner);
      return eliteEntity != null && eliteEntity.getLivingEntity() != null && eliteEntity.getLivingEntity().getUniqueId().equals(physicalEntity)
         ? dispatchIfPowerActive(eliteEntity, () -> this.dispatchActiveMindAction(eliteEntity, actionKey, payload, gameTick, generation))
         : ElitePowerActionResult.REJECTED;
   }

   static ElitePowerActionResult dispatchIfPowerActive(EliteEntity eliteEntity, Supplier<ElitePowerActionResult> activeDispatch) {
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      Objects.requireNonNull(activeDispatch, "activeDispatch");
      return eliteEntity.getPowerSuppression().isSuppressed()
         ? ElitePowerActionResult.REJECTED
         : Objects.requireNonNull(activeDispatch.get(), "Mind action dispatch result");
   }

   private ElitePowerActionResult dispatchActiveMindAction(
      EliteEntity eliteEntity, NamespacedKey actionKey, Map<String, Object> payload, long gameTick, long generation
   ) {
      EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
      if (binding == null) {
         return ElitePowerActionResult.REJECTED;
      } else if (!binding.matchesAttachmentOrder(eliteEntity.copyElitePowersInAttachmentOrder())) {
         clearBindingQuietly(eliteEntity, "managed power collection drift", true);
         return ElitePowerActionResult.REJECTED;
      } else {
         EliteMindBinding mindBinding = eliteEntity.getEliteMindBinding();
         if (mindBinding == null) {
            return ElitePowerActionResult.REJECTED;
         } else {
            Plugin owner = mindBinding.ownerForGeneration(generation);
            if (owner != null && owner.isEnabled() && binding.isOwnedBy(owner)) {
               ElitePowerActionRequest request = new ElitePowerActionRequest(eliteEntity, actionKey, payload, gameTick, generation);
               ElitePowerActionResult luaResult = binding.dispatchMindAction(request);
               if (luaResult == ElitePowerActionResult.ACCEPTED) {
                  return luaResult;
               } else {
                  boolean deferred = luaResult == ElitePowerActionResult.DEFERRED;
                  Map<NamespacedKey, EliteLuaPowerServiceImpl.ActionRegistration> handlers = this.actionHandlers.get(actionKey);
                  if (handlers == null) {
                     return deferred ? ElitePowerActionResult.DEFERRED : ElitePowerActionResult.REJECTED;
                  } else {
                     for (NamespacedKey attachedPower : binding.powerKeys()) {
                        EliteLuaPowerServiceImpl.ActionRegistration registration = handlers.get(attachedPower);
                        if (registration != null && registration.owner() == owner) {
                           try {
                              ElitePowerActionResult result = Objects.requireNonNull(registration.handler().handle(request), "Power action handler result");
                              if (result == ElitePowerActionResult.ACCEPTED) {
                                 return result;
                              }

                              deferred |= result == ElitePowerActionResult.DEFERRED;
                           } catch (RuntimeException var19) {
                              Logger.warn("Power action handler for " + actionKey + " on " + attachedPower + " failed: " + var19.getMessage());
                           }
                        }
                     }

                     return deferred ? ElitePowerActionResult.DEFERRED : ElitePowerActionResult.REJECTED;
                  }
               }
            } else {
               return ElitePowerActionResult.REJECTED;
            }
         }
      }
   }

   void suspendMindActorPowers(EliteEntity eliteEntity) {
      EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
      if (binding != null) {
         try {
            binding.stopRuntimes();
         } catch (RuntimeException var4) {
            Logger.warn("Failed to stop Lua powers during native body unload: " + var4.getMessage());
         }
      } else {
         eliteEntity.suspendUnmanagedPowerRuntimes();
      }

      eliteEntity.suspendPowerStances();
   }

   void resumeMindActorPowers(EliteEntity eliteEntity) {
      EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
      if (binding != null) {
         binding.restartRuntimes();
      } else {
         eliteEntity.resumeUnmanagedPowerRuntimes();
      }

      eliteEntity.refreshPowerStances();
   }

   void setMindActorPowersPaused(EliteEntity eliteEntity, boolean paused) {
      EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
      if (binding != null) {
         binding.setPaused(paused);
      } else if (paused) {
         eliteEntity.suspendUnmanagedPowerRuntimes();
      } else {
         eliteEntity.resumeUnmanagedPowerRuntimes();
      }

      if (paused) {
         eliteEntity.suspendPowerStances();
      } else {
         eliteEntity.refreshPowerStances();
      }
   }

   private List<EliteLuaPowerProgram> setPowersInternal(Plugin owner, EliteEntity eliteEntity, List<NamespacedKey> orderedPowerKeys, boolean preparedActor) {
      Objects.requireNonNull(orderedPowerKeys, "orderedPowerKeys");
      if (eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid()) {
         throw new IllegalStateException("Elite actor has no loaded living entity");
      } else if (preparedActor && !eliteEntity.isPreparedMindSpawn()) {
         throw new IllegalStateException("Elite actor is not in native Mind preparation");
      } else {
         List<EliteLuaPowerCatalog.Entry> entries = this.catalog.resolveOwned(owner, orderedPowerKeys);
         List<ElitePower> previousPowers = eliteEntity.copyElitePowersInAttachmentOrder();
         EliteLuaPowerBinding previousBinding = eliteEntity.getEliteLuaPowerBinding();
         requireReplaceable(owner, eliteEntity.getEliteMindBinding(), previousBinding, previousPowers);
         ArrayList<EliteLuaPowerBinding.AttachedPower> attached = new ArrayList<>(entries.size());

         try {
            for (EliteLuaPowerCatalog.Entry entry : entries) {
               LuaElitePower power = entry.registration().newPower();
               power.setOwnerEntity(eliteEntity);

               for (ElitePowerPauseReason reason : ElitePowerPauseReason.values()) {
                  power.setRuntimePauseReason(reason, eliteEntity.isPowerPaused(reason));
               }

               power.startRuntimeOrThrow();
               attached.add(new EliteLuaPowerBinding.AttachedPower(entry, power));
            }
         } catch (RuntimeException var18) {
            closeNewRuntimes(attached, var18);
            throw var18;
         }

         EliteLuaPowerBinding replacement = attached.isEmpty() ? null : new EliteLuaPowerBinding(owner, attached);

         try {
            eliteEntity.replaceElitePowersInOrder(replacement == null ? List.of() : replacement.powers());
            eliteEntity.setEliteLuaPowerBinding(replacement);
            eliteEntity.refreshPowerStances();
         } catch (RuntimeException var17) {
            if (replacement != null) {
               try {
                  replacement.close();
               } catch (RuntimeException var16) {
                  var17.addSuppressed(var16);
               }
            }

            restorePreviousState(eliteEntity, previousPowers, previousBinding, var17);
            throw var17;
         }

         if (preparedActor && replacement != null) {
            eliteEntity.addPreparedMindSpawnCleanup(() -> detachIfCurrent(eliteEntity, replacement, false));
         }

         if (previousBinding != null && previousBinding != replacement) {
            closeBindingQuietly(previousBinding, "replaced Lua power loadout");
         } else if (previousBinding == null) {
            closePowerRuntimesQuietly(previousPowers, "replaced unmanaged power loadout");
         }

         return replacement == null ? List.of() : replacement.descriptors();
      }
   }

   private static void restorePreviousState(
      EliteEntity eliteEntity, List<ElitePower> previousPowers, EliteLuaPowerBinding previousBinding, RuntimeException originalFailure
   ) {
      try {
         eliteEntity.replaceElitePowersInOrder(previousPowers);
         eliteEntity.setEliteLuaPowerBinding(previousBinding);
         eliteEntity.refreshPowerStances();
      } catch (RuntimeException var5) {
         originalFailure.addSuppressed(var5);
      }
   }

   private static void requireReplaceable(Plugin owner, EliteMindBinding mindBinding, EliteLuaPowerBinding binding, List<ElitePower> currentPowers) {
      if (mindBinding != null && !mindBinding.isOwnedBy(owner)) {
         throw new IllegalArgumentException("A plugin cannot replace another plugin's Mind actor power loadout");
      } else if (binding != null) {
         if (!binding.isOwnedBy(owner)) {
            throw new IllegalArgumentException("A plugin cannot replace another plugin's managed power loadout");
         } else if (!binding.matchesAttachmentOrder(currentPowers)) {
            throw new IllegalStateException("Elite actor power collection changed outside the Lua power service");
         }
      }
   }

   private void unregisterOwnerInternal(Plugin owner) {
      this.actionHandlers.values().forEach(handlers -> handlers.values().removeIf(registration -> registration.owner() == owner));
      this.actionHandlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
      this.catalog.unregisterOwner(owner);

      for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
         EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
         if (binding != null && binding.isOwnedBy(owner)) {
            clearBindingQuietly(eliteEntity, "owner plugin disable", true);
         }
      }
   }

   private static void clearBindingQuietly(EliteEntity eliteEntity, String context, boolean refreshStances) {
      EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
      if (binding != null) {
         try {
            detachIfCurrent(eliteEntity, binding, refreshStances);
         } catch (RuntimeException var5) {
            Logger.warn("Failed to clear Lua power binding during " + context + ": " + var5.getMessage());
         }
      }
   }

   private static void detachIfCurrent(EliteEntity eliteEntity, EliteLuaPowerBinding expected, boolean refreshStances) {
      if (eliteEntity.getEliteLuaPowerBinding() == expected) {
         eliteEntity.setEliteLuaPowerBinding(null);
         RuntimeException failure = null;

         try {
            expected.close();
         } catch (RuntimeException var12) {
            failure = var12;
         } finally {
            try {
               eliteEntity.replaceElitePowersInOrder(List.of());
               if (refreshStances && eliteEntity.getLivingEntity() != null) {
                  eliteEntity.refreshPowerStances();
               }
            } catch (RuntimeException var13) {
               if (failure != null) {
                  failure.addSuppressed(var13);
               }
            }
         }

         if (failure != null) {
            throw failure;
         }
      }
   }

   private static void closeNewRuntimes(List<EliteLuaPowerBinding.AttachedPower> attachedPowers, RuntimeException primaryFailure) {
      for (int index = attachedPowers.size() - 1; index >= 0; index--) {
         try {
            attachedPowers.get(index).power().closeRuntime();
         } catch (RuntimeException var4) {
            primaryFailure.addSuppressed(var4);
         }
      }
   }

   private static void closeBindingQuietly(EliteLuaPowerBinding binding, String context) {
      try {
         binding.close();
      } catch (RuntimeException var3) {
         Logger.warn("Failed to close " + context + ": " + var3.getMessage());
      }
   }

   private static void closePowerRuntimesQuietly(List<ElitePower> powers, String context) {
      for (ElitePower power : powers) {
         try {
            power.closeRuntime();
         } catch (RuntimeException var5) {
            Logger.warn("Failed to close " + context + " for " + power.getFileName() + ": " + var5.getMessage());
         }
      }
   }

   private void requireAvailable() {
      if (this.closed) {
         throw new IllegalStateException("EliteMobs Lua power service is shut down");
      }
   }

   private static void requireEnabledOwner(Plugin owner) {
      Objects.requireNonNull(owner, "owner");
      if (!owner.isEnabled()) {
         throw new IllegalStateException("Lua power owner plugin is not enabled");
      }
   }

   private static void requireOwnerNamespace(Plugin owner, NamespacedKey key, String subject) {
      String ownerNamespace = new NamespacedKey(owner, "ownership_probe").getNamespace();
      if (!ownerNamespace.equals(key.getNamespace())) {
         throw new IllegalArgumentException(subject + " key " + key + " is outside plugin namespace " + ownerNamespace);
      }
   }

   private static void requireServerThread() {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("EliteMobs Lua power operations must run on the server thread");
      }
   }

   private record ActionRegistration(Plugin owner, NamespacedKey powerKey, ElitePowerActionHandler handler) {
   }
}
