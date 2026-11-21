package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.ProcRoll;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

final class MagicSkillScenarios implements Listener {
   static final int LEVEL = 75;
   private static final String DUMMY_CONFIG = "damage_test_dummy.yml";
   private static final String MULTICAST = "freeminecraftmodels:multicast";
   private static final int LANDING_TIMEOUT_TICKS = 60;
   private static final int SETTLE_TICKS = 5;
   private static final double MULTIPLIER_TOLERANCE = 0.002;
   private static final double DAMAGE_TOLERANCE = 0.01;
   private static final double INCOMING_HIT = 10.0;
   private final Player player;
   private final UUID playerId;
   private final SkillType type;
   private final CombatSimulator simulator;
   private final TestReport report;
   private final CombatTestLog log;
   private final Runnable onComplete;
   private final Deque<MagicSkillScenarios.Step> steps = new ArrayDeque<>();
   private final Map<String, MagicSkillScenarios.Dummy> dummies = new LinkedHashMap<>();
   private final Map<String, UUID> ids = new HashMap<>();
   private final Map<String, Location> marks = new HashMap<>();
   private final Map<String, Double> values = new HashMap<>();
   private final List<MagicSkillScenarios.Hit> hits = new ArrayList<>();
   private final List<MagicSkillScenarios.Cast> casts = new ArrayList<>();
   private final Map<EliteMobDamagedByPlayerEvent, Double> enteringSkills = new IdentityHashMap<>();
   private final Map<EntityDamageByEntityEvent, Integer> unsettled = new IdentityHashMap<>();
   private long scenarioStart;
   private final List<String> trace = new ArrayList<>();
   private final Map<MagicStrike, Long> readyAt = new EnumMap<>(MagicStrike.class);
   private Location anchor;
   private Vector forward;
   private Vector right;
   private SkillTestResult result;
   private PotionEffect casterSlownessAfterCast;
   private String lastClick = "";
   private static boolean firingCast;
   private BukkitTask ticker;
   private long tick;
   private int plannedCasts;
   private boolean closed;

   MagicSkillScenarios(Player player, SkillType type, CombatSimulator simulator, TestReport report, CombatTestLog log, Runnable onComplete) {
      this.player = player;
      this.playerId = player.getUniqueId();
      this.type = type;
      this.simulator = simulator;
      this.report = report;
      this.log = log;
      this.onComplete = onComplete;
   }

   static boolean covers(SkillType type) {
      return type == SkillType.STAVES || type == SkillType.WANDS;
   }

