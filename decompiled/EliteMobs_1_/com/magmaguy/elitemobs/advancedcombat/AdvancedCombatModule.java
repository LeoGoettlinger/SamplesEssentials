package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityCommitEffects;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityContribution;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFailureReason;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityMechanicModifiers;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityResult;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityRuntimeEvidenceLedger;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityRuntimeObservation;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityRuntimeSignal;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilitySemantics;
import com.magmaguy.elitemobs.advancedcombat.abilities.BukkitClassAbilityEngine;
import com.magmaguy.elitemobs.advancedcombat.abilities.ClassAbilityDamage;
import com.magmaguy.elitemobs.advancedcombat.abilities.ClassAbilityEngine;
import com.magmaguy.elitemobs.advancedcombat.abilities.EliteMobsAbilitySemantics;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.challenges.ClassChallengeInstance;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.advancedcombat.input.ClassAbilityInput;
import com.magmaguy.elitemobs.advancedcombat.input.ClassAbilityInputRouter;
import com.magmaguy.elitemobs.advancedcombat.minions.ClassMinionManager;
import com.magmaguy.elitemobs.advancedcombat.participation.ClassParticipationTracker;
import com.magmaguy.elitemobs.advancedcombat.passives.ClassPassiveRuntime;
import com.magmaguy.elitemobs.advancedcombat.passives.FixedPassiveRegistry;
import com.magmaguy.elitemobs.advancedcombat.passives.PassiveAggregate;
import com.magmaguy.elitemobs.advancedcombat.passives.PassiveMechanics;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassAbilityActivationFeedback;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassHudPresentation;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassSkillTutorial;
import com.magmaguy.elitemobs.advancedcombat.progression.ActiveLineageSnapshot;
import com.magmaguy.elitemobs.advancedcombat.progression.AwardResult;
import com.magmaguy.elitemobs.advancedcombat.progression.ClassContentAvailability;
import com.magmaguy.elitemobs.advancedcombat.progression.ClassProgressionForgetResult;
import com.magmaguy.elitemobs.advancedcombat.progression.ClassProgressionModule;
import com.magmaguy.elitemobs.advancedcombat.progression.ClassProgressionSetResult;
import com.magmaguy.elitemobs.advancedcombat.progression.FormProgressSnapshot;
import com.magmaguy.elitemobs.advancedcombat.progression.FoundationLevelSnapshot;
import com.magmaguy.elitemobs.advancedcombat.progression.InputProfile;
import com.magmaguy.elitemobs.advancedcombat.progression.ProfileSnapshot;
import com.magmaguy.elitemobs.advancedcombat.progression.ProgressionCapReason;
import com.magmaguy.elitemobs.advancedcombat.progression.ProgressionReadiness;
import com.magmaguy.elitemobs.advancedcombat.progression.RunLockResult;
import com.magmaguy.elitemobs.advancedcombat.progression.SelectionResult;
import com.magmaguy.elitemobs.advancedcombat.resources.ClassResourceController;
import com.magmaguy.elitemobs.advancedcombat.weapons.AdvancedMagicWeaponIntegration;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.PlayerDataLoadedEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.api.instanced.MatchStartEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.combatsystem.combattag.PlayerCombatState;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.database.JdbcClassProgressionStore;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.quests.objectives.ClassUnlockObjective;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.PlayerIdentityLabelRenderer;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.transport.TransportModule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public final class AdvancedCombatModule implements Listener, ClassAbilityInput, AutoCloseable {
   private static final long CAP_WARNING_INTERVAL_NANOS = 15000000000L;
   private static AdvancedCombatModule instance;
   private final PlayerCombatState combatState;
   private final ClassCatalog catalog = BuiltInClassContent.catalog();
   private final ClassProgressionModule progression;
   private final ClassResourceController resources = new ClassResourceController(BuiltInClassContent.resourceDefinitions());
   private final ClassParticipationTracker participation = new ClassParticipationTracker();
   private final ClassHudPresentation hudPresentation = new ClassHudPresentation();
   private final ClassSkillTutorial skillTutorial = new ClassSkillTutorial();
   private final Set<UUID> practicingPlayers = new HashSet<>();
   private final AbilityRuntimeEvidenceLedger abilityEvidence = new AbilityRuntimeEvidenceLedger();
   private final FixedAbilityRegistry abilityRegistry = BuiltInClassContent.abilityRegistry();
   private final FixedPassiveRegistry passiveRegistry;
   private final ClassPassiveRuntime passiveRuntime;
   private int reconciliationTicks;
   private final Predicate<Player> passiveActive;
   private final ClassAbilityEngine abilityEngine;
   private final ClassAbilityInputRouter inputRouter;
   private final ClassWeaponAffinity weaponAffinity = new ClassWeaponAffinity();
   private final AdvancedMagicWeaponIntegration magicWeaponIntegration;
   private final Map<UUID, UUID> observedRunIds = new HashMap<>();
   private final Map<UUID, Long> lastCapWarning = new HashMap<>();
   private final Set<UUID> progressionFailureWarnings = new LinkedHashSet<>();
   private BukkitTask updateTask;
   private static final long CONTROL_TOGGLE_NOTICE_DELAY_TICKS = 100L;

   private AdvancedCombatModule(PlayerCombatState combatState) {
      this(combatState, availability -> new ClassProgressionModule(BuiltInClassContent.catalog(), playerId -> {
         if (!PlayerData.isDataLoaded(playerId)) {
            return Optional.empty();
         } else {
            Map<SkillType, Integer> levels = new EnumMap<>(SkillType.class);

            for (SkillType skillType : SkillType.values()) {
               levels.put(skillType, PlayerData.getSkillLevel(playerId, skillType));
            }

            return Optional.of(new FoundationLevelSnapshot(playerId, levels));
         }
      }, new JdbcClassProgressionStore(), availability), AdvancedCombatRuntime::isActive);
   }

   AdvancedCombatModule(
      PlayerCombatState combatState, Function<ClassContentAvailability, ClassProgressionModule> progressionFactory, Predicate<Player> passiveActive
   ) {
      this(combatState, progressionFactory, passiveActive, ClassAbilityDamage.NATIVE);
   }

   AdvancedCombatModule(
      PlayerCombatState combatState,
      Function<ClassContentAvailability, ClassProgressionModule> progressionFactory,
      Predicate<Player> passiveActive,
      ClassAbilityDamage.Delivery damageDelivery
   ) {
      this.combatState = Objects.requireNonNull(combatState, "combatState");
      this.passiveActive = Objects.requireNonNull(passiveActive, "passiveActive");
      this.magicWeaponIntegration = new AdvancedMagicWeaponIntegration(MetadataHandler.PLUGIN);
      this.progression = Objects.requireNonNull(progressionFactory.apply(this.magicWeaponIntegration::unavailableReason), "progression");
      this.passiveRegistry = BuiltInClassContent.passiveRegistry();
      this.passiveRuntime = new ClassPassiveRuntime(this::passivesFor, passiveActive);
      EliteMobsAbilitySemantics semantics = new EliteMobsAbilitySemantics(
         this::applyThreat,
         this::recordAbilityContribution,
         this.passiveRuntime::healingDoneMultiplier,
         this.resources::grant,
         this::activeFormIds,
         this::clearThreatLeases,
         this::abilityMechanicModifiers,
         (player, signal, ticks) -> {
            if (signal == AbilityRuntimeSignal.WARD_BROKEN) {
               this.passiveRuntime.signalWardBroken(player, ticks);
            }
         },
         this.abilityEvidence::record
      );
      this.abilityEngine = new BukkitClassAbilityEngine(
         MetadataHandler.PLUGIN, this.abilityRegistry, semantics, this::activeMinionProfile, new ClassMinionManager.MinionDamageEvaluator() {
            @Override
            public double evaluate(Player owner, EliteEntity target, double rawDamage, CombatDamageContext.ClassAbilityDamageDomain domain) {
               return rawDamage * AdvancedCombatModule.this.passiveRuntime.outgoingClassAbilityDamageMultiplier(owner, target, domain);
            }

            @Override
            public double healingDoneMultiplier(Player owner) {
               return AdvancedCombatModule.this.passiveRuntime.healingDoneMultiplier(owner);
            }
         }, damageDelivery
      );
      this.inputRouter = new ClassAbilityInputRouter(MetadataHandler.PLUGIN, this);
   }

   public static AdvancedCombatModule initialize(PlayerCombatState combatState) {
      if (instance != null) {
         throw new IllegalStateException("[Alpha] Advanced Combat System module is already initialized");
      } else {
         AdvancedCombatModule module = new AdvancedCombatModule(combatState);
         instance = module;
         ClassAbilityEligibility.install(module::controlsAlwaysAvailable, module.inputRouter::controlsEnabled);
         module.magicWeaponIntegration.start();
         module.startGameplay();
         PlayerIdentityLabelRenderer.installClassLabelProvider(module::classLabel);
         AdvancedCombatRuntime.installHudProvider(module::renderHud);

         for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerData.isDataLoaded(player.getUniqueId())) {
               module.load(player);
            }
         }

         return module;
      }
   }

   public static boolean isInitialized() {
      return instance != null;
   }

   void startGameplay() {
      if (this.updateTask != null) {
         throw new IllegalStateException("Combat gameplay is already started");
      } else {
         Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
         Bukkit.getPluginManager().registerEvents(this.passiveRuntime, MetadataHandler.PLUGIN);
         this.passiveRuntime.registerKnockback(MetadataHandler.PLUGIN);
         Bukkit.getPluginManager().registerEvents(this.inputRouter, MetadataHandler.PLUGIN);
         Bukkit.getPluginManager().registerEvents(this.weaponAffinity, MetadataHandler.PLUGIN);
         this.updateTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::tick, 1L, 1L);
      }
   }

   public static List<AbilityRuntimeObservation> abilityEvidence(UUID playerId) {
      return instance == null ? List.of() : instance.abilityEvidence.snapshot(playerId);
   }

   public static List<AbilityRuntimeObservation> drainAbilityEvidence(UUID playerId) {
      return instance == null ? List.of() : instance.abilityEvidence.drain(playerId);
   }

   public static AdvancedCombatModule get() {
      if (instance == null) {
         throw new IllegalStateException("[Alpha] Advanced Combat System is not initialized");
      } else {
         return instance;
      }
   }

   public static boolean isAbilityGestureOpen(UUID playerId) {
      return instance != null && instance.inputRouter != null && instance.inputRouter.isGestureOpen(playerId);
   }

   public static Optional<ClassResourceController.Snapshot> resourceSnapshot(UUID playerId) {
      return instance == null ? Optional.empty() : instance.resources.snapshot(playerId);
   }

   public static Optional<AdvancedCombatModule.AbilityResourceSnapshot> abilityResourceSnapshot(Player player, AbilitySlot slot) {
      return activeClassLineageSnapshot(player.getUniqueId()).map(lineage -> {
         FixedAbilitySpec spec = instance.abilityRegistry.require(abilityId(lineage, slot));
         double cost = instance.abilityCost(player, spec);
         return new AdvancedCombatModule.AbilityResourceSnapshot(spec.id(), cost, instance.resources.canAfford(player, cost));
      });
   }

   public static Optional<FormProgressSnapshot> classProgressSnapshot(UUID playerId) {
      return instance == null ? Optional.empty() : instance.progression.activeSnapshot(playerId).map(ClassProgressionModule.ActiveProfileSnapshot::activeForm);
   }

   public static void shutdownIfInitialized() {
      if (instance != null) {
         instance.close();
      }
   }

   public ClassCatalog catalog() {
      return this.catalog;
   }

   public static Optional<ClassLineage> activeClassLineageSnapshot(UUID playerId) {
      return instance == null
         ? Optional.empty()
         : instance.progression
            .activeSnapshot(playerId)
            .flatMap(ClassProgressionModule.ActiveProfileSnapshot::optionalActiveLineage)
            .map(active -> instance.catalog.lineageOf(active.activeFormId()));
   }

   public Optional<ProfileSnapshot> profile(UUID playerId) {
      return this.progression.snapshot(playerId);
   }

   public boolean canChallenge(Player player, String formId) {
      return this.progression.canChallenge(player.getUniqueId(), formId);
   }

   public boolean completeChallenge(Player player, String formId) {
      if (!this.progression.completeChallenge(player.getUniqueId(), formId)) {
         return false;
      } else {
         ClassUnlockObjective.refresh(player);
         return true;
      }
   }

   public void onClassTrialEnded(Player player) {
      this.reconcileRunLock(player);
      this.reconcileAfterClassSelection(player);
   }

   public boolean mechanicsActive(Player player) {
      return this.inputRouter.controlsEnabled(player);
   }

   @Override
   public boolean hasActiveClass(Player player) {
      return this.progression.activeSnapshot(player.getUniqueId()).flatMap(ClassProgressionModule.ActiveProfileSnapshot::optionalActiveLineage).isPresent();
   }

   @Override
   public boolean fLayerSupported(Player player) {
      return !BedrockChecker.isBedrock(player);
   }

   public Set<SkillType> activeClassWeaponAffinities(Player player) {
      return this.progression
         .activeSnapshot(player.getUniqueId())
         .flatMap(ClassProgressionModule.ActiveProfileSnapshot::optionalActiveLineage)
         .map(active -> Set.copyOf(this.catalog.require(active.activeFormId()).weaponAffinities()))
         .orElse(Set.of());
   }

   @Override
   public boolean controlsAlwaysAvailable(Player player) {
      return isWaitingForMatch(player) || DungeonCombatRuntime.isEligiblePlayer(player);
   }

   private static boolean isWaitingForMatch(Player player) {
      MatchInstance match = PlayerData.getMatchInstance(player);
      return match != null && match.isWaitingPlayer(player);
   }

   @Override
   public boolean outsideControlsAllowed() {
      return AdvancedCombatSystemConfig.isAllowClassAbilitiesOutsideEliteMobsWorlds();
   }

   @Override
   public void onControlModeChanged(Player player) {
      this.reconcilePlayer(player);
   }

   public InputProfile activeInputProfile(Player player) {
      return this.progression
         .activeSnapshot(player.getUniqueId())
         .map(ClassProgressionModule.ActiveProfileSnapshot::activeInputProfile)
         .orElse(InputProfile.DEFAULT);
   }

   @Override
   public String abilityName(Player player, AbilitySlot slot) {
      return this.progression
         .activeSnapshot(player.getUniqueId())
         .flatMap(ClassProgressionModule.ActiveProfileSnapshot::optionalActiveLineage)
         .map(active -> abilityName(this.catalog.lineageOf(active.activeFormId()), slot))
         .orElseGet(() -> {
            return switch (slot) {
               case MOBILITY -> "Mobility";
               case SIGNATURE -> "Signature";
               case UTILITY -> "Utility";
            };
         });
   }

   public SelectionResult selectForm(Player player, String formId) {
      if (!this.mayChangeRunSelection(player)) {
         return new SelectionResult(SelectionResult.Status.LOCKED_FORM, this.progression.snapshot(player.getUniqueId()).orElse(null));
      } else {
         SelectionResult result = this.progression.selectForm(player.getUniqueId(), formId);
         if (result.accepted()) {
            this.reconcileAfterClassSelection(player);
         }

         return result;
      }
   }

   public ClassProgressionSetResult setClassLevelForAdministration(Player player, String formId, int effectiveLevel) {
      ClassProgressionSetResult result = this.progression.setLineageForAdministration(player.getUniqueId(), formId, effectiveLevel);
      if (result.applied()) {
         this.reconcileAfterClassSelection(player);
      }

      return result;
   }

   public ClassProgressionForgetResult forgetClassForAdministration(Player player, String formId) {
      if (PlayerData.getMatchInstance(player) != null) {
         return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.RUN_LOCKED, 0, false);
      } else {
         this.reconcileRunLock(player);
         ClassProgressionForgetResult result = this.progression.forgetForAdministration(player.getUniqueId(), formId);
         if (result.status() == ClassProgressionForgetResult.Status.APPLIED && result.selectionCleared()) {
            this.reconcileAfterClassSelection(player);
         }

         return result;
      }
   }

   public int scaleAllClassesForAdministration(Player player, int effectiveLevel) {
      int formsAtLevel = this.progression.setAllLineagesForAdministration(player.getUniqueId(), Math.max(1, Math.min(100, effectiveLevel)));
      if (formsAtLevel > 0) {
         this.reconcileAfterClassSelection(player);
      }

      return formsAtLevel;
   }

   public static void scaleClassesForDebugLoadout(Player player, int effectiveLevel) {
      if (!isInitialized()) {
         Logger.sendMessage(player, "&eClasses were not changed: the [Alpha] Advanced Combat System is disabled.");
      } else {
         AdvancedCombatModule module = get();
         int formsAtLevel = module.scaleAllClassesForAdministration(player, effectiveLevel);
         if (formsAtLevel > 0) {
            Logger.sendMessage(player, "&7Scaled &f" + formsAtLevel + " &7class branch(es) to level &f" + effectiveLevel + "&7. Pick one with &f/em class&7.");
         } else {
            UUID playerId = player.getUniqueId();
            if (module.progression.lockedRunId(playerId).isPresent()) {
               Logger.sendMessage(player, "&eClasses were not changed: they are locked for your current dungeon or trial run.");
            } else if (module.progression.snapshot(playerId).isEmpty()) {
               Logger.sendMessage(player, "&eClasses were not changed: your class data is not loaded yet. Try again in a moment.");
            } else {
               Logger.sendMessage(player, "&eClasses were not changed at level &f" + effectiveLevel + "&e.");
            }
         }
      }
   }

   public SelectionResult clearSelectedForm(Player player) {
      if (!this.mayChangeRunSelection(player)) {
         return new SelectionResult(SelectionResult.Status.LOCKED_FORM, this.progression.snapshot(player.getUniqueId()).orElse(null));
      } else {
         SelectionResult result = this.progression.clearSelectedForm(player.getUniqueId());
         if (result.accepted()) {
            this.reconcileAfterClassSelection(player);
         }

         return result;
      }
   }

   @Override
   public AbilityResult useAbility(Player player, AbilitySlot slot) {
      if (TransportModule.isInTransit(player)) {
         sendFeedback(player, "&eClass skills are unavailable during transport.");
         return AbilityResult.failure("transport." + slot.name().toLowerCase(Locale.ROOT), AbilityFailureReason.INVALID_PLAYER);
      } else {
         Optional<ClassProgressionModule.ActiveProfileSnapshot> optionalProfile = this.progression.activeSnapshot(player.getUniqueId());
         if (!optionalProfile.isEmpty() && this.inputRouter.controlsEnabled(player, optionalProfile.get().activeLineage() != null)) {
            ClassProgressionModule.ActiveProfileSnapshot profile = optionalProfile.get();
            ActiveLineageSnapshot active = profile.activeLineage();
            if (active == null) {
               sendFeedback(player, "&eSelect an unlocked class with &f/em class&e first.");
               return AbilityResult.failure("unselected." + slot.name().toLowerCase(Locale.ROOT), AbilityFailureReason.INVALID_PLAYER);
            } else {
               ClassLineage lineage = this.catalog.lineageOf(active.activeFormId());
               FixedAbilitySpec abilitySpec = this.abilityRegistry.require(abilityId(lineage, slot));
               double abilityCost = this.abilityCost(player, abilitySpec, active);
               this.reconcilePlayer(player, optionalProfile);
               boolean practicing = isWaitingForMatch(player);
               if (!this.resources.canAfford(player, abilityCost)) {
                  this.observeFailedCast(player, abilitySpec);
                  ClassResourceController.Snapshot resource = this.resources.snapshot(player.getUniqueId()).orElse(null);
                  String amount = resource == null ? "0" : String.valueOf((int)Math.floor(resource.amount()));
                  sendFeedback(player, "&cNot enough " + resourceName(lineage.resourceType()) + " &7(" + amount + "/" + (int)abilityCost + " required).");
                  return AbilityResult.failure(abilityId(lineage, slot), AbilityFailureReason.INVALID_PLAYER);
               } else {
                  AbilityResult result = this.abilityEngine.execute(player, lineage, slot, active.activeEffectiveLevel());
                  if (!result.successful()) {
                     this.observeFailedCast(player, abilitySpec);
                     sendFeedback(player, failureMessage(abilityName(lineage, slot), result.failureReason()));
                     return result;
                  } else if (!this.resources.trySpend(player, abilityCost)) {
                     throw new IllegalStateException("Class resource changed during a main-thread ability execution");
                  } else {
                     AbilityCommitEffects commitEffects = AbilityCommitEffects.resolve(abilitySpec, active.activeEffectiveLevel());
                     if (commitEffects.resourceGrant() > 0.0) {
                        this.resources.grant(player, commitEffects.resourceGrant());
                     }

                     this.abilityEvidence
                        .record(
                           new AbilityRuntimeObservation(
                              AbilityRuntimeObservation.Kind.RESOURCE_SPENT,
                              player.getUniqueId(),
                              null,
                              abilitySpec.id(),
                              practicing ? 0.0 : abilityCost,
                              0,
                              1,
                              Set.of(),
                              abilitySpec.executionTraits().mechanics()
                           )
                        );
                     if (commitEffects.resourceGrant() > 0.0) {
                        this.abilityEvidence
                           .record(
                              new AbilityRuntimeObservation(
                                 AbilityRuntimeObservation.Kind.RESOURCE_GAINED,
                                 player.getUniqueId(),
                                 null,
                                 abilitySpec.id(),
                                 practicing ? 0.0 : commitEffects.resourceGrant(),
                                 0,
                                 1,
                                 Set.of(),
                                 abilitySpec.executionTraits().mechanics()
                              )
                           );
                     }

                     this.abilityEvidence
                        .record(
                           new AbilityRuntimeObservation(
                              AbilityRuntimeObservation.Kind.RESOURCE_DELTA,
                              player.getUniqueId(),
                              null,
                              abilitySpec.id(),
                              practicing ? 0.0 : commitEffects.resourceGrant() - abilityCost,
                              0,
                              1,
                              Set.of(),
                              abilitySpec.executionTraits().mechanics()
                           )
                        );
                     this.recordAbilityContribution(player, result.abilityId(), result.contribution());
                     sendFeedback(
                        player,
                        ClassAbilityActivationFeedback.message(lineage, slot, abilitySpec, active.activeEffectiveLevel(), practicing ? 0.0 : abilityCost)
                     );
                     this.skillTutorial.successfulCast(player, this.progression, slot);
                     return result;
                  }
               }
            }
         } else {
            sendFeedback(player, "&cClass controls are not active here.");
            return AbilityResult.failure("unavailable." + slot.name().toLowerCase(Locale.ROOT), AbilityFailureReason.INVALID_PLAYER);
         }
      }
   }

   private void observeFailedCast(Player player, FixedAbilitySpec spec) {
      this.abilityEvidence
         .record(
            new AbilityRuntimeObservation(
               AbilityRuntimeObservation.Kind.CAST_FAILED, player.getUniqueId(), null, spec.id(), 0.0, 0, 1, Set.of(), spec.executionTraits().mechanics()
            )
         );
   }

   private double abilityCost(Player player, FixedAbilitySpec spec) {
      return this.abilityCost(
         player, spec, this.progression.activeSnapshot(player.getUniqueId()).map(ClassProgressionModule.ActiveProfileSnapshot::activeLineage).orElse(null)
      );
   }

   private double abilityCost(Player player, FixedAbilitySpec spec, ActiveLineageSnapshot active) {
      double multiplier = this.passiveRuntime.mechanics(player).abilityCostMultiplier();
      double reduction = (active == null
            ? PassiveAggregate.NEUTRAL
            : PassiveAggregate.resolve(this.catalog.lineageOf(active.activeFormId()), active, this.passiveRegistry))
         .abilityCostReductionFraction();
      return Math.max(1.0, spec.resourceCost() * multiplier * (1.0 - reduction));
   }

   public AwardResult awardClassXp(Player player, long rawReward) {
      if (!AdvancedCombatRuntime.isActive(player)) {
         return new AwardResult(AwardResult.Status.NO_SELECTED_FORM, null, rawReward, 0L, Math.max(0L, rawReward), 0L, 0L, 0, 0, 0, 0, 0, List.of());
      } else {
         AwardResult result = this.progression.award(player.getUniqueId(), rawReward);
         if (result.changed()) {
            if (result.currentLocalLevel() > result.previousLocalLevel()) {
               ClassFormDefinition form = this.catalog.require(result.formId());
               player.sendMessage(
                  ChatColorConverter.convert("&b&lClass level up! &f" + form.displayName() + " &7is now level &f" + result.currentEffectiveLevel() + "&7.")
               );
            }

            CombatLevelDisplay.updateDisplay(player);
         }

         if (result.status() == AwardResult.Status.AT_CAP
            || result.status() == AwardResult.Status.PARTIALLY_APPLIED
            || result.status() == AwardResult.Status.APPLIED && result.currentLocalLevel() >= result.localCap()) {
            this.warnAtCap(player, result);
         }

         return result;
      }
   }

   public Set<Player> meaningfulParticipants(EliteEntity elite) {
      return this.participation.participants(elite);
   }

   private void load(Player player) {
      PlayerData loadedData = PlayerData.getPlayerData(player.getUniqueId());
      if (loadedData != null && PlayerData.isDataLoaded(player.getUniqueId())) {
         this.progression
            .load(player.getUniqueId())
            .whenComplete(
               (snapshot, failure) -> Bukkit.getScheduler()
                  .runTask(
                     MetadataHandler.PLUGIN,
                     () -> {
                        if (player.isOnline()
                           && Bukkit.getPlayer(player.getUniqueId()) == player
                           && instance == this
                           && PlayerData.getPlayerData(player.getUniqueId()) == loadedData) {
                           if (failure != null) {
                              this.progressionFailureWarnings.add(player.getUniqueId());
                              Logger.warn(
                                 "Could not load [Alpha] Advanced Combat System class data for " + player.getName() + ": " + rootCause(failure).getMessage()
                              );
                              player.sendMessage(
                                 ChatColorConverter.convert(
                                    "&cYour [Alpha] Advanced Combat System class data could not be loaded. Please report this to the developer."
                                 )
                              );
                           } else {
                              this.progressionFailureWarnings.remove(player.getUniqueId());
                              ClassUnlockObjective.refresh(player);
                              CombatLevelDisplay.updateDisplay(player);
                              this.reconcileRunLock(player);
                              this.reconcilePlayer(player);
                           }
                        }
                     }
                  )
            );
      }
   }

   private void tick() {
      Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
      this.passiveRuntime.tickItemUse(onlinePlayers);
      if (this.reconciliationTicks > 0) {
         this.reconciliationTicks--;
      } else {
         this.reconciliationTicks = 19;
         this.resources.tick(onlinePlayers, this.combatState::isInCombat);

         for (Player player : onlinePlayers) {
            if (!this.progression.isReady(player.getUniqueId())) {
               ProgressionReadiness readiness = this.progression.readiness(player.getUniqueId());
               this.reconcilePlayer(player);
               if (readiness == ProgressionReadiness.FAILED && this.progressionFailureWarnings.add(player.getUniqueId())) {
                  Throwable failure = this.progression.failure(player.getUniqueId()).orElse(null);
                  Logger.warn(
                     "[Alpha] Advanced Combat System class progression became unavailable for "
                        + player.getName()
                        + (failure == null ? "." : ": " + rootCause(failure).getMessage())
                  );
                  player.sendMessage(
                     ChatColorConverter.convert(
                        "&cYour [Alpha] Advanced Combat System class data became unavailable. Class mechanics were disabled safely; please report this to the developer."
                     )
                  );
               }

               if (readiness == ProgressionReadiness.UNLOADED && PlayerData.isDataLoaded(player.getUniqueId())) {
                  this.load(player);
               }
            } else {
               this.reconcileRunLock(player);
               this.reconcilePlayer(player);
            }
         }
      }
   }

   private void reconcileRunLock(Player player) {
      UUID playerId = player.getUniqueId();
      DungeonInstance dungeonInstance = PlayerData.getMatchInstance(player) instanceof DungeonInstance instance ? instance : null;
      UUID runId = dungeonInstance == null ? null : player.getWorld().getUID();
      if (PlayerData.getMatchInstance(player) instanceof ClassChallengeInstance trial) {
         runId = trial.runId();
      }

      UUID lockedRunId = this.progression.lockedRunId(playerId).orElse(null);
      UUID previousRunId = runId == null ? this.observedRunIds.remove(playerId) : this.observedRunIds.put(playerId, runId);
      if (lockedRunId != null && !lockedRunId.equals(runId)) {
         this.progression.unlockRun(playerId, lockedRunId);
         this.abilityEngine.deactivate(player);
         this.resources.close(player);
         lockedRunId = null;
      }

      if (runId != null && lockedRunId == null && !runId.equals(previousRunId)) {
         RunLockResult result = this.progression.lockRun(playerId, runId);
         if (result.status() == RunLockResult.Status.NO_SELECTED_FORM) {
            player.sendMessage(ChatColorConverter.convert("&eNo class was selected when this instance began, so class abilities are unavailable for this run."));
         }
      }
   }

   private void reconcilePlayer(Player player) {
      this.reconcilePlayer(player, this.progression.activeSnapshot(player.getUniqueId()));
   }

   private void reconcilePlayer(Player player, Optional<ClassProgressionModule.ActiveProfileSnapshot> optional) {
      ActiveLineageSnapshot active = optional.map(ClassProgressionModule.ActiveProfileSnapshot::activeLineage).orElse(null);
      boolean controlsActive = this.inputRouter.controlsEnabled(player, active != null);
      if (!controlsActive) {
         this.endLobbyPractice(player);
         if (this.resources.snapshot(player.getUniqueId()).isPresent()) {
            this.abilityEngine.deactivate(player);
         }

         this.resources.close(player);
         this.passiveRuntime.reconcile(player, false);
      } else {
         ClassResourceType resourceType = this.catalog.lineageOf(active.activeFormId()).resourceType();
         UUID runToken = optional.get().lockedRunId();
         if (!this.resources.isOpen(player.getUniqueId(), resourceType, runToken)) {
            this.resources.open(player, resourceType, runToken);
         }

         this.passiveRuntime.reconcile(player, this.passiveActive.test(player));
         boolean waiting = isWaitingForMatch(player);
         if (waiting) {
            this.practicingPlayers.add(player.getUniqueId());
            this.resources.setPracticeMode(player, true);
            if (this.fLayerSupported(player)) {
               ActionBarCompositor.show(
                  player, ActionBarCompositor.Source.CONTEXT_HINT, ChatColorConverter.convert("&7F,F: Mobility | F+LMB: Signature | F+RMB: Utility")
               );
            }
         } else {
            this.endLobbyPractice(player);
         }

         if (this.fLayerSupported(player)) {
            this.skillTutorial.prompt(player, this.progression, waiting);
         }
      }
   }

   private void endLobbyPractice(Player player) {
      this.resources.setPracticeMode(player, false);
      if (this.practicingPlayers.remove(player.getUniqueId())) {
         this.abilityEngine.deactivate(player);
         this.hudPresentation.discard(player.getUniqueId());
         ActionBarCompositor.clear(player, ActionBarCompositor.Source.CONTEXT_HINT);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onMatchStart(MatchStartEvent event) {
      event.getInstance().getPlayers().forEach(this::endLobbyPractice);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onMatchLeave(MatchLeaveEvent event) {
      this.endLobbyPractice(event.getPlayer());
   }

   private void reconcileAfterClassSelection(Player player) {
      this.abilityEngine.deactivate(player);
      this.reconcilePlayer(player);
      CombatLevelDisplay.updateDisplay(player);
   }

   private boolean mayChangeRunSelection(Player player) {
      if (!(PlayerData.getMatchInstance(player) instanceof DungeonInstance) && !(PlayerData.getMatchInstance(player) instanceof ClassChallengeInstance)) {
         if (this.combatState.isInCombat(player.getUniqueId())) {
            player.sendMessage(ChatColorConverter.convert("&cYou cannot change classes or controls while in combat."));
            return false;
         } else {
            return true;
         }
      } else {
         player.sendMessage(ChatColorConverter.convert("&cYour class and input profile are locked until this run ends."));
         return false;
      }
   }

   private Optional<String> classLabel(UUID playerId) {
      return this.progression.activeSnapshot(playerId).flatMap(snapshot -> {
         ActiveLineageSnapshot active = snapshot.activeLineage();
         if (active == null) {
            return Optional.empty();
         } else {
            ClassFormDefinition form = this.catalog.require(active.activeFormId());
            return Optional.of(ClassIdentityLabelFormatter.format(form.displayName(), active.activeEffectiveLevel()));
         }
      });
   }

   private String renderHud(Player player) {
      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      double health = player.getHealth();
      double maximumHealth = maxHealth == null ? health : maxHealth.getValue();
      String healthDisplay = "&cHP " + CombatHealthFormatter.format(health) + "/" + CombatHealthFormatter.format(maximumHealth);
      Optional<ClassProgressionModule.ActiveProfileSnapshot> optional = this.progression.activeSnapshot(player.getUniqueId());
      if (!optional.isEmpty() && optional.get().activeLineage() != null) {
         ClassProgressionModule.ActiveProfileSnapshot profile = optional.get();
         ActiveLineageSnapshot active = profile.activeLineage();
         ClassLineage lineage = this.catalog.lineageOf(active.activeFormId());
         ClassResourceController.Snapshot resource = this.resources.snapshot(player.getUniqueId()).orElse(null);
         double amount = resource == null ? 0.0 : resource.amount();
         double maximum = resource == null ? BuiltInClassContent.resourceDefinitions().get(lineage.resourceType()).maximum() : resource.maximum();
         String controls = "&7F,F: Mobility | F+LMB: Signature | F+RMB: Utility";
         String compact = healthDisplay + " &8| &e" + resourceName(lineage.resourceType()) + " " + Math.round(amount) + "/" + Math.round(maximum);
         if (isWaitingForMatch(player)) {
            return compact;
         } else {
            String reminder = "&b[" + active.activeEffectiveLevel() + "] " + lineage.activeForm().displayName() + " &8| " + controls;
            return this.hudPresentation
               .render(
                  player.getUniqueId(),
                  new ClassHudPresentation.Vitals(health, maximumHealth, player.getAbsorptionAmount(), amount, maximum, active.activeFormId()),
                  compact,
                  reminder,
                  System.nanoTime()
               );
         }
      } else {
         return this.hudPresentation
            .render(
               player.getUniqueId(),
               new ClassHudPresentation.Vitals(health, maximumHealth, player.getAbsorptionAmount(), 0.0, 0.0, null),
               healthDisplay,
               "&7No active class &8| &e/em class",
               System.nanoTime()
            );
      }
   }

   private PassiveAggregate passivesFor(UUID playerId) {
      Optional<ClassProgressionModule.ActiveProfileSnapshot> optional = this.progression.activeSnapshot(playerId);
      if (!optional.isEmpty() && optional.get().activeLineage() != null) {
         ActiveLineageSnapshot active = optional.get().activeLineage();
         return PassiveAggregate.resolve(this.catalog.lineageOf(active.activeFormId()), active, this.passiveRegistry);
      } else {
         return PassiveAggregate.NEUTRAL;
      }
   }

   private AbilityMechanicModifiers abilityMechanicModifiers(Player player) {
      PassiveMechanics mechanics = this.passiveRuntime.mechanics(player);
      return new AbilityMechanicModifiers(
         mechanics.burstHealingMultiplier(),
         mechanics.periodicHealingMultiplier(),
         mechanics.groupedEnemyHealingMultiplier(),
         mechanics.periodicDurationMultiplier(),
         mechanics.shieldStrengthMultiplier(),
         mechanics.redirectedDamageMultiplier(),
         mechanics.controlDurationMultiplier(),
         mechanics.controlPotencyMultiplier()
      );
   }

   private List<String> activeFormIds(Player player) {
      Optional<ClassProgressionModule.ActiveProfileSnapshot> optional = this.progression.activeSnapshot(player.getUniqueId());
      return !optional.isEmpty() && optional.get().activeLineage() != null
         ? List.copyOf(this.catalog.lineageOf(optional.get().activeLineage().activeFormId()).formIds())
         : List.of();
   }

   private Optional<ClassMinionManager.OwnerProfile> activeMinionProfile(Player player) {
      return player != null && ClassAbilityEligibility.isEligible(player)
         ? this.progression
            .activeSnapshot(player.getUniqueId())
            .flatMap(ClassProgressionModule.ActiveProfileSnapshot::optionalActiveLineage)
            .map(active -> new ClassMinionManager.OwnerProfile(active.activeFormId(), active.activeEffectiveLevel()))
         : Optional.empty();
   }

   private void applyThreat(AbilitySemantics.ThreatRequest request) {
      List<EliteEntity> affected = new ArrayList<>();

      for (LivingEntity enemy : request.enemies()) {
         EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
         if (elite != null) {
            elite.addThreat(request.caster(), request.amountPerEnemy());
            elite.forceTarget(request.caster(), request.forcedTargetTicks());
            affected.add(elite);
         }
      }

      if (!affected.isEmpty()) {
         this.resources.onTaunt(request.caster(), affected.size());
         this.participation.recordThreat(request.caster(), affected);
      }
   }

   private void clearThreatLeases(Player player) {
      UUID playerId = player.getUniqueId();

      for (EliteEntity elite : EntityTracker.getEliteMobEntities().values()) {
         elite.clearForcedTarget(playerId);
      }
   }

   private void recordAbilityContribution(Player caster, String abilityId, AbilityContribution contribution) {
      if (contribution != null && contribution.isMeaningful()) {
         if (contribution.effectiveHealing() > 0.0) {
            this.resources.onEffectiveHealing(caster, contribution.effectiveHealing());
         }

         if (contribution.mitigatedDamage() > 0.0) {
            this.resources.onDamagePrevented(caster, contribution.mitigatedDamage());
         }

         if (contribution.effectiveHealing() > 0.0 || contribution.mitigatedDamage() > 0.0) {
            this.participation.recordSupport(caster);
         }
      }
   }

   private void warnAtCap(Player player, AwardResult result) {
      long now = System.nanoTime();
      Long previous = this.lastCapWarning.get(player.getUniqueId());
      if (previous == null || now - previous >= 15000000000L) {
         this.lastCapWarning.put(player.getUniqueId(), now);
         ClassFormDefinition form = this.catalog.require(result.formId());
         String message;
         if (result.capReason() == ProgressionCapReason.BAND_COMPLETE) {
            String choices = String.join(" or ", this.catalog.childrenOf(form.id()).stream().map(ClassFormDefinition::displayName).toList());
            message = choices.isBlank()
               ? "&eClass XP paused at &f" + form.displayName() + " " + result.currentEffectiveLevel() + "&e, the current v0 cap. Overflow XP is not banked."
               : "&eClass XP paused at &f"
                  + form.displayName()
                  + " "
                  + result.currentEffectiveLevel()
                  + "&e. Choose &f"
                  + choices
                  + " &ewith &f/em class&e; overflow XP is not banked.";
         } else {
            String skills = result.limitingSkills().isEmpty()
               ? "its required skills"
               : String.join(" and ", result.limitingSkills().stream().map(SkillType::getDisplayName).toList());
            message = "&eClass XP paused at &f"
               + form.displayName()
               + " "
               + result.currentEffectiveLevel()
               + "&e because &f"
               + skills
               + " &eis the limiting requirement. Raise it to progress; overflow XP is not banked.";
         }

         player.sendMessage(ChatColorConverter.convert(message));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
      if (!CombatDamageContext.isDamageTransferActive()) {
         if (this.mechanicsActive(event.getPlayer()) && !(event.getDamage() <= 0.0)) {
            this.resources.onDamageDealt(event.getPlayer(), event.getDamage());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDamagesNonElite(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof LivingEntity target
         && !(target instanceof ArmorStand)
         && !target.hasMetadata("NPC")
         && EntityTracker.getNPCEntity(target) == null
         && !(event.getFinalDamage() <= 0.0)) {
         if (EntityTracker.getEliteMobEntity(target) == null) {
            Player attacker = event.getDamager() instanceof Player player
               ? player
               : (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter ? shooter : null);
            if (attacker != null && !attacker.equals(target) && this.mechanicsActive(attacker)) {
               this.resources.onDamageDealt(attacker, event.getFinalDamage());
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEliteDamagesPlayer(PlayerDamagedByEliteMobEvent event) {
      if (this.mechanicsActive(event.getPlayer()) && !(event.getDamage() <= 0.0)) {
         this.resources.onDamageReceived(event.getPlayer(), event.getDamage());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerTakesDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player player && this.mechanicsActive(player) && event.getFinalDamage() > 0.0) {
         this.resources.observeDamage(player);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteDeath(EliteMobDeathEvent event) {
      this.participation.clear(event.getEliteEntity());
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerDataLoaded(PlayerDataLoadedEvent event) {
      this.load(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      this.hudPresentation.discard(player.getUniqueId());
      this.abilityEngine.deactivate(player);
      Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
         if (player.isOnline() && instance == this) {
            this.reconcilePlayer(player);
         }
      });
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      this.hudPresentation.discard(player.getUniqueId());
      this.abilityEngine.deactivate(player);
      this.resources.discard(player);
      this.passiveRuntime.discard(player);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();

      try {
         this.endLobbyPractice(player);
         this.skillTutorial.discard(playerId);
         this.hudPresentation.discard(playerId);
         boolean retainRunState = this.progression.lockedRunId(playerId).isPresent();
         this.abilityEngine.deactivate(player);
         if (retainRunState) {
            this.resources.suspend(player);
         } else {
            this.resources.discard(player);
         }

         this.passiveRuntime.discard(player);
         this.participation.discard(playerId);
         this.observedRunIds.remove(playerId);
         this.lastCapWarning.remove(playerId);
         this.progressionFailureWarnings.remove(playerId);
         String playerName = player.getName();
         CompletableFuture<Void> flush = this.progression.flush(playerId);
         this.progression.unload(playerId);
         flush.whenComplete((ignored, failure) -> {
            if (failure != null) {
               Logger.warn("Could not flush [Alpha] Advanced Combat System class progression for " + playerName + ": " + rootCause(failure).getMessage());
            }
         });
      } finally {
         this.abilityEvidence.discard(playerId);
      }
   }

   @Override
   public void close() {
      if (this.updateTask != null) {
         this.updateTask.cancel();
         this.updateTask = null;
      }

      HandlerList.unregisterAll(this);
      this.passiveRuntime.shutdown();
      this.inputRouter.shutdown();
      HandlerList.unregisterAll(this.inputRouter);
      this.magicWeaponIntegration.close();

      for (Player player : Bukkit.getOnlinePlayers()) {
         this.abilityEngine.deactivate(player);
      }

      this.abilityEngine.close();
      this.resources.shutdown();
      this.hudPresentation.clear();
      this.skillTutorial.clear();

      for (Player player : Bukkit.getOnlinePlayers()) {
         ActionBarCompositor.clear(player, ActionBarCompositor.Source.CONTEXT_HINT);
      }

      this.practicingPlayers.clear();
      this.participation.clearAll();
      this.observedRunIds.clear();
      this.lastCapWarning.clear();
      this.progressionFailureWarnings.clear();
      this.abilityEvidence.clear();
      AdvancedCombatRuntime.clearHudProvider();
      PlayerIdentityLabelRenderer.clearClassLabelProvider();

      for (Player player : Bukkit.getOnlinePlayers()) {
         CombatLevelDisplay.updateDisplay(player);
      }

      try {
         this.progression.closeAsync().join();
      } catch (CompletionException var3) {
         Logger.warn("Could not flush [Alpha] Advanced Combat System class progression during shutdown: " + rootCause(var3).getMessage());
      }

      this.weaponAffinity.shutdown();
      ClassAbilityEligibility.clear();
      if (instance == this) {
         instance = null;
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onJoinControlToggleNotice(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Bukkit.getScheduler()
         .runTaskLater(
            MetadataHandler.PLUGIN,
            () -> {
               if (player.isOnline() && AdvancedCombatSystemConfig.isEnabled()) {
                  if (AdvancedCombatSystemConfig.isShowJoinMessage()) {
                     if (this.outsideControlsAllowed() && this.fLayerSupported(player)) {
                        if (this.progression
                           .snapshot(player.getUniqueId())
                           .map(profile -> profile.forms().values().stream().anyMatch(FormProgressSnapshot::unlocked))
                           .orElse(false)) {
                           player.sendMessage(
                              ChatColorConverter.convert(
                                 ClassPresentationTheme.gradient("#8B0000:#CC4400:#DAA520", "[Alpha] Advanced Combat System")
                                    + " &8» &7New: hold &fsneak&7 and double-tap &fF&7 to toggle class controls anywhere outside EliteMobs content."
                              )
                           );
                        }
                     }
                  }
               }
            },
            100L
         );
   }

   private static void sendFeedback(Player player, String message) {
      ActionBarCompositor.show(player, ActionBarCompositor.Source.SKILL_FEEDBACK, ChatColorConverter.convert(message));
   }

   private static String failureMessage(String abilityName, AbilityFailureReason reason) {
      return switch (reason) {
         case NO_VALID_TARGET -> "&c" + abilityName + " needs a valid target.";
         case NO_CORPSE -> "&cAim at a recent Elite corpse.";
         case PATH_BLOCKED -> "&c" + abilityName + " is blocked by terrain.";
         case UNSAFE_DESTINATION -> "&c" + abilityName + " could not find safe footing.";
         case ENGINE_CLOSED, WRONG_THREAD, ABILITY_NOT_REGISTERED -> "&c" + abilityName + " is unavailable due to an internal error. Please report this.";
         case INVALID_LEVEL, INVALID_PLAYER, NONE -> "&c" + abilityName + " cannot be used right now.";
      };
   }

   private static String abilityId(ClassLineage lineage, AbilitySlot slot) {
      return switch (slot) {
         case MOBILITY -> lineage.mobility().id();
         case SIGNATURE -> lineage.signature().id();
         case UTILITY -> lineage.utility().id();
      };
   }

   private static String abilityName(ClassLineage lineage, AbilitySlot slot) {
      return switch (slot) {
         case MOBILITY -> lineage.mobility().displayName();
         case SIGNATURE -> lineage.signature().displayName();
         case UTILITY -> lineage.utility().displayName();
      };
   }

   private static String resourceName(ClassResourceType type) {
      return type.displayName();
   }

   private static Throwable rootCause(Throwable throwable) {
      Throwable cause = throwable;

      while (cause.getCause() != null) {
         cause = cause.getCause();
      }

      return cause;
   }

   public record AbilityResourceSnapshot(String abilityId, double cost, boolean affordable) {
   }
}
