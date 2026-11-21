package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.combatsystem.combattag.PlayerCombatState;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.ammunition.RangedAmmunition;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.HealthDisplayCoordinator;
import com.magmaguy.elitemobs.skills.HealthPercentagePreserver;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Cake;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public final class AdvancedCombatRuntime implements Listener, PlayerCombatState.Listener {
   private static final String HEALTH_MODIFIER_KEY = "advanced_combat_health";
   private static final String ENTRY_WARNING = "&6&l[Alpha] Advanced Combat System &7is active here. The combat system is still in alpha, but testers have found it extremely enjoyable. &fPlease share your feedback with the developer&7!";
   private static final String COMBAT_STARTED = "&cEntering combat!";
   private static final String COMBAT_ENDED = "&aOut of combat &8- &7slow healing resumed";
   private static AdvancedCombatRuntime instance;
   private static Function<Player, String> hudProvider = AdvancedCombatRuntime::defaultHud;
   private final PlayerCombatState combatState;
   private final Map<UUID, AdvancedCombatRuntime.PlayerBaseline> activePlayers = new HashMap<>();
   private final AdvancedCombatShutdownPolicy shutdownPolicy = new AdvancedCombatShutdownPolicy();
   private BukkitTask reconciliationTask;
   private BukkitTask hungerEnforcementTask;
   private RangedAmmunition rangerAmmunition;

   public AdvancedCombatRuntime(PlayerCombatState combatState) {
      if (instance != null) {
         throw new IllegalStateException("[Alpha] Advanced Combat System runtime is already initialized.");
      } else {
         this.combatState = combatState;
         instance = this;
      }
   }

   public static boolean isActive(Player player) {
      return instance != null
         && AdvancedCombatSystemConfig.isEnabled()
         && DungeonCombatRuntime.isEligiblePlayer(player)
         && instance.activePlayers.containsKey(player.getUniqueId());
   }

   public static void shutdownIfInitialized() {
      if (instance != null) {
         instance.shutdown();
      }
   }

   public static void prepareForSoftReload() {
      if (instance != null) {
         instance.shutdownPolicy.prepareSoftReload();
      }
   }

   public static void installHudProvider(Function<Player, String> provider) {
      hudProvider = Objects.requireNonNull(provider, "provider");
   }

   public static void clearHudProvider() {
      hudProvider = AdvancedCombatRuntime::defaultHud;
   }

   public void start() {
      if (this.reconciliationTask != null) {
         throw new IllegalStateException("[Alpha] Advanced Combat System runtime is already running.");
      } else {
         this.rangerAmmunition = NMSManager.getAdapter()
            .grantOrdinaryAmmunition(
               MetadataHandler.PLUGIN,
               player -> isActive(player)
                  && AdvancedCombatModule.activeClassLineageSnapshot(player.getUniqueId()).map(lineage -> lineage.root().id().equals("ranger")).orElse(false)
            );
         this.combatState.addListener(this);
         this.reconciliationTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::reconcilePlayers, 1L, 20L);
         this.hungerEnforcementTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::enforceActiveHunger, 1L, 1L);
      }
   }

   public void shutdown() {
      if (this.rangerAmmunition != null) {
         this.rangerAmmunition.close();
         this.rangerAmmunition = null;
      }

      boolean preserveHealth = this.shutdownPolicy.consumeHealthCleanup() == AdvancedCombatShutdownPolicy.HealthCleanup.PRESERVE_FOR_SOFT_RELOAD;
      this.combatState.removeListener(this);
      if (this.reconciliationTask != null) {
         this.reconciliationTask.cancel();
         this.reconciliationTask = null;
      }

      if (this.hungerEnforcementTask != null) {
         this.hungerEnforcementTask.cancel();
         this.hungerEnforcementTask = null;
      }

      for (UUID playerId : this.activePlayers.keySet().toArray(UUID[]::new)) {
         Player player = Bukkit.getPlayer(playerId);
         if (player != null) {
            this.leave(player, preserveHealth);
         }
      }

      this.activePlayers.clear();
      HandlerList.unregisterAll(this);
      if (instance == this) {
         instance = null;
      }
   }

   private void reconcilePlayers() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.reconcilePlayer(player, false, true);
      }
   }

   private void reconcilePlayer(Player player, boolean enteredNewWorld) {
      this.reconcilePlayer(player, enteredNewWorld, false);
   }

   private void reconcilePlayer(Player player, boolean enteredNewWorld, boolean periodicHealing) {
      boolean shouldBeActive = AdvancedCombatSystemConfig.isEnabled() && DungeonCombatRuntime.isEligiblePlayer(player);
      boolean isActive = this.activePlayers.containsKey(player.getUniqueId());
      if (!isActive || !player.isDead() || !AdvancedCombatSystemConfig.isEnabled() || !DungeonCombatRuntime.isInManagedCombatWorld(player)) {
         if (shouldBeActive && !isActive) {
            this.enter(player);
         } else if (!shouldBeActive && isActive) {
            this.leave(player);
         } else if (shouldBeActive) {
            this.maintain(player, periodicHealing);
            if (enteredNewWorld) {
               player.sendMessage(
                  ChatColorConverter.convert(
                     "&6&l[Alpha] Advanced Combat System &7is active here. The combat system is still in alpha, but testers have found it extremely enjoyable. &fPlease share your feedback with the developer&7!"
                  )
               );
            }
         }
      }
   }

   private void enter(Player player) {
      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      if (maxHealth != null) {
         clearPersistedState(player);
         AdvancedCombatRuntime.PlayerBaseline baseline = new AdvancedCombatRuntime.PlayerBaseline(
            player.getFoodLevel(), player.getSaturation(), player.getExhaustion()
         );
         this.activePlayers.put(player.getUniqueId(), baseline);
         boolean healthChanged = updateHealthModifier(player, true);
         HealthDisplayCoordinator.acquire(player, HealthDisplayCoordinator.Owner.ADVANCED_COMBAT);
         AdvancedFoodItems.preparePlayerInventory(player);
         maintainHunger(player);
         if (healthChanged) {
            scheduleHungerRefresh(player);
         }

         renderHud(player);
         player.sendMessage(
            ChatColorConverter.convert(
               "&6&l[Alpha] Advanced Combat System &7is active here. The combat system is still in alpha, but testers have found it extremely enjoyable. &fPlease share your feedback with the developer&7!"
            )
         );
         if (AdvancedCombatModule.isInitialized() && !AdvancedCombatModule.get().hasActiveClass(player)) {
            player.sendMessage(
               ChatColorConverter.convert(
                  ClassPresentationTheme.gradient("#B8860B:#F0C040", "No class active!") + " &7Open &f/em class &7and pick a free class to use abilities here."
               )
            );
            ActionBarCompositor.show(
               player,
               ActionBarCompositor.Source.SKILL_FEEDBACK,
               ChatColorConverter.convert(ClassPresentationTheme.gradient("#B8860B:#F0C040", "No class") + " &8» &7pick one with &f/em class")
            );
         }
      }
   }

   private void maintain(Player player, boolean periodicHealing) {
      boolean healthChanged = updateHealthModifier(player, true);
      if (periodicHealing && !this.combatState.isInCombat(player.getUniqueId())) {
         AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
         if (maxHealth != null && player.getHealth() < maxHealth.getValue()) {
            double healing = maxHealth.getValue() * 0.01;
            applyHealing(player, healing, RegainReason.CUSTOM);
         }
      }

      AdvancedFoodItems.preparePlayerInventory(player);
      maintainHunger(player);
      if (healthChanged) {
         scheduleHungerRefresh(player);
      }

      renderHud(player);
   }

   private void leave(Player player) {
      this.leave(player, false);
   }

   private void leave(Player player, boolean preserveHealth) {
      AdvancedCombatRuntime.PlayerBaseline baseline = this.activePlayers.remove(player.getUniqueId());
      AdvancedFoodItems.restorePlayerInventory(player);
      if (!preserveHealth) {
         updateHealthModifier(player, false);
      }

      HealthDisplayCoordinator.release(player, HealthDisplayCoordinator.Owner.ADVANCED_COMBAT);
      ActionBarCompositor.clear(player, ActionBarCompositor.Source.CLASS_HUD);
      if (baseline != null) {
         player.setFoodLevel(baseline.foodLevel());
         player.setSaturation(Math.min(baseline.saturation(), (float)baseline.foodLevel()));
         player.setExhaustion(baseline.exhaustion());
         player.sendHealthUpdate();
      }
   }

   public static void clearPersistedState(Player player) {
      if (updateHealthModifier(player, false)) {
         HealthDisplayCoordinator.release(player, HealthDisplayCoordinator.Owner.ADVANCED_COMBAT);
         HealthDisplayCoordinator.clearStaleAdvancedDisplay(player);
         player.setFoodLevel(20);
         player.setSaturation(0.0F);
         player.setExhaustion(0.0F);
         player.sendHealthUpdate();
      }
   }

   private static boolean updateHealthModifier(Player player, boolean shouldExist) {
      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      if (maxHealth == null) {
         return false;
      } else {
         NamespacedKey modifierKey = new NamespacedKey(MetadataHandler.PLUGIN, "advanced_combat_health");
         AttributeModifier existing = maxHealth.getModifiers().stream().filter(modifier -> modifier.getKey().equals(modifierKey)).findFirst().orElse(null);
         if (shouldExist == (existing != null)) {
            return false;
         } else {
            HealthPercentagePreserver.during(
               player,
               maxHealth,
               () -> {
                  if (existing != null) {
                     maxHealth.removeModifier(existing);
                  }

                  if (shouldExist) {
                     maxHealth.addModifier(
                        new AttributeModifier(modifierKey, AdvancedCombatRules.maxHealthFlatIncrease(), Operation.ADD_NUMBER, EquipmentSlotGroup.ANY)
                     );
                  }
               }
            );
            return true;
         }
      }
   }

   private static void maintainHunger(Player player) {
      if (player.getFoodLevel() != 19) {
         player.setFoodLevel(19);
      }

      if (player.getSaturation() != 0.0F) {
         player.setSaturation(0.0F);
      }

      if (player.getExhaustion() != 0.0F) {
         player.setExhaustion(0.0F);
      }

      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      double displayedHealth = player.getHealth();
      if (player.isHealthScaled() && maxHealth != null && maxHealth.getValue() > 0.0) {
         displayedHealth = player.getHealth() / maxHealth.getValue() * player.getHealthScale();
      }

      player.sendHealthUpdate(displayedHealth, 20, 0.0F);
   }

   private void enforceActiveHunger() {
      for (UUID playerId : this.activePlayers.keySet().toArray(UUID[]::new)) {
         Player player = Bukkit.getPlayer(playerId);
         if (player != null && player.isOnline() && !player.isDead()) {
            if (!isActive(player)) {
               this.reconcilePlayer(player, false);
            } else {
               capDamageOverTime(player);
               if (player.getFoodLevel() != 19 || player.getSaturation() != 0.0F || player.getExhaustion() != 0.0F) {
                  maintainHunger(player);
               }
            }
         }
      }
   }

   private static void capDamageOverTime(Player player) {
      if (player.getFireTicks() > 60) {
         player.setFireTicks(60);
      }

      PotionEffect poison = player.getPotionEffect(PotionEffectType.POISON);
      if (poison != null && (poison.isInfinite() || poison.getDuration() > 60)) {
         player.removePotionEffect(poison.getType());
         player.addPotionEffect(new PotionEffect(poison.getType(), 60, poison.getAmplifier(), poison.isAmbient(), poison.hasParticles(), poison.hasIcon()));
      }
   }

   private static void renderHud(Player player) {
      ActionBarCompositor.show(player, ActionBarCompositor.Source.CLASS_HUD, ChatColorConverter.convert(hudProvider.apply(player)));
   }

   private static String defaultHud(Player player) {
      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      String current = CombatHealthFormatter.format(player.getHealth());
      String maximum = maxHealth == null ? current : CombatHealthFormatter.format(maxHealth.getValue());
      return "&cHP " + current + "/" + maximum;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
      if (event.getEntity() instanceof Player player && isActive(player)) {
         event.setFoodLevel(19);
         scheduleHungerRefresh(player);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onFoodUseAttempt(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (isActive(player) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
         Block clicked = event.getClickedBlock();
         boolean eatingCake = event.getAction() == Action.RIGHT_CLICK_BLOCK && clicked != null && clicked.getType() == Material.CAKE;
         if (isFood(event.getItem()) || eatingCake) {
            player.setFoodLevel(19);
            player.setSaturation(0.0F);
            player.setExhaustion(0.0F);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onNaturalHealing(EntityRegainHealthEvent event) {
      if (event.getEntity() instanceof Player player && isActive(player)) {
         if (event.getRegainReason() != RegainReason.SATIATED && event.getRegainReason() != RegainReason.REGEN) {
            scheduleHungerRefresh(player);
         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDamaged(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player player && isActive(player)) {
         if (event.getFinalDamage() > 0.0) {
            scheduleHungerRefresh(player);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onFoodConsumed(PlayerItemConsumeEvent event) {
      Player player = event.getPlayer();
      if (isActive(player)) {
         double saturation = saturationOf(event.getItem());
         if (!(saturation <= 0.0)) {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
               if (player.isOnline() && isActive(player) && !player.isDead()) {
                  AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                  if (maxHealth != null) {
                     double healing = saturation * 1.0;
                     applyHealing(player, healing, RegainReason.EATING);
                     maintainHunger(player);
                  }
               }
            });
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onCakeConsumed(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (isActive(player)
         && event.getHand() == EquipmentSlot.HAND
         && event.getAction() == Action.RIGHT_CLICK_BLOCK
         && event.useInteractedBlock() != Result.DENY) {
         Block clicked = event.getClickedBlock();
         if (clicked != null && clicked.getType() == Material.CAKE && clicked.getBlockData() instanceof Cake cake) {
            int var8 = cake.getBites();
            int maximumBites = cake.getMaximumBites();
            Location cakeLocation = clicked.getLocation();
            Bukkit.getScheduler()
               .runTask(
                  MetadataHandler.PLUGIN,
                  () -> {
                     if (player.isOnline() && isActive(player) && !player.isDead()) {
                        Block current = cakeLocation.getBlock();
                        boolean sliceConsumed = current.getType() == Material.CAKE
                           ? current.getBlockData() instanceof Cake currentCake && currentCake.getBites() > var8
                           : var8 >= maximumBites;
                        if (sliceConsumed) {
                           applyHealing(player, 0.4, RegainReason.EATING);
                           maintainHunger(player);
                        }
                     }
                  }
               );
         }
      }
   }

   private static void scheduleHungerRefresh(Player player) {
      Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
         if (player.isOnline() && isActive(player)) {
            maintainHunger(player);
         }
      });
   }

   private static double applyHealing(Player player, double requestedHealing, RegainReason reason) {
      if (Double.isFinite(requestedHealing) && !(requestedHealing <= 0.0) && !player.isDead()) {
         AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
         if (maxHealth == null) {
            return 0.0;
         } else {
            double missingHealth = Math.max(0.0, maxHealth.getValue() - player.getHealth());
            if (missingHealth <= 0.0) {
               return 0.0;
            } else {
               EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, Math.min(requestedHealing, missingHealth), reason);
               Bukkit.getPluginManager().callEvent(event);
               if (!event.isCancelled() && Double.isFinite(event.getAmount()) && !(event.getAmount() <= 0.0)) {
                  double before = player.getHealth();
                  player.setHealth(Math.min(maxHealth.getValue(), before + event.getAmount()));
                  return Math.max(0.0, player.getHealth() - before);
               } else {
                  return 0.0;
               }
            }
         }
      } else {
         return 0.0;
      }
   }

   static double saturationOf(ItemStack itemStack) {
      return AdvancedFoodItems.saturationOf(itemStack);
   }

   private static boolean isFood(ItemStack itemStack) {
      return AdvancedFoodItems.isFood(itemStack);
   }

   @Override
   public void onCombatStarted(UUID playerId) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null && isActive(player)) {
         ActionBarCompositor.show(
            player,
            ActionBarCompositor.Source.COMBAT_TRANSITION,
            ChatColorConverter.convert("&cEntering combat!"),
            ActionBarCompositor.readingDurationTicks("&cEntering combat!")
         );
      }
   }

   @Override
   public void onCombatEnded(UUID playerId) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null && isActive(player)) {
         ActionBarCompositor.show(
            player, ActionBarCompositor.Source.COMBAT_TRANSITION, ChatColorConverter.convert("&aOut of combat &8- &7slow healing resumed")
         );
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
         if (instance == this && player.isOnline()) {
            this.reconcilePlayer(player, false);
         }
      }, 1L);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      if (!DungeonCombatRuntime.isEligiblePlayer(player)) {
         if (this.activePlayers.containsKey(player.getUniqueId())) {
            this.leave(player);
         } else {
            AdvancedCombatStateRecovery.reconcile(player);
         }
      } else {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (instance == this && player.isOnline()) {
               this.reconcilePlayer(player, true);
            }
         });
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
         if (instance == this && player.isOnline()) {
            this.reconcilePlayer(player, false);
            if (player.isOnline() && isActive(player)) {
               scheduleHungerRefresh(player);
            }
         }
      });
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      if (this.activePlayers.containsKey(player.getUniqueId())) {
         this.activePlayers.put(player.getUniqueId(), new AdvancedCombatRuntime.PlayerBaseline(20, 5.0F, 0.0F));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      if (this.activePlayers.containsKey(event.getPlayer().getUniqueId())) {
         this.leave(event.getPlayer());
      }
   }

   private record PlayerBaseline(int foodLevel, float saturation, float exhaustion) {
   }
}