   void start() {
      if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels") && MagicWeaponAPI.isOperational()) {
         this.anchor = this.player.getLocation().clone();
         this.forward = this.anchor.getDirection().setY(0);
         if (this.forward.lengthSquared() < 1.0E-6) {
            this.forward = new Vector(0, 0, 1);
         }

         this.forward.normalize();
         this.right = this.forward.clone().crossProduct(new Vector(0, 1, 0)).normalize();
         SkillBonusRegistry.removeAllBonuses(this.player);
         Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
         if (this.type == SkillType.STAVES) {
            this.staffScenarios();
         } else {
            this.wandScenarios();
         }

         this.steps.add(() -> {
            this.close();
            this.onComplete.run();
            return true;
         });
         this.ticker = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::advance, 1L, 1L);
      } else {
         for (SkillBonus skill : SkillBonusRegistry.getEnabledBonuses(this.type)) {
            SkillTestResult skipped = new SkillTestResult(skill.getSkillId(), skill.getBonusName(), this.type, 75);
            skipped.markSkipped("needs FreeMinecraftModels magic weapons");
            this.report.addSkippedResult(skipped);
         }

         this.onComplete.run();
      }
   }

   void close() {
      if (!this.closed) {
         this.closed = true;
         this.steps.clear();
         if (this.ticker != null) {
            this.ticker.cancel();
         }

         HandlerList.unregisterAll(this);
         this.removeDummies();
         ProcRoll.clear(this.playerId);
         this.resetTypeSkills();
         if (this.anchor != null && this.player.isOnline()) {
            this.player.teleport(this.anchor);
         }
      }
   }

   private void staffScenarios() {
      this.scenario("staves_casting_baseline", "Staff casts without skills", 75);
      this.spawn("A", 6.0, 0.0);
      this.spawn("B", 6.0, 1.3);
      this.spawn("C", 6.0, -1.3);
      this.spawn("M", 2.5, 2.5);
      int plainFireball = this.cast(MagicStrike.STAFF_FIREBALL, "A");
      this.expectBlast("A fireball hits every dummy in its blast at plain damage", plainFireball, 3, () -> 1.0);
      int plainStrike = this.cast(MagicStrike.STAFF_MELEE, "M");
      this.expectMultiplier("A staff strike lands as a staff strike at plain damage", plainStrike, "M", () -> 1.0);
      this.end();
      if (this.enabled("staves_smolder")) {
         this.scenario("staves_smolder", "Smolder", 75, "staves_smolder");
         this.spawn("A", 6.0, 0.0);
         this.spawn("M", 2.5, 2.5);
         this.force("staves_smolder", true);
         int burning = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.waitAfterLanding(burning, 90);
         this.expectPulses("A proc burns the target in 4 pulses, one second apart", "A", burning, 4);
         this.expectNear(
            "The burn totals the skill's share of the fireball hit",
            () -> skill("staves_smolder").getBonusValue(75) * this.nonCritical(burning, "A"),
            () -> this.sideDamage("A", this.landed(burning)),
            0.01
         );
         this.check(
            "Burn pulses do not trigger skills again",
            () -> skill("staves_smolder").getProcCount(this.player) == 1,
            () -> "procs " + skill("staves_smolder").getProcCount(this.player)
         );
         this.force("staves_smolder", false);
         int plain = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.waitAfterLanding(plain, 90);
         this.expectNoSideHits("Without a proc the fireball leaves no burn", "A", plain);
         this.force("staves_smolder", true);
         int melee = this.cast(MagicStrike.STAFF_MELEE, "M");
         this.waitAfterLanding(melee, 40);
         this.expectNoSideHits("Staff strikes never smolder", "M", melee);
         this.end();
      }

      if (this.enabled("staves_concussive_blast")) {
         this.scenario("staves_concussive_blast", "Concussive Blast", 75, "staves_concussive_blast");
         this.spawnLoose("A", 6.0, 0.0);
         this.force("staves_concussive_blast", false);
         this.knockback("plain", MagicStrike.STAFF_FIREBALL, "A");
         this.check("Without a proc the fireball does not slow", () -> this.value("plain.slowed") == 0.0, () -> "slowness level " + this.value("plain.slowed"));
         this.force("staves_concussive_blast", true);
         this.knockback("proc", MagicStrike.STAFF_FIREBALL, "A");
         this.check(
            "A proc throws the target at least a block farther than a plain fireball",
            () -> this.value("proc") >= this.value("plain") + 1.0,
            () -> "plain fireball moved it " + format(this.value("plain")) + ", proc " + format(this.value("proc"))
         );
         this.check("…and gives it Slowness II for 2 seconds", () -> this.value("proc.slowed") == 2.0, () -> "slowness level " + this.value("proc.slowed"));
         this.end();
      }

      if (this.enabled("staves_battlemages_guard")) {
         this.scenario("staves_battlemages_guard", "Battlemage's Guard", 75, "staves_battlemages_guard");
         this.spawn("N", 3.0, 0.0);
         this.spawn("F", 7.0, 0.0);
         this.measure("near", () -> this.simulator.takeHitWithOverride(this.body("N"), 10.0));
         this.measure("far", () -> this.simulator.takeHitWithOverride(this.body("F"), 10.0));
         this.act(() -> this.simulator.holdTestItem(new ItemStack(Material.STICK)));
         this.measure("unarmed", () -> this.simulator.takeHitWithOverride(this.body("N"), 10.0));
         this.act(() -> this.simulator.equipWeapon(this.type));
         this.expectNear(
            "Hits from enemies within 4 blocks are reduced by the skill's share",
            () -> 10.0 * (1.0 - skill("staves_battlemages_guard").getBonusValue(75)),
            () -> this.value("near"),
            0.01
         );
         this.expectNear("Hits from farther away are not reduced", () -> 10.0, () -> this.value("far"), 0.01);
         this.expectNear("Without a staff in hand there is no reduction", () -> 10.0, () -> this.value("unarmed"), 0.01);
         this.end();
      }

      if (this.enabled("staves_stoke_the_flames")) {
         this.scenario("staves_stoke_the_flames", "Stoke the Flames", 75, "staves_stoke_the_flames");
         this.spawn("A", 6.0, 0.0);
         this.spawn("B", 6.0, 1.3);
         this.spawn("C", 6.0, -1.3);
         this.spawn("M", 2.5, 2.5);
         int last = -1;

         for (int fireball = 0; fireball < 7; fireball++) {
            int stacks = Math.min(fireball, 5);
            last = this.cast(MagicStrike.STAFF_FIREBALL, "A");
            this.expectBlast(
               "Fireball " + (fireball + 1) + " carries " + stacks + " heat on every target it hits (one stack per fireball, at most 5)",
               last,
               3,
               () -> 1.0 + stacks * stacking("staves_stoke_the_flames").getBonusPerStack(75)
            );
            if (fireball == 2) {
               int melee = this.cast(MagicStrike.STAFF_MELEE, "M");
               this.expectMultiplier("Staff strikes neither use nor build heat", melee, "M", () -> 1.0);
            }
         }

         this.waitAfterLanding(last, 130);
         int cooled = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectBlast("Heat fades 6 seconds after the last fireball", cooled, 3, () -> 1.0);
         this.end();
      }

      if (this.enabled("staves_wildfire")) {
         DoubleSupplier perEnemy = () -> skill("staves_wildfire").getBonusValue(75) / 4.0;
         this.scenario("staves_wildfire", "Wildfire", 75, "staves_wildfire");
         this.spawn("L", 6.0, 0.0);
         int alone = this.cast(MagicStrike.STAFF_FIREBALL, "L");
         this.expectMultiplier("A lone target gets no crowd bonus", alone, "L", () -> 1.0);
         this.remove("L");
         this.spawn("A", 6.0, 0.0);
         this.spawn("B", 6.0, 1.3);
         this.spawn("C", 6.0, -1.3);
         int crowd = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectCrowd("Each hit grows by the skill's share for every other enemy within 4 blocks of it", crowd, perEnemy);
         this.spawn("D", 7.4, 0.0);
         this.spawn("E", 7.4, 1.3);
         this.spawn("F", 7.4, -1.3);
         int packed = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectCrowd("The bonus stops growing at 4 nearby enemies", packed, perEnemy);
         this.check(
            "That blast hit a target with more than 4 enemies around it",
            () -> this.cast(packed).hits().stream().anyMatch(hit -> this.neighbours(hit.target()) > 4),
            () -> this.describe(this.cast(packed))
         );
         this.end();
      }

      if (this.enabled("staves_repel")) {
         this.scenario("staves_repel", "Repel", 75, "staves_repel");
         this.spawnLoose("M", 2.5, 0.0);
         this.spawnLoose("A", 6.0, 3.0);
         this.force("staves_repel", false);
         this.knockback("strike", MagicStrike.STAFF_MELEE, "M");
         this.check("Without a proc the strike does not slow", () -> this.value("strike.slowed") == 0.0, () -> "slowness level " + this.value("strike.slowed"));
         this.knockback("fireball", MagicStrike.STAFF_FIREBALL, "A");
         this.force("staves_repel", true);
         this.knockback("repel", MagicStrike.STAFF_MELEE, "M");
         this.check(
            "A proc knocks the struck enemy at least a block farther than a plain strike",
            () -> this.value("repel") >= this.value("strike") + 1.0,
            () -> "plain strike moved it " + format(this.value("strike")) + ", proc " + format(this.value("repel"))
         );
         this.check("…and gives it Slowness I for 2 seconds", () -> this.value("repel.slowed") == 1.0, () -> "slowness level " + this.value("repel.slowed"));
         this.knockback("forcedFireball", MagicStrike.STAFF_FIREBALL, "A");
         this.check(
            "Fireballs never repel: with a forced proc they push and slow exactly like a plain fireball",
            () -> Math.abs(this.value("forcedFireball") - this.value("fireball")) < 0.3 && this.value("forcedFireball.slowed") == 0.0,
            () -> "plain fireball moved it "
               + format(this.value("fireball"))
               + ", forced "
               + format(this.value("forcedFireball"))
               + ", slowness level "
               + this.value("forcedFireball.slowed")
         );
         this.end();
      }

      if (this.enabled("staves_scorched_earth")) {
         this.scenario("staves_scorched_earth", "Scorched Earth", 75, "staves_scorched_earth");
         this.spawn("T", 6.0, 0.0);
         this.spawn("B", 6.0, 1.8);
         this.spawn("C", 6.0, -3.6);
         this.force("staves_scorched_earth", true);
         int scorch = this.cast(MagicStrike.STAFF_FIREBALL, "T");
         this.waitAfterLanding(scorch, 90);
         this.expectPulses("The ground under the target burns 4 times, one second apart (one patch per fireball)", "T", scorch, 4);
         this.expectPulses("Other enemies standing in it burn the same way", "B", scorch, 4);
         this.expectNoSideHits("Enemies outside the 2.5 block patch are untouched", "C", scorch);
         this.check(
            "Each pulse deals the skill's share of the fireball hit",
            () -> {
               List<MagicSkillScenarios.Hit> pulses = this.sideHits("T", this.landed(scorch));
               double share = skill("staves_scorched_earth").getBonusValue(75) / 4.0;
               return !pulses.isEmpty()
                  && this.cast(scorch)
                     .hits()
                     .stream()
                     .anyMatch(hit -> pulses.stream().allMatch(pulse -> close(pulse.damage(), share * hit.nonCritical(), 0.01)));
            },
            () -> "pulses " + damages(this.sideHits("T", this.landed(scorch))) + ", fireball hits " + this.describe(this.cast(scorch))
         );
         this.end();
      }

      if (this.enabled("staves_immolate")) {
         DoubleSupplier immolated = () -> 1.0 + skill("staves_immolate").getBonusValue(75);
         this.scenario("staves_immolate", "Immolate", 75, "staves_immolate");
         this.spawn("A", 6.0, 0.0);
         int healthy = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectMultiplier("Healthy targets take plain damage", healthy, "A", () -> 1.0);
         this.healthFraction("A", 0.2);
         int wounded = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectMultiplier("Below 30% health the fireball deals the bonus", wounded, "A", immolated);
         this.healthFraction("A", 0.4);
         int recovered = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectMultiplier("Above 30% health it does not", recovered, "A", () -> 1.0);
         this.end();
      }

      if (this.enabled("staves_sunfall")) {
         this.scenario("staves_sunfall", "Sunfall", 74, "staves_sunfall");
         this.spawn("A", 6.0, 0.0);
         this.spawn("B", 6.0, 1.3);
         this.spawn("C", 6.0, -1.3);
         int locked = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectBlast("Sunfall stays locked below level 75", locked, 3, () -> 1.0);
         this.level(75);
         int sunfall = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectBlast(
            "The next fireball lands with the bonus on every target in its blast", sunfall, 3, () -> 1.0 + skill("staves_sunfall").getBonusValue(75)
         );
         this.expectCooldown("…and starts the 20 second cooldown", "staves_sunfall", 18L, 20L);
         int after = this.cast(MagicStrike.STAFF_FIREBALL, "A");
         this.expectBlast("The fireball after it is back to plain damage", after, 3, () -> 1.0);
         this.end();
      }

      if (this.enabled("staves_phoenix_mantle")) {
         this.scenario("staves_phoenix_mantle", "Phoenix Mantle", 75, "staves_phoenix_mantle");
         this.spawnLoose("N", 2.5, 0.0);
         this.spawn("F", 7.0, 0.0);
         this.mark("N");
         this.measure("saved", () -> this.simulator.takeFatalHit(this.body("N")));
         this.waitTicks(8);
         this.expectNear("A fatal blow leaves the player at exactly one heart", () -> 2.0, () -> this.value("saved"), 0.01);
         this.check(
            "Enemies within 4 blocks are thrown back and set on fire",
            () -> this.movedAway("N") >= 0.8 && this.body("N").getFireTicks() > 0,
            () -> blocks(this.movedAway("N")) + ", fire ticks " + this.body("N").getFireTicks()
         );
         this.check("Enemies farther away are untouched", () -> this.body("F").getFireTicks() <= 0, () -> "fire ticks " + this.body("F").getFireTicks());
         this.measure("again", () -> this.simulator.takeFatalHit(this.body("N")));
         this.check(
            "A second fatal blow inside the cooldown is not prevented", () -> this.value("again") > 2.5, () -> "health after the blow " + this.value("again")
         );
         this.expectCooldown("The cooldown is 120 seconds", "staves_phoenix_mantle", 118L, 120L);
         this.end();
      }
   }

   private void wandScenarios() {
      this.scenario("wands_casting_baseline", "Wand casts without skills", 75);
      this.spawn("A", 8.0, 0.0);
      int plainMissile = this.cast(MagicStrike.WAND_MISSILE, "A");
      this.expectMultiplier("A wand missile lands as a missile at plain damage", plainMissile, "A", () -> 1.0);
      this.end();
      if (this.enabled("wands_hex_brand")) {
         DoubleSupplier branded = () -> 1.0 + skill("wands_hex_brand").getBonusValue(75);
         this.scenario("wands_hex_brand", "Hex Brand", 75, "wands_hex_brand");
         this.spawn("A", 8.0, 0.0);
         this.spawn("B", 8.0, -4.0);
         this.force("wands_hex_brand", true);
         int brand = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.force("wands_hex_brand", false);
         this.expectMultiplier("The branding missile itself is not boosted", brand, "A", () -> 1.0);
         int follow = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("Later missiles on the branded target deal the bonus", follow, "A", branded);
         int other = this.cast(MagicStrike.WAND_MISSILE, "B");
         this.expectMultiplier("Other targets are not branded", other, "B", () -> 1.0);
         this.waitAfterLanding(brand, 125);
         int faded = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("The brand fades after 6 seconds", faded, "A", () -> 1.0);
         this.end();
      }

      if (this.enabled("wands_chilling_bolt")) {
         this.scenario("wands_chilling_bolt", "Chilling Bolt", 75, "wands_chilling_bolt");
         this.spawn("A", 8.0, 0.0);
         this.force("wands_chilling_bolt", true);
         int chilled = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(chilled, 2);
         this.expectSlowness("A proc gives the target Slowness II for 2 seconds", "A", 1);
         this.restore("A");
         this.force("wands_chilling_bolt", false);
         int plain = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(plain, 2);
         this.check(
            "Without a proc the target is not slowed", () -> this.body("A").getPotionEffect(PotionEffectType.SLOWNESS) == null, () -> this.slowness("A")
         );
         this.end();
      }

      if (this.enabled("wands_arcane_ward")) {
         this.scenario("wands_arcane_ward", "Arcane Ward", 75, "wands_arcane_ward");
         this.spawn("S", 4.0, 0.0);
         this.measure("warded", () -> this.simulator.takeProjectileHitWithOverride(this.body("S"), 10.0));
         this.measure("melee", () -> this.simulator.takeHitWithOverride(this.body("S"), 10.0));
         this.act(() -> this.simulator.holdTestItem(new ItemStack(Material.STICK)));
         this.measure("unarmed", () -> this.simulator.takeProjectileHitWithOverride(this.body("S"), 10.0));
         this.act(() -> this.simulator.equipWeapon(this.type));
         this.select();
         this.measure("plain", () -> this.simulator.takeProjectileHitWithOverride(this.body("S"), 10.0));
         this.expectNear("Without the skill an elite's arrow lands in full", () -> 10.0, () -> this.value("plain"), 0.01);
         this.expectNear(
            "With it, arrows are reduced by the skill's share",
            () -> 10.0 * (1.0 - skill("wands_arcane_ward").getBonusValue(75)),
            () -> this.value("warded"),
            0.01
         );
         this.expectNear("Melee hits are not reduced", () -> 10.0, () -> this.value("melee"), 0.01);
         this.expectNear("Without a wand in hand there is no reduction", () -> 10.0, () -> this.value("unarmed"), 0.01);
         this.end();
      }

      if (this.enabled("wands_spellweave")) {
         this.scenario("wands_spellweave", "Spellweave", 75, "wands_spellweave");
         this.spawn("A", 8.0, 0.0);
         this.spawn("B", 8.0, -4.0);
         int last = -1;

         for (int missile = 0; missile < 10; missile++) {
            last = this.expectWeave(
               "Missile " + (missile + 1) + " on the same target carries " + Math.min(missile, 8) + " stacks (at most 8)", "A", Math.min(missile, 8)
            );
         }

         this.expectWeave("A missile on another target breaks the weave", "B", 0);
         this.expectWeave("…which then starts over on that target", "B", 0);
         this.expectWeave("…and builds from there", "B", 1);
         this.expectWeave("Going back to the first target does not recover its stacks", "A", 0);
         last = this.expectWeave("…it starts over", "A", 0);
         this.waitAfterLanding(last, 70);
         this.expectWeave("The weave fades after 3 seconds without a missile", "A", 0);
         this.remove("B");
         this.spawn("B", 8.0, 1.5);
         this.act(this::holdMulticastWand);
         this.select("wands_spellweave");
         int first = this.plannedCasts;

         for (int missile = 0; missile < 6; missile++) {
            this.cast(MagicStrike.WAND_MISSILE, "A");
         }

         this.check(
            "Each Multicast cast lands one missile on each of the two enemies",
            () -> this.casts.subList(first, first + 6).stream().allMatch(cast -> cast.on(this.id("A")) != null && cast.on(this.id("B")) != null),
            () -> this.casts.subList(first, first + 6).stream().map(this::describe).collect(Collectors.joining(" / "))
         );
         this.check("With Multicast the focused target keeps building stacks while the other missile stays plain", () -> {
            double perStack = stacking("wands_spellweave").getBonusPerStack(75);
            UUID woven = this.multiplierOn(first + 5, "A") > this.multiplierOn(first + 5, "B") ? this.id("A") : this.id("B");
            UUID otherx = woven.equals(this.id("A")) ? this.id("B") : this.id("A");

            for (int index = 0; index < 6; index++) {
               MagicSkillScenarios.Cast cast = this.casts.get(first + index);
               if (cast.on(woven) == null || cast.on(otherx) == null) {
                  return false;
               }

               if (!closeAbsolute(cast.on(woven).multiplier(), 1.0 + index * perStack)) {
                  return false;
               }

               if (!closeAbsolute(cast.on(otherx).multiplier(), 1.0)) {
                  return false;
               }
            }

            return true;
         }, () -> this.casts.subList(first, first + 6).stream().map(this::describe).collect(Collectors.joining(" / ")));
         this.act(() -> this.simulator.equipWeapon(this.type));
         this.end();
      }

      if (this.enabled("wands_arcing_bolt")) {
         this.scenario("wands_arcing_bolt", "Arcing Bolt", 75, "wands_arcing_bolt");
         this.spawn("A", 8.0, 0.0);
         this.spawn("B", 8.0, -3.0);
         this.spawn("C", 8.0, -7.0);
         this.force("wands_arcing_bolt", true);
         int arc = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(arc, 5);
         this.check("A proc arcs once to the nearest enemy within 5 blocks for the skill's share of the hit", () -> {
            List<MagicSkillScenarios.Hit> arcs = this.sideHits("B", this.landed(arc));
            return arcs.size() == 1 && close(arcs.get(0).damage(), skill("wands_arcing_bolt").getBonusValue(75) * this.damageOn(arc, "A"), 0.01);
         }, () -> "arcs " + damages(this.sideHits("B", this.landed(arc))) + ", missile " + this.describe(this.cast(arc)));
         this.expectNoSideHits("The arc does not chain on to the next enemy", "C", arc);
         this.remove("B");
         this.remove("C");
         int lone = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(lone, 5);
         this.expectNoSideHits("With no second enemy nearby there is no arc", "A", lone);
         this.end();
      }

      if (this.enabled("wands_duelists_focus")) {
         DoubleSupplier focused = () -> 1.0 + skill("wands_duelists_focus").getBonusValue(75);
         this.scenario("wands_duelists_focus", "Duelist's Focus", 75, "wands_duelists_focus");
         this.spawn("A", 8.0, 0.0);
         int alone = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("Alone with the target, missiles deal the bonus", alone, "A", focused);
         this.spawn("W", -8.0, 0.0);
         int watched = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("Another enemy within 12 blocks of the player cancels it", watched, "A", () -> 1.0);
         this.move("W", -13.0, 0.0);
         int distant = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("Enemies farther than 12 blocks do not count", distant, "A", focused);
         this.end();
      }

      if (this.enabled("wands_unravel")) {
         DoubleSupplier unravelled = () -> 1.0 + skill("wands_unravel").getBonusValue(75);
         this.scenario("wands_unravel", "Unravel", 75, "wands_unravel");
         this.spawn("A", 8.0, 0.0);
         this.spawn("B", 8.0, -4.0);
         this.force("wands_unravel", true);
         int unravel = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.force("wands_unravel", false);
         this.expectMultiplier("The unravelling missile itself is not boosted", unravel, "A", () -> 1.0);
         int follow = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("Later hits on the target deal the bonus", follow, "A", unravelled);
         int other = this.cast(MagicStrike.WAND_MISSILE, "B");
         this.expectMultiplier("Other targets are not unravelled", other, "B", () -> 1.0);
         this.select();
         int party = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("The bonus holds for an attacker without Unravel, so the whole party gets it", party, "A", unravelled);
         this.waitAfterLanding(unravel, 105);
         int faded = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.expectMultiplier("It fades after 5 seconds", faded, "A", () -> 1.0);
         this.end();
      }

      if (this.enabled("wands_echo_bolt")) {
         boolean witness = this.enabled("wands_hex_brand");
         if (witness) {
            this.scenario("wands_echo_bolt", "Echo Bolt", 75, "wands_echo_bolt", "wands_hex_brand");
         } else {
            this.scenario("wands_echo_bolt", "Echo Bolt", 75, "wands_echo_bolt");
         }

         this.spawn("A", 8.0, 0.0);
         this.force("wands_echo_bolt", true);
         this.force("wands_hex_brand", true);
         int echoed = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(echoed, 20);
         this.check(
            "A proc repeats the hit once, half a second later, for the same damage",
            () -> {
               List<MagicSkillScenarios.Hit> echoes = this.sideHits("A", this.landed(echoed));
               return echoes.size() == 1
                  && echoes.get(0).tick() - this.landed(echoed) >= 9L
                  && echoes.get(0).tick() - this.landed(echoed) <= 12L
                  && close(echoes.get(0).damage(), this.damageOn(echoed, "A"), 0.01);
            },
            () -> "echo ticks "
               + ticks(this.sideHits("A", this.landed(echoed)), this.landed(echoed))
               + ", damages "
               + damages(this.sideHits("A", this.landed(echoed)))
               + ", missile "
               + this.describe(this.cast(echoed))
         );
         if (witness) {
            this.check(
               "The echo does not trigger other skills",
               () -> skill("wands_hex_brand").getProcCount(this.player) == 1,
               () -> "Hex Brand procs " + skill("wands_hex_brand").getProcCount(this.player)
            );
         }

         this.force("wands_echo_bolt", false);
         this.force("wands_hex_brand", false);
         int plain = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(plain, 20);
         this.expectNoSideHits("Without a proc there is no echo", "A", plain);
         this.end();
      }

      if (this.enabled("wands_starfall_volley")) {
         this.scenario("wands_starfall_volley", "Starfall Volley", 74, "wands_starfall_volley");
         this.spawn("A", 8.0, 0.0);
         this.spawn("D1", 8.0, 2.0);
         this.spawn("D2", 8.0, -2.0);
         this.spawn("D3", 10.0, 0.0);
         this.spawn("D4", 8.0, 6.0);
         int locked = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(locked, 30);
         this.check(
            "Starfall stays locked below level 75",
            () -> this.sideHitsSince(this.landed(locked)).isEmpty(),
            () -> this.sideHitsSince(this.landed(locked)).size() + " bolts"
         );
         this.level(75);
         int volley = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(volley, 30);
         this.check(
            "A missile hit calls down 8 bolts, 2 on the target and on each of the 3 nearest enemies",
            () -> {
               long from = this.landed(volley);
               return this.sideHitsSince(from).size() == 8
                  && List.of("A", "D1", "D2", "D3").stream().allMatch(name -> this.sideHits(name, from).size() == 2)
                  && this.sideHits("D4", from).isEmpty();
            },
            () -> this.boltSpread(this.landed(volley))
         );
         this.check("Each bolt deals the skill's share of the missile hit", () -> {
            double bolt = skill("wands_starfall_volley").getBonusValue(75) * this.damageOn(volley, "A");
            List<MagicSkillScenarios.Hit> bolts = this.sideHitsSince(this.landed(volley));
            return !bolts.isEmpty() && bolts.stream().allMatch(hit -> close(hit.damage(), bolt, 0.01));
         }, () -> "bolts " + damages(this.sideHitsSince(this.landed(volley))) + ", missile " + this.describe(this.cast(volley)));
         this.expectCooldown("The cooldown is 20 seconds", "wands_starfall_volley", 17L, 20L);
         int after = this.cast(MagicStrike.WAND_MISSILE, "A");
         this.waitAfterLanding(after, 30);
         this.check(
            "The next missile inside the cooldown calls down nothing",
            () -> this.sideHitsSince(this.landed(after)).isEmpty(),
            () -> this.sideHitsSince(this.landed(after)).size() + " bolts"
         );
         this.end();
      }

      if (this.enabled("wands_spell_mirror")) {
         this.scenario("wands_spell_mirror", "Spell Mirror", 75, "wands_spell_mirror");
         this.spawn("N", 3.0, 0.0);
         this.act(() -> this.values.put("mirroredAt", (double)this.tick));
         this.measure("mirrored", () -> this.simulator.takeHitWithOverride(this.body("N"), 10.0));
         this.waitTicks(2);
         this.expectNear("The first hit is negated", () -> 0.0, () -> this.value("mirrored"), 0.01);
         this.check("…and answered with the skill's multiple of it", () -> {
            List<MagicSkillScenarios.Hit> counters = this.sideHits("N", (long)this.value("mirroredAt"));
            return counters.size() == 1 && close(counters.get(0).damage(), 10.0 * skill("wands_spell_mirror").getBonusValue(75), 0.01);
         }, () -> "counters " + damages(this.sideHits("N", (long)this.value("mirroredAt"))));
         this.measure("next", () -> this.simulator.takeHitWithOverride(this.body("N"), 10.0));
         this.expectNear("Hits inside the cooldown land normally", () -> 10.0, () -> this.value("next"), 0.01);
         this.expectCooldown("The cooldown is 15 seconds", "wands_spell_mirror", 14L, 15L);
         this.end();
      }

      this.scenario("wands_no_mobility", "No wand skill grants movement", 75);
      this.spawn("A", 8.0, 0.0);
      this.act(() -> {
         this.values.put("walkSpeed", (double)this.player.getWalkSpeed());
         this.values.put("speedModifiers", (double)this.movementModifiers().size());
      });
      this.select(SkillBonusRegistry.getEnabledBonuses(SkillType.WANDS).stream().map(SkillBonus::getSkillId).toArray(String[]::new));

      for (int missile = 0; missile < 3; missile++) {
         this.cast(MagicStrike.WAND_MISSILE, "A");
      }

      this.check(
         "Selecting every wand skill adds no movement speed",
         () -> this.player.getWalkSpeed() == this.value("walkSpeed")
            && this.movementModifiers().size() == this.value("speedModifiers")
            && this.player.getPotionEffect(PotionEffectType.SPEED) == null,
         () -> "walk speed " + this.player.getWalkSpeed() + ", movement modifiers " + this.movementModifiers()
      );
      this.check(
         "Casting still slows the caster", () -> this.casterSlownessAfterCast != null, () -> "slowness after the last cast: " + this.casterSlownessAfterCast
      );
      this.end();
   }

   private void scenario(String id, String name, int level, String... selected) {
      this.steps.add(() -> {
         this.removeDummies();
         this.ids.clear();
         this.marks.clear();
         this.values.clear();
         this.casts.clear();
         ProcRoll.clear(this.playerId);
         this.result = new SkillTestResult(id, name, this.type, 75);
         this.scenarioStart = this.tick;
         this.log.log("Scenario: " + name);
         this.setLevel(level);
         this.applySelection(selected);
         this.simulator.equipWeapon(this.type);
         this.player.teleport(this.anchor);
         this.player.setHealth(this.player.getMaxHealth());
         this.player.setFireTicks(0);

         for (PotionEffect effect : this.player.getActivePotionEffects()) {
            this.player.removePotionEffect(effect.getType());
         }

         return true;
      });
      this.plannedCasts = 0;
   }

   private void end() {
      this.steps.add(new MagicSkillScenarios.End());
   }

   private void advance() {
      this.tick++;

      try {
         while (!this.steps.isEmpty()) {
            if (!this.steps.peek().advance()) {
               return;
            }

            this.steps.poll();
         }
      } catch (RuntimeException var2) {
         if (this.result != null) {
            this.result.addIssue("Scenario stopped: " + var2);
         }

         this.log.log("  Scenario stopped: " + var2);

         while (!this.steps.isEmpty() && !(this.steps.peek() instanceof MagicSkillScenarios.End)) {
            this.steps.poll();
         }

         if (this.steps.isEmpty()) {
            this.close();
            this.onComplete.run();
         }
      }
   }

   private void act(Runnable action) {
      this.steps.add(() -> {
         action.run();
         return true;
      });
   }

   private void waitTicks(int ticks) {
      long[] until = new long[]{-1L};
      this.steps.add(() -> {
         if (until[0] < 0L) {
            until[0] = this.tick + ticks;
         }

         return this.tick >= until[0];
      });
   }

   private void waitAfterLanding(int cast, int ticks) {
      this.steps.add(() -> this.tick >= this.landed(cast) + ticks);
   }

   private void measure(String key, DoubleSupplier measurement) {
      this.act(() -> this.values.put(key, measurement.getAsDouble()));
   }

   private void force(String skillId, boolean outcome) {
      this.act(() -> ProcRoll.force(this.playerId, skillId, outcome));
   }

   private void select(String... skillIds) {
      this.act(() -> this.applySelection(skillIds));
   }

   private void level(int level) {
      this.act(() -> {
         this.setLevel(level);
         SkillBonusRegistry.applyBonuses(this.player, this.type);
      });
   }

   private void setLevel(int level) {
      PlayerData.setSkillXP(this.playerId, this.type, SkillXPCalculator.totalXPForLevel(level));
   }

   private void applySelection(String... skillIds) {
      this.resetTypeSkills();

      for (String skillId : skillIds) {
         PlayerSkillSelection.addActiveSkill(this.playerId, this.type, skillId, true);
      }

      SkillBonusRegistry.applyBonuses(this.player, this.type);
   }

   private void resetTypeSkills() {
      for (String active : List.copyOf(PlayerSkillSelection.getActiveSkills(this.playerId, this.type))) {
         PlayerSkillSelection.removeActiveSkill(this.playerId, this.type, active);
      }

      for (SkillBonus skill : SkillBonusRegistry.getAllBonuses()) {
         if (skill.getSkillType() == this.type) {
            if (skill.isActive(this.player)) {
               skill.removeBonus(this.player);
            }

            if (skill instanceof CooldownSkill cooldown) {
               cooldown.endCooldown(this.player);
            }

            skill.resetProcCount(this.player);
         }
      }
   }

   private void holdMulticastWand() {
      ItemStack wand = this.player.getInventory().getItemInMainHand();
      this.simulator
         .holdTestItem(
            EnchantmentItems.prepareConstruction(List.of("freeminecraftmodels:multicast")).applyCustom(wand, Map.of("freeminecraftmodels:multicast", 1))
         );
   }

   private void spawn(String name, double ahead, double side) {
      this.spawn(name, ahead, side, false);
   }

   private void spawnLoose(String name, double ahead, double side) {
      this.spawn(name, ahead, side, true);
   }

   private void spawn(String name, double ahead, double side, boolean loose) {
      this.act(() -> {
         Location at = this.place(ahead, side);
         CustomBossEntity boss = RegionalBossEntity.createTemporaryRegionalBossEntity("damage_test_dummy.yml", at);
         if (boss == null) {
            throw new IllegalStateException("the damage test dummy is not loaded");
         } else {
            boss.spawn(true);
            LivingEntity body = boss.getLivingEntity();
            if (body == null) {
               throw new IllegalStateException("dummy " + name + " did not spawn");
            } else {
               body.setMaximumNoDamageTicks(0);
               body.setCustomName("§e" + name);
               body.setCustomNameVisible(true);
               if (loose) {
                  body.setAI(true);
                  if (body instanceof Mob mob) {
                     mob.setAware(false);
                  }
               }

               this.dummies.put(name, new MagicSkillScenarios.Dummy(boss, body, at.clone()));
               this.ids.put(name, body.getUniqueId());
            }
         }
      });
   }

   private Location place(double ahead, double side) {
      Location at = this.anchor.clone().add(this.forward.clone().multiply(ahead)).add(this.right.clone().multiply(side));

      for (int step = 0; step < 4 && !at.getBlock().isPassable(); step++) {
         at.add(0.0, 1.0, 0.0);
      }

      for (int step = 0; step < 4 && at.clone().add(0.0, -1.0, 0.0).getBlock().isPassable(); step++) {
         at.add(0.0, -1.0, 0.0);
      }

      at.setDirection(this.anchor.toVector().subtract(at.toVector()));
      return at;
   }

   private void move(String name, double ahead, double side) {
      this.act(() -> this.body(name).teleport(this.place(ahead, side)));
   }

   private void remove(String name) {
      this.act(() -> {
         MagicSkillScenarios.Dummy dummy = this.dummies.remove(name);
         if (dummy != null) {
            discard(dummy);
         }
      });
   }

   private void restore(String name) {
      this.act(() -> {
         MagicSkillScenarios.Dummy dummy = this.dummies.get(name);
         dummy.body().teleport(dummy.spawn());
         dummy.body().setVelocity(new Vector());
         dummy.body().setFireTicks(0);

         for (PotionEffect effect : dummy.body().getActivePotionEffects()) {
            dummy.body().removePotionEffect(effect.getType());
         }
      });
      this.waitTicks(3);
   }

   private void mark(String name) {
      this.act(() -> this.marks.put(name, this.body(name).getLocation().clone()));
   }

   private void healthFraction(String name, double fraction) {
      this.act(() -> {
         EliteEntity elite = EntityTracker.getEliteMobEntity(this.body(name));
         elite.setHealth(elite.getMaxHealth() * fraction);
      });
   }

   private void removeDummies() {
      for (MagicSkillScenarios.Dummy dummy : this.dummies.values()) {
         discard(dummy);
      }

      this.dummies.clear();
   }

   private static void discard(MagicSkillScenarios.Dummy dummy) {
      LivingEntity body = dummy.body();
      if (body.isValid()) {
         body.setHealth(0.0);
         body.remove();
      }
   }

   private LivingEntity body(String name) {
      MagicSkillScenarios.Dummy dummy = this.dummies.get(name);
      if (dummy == null) {
         throw new IllegalStateException("no dummy " + name);
      } else {
         return dummy.body();
      }
   }

   private UUID id(String name) {
      UUID id = this.ids.get(name);
      if (id == null) {
         throw new IllegalStateException("no dummy " + name);
      } else {
         return id;
      }
   }

   private int cast(MagicStrike strike, String target) {
      int index = this.plannedCasts++;
      int[] firstHit = new int[]{-1};
      long[] firedAt = new long[]{-1L};
      this.steps.add(() -> this.tick >= this.readyAt.getOrDefault(strike, 0L));
      this.steps.add(() -> {
         LivingEntity body = this.body(target);
         this.aimAt(body);
         firstHit[0] = this.hits.size();
         firedAt[0] = this.tick;
         this.lastClick = "";
         switch (strike) {
            case STAFF_FIREBALL:
               this.click(Action.RIGHT_CLICK_AIR);
               break;
            case STAFF_MELEE:
               this.player.attack(body);
               break;
            case WAND_MISSILE:
               this.click(Action.LEFT_CLICK_AIR);
         }

         this.casterSlownessAfterCast = this.player.getPotionEffect(PotionEffectType.SLOWNESS);
         this.lastClick = this.lastClick + ", weapon cooldown " + this.player.getCooldown(this.player.getInventory().getItemInMainHand().getType());
         this.readyAt.put(strike, this.tick + reloadTicks(strike) + 1L);
         return true;
      });
      this.steps
         .add(
            () -> {
               MagicSkillScenarios.Hit landing = this.firstHitOf(firstHit[0], strike);
               if (landing == null) {
                  if (this.tick - firedAt[0] < 60L) {
                     return false;
                  } else {
                     this.issue(
                        "The "
                           + strikeName(strike)
                           + " aimed at "
                           + target
                           + " never landed (click "
                           + this.lastClick
                           + ", caster slowed by the cast: "
                           + (this.casterSlownessAfterCast != null)
                           + ", damage events since: "
                           + this.trace.stream().filter(line -> Long.parseLong(line.substring(0, line.indexOf(58))) >= firedAt[0]).limit(6L).toList()
                           + ")"
                     );
                     this.casts.add(new MagicSkillScenarios.Cast(null, firedAt[0], List.of()));
                     return true;
                  }
               } else {
                  List<MagicSkillScenarios.Hit> landed = this.hits
                     .subList(firstHit[0], this.hits.size())
                     .stream()
                     .filter(hit -> landing.attackId().equals(hit.attackId()))
                     .toList();
                  long lastLanding = landed.get(landed.size() - 1).tick();
                  if (this.tick - lastLanding < 5L && this.tick - landing.tick() < 40L) {
                     return false;
                  } else {
                     this.casts.add(new MagicSkillScenarios.Cast(landing.attackId(), landing.tick(), landed));
                     return true;
                  }
               }
            }
         );
      return index;
   }

   private void aimAt(LivingEntity target) {
      Location center = target.getLocation().add(0.0, Math.max(0.35, target.getHeight() * 0.52), 0.0);
      Location look = this.anchor.clone();
      look.setDirection(center.toVector().subtract(this.anchor.clone().add(0.0, this.player.getEyeHeight(), 0.0).toVector()));
      this.player.teleport(look);
   }

   private void click(Action action) {
      PlayerInteractEvent click = new PlayerInteractEvent(
         this.player, action, this.player.getInventory().getItemInMainHand(), null, BlockFace.SELF, EquipmentSlot.HAND
      );
      firingCast = true;

      try {
         Bukkit.getPluginManager().callEvent(click);
      } finally {
         firingCast = false;
      }

      this.lastClick = "cancelled " + click.isCancelled() + ", item use " + click.useItemInHand();
   }

   static boolean isFiringCast() {
      return firingCast;
   }

   private static int reloadTicks(MagicStrike strike) {
      return switch (strike) {
         case STAFF_FIREBALL -> 40;
         case STAFF_MELEE -> 12;
         case WAND_MISSILE -> 11;
      };
   }

   private MagicSkillScenarios.Hit firstHitOf(int from, MagicStrike strike) {
      for (int index = from; index < this.hits.size(); index++) {
         MagicSkillScenarios.Hit hit = this.hits.get(index);
         if (hit.attackId() != null && hit.strike() == strike) {
            return hit;
         }
      }

      return null;
   }

   private int knockback(String key, MagicStrike strike, String target) {
      this.restore(target);
      this.mark(target);
      int cast = this.cast(strike, target);
      this.waitAfterLanding(cast, 12);
      this.measure(key, () -> this.movedAway(target));
      this.measure(key + ".slowed", () -> {
         PotionEffect slowness = this.body(target).getPotionEffect(PotionEffectType.SLOWNESS);
         return slowness == null ? 0.0 : slowness.getAmplifier() + 1;
      });
      return cast;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void beforeSkills(EliteMobDamagedByPlayerEvent event) {
      if (event.getPlayer() == this.player) {
         this.enteringSkills.put(event, event.getDamage());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void afterSkills(EliteMobDamagedByPlayerEvent event) {
      Double entering = this.enteringSkills.remove(event);
      if (entering != null) {
         CombatDamageContext.MagicHit magicHit = event.getMagicHit();
         LivingEntity struck = event.getEliteMobEntity().getLivingEntity();
         this.trace
            .add(
               this.tick
                  + ": "
                  + (struck == null ? "?" : this.name(struck.getUniqueId()))
                  + (magicHit != null ? " " + magicHit.strike() : (event.isCustomDamage() ? " side" : " plain"))
                  + (event.isCancelled() ? " cancelled" : "")
            );
         if (!event.isCancelled()) {
            if (magicHit != null || event.isCustomDamage()) {
               LivingEntity target = event.getEliteMobEntity().getLivingEntity();
               if (target != null) {
                  this.hits
                     .add(
                        new MagicSkillScenarios.Hit(
                           this.tick,
                           target.getUniqueId(),
                           magicHit == null ? null : magicHit.attackId(),
                           magicHit == null ? null : magicHit.strike(),
                           entering > 0.0 ? event.getDamage() / entering : 1.0,
                           event.getDamage(),
                           event.getDamageWithoutCriticalStrike(),
                           true
                        )
                     );
                  if (event.getEntityDamageByEntityEvent() != null) {
                     this.unsettled.put(event.getEntityDamageByEntityEvent(), this.hits.size() - 1);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void settled(EntityDamageByEntityEvent event) {
      Integer index = this.unsettled.remove(event);
      if (index != null && event.isCancelled()) {
         this.hits.set(index, this.hits.get(index).cancelled());
         this.trace.add(this.tick + ": " + this.name(event.getEntity().getUniqueId()) + " cancelled by a later listener");
      }
   }

   private void check(String expectation, BooleanSupplier holds, Supplier<String> observed) {
      this.steps.add(() -> {
         boolean held;
         String detail;
         try {
            held = holds.getAsBoolean();
            detail = observed.get();
         } catch (RuntimeException var7) {
            held = false;
            detail = var7.toString();
         }

         String line = (held ? "PASS " : "FAIL ") + expectation + " | " + detail;
         this.result.addLog(line);
         this.log.log("  " + line);
         if (!held) {
            this.result.addIssue(expectation + " (" + detail + ")");
         }

         return true;
      });
   }

   private void issue(String text) {
      this.result.addLog("FAIL " + text);
      this.log.log("  FAIL " + text);
      this.result.addIssue(text);
   }

   private void expectNear(String expectation, DoubleSupplier expected, DoubleSupplier actual, double tolerance) {
      this.check(
         expectation,
         () -> close(actual.getAsDouble(), expected.getAsDouble(), tolerance),
         () -> "expected " + format(expected.getAsDouble()) + ", observed " + format(actual.getAsDouble())
      );
   }

   private void expectMultiplier(String expectation, int cast, String target, DoubleSupplier expected) {
      this.check(
         expectation,
         () -> closeAbsolute(this.multiplierOn(cast, target), expected.getAsDouble()),
         () -> "expected ×" + format(expected.getAsDouble()) + ", observed " + this.describe(this.cast(cast))
      );
   }

   private void expectBlast(String expectation, int cast, int minimumTargets, DoubleSupplier expected) {
      this.check(
         expectation,
         () -> this.cast(cast).hits().size() >= minimumTargets
            && this.cast(cast).hits().stream().allMatch(hit -> closeAbsolute(hit.multiplier(), expected.getAsDouble())),
         () -> "expected ×" + format(expected.getAsDouble()) + " on " + minimumTargets + "+ targets, observed " + this.describe(this.cast(cast))
      );
   }

   private void expectCrowd(String expectation, int cast, DoubleSupplier perEnemy) {
      this.check(
         expectation,
         () -> !this.cast(cast).hits().isEmpty()
            && this.cast(cast)
               .hits()
               .stream()
               .allMatch(hit -> closeAbsolute(hit.multiplier(), 1.0 + Math.min(4, this.neighbours(hit.target())) * perEnemy.getAsDouble())),
         () -> "per enemy "
            + format(perEnemy.getAsDouble())
            + ", observed "
            + this.cast(cast)
               .hits()
               .stream()
               .map(hit -> this.name(hit.target()) + " ×" + format(hit.multiplier()) + " with " + this.neighbours(hit.target()) + " near")
               .collect(Collectors.joining(", "))
      );
   }

   private void expectPulses(String expectation, String target, int cast, int count) {
      this.check(expectation, () -> {
         List<MagicSkillScenarios.Hit> pulses = this.sideHits(target, this.landed(cast));
         if (pulses.size() != count) {
            return false;
         } else {
            long previous = this.landed(cast);

            for (MagicSkillScenarios.Hit pulse : pulses) {
               if (pulse.tick() - previous < 18L || pulse.tick() - previous > 22L) {
                  return false;
               }

               previous = pulse.tick();
            }

            return true;
         }
      }, () -> "pulse ticks after landing " + ticks(this.sideHits(target, this.landed(cast)), this.landed(cast)));
   }

   private void expectNoSideHits(String expectation, String target, int cast) {
      this.check(expectation, () -> this.sideHits(target, this.landed(cast)).isEmpty(), () -> "side hits " + damages(this.sideHits(target, this.landed(cast))));
   }

   private void expectSlowness(String expectation, String target, int amplifier) {
      this.check(expectation, () -> {
         PotionEffect slowness = this.body(target).getPotionEffect(PotionEffectType.SLOWNESS);
         return slowness != null && slowness.getAmplifier() == amplifier && slowness.getDuration() > 20 && slowness.getDuration() <= 40;
      }, () -> this.slowness(target));
   }

   private void expectCooldown(String expectation, String skillId, long minimum, long maximum) {
      this.check(expectation, () -> {
         long remaining = ((CooldownSkill)skill(skillId)).getRemainingCooldown(this.player);
         return remaining >= minimum && remaining <= maximum;
      }, () -> "remaining " + ((CooldownSkill)skill(skillId)).getRemainingCooldown(this.player) + "s");
   }

   private int expectWeave(String expectation, String target, int stacks) {
      int cast = this.cast(MagicStrike.WAND_MISSILE, target);
      this.expectMultiplier(expectation, cast, target, () -> 1.0 + stacks * stacking("wands_spellweave").getBonusPerStack(75));
      return cast;
   }

   private MagicSkillScenarios.Cast cast(int index) {
      if (index >= this.casts.size()) {
         throw new IllegalStateException("cast " + index + " has not happened");
      } else {
         return this.casts.get(index);
      }
   }

   private long landed(int cast) {
      return this.cast(cast).landedAt();
   }

   private double multiplierOn(int cast, String target) {
      MagicSkillScenarios.Hit hit = this.cast(cast).on(this.id(target));
      return hit == null ? Double.NaN : hit.multiplier();
   }

   private double damageOn(int cast, String target) {
      MagicSkillScenarios.Hit hit = this.cast(cast).on(this.id(target));
      return hit == null ? Double.NaN : hit.damage();
   }

   private double nonCritical(int cast, String target) {
      MagicSkillScenarios.Hit hit = this.cast(cast).on(this.id(target));
      return hit == null ? Double.NaN : hit.nonCritical();
   }

   private List<MagicSkillScenarios.Hit> sideHits(String target, long from) {
      UUID id = this.id(target);
      return this.hits.stream().filter(hit -> hit.attackId() == null && hit.landed() && hit.target().equals(id) && hit.tick() >= from).toList();
   }

   private List<MagicSkillScenarios.Hit> sideHitsSince(long from) {
      return this.hits.stream().filter(hit -> hit.attackId() == null && hit.landed() && hit.tick() >= from).toList();
   }

   private double sideDamage(String target, long from) {
      return this.sideHits(target, from).stream().mapToDouble(MagicSkillScenarios.Hit::damage).sum();
   }

   private double value(String key) {
      Double value = this.values.get(key);
      if (value == null) {
         throw new IllegalStateException("nothing measured as " + key);
      } else {
         return value;
      }
   }

   private int neighbours(UUID target) {
      LivingEntity center = this.dummies
         .values()
         .stream()
         .map(MagicSkillScenarios.Dummy::body)
         .filter(body -> body.getUniqueId().equals(target))
         .findFirst()
         .orElse(null);
      return center == null
         ? 0
         : (int)this.dummies
            .values()
            .stream()
            .map(MagicSkillScenarios.Dummy::body)
            .filter(body -> body != center && body.isValid() && body.getLocation().distanceSquared(center.getLocation()) <= 16.0)
            .count();
   }

   private double movedAway(String name) {
      Location from = this.marks.get(name);
      if (from == null) {
         throw new IllegalStateException(name + " was not marked");
      } else {
         Vector away = from.toVector().subtract(this.anchor.toVector()).setY(0).normalize();
         return this.body(name).getLocation().toVector().subtract(from.toVector()).setY(0).dot(away);
      }
   }

   private List<NamespacedKey> movementModifiers() {
      AttributeInstance movement = this.player.getAttribute(Attribute.MOVEMENT_SPEED);
      return movement == null
         ? List.of()
         : movement.getModifiers().stream().<NamespacedKey>map(AttributeModifier::getKey).filter(key -> !key.getKey().startsWith("effect.")).toList();
   }

   private static <T extends SkillBonus> T skill(String skillId) {
      SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
      if (skill == null) {
         throw new IllegalStateException("skill " + skillId + " is not registered");
      } else {
         return (T)skill;
      }
   }

   private static StackingSkill stacking(String skillId) {
      return (StackingSkill)SkillBonusRegistry.getSkillById(skillId);
   }

   private boolean enabled(String skillId) {
      return SkillBonusRegistry.getEnabledBonuses(this.type).stream().anyMatch(skill -> skill.getSkillId().equals(skillId));
   }

   private static boolean close(double actual, double expected, double relative) {
      return Double.isFinite(actual) && Double.isFinite(expected) ? Math.abs(actual - expected) <= Math.max(relative * Math.abs(expected), 1.0E-6) : false;
   }

   private static boolean closeAbsolute(double actual, double expected) {
      return Double.isFinite(actual) && Math.abs(actual - expected) <= 0.002;
   }

   private String name(UUID id) {
      return this.ids.entrySet().stream().filter(entry -> entry.getValue().equals(id)).map(Entry::getKey).findFirst().orElse("?");
   }

   private String describe(MagicSkillScenarios.Cast cast) {
      return (CharSequence)(cast.attackId() == null
         ? "no landing"
         : cast.hits().stream().map(hit -> this.name(hit.target()) + " ×" + format(hit.multiplier())).collect(Collectors.joining(", ")));
   }

   private String slowness(String name) {
      PotionEffect effect = this.body(name).getPotionEffect(PotionEffectType.SLOWNESS);
      return effect == null ? "no slowness" : "slowness " + (effect.getAmplifier() + 1) + " for " + effect.getDuration() + " ticks";
   }

   private String boltSpread(long from) {
      return this.ids
         .keySet()
         .stream()
         .filter(this.dummies::containsKey)
         .map(name -> name + "=" + this.sideHits(name, from).size())
         .collect(Collectors.joining(", "));
   }

   private static String damages(List<MagicSkillScenarios.Hit> hits) {
      return hits.stream().map(hit -> format(hit.damage())).collect(Collectors.joining(", ", "[", "]"));
   }

   private static String ticks(List<MagicSkillScenarios.Hit> hits, long from) {
      return hits.stream().map(hit -> String.valueOf(hit.tick() - from)).collect(Collectors.joining(", ", "[", "]"));
   }

   private static String blocks(double distance) {
      return "moved " + format(distance) + " blocks away";
   }

   private static String format(double value) {
      return String.format(Locale.ROOT, "%.3f", value);
   }

   private static String strikeName(MagicStrike strike) {
      return switch (strike) {
         case STAFF_FIREBALL -> "fireball";
         case STAFF_MELEE -> "staff strike";
         case WAND_MISSILE -> "wand missile";
      };
   }

   private record Cast(UUID attackId, long landedAt, List<MagicSkillScenarios.Hit> hits) {
      MagicSkillScenarios.Hit on(UUID target) {
         return this.hits.stream().filter(hit -> hit.target().equals(target)).findFirst().orElse(null);
      }
   }

   private record Dummy(CustomBossEntity boss, LivingEntity body, Location spawn) {
   }

   private final class End implements MagicSkillScenarios.Step {
      @Override
      public boolean advance() {
         if (MagicSkillScenarios.this.result != null) {
            Set<UUID> fired = MagicSkillScenarios.this.casts
               .stream()
               .map(MagicSkillScenarios.Cast::attackId)
               .filter(Objects::nonNull)
               .collect(Collectors.toSet());
            List<MagicSkillScenarios.Hit> scenarioHits = MagicSkillScenarios.this.hits
               .stream()
               .filter(hit -> hit.tick() >= MagicSkillScenarios.this.scenarioStart)
               .toList();
            List<String> stray = scenarioHits.stream()
               .filter(hit -> hit.attackId() != null && !fired.contains(hit.attackId()))
               .map(hit -> hit.strike() + " on " + MagicSkillScenarios.this.name(hit.target()))
               .distinct()
               .toList();
            if (!stray.isEmpty()) {
               MagicSkillScenarios.this.issue("Casts fired that the scenario never made: " + stray);
            }

            List<String> lost = scenarioHits.stream()
               .filter(hit -> !hit.landed())
               .map(
                  hit -> (hit.strike() == null ? "side hit" : hit.strike().toString())
                     + " on "
                     + MagicSkillScenarios.this.name(hit.target())
                     + " for "
                     + MagicSkillScenarios.format(hit.damage())
               )
               .toList();
            if (!lost.isEmpty()) {
               MagicSkillScenarios.this.issue("Hits were cancelled before dealing any damage: " + lost);
            }

            MagicSkillScenarios.this.report.addResult(MagicSkillScenarios.this.result);
            String first = MagicSkillScenarios.this.result.getIssues().isEmpty() ? "" : " §7" + MagicSkillScenarios.this.result.getIssues().get(0);
            MagicSkillScenarios.this.player
               .sendMessage((MagicSkillScenarios.this.result.isPassed() ? "§a✓ " : "§c✗ ") + MagicSkillScenarios.this.result.getSkillName() + first);
         }

         MagicSkillScenarios.this.removeDummies();
         ProcRoll.clear(MagicSkillScenarios.this.playerId);
         MagicSkillScenarios.this.result = null;
         return true;
      }
   }

   private record Hit(long tick, UUID target, UUID attackId, MagicStrike strike, double multiplier, double damage, double nonCritical, boolean landed) {
      MagicSkillScenarios.Hit cancelled() {
         return new MagicSkillScenarios.Hit(this.tick, this.target, this.attackId, this.strike, this.multiplier, this.damage, this.nonCritical, false);
      }
   }

   private interface Step {
      boolean advance();
   }
}
