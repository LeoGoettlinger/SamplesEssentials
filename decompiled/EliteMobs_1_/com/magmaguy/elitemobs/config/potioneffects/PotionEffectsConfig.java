package com.magmaguy.elitemobs.config.potioneffects;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.potioneffects.premade.AbsorptionConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.BlindnessConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.ConduitPowerConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.DarknessConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.DolphinsGraceConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.FireResistanceConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.GlowingConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.HasteConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.HealthBoostConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.HungerConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.InfestedConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.InstantDamageConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.InstantHealthConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.InvisibilityConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.JumpBoostConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.LevitationConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.LuckConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.MiningFatigueConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.NauseaConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.NightVisionConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.OozingConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.PoisonConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.RaidOmenConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.RegenerationConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.ResistanceConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.SaturationConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.SlowFallingConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.SlownessConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.SpeedConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.StrengthConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.TrialOmenConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.UnluckConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.WaterBreathingConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.WeaknessConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.WeavingConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.WindChargedConfig;
import com.magmaguy.elitemobs.config.potioneffects.premade.WitherConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.bukkit.configuration.file.FileConfiguration;

public class PotionEffectsConfig {
   private static long revision;
   private static final HashMap<String, PotionEffectsConfigFields> potionEffects = new HashMap<>();
   private static final List<PotionEffectsConfigFields> potionEffectsConfigFields = new ArrayList<>(
      new ArrayList<>(
         List.of(
            new AbsorptionConfig(),
            new BlindnessConfig(),
            new ConduitPowerConfig(),
            new NauseaConfig(),
            new ResistanceConfig(),
            new DolphinsGraceConfig(),
            new HasteConfig(),
            new FireResistanceConfig(),
            new GlowingConfig(),
            new InstantDamageConfig(),
            new InstantHealthConfig(),
            new HealthBoostConfig(),
            new HungerConfig(),
            new StrengthConfig(),
            new InvisibilityConfig(),
            new JumpBoostConfig(),
            new LevitationConfig(),
            new LuckConfig(),
            new NightVisionConfig(),
            new PoisonConfig(),
            new RegenerationConfig(),
            new SaturationConfig(),
            new SlownessConfig(),
            new MiningFatigueConfig(),
            new SlowFallingConfig(),
            new SpeedConfig(),
            new UnluckConfig(),
            new WaterBreathingConfig(),
            new WeaknessConfig(),
            new WitherConfig(),
            new DarknessConfig(),
            new InfestedConfig(),
            new OozingConfig(),
            new RaidOmenConfig(),
            new TrialOmenConfig(),
            new WeavingConfig(),
            new WindChargedConfig()
         )
      )
   );

   public static long revision() {
      return revision;
   }

   public static void addPotionEffect(String fileName, PotionEffectsConfigFields powersConfigFields) {
      potionEffects.put(fileName, powersConfigFields);
   }

   public static PotionEffectsConfigFields getPotionEffect(String fileName) {
      fileName = fileName.toLowerCase(Locale.ROOT);
      if (!fileName.endsWith(".yml")) {
         fileName = fileName + ".yml";
      }

      return potionEffects.get(fileName);
   }

   public static void initializeConfigs() {
      revision++;
      potionEffects.clear();

      for (PotionEffectsConfigFields powersConfigFields : potionEffectsConfigFields) {
         initializeConfiguration(powersConfigFields);
      }
   }

   private static FileConfiguration initializeConfiguration(PotionEffectsConfigFields potionEffectsConfigFields) {
      File file = ConfigurationEngine.fileCreator("potionEffects", potionEffectsConfigFields.getFileName());
      FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
      potionEffectsConfigFields.generateConfigDefaults(fileConfiguration);
      ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
      addPotionEffect(file.getName(), new PotionEffectsConfigFields(fileConfiguration, file));
      return fileConfiguration;
   }
}
