package com.magmaguy.elitemobs.advancedcombat.input;

import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class ClassAbilityInputRouter implements Listener {
   private final Plugin plugin;
   private final ClassAbilityInput input;
   private final ClassControlMode controlMode = new ClassControlMode();
   private final Map<UUID, ClassAbilityInputRouter.PendingGesture> pendingGestures = new HashMap<>();
   private final Map<UUID, ClassAbilityInputRouter.RecentDispatch> recentDispatches = new HashMap<>();
   private final Set<UUID> dispatchingAbilities = new HashSet<>();
   private long nextGestureGeneration;

   public ClassAbilityInputRouter(Plugin plugin, ClassAbilityInput input) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.input = Objects.requireNonNull(input, "input");
   }

   public boolean isGestureOpen(UUID playerId) {
      ClassAbilityInputRouter.PendingGesture pending = this.pendingGestures.get(playerId);
      return pending != null && pending.state().isOpenAt(MonotonicTickClock.currentTick());
   }

   public boolean controlsEnabled(Player player) {
      Objects.requireNonNull(player, "player");
      return this.controlsEnabled(player, this.input.hasActiveClass(player));
   }

   public boolean controlsEnabled(Player player, boolean hasActiveClass) {
      return hasActiveClass && this.controlModeEnabled(Objects.requireNonNull(player, "player"));
   }

   public void shutdown() {
      for (ClassAbilityInputRouter.PendingGesture gesture : this.pendingGestures.values()) {
         gesture.timeoutTask().cancel();
      }

      this.pendingGestures.clear();
      this.controlMode.clearAll();
      this.recentDispatches.clear();
      this.dispatchingAbilities.clear();
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSwapHands(PlayerSwapHandItemsEvent event) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      boolean abilityInputReady = this.input.hasActiveClass(player) && this.input.fLayerSupported(player);
      ClassControlMode.FAction action = this.controlMode
         .pressF(
            playerId,
            MonotonicTickClock.currentTick(),
            player.isSneaking(),
            this.input.controlsAlwaysAvailable(player),
            this.input.outsideControlsAllowed(),
            abilityInputReady
         );
      switch (action) {
         case PASS_THROUGH:
            this.closeGesture(player);
            break;
         case TOGGLE_BLOCKED:
            this.closeGesture(player);
            sendControlFeedback(player, action);
            break;
         case TOGGLED_ON:
         case TOGGLED_OFF:
            this.closeGesture(player);
            this.input.onControlModeChanged(player);
            sendControlFeedback(player, action);
            break;
         case OPEN_ABILITIES:
            event.setCancelled(true);
            ClassAbilityInputRouter.PendingGesture pending = this.pendingGestures.get(playerId);
            ClassAbilityGestureState state = pending == null ? ClassAbilityGestureState.closed() : pending.state();
            this.applyGestureTransition(player, state.pressF(MonotonicTickClock.currentTick(), player.isSneaking()));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onAbilityHotbarSelection(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      ClassAbilityInputRouter.PendingGesture pending = this.pendingGestures.get(player.getUniqueId());
      if (pending != null) {
         ClassAbilityGestureState.Transition transition = pending.state().selectHotbar(MonotonicTickClock.currentTick(), event.getNewSlot());
         if (transition.consumesInput()) {
            event.setCancelled(true);
         }

         this.applyGestureTransition(player, transition);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChordInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ClassAbilityInputRouter.PendingGesture pending = this.pendingGestures.get(player.getUniqueId());
      long currentTick = MonotonicTickClock.currentTick();
      if (pending != null) {
         ClassAbilityGestureState.Transition transition = switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> pending.state().leftClick(currentTick);
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> pending.state().rightClick(currentTick);
            default -> null;
         };
         if (transition != null) {
            if (transition.consumesInput()) {
               event.setCancelled(true);
            }

            this.applyGestureTransition(player, transition);
         }
      } else {
         ClassAbilityInputRouter.RecentDispatch recent = this.recentDispatches.get(player.getUniqueId());
         if (recent != null
            && recent.serverTick() == currentTick
            && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onChordInteractEntity(PlayerInteractEntityEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Player player = event.getPlayer();
         ClassAbilityInputRouter.PendingGesture pending = this.pendingGestures.get(player.getUniqueId());
         if (pending == null) {
            ClassAbilityInputRouter.RecentDispatch recent = this.recentDispatches.get(player.getUniqueId());
            if (recent != null && MonotonicTickClock.currentTick() - recent.serverTick() <= 1L) {
               event.setCancelled(true);
            }
         } else {
            ClassAbilityGestureState.Transition transition = pending.state().rightClick(MonotonicTickClock.currentTick());
            if (transition.consumesInput()) {
               event.setCancelled(true);
            }

            this.applyGestureTransition(player, transition);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onAttack(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player player) {
         if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK) {
            if (!CombatDamageContext.isPlayerToEliteBypassActive()) {
               if (!this.dispatchingAbilities.contains(player.getUniqueId())) {
                  ClassAbilityInputRouter.PendingGesture pending = this.pendingGestures.get(player.getUniqueId());
                  if (pending != null) {
                     ClassAbilityGestureState.Transition transition = pending.state().leftClick(MonotonicTickClock.currentTick());
                     if (transition.consumesInput()) {
                        event.setCancelled(true);
                     }

                     this.applyGestureTransition(player, transition);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.clearPlayer(player);
      this.controlMode.clear(player.getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldChange(PlayerChangedWorldEvent event) {
      this.clearPlayer(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      this.clearPlayer(player);
   }

   private void openGesture(Player player, ClassAbilityGestureState state) {
      UUID playerId = player.getUniqueId();
      this.closeGesture(player);
      long generation = ++this.nextGestureGeneration;
      BukkitTask timeout = Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.expireGesture(playerId, generation), 13L);
      this.pendingGestures.put(playerId, new ClassAbilityInputRouter.PendingGesture(state, generation, timeout));
      int heldSlot = player.getInventory().getHeldItemSlot();
      ActionBarCompositor.show(
         player,
         ActionBarCompositor.Source.ABILITY_INPUT,
         ChatColor.GRAY
            + "["
            + (heldSlot == 0 ? "7" : "1")
            + "/F] "
            + ChatColor.WHITE
            + this.input.abilityName(player, AbilitySlot.MOBILITY)
            + ChatColor.GRAY
            + "  ["
            + (heldSlot == 1 ? "8" : "2")
            + "/LMB] "
            + ChatColor.WHITE
            + this.input.abilityName(player, AbilitySlot.SIGNATURE)
            + ChatColor.GRAY
            + "  ["
            + (heldSlot == 2 ? "9" : "3")
            + "/RMB] "
            + ChatColor.WHITE
            + this.input.abilityName(player, AbilitySlot.UTILITY),
         13L
      );
   }

   private void expireGesture(UUID playerId, long generation) {
      ClassAbilityInputRouter.PendingGesture gesture = this.pendingGestures.get(playerId);
      if (gesture != null && gesture.generation() == generation) {
         this.pendingGestures.remove(playerId);
         Player player = Bukkit.getPlayer(playerId);
         if (player != null) {
            ActionBarCompositor.clear(player, ActionBarCompositor.Source.ABILITY_INPUT);
         }
      }
   }

   private void closeGesture(Player player) {
      ClassAbilityInputRouter.PendingGesture gesture = this.pendingGestures.remove(player.getUniqueId());
      if (gesture != null) {
         gesture.timeoutTask().cancel();
         ActionBarCompositor.clear(player, ActionBarCompositor.Source.ABILITY_INPUT);
      }
   }

   private void applyGestureTransition(Player player, ClassAbilityGestureState.Transition transition) {
      this.closeGesture(player);
      switch (transition.outcome()) {
         case CHORD_OPENED:
            this.openGesture(player, transition.next());
            break;
         case MOBILITY:
            this.dispatch(player, AbilitySlot.MOBILITY);
            break;
         case SIGNATURE:
            this.dispatch(player, AbilitySlot.SIGNATURE);
            break;
         case UTILITY:
            this.dispatch(player, AbilitySlot.UTILITY);
         case PASS_THROUGH:
      }
   }

   private void clearPlayer(Player player) {
      this.closeGesture(player);
      this.recentDispatches.remove(player.getUniqueId());
      this.dispatchingAbilities.remove(player.getUniqueId());
   }

   private boolean controlModeEnabled(Player player) {
      return this.controlMode.enabled(player.getUniqueId(), this.input.controlsAlwaysAvailable(player), this.input.outsideControlsAllowed());
   }

   private static void sendControlFeedback(Player player, ClassControlMode.FAction action) {
      String header = ClassPresentationTheme.gradient("#6D3AA8:#A855F7", "Class Controls");

      String message = switch (action) {
         case TOGGLE_BLOCKED -> header + " &8» &cBLOCKED &8- &7outside EliteMobs worlds";
         case TOGGLED_ON -> header + " &8» &aON &8- &7F,F: Mobility | F+LMB: Signature | F+RMB: Utility";
         case TOGGLED_OFF -> header + " &8» &cOFF &8- &7F swaps hands again";
         default -> null;
      };
      if (message != null) {
         player.sendMessage(ChatColorConverter.convert(message));
      }
   }

   private void dispatch(Player player, AbilitySlot abilitySlot) {
      UUID playerId = player.getUniqueId();
      long currentTick = MonotonicTickClock.currentTick();
      ClassAbilityInputRouter.RecentDispatch recent = this.recentDispatches.get(playerId);
      if (recent == null || recent.serverTick() != currentTick || recent.abilitySlot() != abilitySlot) {
         this.recentDispatches.put(playerId, new ClassAbilityInputRouter.RecentDispatch(currentTick, abilitySlot));
         if (this.dispatchingAbilities.add(playerId)) {
            try {
               this.input.useAbility(player, abilitySlot);
            } finally {
               this.dispatchingAbilities.remove(playerId);
            }
         }
      }
   }

   private record PendingGesture(ClassAbilityGestureState state, long generation, BukkitTask timeoutTask) {
   }

   private record RecentDispatch(long serverTick, AbilitySlot abilitySlot) {
   }
}
