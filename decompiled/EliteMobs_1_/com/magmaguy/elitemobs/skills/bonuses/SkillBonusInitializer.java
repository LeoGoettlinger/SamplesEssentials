package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusesConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorAdrenalineSurgeConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorBattleHardenedConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorEvasionConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorFortifyConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorGritConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorIronStanceConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorLastStandConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorReactiveShieldingConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorRetaliationConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.ArmorSecondWindConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.AxesDevastatingBlowConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.AxesExecutionerConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.AxesMomentumConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.AxesStaggerConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.AxesTimberConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.AxesWoundConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsBarrageConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsDeadEyeConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsFrostbiteConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsHuntersMarkConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsMultishotConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsOverdrawConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsPackHunterConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsRangersFocusConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsRicochetConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.BowsWindRunnerConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsArrowRainConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsExplosiveTipConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsFirstBloodConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsHeavyBoltsConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsHuntersPreyConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsQuickReloadConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsSteadyAimConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.CrossbowsSuppressingFireConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesDeathMarkConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesDeathsEmbraceConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesGrimReachConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesHarvesterConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesReapWhatYouSowConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesReapersHarvestConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesScytheSweepConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesSoulDrainConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesSoulSiphonConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.HoesSpectralScytheConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesAvatarOfJudgmentConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesConcussionConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesConsecrationConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesCrushingBlowConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesDivineShieldConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesHammerOfWrathConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesJudgmentConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesRighteousFuryConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesShatterConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesStunningForceConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsFirstStrikeConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsImpalerConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsImpalingStrikeConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsLegionsDisciplineConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsLongReachConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsPhalanxConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsPolearmMasteryConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsPrecisionThrustConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsVanguardConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SpearsVortexThrustConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesBattlemagesGuardConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesConcussiveBlastConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesImmolateConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesPhoenixMantleConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesRepelConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesScorchedEarthConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesSmolderConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesStokeTheFlamesConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesSunfallConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.StavesWildfireConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsDuelistConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsExposeWeaknessConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsFinishingFlourishConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsFlurryConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsLacerateConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsParryConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsPoiseConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsRiposteConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsSwiftStrikesConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.SwordsVorpalStrikeConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsDepthChargeConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsImpaleConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsLeviathanWrathConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsPoseidonsFavorConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsReturningHasteConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsRiptideMasteryConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsSnareConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsStormCallerConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsTidalSurgeConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.TridentsUndertowConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsArcaneWardConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsArcingBoltConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsChillingBoltConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsDuelistsFocusConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsEchoBoltConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsHexBrandConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsSpellMirrorConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsSpellweaveConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsStarfallVolleyConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.WandsUnravelConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.AdrenalineSurgeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.BattleHardenedSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.EvasionSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.FortifySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.GritSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.IronStanceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.LastStandSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.ReactiveShieldingSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.RetaliationSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.SecondWindSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.DevastatingBlowSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.ExecutionerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.MomentumSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.StaggerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.TimberSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.WoundSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.BarrageSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.DeadEyeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.FrostbiteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.HuntersMarkSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.MultishotSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.OverdrawSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.PackHunterSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.RangersFocusSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.RicochetSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.WindRunnerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.ArrowRainSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.ExplosiveTipSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.FirstBloodSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.HeavyBoltsSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.HuntersPreySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.QuickReloadSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.SteadyAimSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.SuppressingFireSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathMarkSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathsEmbraceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.HarvesterSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.ReapWhatYouSowSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.ReapersHarvestSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.ScytheSweepSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.SoulDrainSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.SoulSiphonSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.SpectralScytheSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.AvatarOfJudgmentSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.ConcussionSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.ConsecrationSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.CrushingBlowSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.DivineShieldSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.HammerOfWrathSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.JudgmentSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.RighteousFurySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.ShatterSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.StunningForceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.FirstStrikeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.ImpalerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.ImpalingStrikeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LegionsDisciplineSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PhalanxSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PrecisionThrustSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.VanguardSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.VortexThrustSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.BattlemagesGuardSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.ConcussiveBlastSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.ImmolateSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.PhoenixMantleSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.RepelSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.ScorchedEarthSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.SmolderSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.StokeTheFlamesSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.SunfallSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.WildfireSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.DuelistSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.ExposeWeaknessSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.FinishingFlourishSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.FlurrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.LacerateSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.ParrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.PoiseSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.SwiftStrikesSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.VorpalStrikeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.DepthChargeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.ImpaleSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.LeviathanWrathSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.PoseidonsFavorSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.ReturningHasteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.RiptideMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.SnareSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.StormCallerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.TidalSurgeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.UndertowSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.ArcaneWardSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.ArcingBoltSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.ChillingBoltSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.DuelistsFocusSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.EchoBoltSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.HexBrandSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.SpellMirrorSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.SpellweaveSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.StarfallVolleySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.UnravelSkill;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkillBonusInitializer {
   private SkillBonusInitializer() {
   }

   public static void initialize() {
      Logger.info("Initializing skill bonuses...");
      registerSwordsSkills();
      registerAxesSkills();
      registerBowsSkills();
      registerCrossbowsSkills();
      registerTridentsSkills();
      registerHoesSkills();
      registerArmorSkills();
      registerMacesSkills();
      registerSpearsSkills();
      registerStavesSkills();
      registerWandsSkills();
      Logger.info("Registered " + SkillBonusRegistry.getAllBonuses().size() + " skill bonuses.");
   }

   private static void registerSwordsSkills() {
      registerSkill(new LacerateSkill(), new SwordsLacerateConfig());
      registerSkill(new PoiseSkill(), new SwordsPoiseConfig());
      registerSkill(new SwiftStrikesSkill(), new SwordsSwiftStrikesConfig());
      registerSkill(new FlurrySkill(), new SwordsFlurryConfig());
      registerSkill(new RiposteSkill(), new SwordsRiposteConfig());
      registerSkill(new ExposeWeaknessSkill(), new SwordsExposeWeaknessConfig());
      registerSkill(new DuelistSkill(), new SwordsDuelistConfig());
      registerSkill(new FinishingFlourishSkill(), new SwordsFinishingFlourishConfig());
      registerSkill(new ParrySkill(), new SwordsParryConfig());
      registerSkill(new VorpalStrikeSkill(), new SwordsVorpalStrikeConfig());
   }

   private static void registerAxesSkills() {
      registerSkill(new DevastatingBlowSkill(), new AxesDevastatingBlowConfig());
      registerSkill(new WoundSkill(), new AxesWoundConfig());
      registerSkill(new ExecutionerSkill(), new AxesExecutionerConfig());
      registerSkill(new StaggerSkill(), new AxesStaggerConfig());
      registerSkill(new MomentumSkill(), new AxesMomentumConfig());
      registerSkill(new TimberSkill(), new AxesTimberConfig());
   }

   private static void registerBowsSkills() {
      registerSkill(new FrostbiteSkill(), new BowsFrostbiteConfig());
      registerSkill(new PackHunterSkill(), new BowsPackHunterConfig());
      registerSkill(new MultishotSkill(), new BowsMultishotConfig());
      registerSkill(new OverdrawSkill(), new BowsOverdrawConfig());
      registerSkill(new HuntersMarkSkill(), new BowsHuntersMarkConfig());
      registerSkill(new RicochetSkill(), new BowsRicochetConfig());
      registerSkill(new WindRunnerSkill(), new BowsWindRunnerConfig());
      registerSkill(new BarrageSkill(), new BowsBarrageConfig());
      registerSkill(new RangersFocusSkill(), new BowsRangersFocusConfig());
      registerSkill(new DeadEyeSkill(), new BowsDeadEyeConfig());
   }

   private static void registerCrossbowsSkills() {
      registerSkill(new SteadyAimSkill(), new CrossbowsSteadyAimConfig());
      registerSkill(new QuickReloadSkill(), new CrossbowsQuickReloadConfig());
      registerSkill(new ExplosiveTipSkill(), new CrossbowsExplosiveTipConfig());
      registerSkill(new HuntersPreySkill(), new CrossbowsHuntersPreyConfig());
      registerSkill(new HeavyBoltsSkill(), new CrossbowsHeavyBoltsConfig());
      registerSkill(new SuppressingFireSkill(), new CrossbowsSuppressingFireConfig());
      registerSkill(new ArrowRainSkill(), new CrossbowsArrowRainConfig());
      registerSkill(new FirstBloodSkill(), new CrossbowsFirstBloodConfig());
   }

   private static void registerTridentsSkills() {
      registerSkill(new ImpaleSkill(), new TridentsImpaleConfig());
      registerSkill(new SnareSkill(), new TridentsSnareConfig());
      registerSkill(new TidalSurgeSkill(), new TridentsTidalSurgeConfig());
      registerSkill(new StormCallerSkill(), new TridentsStormCallerConfig());
      registerSkill(new PoseidonsFavorSkill(), new TridentsPoseidonsFavorConfig());
      registerSkill(new RiptideMasterySkill(), new TridentsRiptideMasteryConfig());
      registerSkill(new ReturningHasteSkill(), new TridentsReturningHasteConfig());
      registerSkill(new UndertowSkill(), new TridentsUndertowConfig());
      registerSkill(new DepthChargeSkill(), new TridentsDepthChargeConfig());
      registerSkill(new LeviathanWrathSkill(), new TridentsLeviathanWrathConfig());
   }

   private static void registerHoesSkills() {
      registerSkill(new SoulDrainSkill(), new HoesSoulDrainConfig());
      registerSkill(new ReapersHarvestSkill(), new HoesReapersHarvestConfig());
      registerSkill(new GrimReachSkill(), new HoesGrimReachConfig());
      registerSkill(new SoulSiphonSkill(), new HoesSoulSiphonConfig());
      registerSkill(new DeathMarkSkill(), new HoesDeathMarkConfig());
      registerSkill(new HarvesterSkill(), new HoesHarvesterConfig());
      registerSkill(new ScytheSweepSkill(), new HoesScytheSweepConfig());
      registerSkill(new ReapWhatYouSowSkill(), new HoesReapWhatYouSowConfig());
      registerSkill(new SpectralScytheSkill(), new HoesSpectralScytheConfig());
      registerSkill(new DeathsEmbraceSkill(), new HoesDeathsEmbraceConfig());
   }

   private static void registerArmorSkills() {
      registerSkill(new EvasionSkill(), new ArmorEvasionConfig());
      registerSkill(new IronStanceSkill(), new ArmorIronStanceConfig());
      registerSkill(new BattleHardenedSkill(), new ArmorBattleHardenedConfig());
      registerSkill(new RetaliationSkill(), new ArmorRetaliationConfig());
      registerSkill(new AdrenalineSurgeSkill(), new ArmorAdrenalineSurgeConfig());
      registerSkill(new FortifySkill(), new ArmorFortifyConfig());
      registerSkill(new SecondWindSkill(), new ArmorSecondWindConfig());
      registerSkill(new GritSkill(), new ArmorGritConfig());
      registerSkill(new LastStandSkill(), new ArmorLastStandConfig());
      registerSkill(new ReactiveShieldingSkill(), new ArmorReactiveShieldingConfig());
   }

   private static void registerMacesSkills() {
      registerSkill(new ConcussionSkill(), new MacesConcussionConfig());
      registerSkill(new CrushingBlowSkill(), new MacesCrushingBlowConfig());
      registerSkill(new RighteousFurySkill(), new MacesRighteousFuryConfig());
      registerSkill(new JudgmentSkill(), new MacesJudgmentConfig());
      registerSkill(new ShatterSkill(), new MacesShatterConfig());
      registerSkill(new DivineShieldSkill(), new MacesDivineShieldConfig());
      registerSkill(new ConsecrationSkill(), new MacesConsecrationConfig());
      registerSkill(new StunningForceSkill(), new MacesStunningForceConfig());
      registerSkill(new HammerOfWrathSkill(), new MacesHammerOfWrathConfig());
      registerSkill(new AvatarOfJudgmentSkill(), new MacesAvatarOfJudgmentConfig());
   }

   private static void registerSpearsSkills() {
      registerSkill(new FirstStrikeSkill(), new SpearsFirstStrikeConfig());
      registerSkill(new LongReachSkill(), new SpearsLongReachConfig());
      registerSkill(new PrecisionThrustSkill(), new SpearsPrecisionThrustConfig());
      registerSkill(new PhalanxSkill(), new SpearsPhalanxConfig());
      registerSkill(new ImpalingStrikeSkill(), new SpearsImpalingStrikeConfig());
      registerSkill(new VortexThrustSkill(), new SpearsVortexThrustConfig());
      registerSkill(new LegionsDisciplineSkill(), new SpearsLegionsDisciplineConfig());
      registerSkill(new VanguardSkill(), new SpearsVanguardConfig());
      registerSkill(new PolearmMasterySkill(), new SpearsPolearmMasteryConfig());
      registerSkill(new ImpalerSkill(), new SpearsImpalerConfig());
   }

   private static void registerStavesSkills() {
      registerSkill(new SmolderSkill(), new StavesSmolderConfig());
      registerSkill(new ConcussiveBlastSkill(), new StavesConcussiveBlastConfig());
      registerSkill(new BattlemagesGuardSkill(), new StavesBattlemagesGuardConfig());
      registerSkill(new StokeTheFlamesSkill(), new StavesStokeTheFlamesConfig());
      registerSkill(new WildfireSkill(), new StavesWildfireConfig());
      registerSkill(new RepelSkill(), new StavesRepelConfig());
      registerSkill(new ScorchedEarthSkill(), new StavesScorchedEarthConfig());
      registerSkill(new ImmolateSkill(), new StavesImmolateConfig());
      registerSkill(new SunfallSkill(), new StavesSunfallConfig());
      registerSkill(new PhoenixMantleSkill(), new StavesPhoenixMantleConfig());
   }

   private static void registerWandsSkills() {
      registerSkill(new HexBrandSkill(), new WandsHexBrandConfig());
      registerSkill(new ChillingBoltSkill(), new WandsChillingBoltConfig());
      registerSkill(new ArcaneWardSkill(), new WandsArcaneWardConfig());
      registerSkill(new SpellweaveSkill(), new WandsSpellweaveConfig());
      registerSkill(new ArcingBoltSkill(), new WandsArcingBoltConfig());
      registerSkill(new DuelistsFocusSkill(), new WandsDuelistsFocusConfig());
      registerSkill(new UnravelSkill(), new WandsUnravelConfig());
      registerSkill(new EchoBoltSkill(), new WandsEchoBoltConfig());
      registerSkill(new StarfallVolleySkill(), new WandsStarfallVolleyConfig());
      registerSkill(new SpellMirrorSkill(), new WandsSpellMirrorConfig());
   }

   private static void registerSkill(SkillBonus skill, SkillBonusConfigFields config) {
      SkillBonusConfigFields loadedConfig = SkillBonusesConfig.getSkillBonus(skill.getSkillId());
      if (loadedConfig != null) {
         skill.setConfigFields(loadedConfig);
      } else if (config != null) {
         skill.setConfigFields(config);
      }

      SkillBonusRegistry.registerBonus(skill);
   }

   public static void shutdown() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         SkillBonusRegistry.removeAllBonuses(player);
      }

      SkillBonusRegistry.shutdown();
      SkillBonusesConfig.shutdown();
      PlayerSkillSelection.shutdown();
      SkillBonusEventHandler.shutdown();
   }
}
