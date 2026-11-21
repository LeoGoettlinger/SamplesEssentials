package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerDataLoadedEvent;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.ProcRoll;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.IronStanceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.LastStandSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.OverdrawSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.PackHunterSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.SteadyAimSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.ReturningHasteSkill;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class SkillSystemTest implements Listener {
   private static final Map<UUID, SkillSystemTest> activeSessions = new HashMap<>();
   private final Set<String> restored = new HashSet<>();
   static final int[] TEST_LEVELS = new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
   static final int HITS_PER_LEVEL = 200;
   private static final Map<String, String> SKIP_REASONS = Map.ofEntries(Map.entry("spears_phalanx", "defensive (frontal attack)"));
   static final Set<String> KILL_REQUIRED_SKILLS = Set.of("hoes_soul_siphon");
   static final int KILLS_PER_LEVEL = 12;
   private static final Set<String> HEALTH_THRESHOLD_SKILLS = Set.of(
      "axes_executioner", "swords_finishing_flourish", "crossbows_hunters_prey", "hoes_reapers_harvest"
   );
   private static final Set<String> PLAYER_HEALTH_THRESHOLD_SKILLS = Set.of("armor_grit", "armor_adrenaline_surge", "armor_second_wind");
   private static final Set<String> FATAL_DAMAGE_ARMOR_SKILLS = Set.of("armor_last_stand");
   private static final Set<String> HIGH_DAMAGE_THRESHOLD_SKILLS = Set.of("armor_reactive_shielding");
   private static final Set<String> RIPOSTE_SETUP_SKILLS = Set.of("swords_riposte");
   private static final Set<String> CRITICAL_HIT_SKILLS = Set.of("swords_vorpal_strike", "bows_dead_eye");
   private static final Set<String> BLOCKING_DEFENSE_SKILLS = Set.of("swords_parry");
   private static final Map<String, SkillType> DEFENSIVE_WEAPON_SKILLS = Map.of("hoes_deaths_embrace", SkillType.HOES, "maces_divine_shield", SkillType.MACES);
   private static final Set<String> OFFENSIVE_PLAYER_HEALTH_SKILLS = Set.of("hoes_reap_what_you_sow");
   private static final Set<String> WATER_REQUIRED_SKILLS = Set.of("tridents_depth_charge");
   private static final Set<String> STORM_REQUIRED_SKILLS = Set.of("tridents_riptide_mastery");
   static final int BASELINE_HITS = 10;
   private static boolean spearsAvailable = true;
   private final Player player;
   private final UUID playerUUID;
   private final CombatSimulator combatSimulator;
   private final CombatTestLog testLog;
   private final TestReport report;
   private final SkillTestEvaluator evaluator;
   private final SkillTestPlayerState playerState;
   private boolean cancelled = false;
   private boolean cleaned;
   private boolean cleaning;
   private boolean stormStateCaptured;
   private World savedStormWorld;
   private boolean packHunterOverrideActive;
   private final Set<BukkitTask> scheduledTasks = new HashSet<>();
   private BossBar progressBar;
   private MagicSkillScenarios magicScenarios;
   private final SkillType filterType;
   private int killPhaseHits = 0;
   private final Map<SkillType, List<SkillBonus>> skillsByType = new EnumMap<>(SkillType.class);
   private final Queue<SkillType> typeQueue = new LinkedList<>();
   private SkillType currentType = null;
   private int currentLevelIndex = 0;
   private int totalTypes = 0;
   private int completedTypes = 0;
   private boolean savedStormState = false;

   public SkillSystemTest(Player player) {
      this(player, null);
   }

   public SkillSystemTest(Player player, SkillType filterType) {
      this.player = player;
      this.playerUUID = player.getUniqueId();
      this.combatSimulator = new CombatSimulator(player);
      this.testLog = new CombatTestLog();
      this.report = new TestReport(this.playerUUID);
      this.evaluator = new SkillTestEvaluator(player, this.combatSimulator, this.testLog, this.report, this.skillsByType);
      this.playerState = new SkillTestPlayerState(player, this.combatSimulator);
      this.filterType = filterType;
   }

   public static SkillSystemTest getSession(UUID playerUUID) {
      return activeSessions.get(playerUUID);
   }

   public static boolean hasActiveSession(UUID playerUUID) {
      return activeSessions.containsKey(playerUUID);
   }

   public static boolean hasActiveSession() {
      return !activeSessions.isEmpty();
   }

   public static void shutdown() {
      for (SkillSystemTest session : new ArrayList<>(activeSessions.values())) {
         session.cancel();
      }

      if (!activeSessions.isEmpty()) {
         Logger.warn("Combat diagnostic recovery remains unfinished for " + activeSessions.keySet());
      }
   }

   public static void registerRecoveryListeners() {
      for (SkillSystemTest session : activeSessions.values()) {
         HandlerList.unregisterAll(session);
         Bukkit.getPluginManager().registerEvents(session, MetadataHandler.PLUGIN);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerDataLoaded(PlayerDataLoadedEvent event) {
      if (this.cancelled && event.getPlayer().getUniqueId().equals(this.playerUUID)) {
         this.cleanup();
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      if (event.getPlayer().getUniqueId().equals(this.playerUUID)) {
         this.cancel();
      }
   }

   public void start() {
      if (this.player.isOnline() && !this.player.isDead()) {
         if (!PlayerData.isDataLoaded(this.playerUUID)) {
            this.log("§cPlayer data is still loading; retry the combat diagnostic in a moment.");
         } else if (!activeSessions.isEmpty()) {
            SkillSystemTest active = activeSessions.values().iterator().next();
            this.log("§cA combat diagnostic is already running for " + active.player.getName() + ".");
         } else {
            for (SkillType type : SkillType.values()) {
               if (this.filterType == null || type == this.filterType) {
                  for (SkillBonus skill : SkillBonusRegistry.getEnabledBonuses(type)) {
                     this.report.expect(skill);
                  }

                  if (type == SkillType.SPEARS && !spearsAvailable) {
                     this.log("§7Skipping " + type.getDisplayName() + " §7(not available on this MC version)");

                     for (SkillBonus skill : SkillBonusRegistry.getEnabledBonuses(type)) {
                        SkillTestResult result = new SkillTestResult(skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());
                        result.markSkipped("spears not available on this MC version");
                        this.report.addSkippedResult(result);
                     }
                  } else {
                     List<SkillBonus> skills = SkillBonusRegistry.getEnabledBonuses(type);
                     if (!skills.isEmpty()) {
                        this.skillsByType.put(type, skills);
                        this.typeQueue.add(type);
                     }
                  }
               }
            }

            if (this.typeQueue.isEmpty()) {
               this.log("§c[Test] No skills found to test!");
            } else {
               activeSessions.put(this.playerUUID, this);

               try {
                  Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
                  CombatSimulator.setTestingActive(this.player, true);
                  this.totalTypes = this.typeQueue.size();
                  int totalSkills = this.skillsByType.values().stream().mapToInt(List::size).sum();
                  String filterMsg = this.filterType != null ? " §7(filtered: §e" + this.filterType.getDisplayName() + "§7)" : "";
                  this.log("§a§l=== BATCH SKILL TEST STARTING ===" + filterMsg);
                  this.log("§7Testing §e" + totalSkills + "§7 skills across §e" + this.typeQueue.size() + "§7 weapon types");
                  this.log("§7All skills of each type tested simultaneously!");
                  this.testLog.logSection("BATCH SKILL SYSTEM TEST");
                  this.testLog.log("Player: " + this.player.getName());
                  this.testLog.log("Total skills: " + totalSkills);
                  if (this.filterType != null) {
                     this.testLog.log("Filter: " + this.filterType.getDisplayName());
                  }

                  this.progressBar = Bukkit.createBossBar("Initializing...", BarColor.BLUE, BarStyle.SEGMENTED_10, new BarFlag[0]);
                  this.progressBar.setProgress(0.0);
                  BossBarOrderManager.show(this.player, this.progressBar);

                  try {
                     this.playerState.captureAndPrepare();
                  } catch (RuntimeException var9) {
                     this.log("§cFailed to prepare the combat diagnostic: " + var9.getMessage());
                     this.cleanup();
                     return;
                  }

                  this.testNextType();
               } catch (RuntimeException var10) {
                  this.abort(var10);
               }
            }
         }
      }
   }

   private void testNextType() {
      if (this.cancelled) {
         this.cleanup();
      } else if (this.typeQueue.isEmpty()) {
         this.completeTest();
      } else {
         this.combatSimulator.removeAllDummies();
         this.combatSimulator.cleanupTestEntities();
         this.currentType = this.typeQueue.poll();
         this.currentLevelIndex = 0;
         this.evaluator.resetTypeCounters();
         List<SkillBonus> skills = this.skillsByType.get(this.currentType);
         List<SkillBonus> skippedSkills = new ArrayList<>();
         List<SkillBonus> testableSkills = new ArrayList<>();

         for (SkillBonus skill : skills) {
            String reason = SKIP_REASONS.get(skill.getSkillId());
            if (reason != null) {
               skippedSkills.add(skill);
               SkillTestResult result = new SkillTestResult(skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());
               result.markSkipped(reason);
               this.report.addSkippedResult(result);
            } else {
               testableSkills.add(skill);
            }
         }

         this.skillsByType.put(this.currentType, testableSkills);
         if (testableSkills.isEmpty()) {
            this.log("§7All " + this.currentType.getDisplayName() + " skills skipped");
            this.completedTypes++;
            this.testNextType();
         } else {
            this.log("");
            int skippedCount = skippedSkills.size();
            String skippedMsg = skippedCount > 0 ? " §7(" + skippedCount + " skipped)" : "";
            this.log(
               "§6=== Testing "
                  + this.currentType.getDisplayName()
                  + " §7("
                  + testableSkills.size()
                  + " skills)"
                  + skippedMsg
                  + " §6["
                  + (this.completedTypes + 1)
                  + "/"
                  + this.totalTypes
                  + "] ==="
            );
            this.testLog
               .logSection(
                  this.currentType.getDisplayName()
                     + " ("
                     + testableSkills.size()
                     + " skills"
                     + (skippedCount > 0 ? ", " + skippedCount + " skipped" : "")
                     + ")"
               );
            this.updateOverallProgress();
            if (MagicSkillScenarios.covers(this.currentType)) {
               this.magicScenarios = new MagicSkillScenarios(this.player, this.currentType, this.combatSimulator, this.report, this.testLog, () -> {
                  this.magicScenarios = null;
                  this.completedTypes++;
                  this.testNextType();
               });
               this.magicScenarios.start();
            } else {
               this.schedule(
                  () -> {
                     if (!this.cancelled) {
                        SkillBonus firstSkill = testableSkills.get(0);
                        if (this.combatSimulator.spawnSingleDummy(firstSkill)) {
                           this.testNextLevel();
                        } else {
                           this.log("§cFailed to spawn dummy for " + this.currentType.getDisplayName());

                           for (SkillBonus skillx : testableSkills) {
                              SkillTestResult resultx = new SkillTestResult(
                                 skillx.getSkillId(), skillx.getBonusName(), skillx.getSkillType(), skillx.getRequiredLevel()
                              );
                              resultx.addIssue("Dummy admission failed; requested levels were not run");
                              this.report.addResult(resultx);
                           }

                           this.completedTypes++;
                           this.testNextType();
                        }
                     }
                  },
                  2L
               );
            }
         }
      }
   }

   private void testNextLevel() {
      if (this.cancelled) {
         this.cleanup();
      } else if (this.currentLevelIndex >= TEST_LEVELS.length) {
         this.evaluator.summarizeType(this.currentType);
         this.completedTypes++;
         this.testNextType();
      } else {
         int testLevel = TEST_LEVELS[this.currentLevelIndex];
         List<SkillBonus> skills = this.skillsByType.get(this.currentType);
         this.setPlayerSkillLevel(this.currentType, testLevel);
         this.combatSimulator.equipWeapon(this.currentType);
         if (this.currentType == SkillType.ARMOR) {
            this.combatSimulator.equipArmorSet(testLevel);
            double formulaMaxHealth = 20.0 + Math.max(0, testLevel - 1) * 2.0;
            AttributeInstance maxHealthAttr = this.player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
               maxHealthAttr.setBaseValue(formulaMaxHealth);
            }

            this.player.setHealth(formulaMaxHealth);
         }

         String baselineDummyId = skills.get(0).getSkillId();
         if (this.currentType == SkillType.ARMOR) {
            this.evaluator.runDefensiveBaseline(testLevel, baselineDummyId);
         } else {
            this.evaluator.runOffensiveBaseline(testLevel, this.currentType, baselineDummyId);
         }

         this.killPhaseHits = 0;

         for (SkillBonus skill : skills) {
            if (KILL_REQUIRED_SKILLS.contains(skill.getSkillId())) {
               skill.onDeactivate(this.player);
            }
         }

         this.activateAllSkills(this.currentType, skills);
         this.combatSimulator.applyPassiveWeaponEffects(this.currentType, testLevel);
         this.setupConditionalSkillStates(skills);

         for (SkillBonus skillx : skills) {
            skillx.resetProcCount(this.player);
         }

         this.updateOverallProgress();
         this.log("§7Level §e" + testLevel + "§7: Testing " + skills.size() + " skills...");
         this.testInstantLevel(testLevel, skills);
      }
   }

   private void resetAllCooldowns(List<SkillBonus> skills) {
      for (SkillBonus skill : skills) {
         if (skill instanceof CooldownSkill cooldownSkill) {
            cooldownSkill.endCooldown(this.player);
         }
      }
   }

   private void testInstantLevel(int testLevel, List<SkillBonus> skills) {
      String dummyId = skills.get(0).getSkillId();
      boolean hasThresholdSkills = skills.stream().anyMatch(s -> HEALTH_THRESHOLD_SKILLS.contains(s.getSkillId()));
      boolean hasOffensivePlayerHealthSkills = this.currentType != SkillType.ARMOR
         && skills.stream().anyMatch(s -> OFFENSIVE_PLAYER_HEALTH_SKILLS.contains(s.getSkillId()));
      boolean hasWaterSkills = skills.stream().anyMatch(s -> WATER_REQUIRED_SKILLS.contains(s.getSkillId()));
      boolean hasStormSkills = skills.stream().anyMatch(s -> STORM_REQUIRED_SKILLS.contains(s.getSkillId()));
      boolean hasRiposteSkills = skills.stream().anyMatch(s -> RIPOSTE_SETUP_SKILLS.contains(s.getSkillId()));
      boolean hasCriticalHitSkills = skills.stream().anyMatch(s -> CRITICAL_HIT_SKILLS.contains(s.getSkillId()));
      boolean hasBlockingDefenseSkills = skills.stream().anyMatch(s -> BLOCKING_DEFENSE_SKILLS.contains(s.getSkillId()));
      boolean hasDefensiveWeaponSkills = skills.stream().anyMatch(s -> DEFENSIVE_WEAPON_SKILLS.containsKey(s.getSkillId()));
      boolean hasKillSkills = skills.stream().anyMatch(s -> KILL_REQUIRED_SKILLS.contains(s.getSkillId()));
      boolean hasPackHunter = skills.stream().anyMatch(s -> "bows_pack_hunter".equals(s.getSkillId()));
      if (hasPackHunter) {
         PackHunterSkill.setTestOverrideNearbyPlayers(true);
         this.packHunterOverrideActive = true;
      }

      if (hasWaterSkills) {
         this.combatSimulator.placeWaterAtDummy(dummyId);
      }

      if (hasStormSkills) {
         this.savedStormState = this.player.getWorld().hasStorm();
         this.savedStormWorld = this.player.getWorld();
         this.stormStateCaptured = true;
         this.player.getWorld().setStorm(true);
      }

      Runnable hitLoopTask = () -> {
         for (int i = 0; i < 200; i++) {
            if (this.cancelled) {
               return;
            }

            if (!this.combatSimulator.hasDummy(dummyId)) {
               this.combatSimulator.respawnIfDead(dummyId, skills.get(0));
               if (hasWaterSkills) {
                  this.combatSimulator.placeWaterAtDummy(dummyId);
               }
            }

            if (hasThresholdSkills && i >= 100) {
               this.combatSimulator.setDummyHealthPercent(dummyId, 0.2);
            }

            this.resetAllCooldowns(skills);
            if (hasRiposteSkills) {
               RiposteSkill.onPlayerBlock(this.player);
            }

            if (this.currentType == SkillType.ARMOR) {
               if (i < 100) {
                  double damage = this.combatSimulator.simulateIncomingDamageWithOverride(dummyId, 10.0);
                  if (damage > 0.0) {
                     this.evaluator.recordDefensiveDamage(testLevel, damage);
                  }
               } else if (i < 120) {
                  double lowHpDamage = this.player.getMaxHealth() * 0.25;
                  double damage = this.combatSimulator.simulateIncomingDamage(dummyId, lowHpDamage, 0.15);
                  if (damage > 0.0) {
                     this.evaluator.recordDefensiveDamage(testLevel, damage);
                  }
               } else if (i < 140) {
                  double gritDamage = this.player.getMaxHealth() * 0.25;
                  double damage = this.combatSimulator.simulateIncomingDamage(dummyId, gritDamage, 0.4);
                  if (damage > 0.0) {
                     this.evaluator.recordDefensiveDamage(testLevel, damage);
                  }
               } else if (i < 170) {
                  double highDamage = this.player.getMaxHealth() * 5.0;
                  double damage = this.combatSimulator.simulateHighDamageWithOverride(dummyId, highDamage);
                  if (damage > 0.0) {
                     this.evaluator.recordDefensiveDamage(testLevel, damage);
                  }
               } else {
                  boolean lastStandUnlocked = skills.stream()
                     .filter(s -> FATAL_DAMAGE_ARMOR_SKILLS.contains(s.getSkillId()))
                     .anyMatch(s -> testLevel >= s.getRequiredLevel());
                  if (lastStandUnlocked) {
                     this.combatSimulator.simulateFatalIncomingDamage(dummyId);
                     this.evaluator.recordDefensiveDamage(testLevel, 0.0);
                  } else {
                     double damage = this.combatSimulator.simulateIncomingDamageWithOverride(dummyId, 10.0);
                     if (damage > 0.0) {
                        this.evaluator.recordDefensiveDamage(testLevel, damage);
                     }
                  }
               }
            } else {
               if (hasBlockingDefenseSkills && i % 4 == 0) {
                  this.combatSimulator.simulateBlockingIncomingDamage(dummyId, 10.0);
               }

               double damage;
               if (hasCriticalHitSkills) {
                  damage = this.combatSimulator.simulateCriticalAttack(dummyId);
               } else if (hasOffensivePlayerHealthSkills && i >= 100) {
                  this.player.setHealth(this.player.getMaxHealth() * 0.3);
                  damage = this.performAttack(this.currentType, dummyId);
                  this.player.setHealth(this.player.getMaxHealth());
               } else {
                  damage = this.performAttack(this.currentType, dummyId);
               }

               if (damage > 0.0) {
                  this.evaluator.recordOffensiveDamage(this.currentType, testLevel, damage);
               }
            }

            this.combatSimulator.healDummy(dummyId);
         }

         if (hasDefensiveWeaponSkills) {
            SkillBonus lastStandSkill = SkillBonusRegistry.getSkillById("armor_last_stand");
            if (lastStandSkill instanceof LastStandSkill ls) {
               ls.startCooldown(this.player, 100);
            }

            for (SkillBonus skill : skills) {
               if (DEFENSIVE_WEAPON_SKILLS.containsKey(skill.getSkillId()) && testLevel >= skill.getRequiredLevel()) {
                  SkillType weaponType = DEFENSIVE_WEAPON_SKILLS.get(skill.getSkillId());
                  this.combatSimulator.equipWeapon(weaponType);

                  for (int j = 0; j < 50; j++) {
                     if (this.cancelled) {
                        return;
                     }

                     this.resetAllCooldowns(skills);
                     if (lastStandSkill instanceof LastStandSkill ls) {
                        ls.startCooldown(this.player, 100);
                     }

                     if (!this.combatSimulator.hasDummy(dummyId)) {
                        this.combatSimulator.respawnIfDead(dummyId, skills.get(0));
                     }

                     this.combatSimulator.simulateFatalIncomingDamage(dummyId);
                  }

                  this.combatSimulator.equipWeapon(this.currentType);
               }
            }

            if (lastStandSkill instanceof LastStandSkill ls) {
               ls.endCooldown(this.player);
            }
         }

         if (hasKillSkills) {
            this.combatSimulator.equipWeapon(this.currentType);
            this.runKillPhase(testLevel, skills, dummyId);
         }

         if (hasPackHunter) {
            PackHunterSkill.setTestOverrideNearbyPlayers(false);
            this.packHunterOverrideActive = false;
         }

         if (hasWaterSkills) {
            this.combatSimulator.restoreWaterBlock();
         }

         if (hasStormSkills) {
            this.savedStormWorld.setStorm(this.savedStormState);
         }

         this.combatSimulator.cleanupTestEntities();
         this.updateProgressBar(this.currentType.getDisplayName() + " Lv." + testLevel, 200, 200, BarColor.GREEN);
         this.evaluator.checkLevelResults(testLevel, skills, this.killPhaseHits);
         this.currentLevelIndex++;
         this.schedule(this::testNextLevel, 1L);
      };
      if (hasWaterSkills) {
         this.schedule(hitLoopTask, 1L);
      } else {
         hitLoopTask.run();
      }
   }

   private void runKillPhase(int testLevel, List<SkillBonus> skills, String dummyId) {
      List<SkillBonus> killSkills = skills.stream().filter(s -> KILL_REQUIRED_SKILLS.contains(s.getSkillId())).toList();
      if (!killSkills.isEmpty()) {
         SkillBonus respawnTemplate = skills.get(0);
         int kills = 0;

         for (int i = 0; i < 12; i++) {
            if (this.cancelled) {
               return;
            }

            if (!this.combatSimulator.hasDummy(dummyId) && !this.combatSimulator.respawnIfDead(dummyId, respawnTemplate)) {
               this.log("§cKill phase: failed to respawn dummy at Lv." + testLevel);
               break;
            }

            if (!this.combatSimulator.simulateKill(dummyId)) {
               break;
            }

            kills++;
            this.evaluator.observeKillStacks(testLevel, killSkills);
            this.combatSimulator.respawnIfDead(dummyId, respawnTemplate);
         }

         this.killPhaseHits = kills;
         this.evaluator.finishKillPhase(testLevel, kills, killSkills);
         if (kills < 12) {
            this.log("§eKill phase: only " + kills + "/12 kills landed at Lv." + testLevel);
         }
      }
   }

   private void activateAllSkills(SkillType type, List<SkillBonus> skills) {
      for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(this.playerUUID, type))) {
         PlayerSkillSelection.removeActiveSkill(this.playerUUID, type, id);
      }

      for (SkillBonus skill : skills) {
         PlayerSkillSelection.addActiveSkill(this.playerUUID, type, skill.getSkillId(), true);
      }

      SkillBonusRegistry.applyBonuses(this.player, type);
   }

   private void setPlayerSkillLevel(SkillType type, int level) {
      PlayerData.setSkillXP(this.playerUUID, type, SkillXPCalculator.totalXPForLevel(level));
   }

   private double performAttack(SkillType skillType, String skillId) {
      return switch (skillType) {
         case BOWS, CROSSBOWS -> this.combatSimulator.simulateRangedAttack(skillId);
         case TRIDENTS -> this.combatSimulator.simulateTridentAttack(skillId);
         case ARMOR -> this.combatSimulator.simulateIncomingDamage(skillId, 10.0);
         case MACES -> this.combatSimulator.simulateDirectAttack(skillId);
         default -> this.combatSimulator.simulateMeleeAttack(skillId);
      };
   }

   public void cancel() {
      if (!this.cleaned) {
         this.cancelled = true;

         try {
            this.report.finishCoverage("diagnostic cancelled");
            this.report.store();
            this.log("§c§lTest cancelled!");
            File logFile = this.testLog.saveToFile();
            if (logFile != null) {
               this.log("§7Partial log: §e" + logFile.getName());
            }
         } finally {
            this.cleanup();
         }
      }
   }

   private void abort(RuntimeException failure) {
      Logger.warn("Combat diagnostic aborted for " + this.playerUUID + ": " + failure);

      try {
         this.cancel();
      } catch (RuntimeException var3) {
         failure.addSuppressed(var3);
         Logger.warn("Combat diagnostic recovery retained for " + this.playerUUID + ": " + failure);
      }
   }

   private void completeTest() {
      if (!this.cleaned && !this.cancelled) {
         this.evaluator.logDamageValidation();
         this.testLog.logSection("TEST COMPLETE");
         this.report.finishCoverage("requested skill never produced a complete result");
         this.report.store();
         List<String> summary = this.report.generateSummary();

         for (String line : summary) {
            this.testLog.log(line.replace("&", ""));
         }

         for (String line : this.report.generateDetailedReport()) {
            this.testLog.log(line.replace("&", ""));
         }

         String exported = this.report.exportToText();
         this.testLog.log(exported);
         File logFile = this.testLog.saveToFile();
         this.log("");

         for (String line : summary) {
            this.log(line.replace("&", "§"));
         }

         if (logFile != null) {
            this.log("§7Log saved: §e" + logFile.getName());
         }

         this.log("§7Use §e/em debug combat results §7to review this report.");
         this.cleanup();
         if (!this.cleaned) {
            this.log("§cDiagnostic recovery is unfinished. Run cancel again after correcting the logged failure.");
         } else {
            try {
               ItemStack book = this.report.generateBook();
               if (this.player.getInventory().firstEmpty() != -1) {
                  this.player.getInventory().addItem(new ItemStack[]{book});
                  this.log("§7A §eSkill Test Report §7book has been added to your inventory.");
               } else {
                  this.player.getWorld().dropItem(this.player.getLocation(), book);
                  this.log("§7A §eSkill Test Report §7book was dropped at your feet (inventory full).");
               }
            } catch (Exception var7) {
               this.log("§7Could not generate report book: " + var7.getMessage());
            }

            this.cleanup();
         }
      }
   }

   private void log(String message) {
      Player current = Bukkit.getPlayer(this.playerUUID);
      if (current != null) {
         current.sendMessage(message);
      }
   }

   private void updateProgressBar(String action, int current, int max, BarColor color) {
      if (this.progressBar != null) {
         this.progressBar.setTitle("§f" + action + " §7[" + current + "/" + max + "]");
         this.progressBar.setProgress(Math.min(1.0, (double)current / max));
         this.progressBar.setColor(color);
      }
   }

   private void cleanup() {
      if (!this.cleaned && !this.cleaning) {
         this.cleaning = true;

         try {
            boolean complete = true;
            this.cancelled = true;
            CombatSimulator.setTestingActive(this.player, false);
            complete &= this.cleanupSafely("scheduled callbacks", () -> {
               for (BukkitTask task : new ArrayList<>(this.scheduledTasks)) {
                  task.cancel();
               }

               this.scheduledTasks.clear();
            });
            complete &= this.cleanupSafely("magic scenarios", () -> {
               if (this.magicScenarios != null) {
                  this.magicScenarios.close();
               }

               this.magicScenarios = null;
               ProcRoll.clear(this.playerUUID);
            });
            complete &= this.cleanupSafely("progress bar", () -> {
               if (this.progressBar != null) {
                  BossBarOrderManager.hide(this.player, this.progressBar);
                  this.progressBar.removeAll();
                  this.progressBar = null;
               }
            });
            complete &= this.cleanupSafely("water block", this.combatSimulator::restoreWaterBlock);
            complete &= this.cleanupSafely("weather", () -> {
               if (this.stormStateCaptured && this.savedStormWorld != null) {
                  this.savedStormWorld.setStorm(this.savedStormState);
               }
            });
            complete &= this.cleanupSafely("Pack Hunter override", () -> {
               if (this.packHunterOverrideActive) {
                  PackHunterSkill.setTestOverrideNearbyPlayers(false);
               }
            });
            complete &= this.cleanupSafely("test dummies", this.combatSimulator::removeAllDummies);
            complete &= this.cleanupSafely("test projectiles", this.combatSimulator::cleanupTestEntities);
            complete &= this.cleanupSafely("player state", this.playerState::restore);
            complete &= this.cleanupSafely("proc counts", () -> {
               for (SkillBonus skill : SkillBonusRegistry.getAllBonuses()) {
                  skill.resetProcCount(this.player);
               }
            });
            this.cleaned = complete;
            if (this.cleaned) {
               HandlerList.unregisterAll(this);
               activeSessions.remove(this.playerUUID, this);
            }
         } finally {
            this.cleaning = false;
         }
      }
   }

   private boolean cleanupSafely(String state, Runnable cleanupAction) {
      if (this.restored.contains(state)) {
         return true;
      } else {
         try {
            cleanupAction.run();
            this.restored.add(state);
            return true;
         } catch (RuntimeException var4) {
            Logger.warn("Failed to restore combat diagnostic " + state + "; recovery retained: " + var4);
            return false;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onHeldSlot(PlayerItemHeldEvent event) {
      if (event.getPlayer().getUniqueId().equals(this.playerUUID)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked().getUniqueId().equals(this.playerUUID)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked().getUniqueId().equals(this.playerUUID)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSwap(PlayerSwapHandItemsEvent event) {
      if (event.getPlayer().getUniqueId().equals(this.playerUUID)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onDrop(PlayerDropItemEvent event) {
      if (event.getPlayer().getUniqueId().equals(this.playerUUID)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPickup(EntityPickupItemEvent event) {
      if (event.getEntity().getUniqueId().equals(this.playerUUID)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInteract(PlayerInteractEvent event) {
      if (event.getPlayer().getUniqueId().equals(this.playerUUID) && !MagicSkillScenarios.isFiringCast()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onIncomingDamage(EntityDamageEvent event) {
      if (event.getEntity().getUniqueId().equals(this.playerUUID)
         && !(event instanceof EntityDamageByEntityEvent hit && CombatSimulator.ownsIncomingDamage(hit))) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onNaturalHealing(EntityRegainHealthEvent event) {
      if (event.getEntity().getUniqueId().equals(this.playerUUID)) {
         event.setCancelled(true);
      }
   }

   private void schedule(Runnable runnable, long delayTicks) {
      BukkitTask[] taskReference = new BukkitTask[1];
      taskReference[0] = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
         this.scheduledTasks.remove(taskReference[0]);
         if (!this.cleaned && !this.cancelled) {
            try {
               runnable.run();
            } catch (RuntimeException var4x) {
               this.abort(var4x);
            }
         }
      }, delayTicks);
      this.scheduledTasks.add(taskReference[0]);
   }

   private void updateOverallProgress() {
      if (this.progressBar != null && this.currentType != null) {
         int totalLevels = TEST_LEVELS.length;
         double overallProgress = this.totalTypes > 0
            ? (double)(this.completedTypes * totalLevels + this.currentLevelIndex) / (this.totalTypes * totalLevels)
            : 0.0;
         int currentLevel = this.currentLevelIndex < totalLevels ? TEST_LEVELS[this.currentLevelIndex] : 100;
         int overallPct = (int)(overallProgress * 100.0);
         String title = String.format(
            "§fTesting %s §7(%d/%d types) | Lv.%d | %d%% overall",
            this.currentType.getDisplayName(),
            this.completedTypes + 1,
            this.totalTypes,
            currentLevel,
            overallPct
         );
         this.progressBar.setTitle(title);
         this.progressBar.setProgress(Math.min(1.0, overallProgress));
         this.progressBar.setColor(BarColor.BLUE);
      }
   }

   private void setupConditionalSkillStates(List<SkillBonus> skills) {
      for (SkillBonus skill : skills) {
         if (skill.getTestStrategy() == SkillBonus.TestStrategy.CONDITION_SETUP) {
            if (skill instanceof OverdrawSkill) {
               OverdrawSkill.simulateFullDraw(this.playerUUID);
            } else if (skill instanceof SteadyAimSkill) {
               SteadyAimSkill.simulateStationary(this.playerUUID, 3000L);
            } else if (skill instanceof ReturningHasteSkill) {
               ReturningHasteSkill.simulateStacks(this.playerUUID, 3);
            } else if (skill instanceof IronStanceSkill) {
               IronStanceSkill.simulateStationary(this.playerUUID);
            }
         }
      }
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   static {
      try {
         Material.valueOf("IRON_SPEAR");
      } catch (IllegalArgumentException var1) {
         spearsAvailable = false;
      }
   }
}
