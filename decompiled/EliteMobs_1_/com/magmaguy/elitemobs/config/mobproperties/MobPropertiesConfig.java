package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteBeeConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteBlazeConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteBoggedConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteBreezeConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteCaveSpiderConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteCreeperConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteDrownedConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteElderGuardianConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteEnderDragon;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteEndermanConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteEndermiteConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteEvokerConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteGhastConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteGoatConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteGuardianConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteHoglinConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteHuskConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteIllusionerConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteIronGolemConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteKillerBunnyConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteLlamaConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteMagmaCubeConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.ElitePhantomConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.ElitePiglinBruteConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.ElitePiglinConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.ElitePillagerConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.ElitePolarBearConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteRavagerConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteShulkerConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteSilverfishConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteSkeletonConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteSlimeConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteSpiderConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteStrayConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteVexConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteVindicatorConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteWardenConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteWitchConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteWitherConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteWitherSkeletonConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteWolfConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteZoglinConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteZombieConfig;
import com.magmaguy.elitemobs.config.mobproperties.premade.EliteZombiefiedPiglin;
import java.io.File;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

public class MobPropertiesConfig {
   private static Map<EntityType, MobPropertiesConfigFields> mobProperties = Map.of();
   private static final List<MobPropertiesConfigFields> mobPropertiesConfigFieldsList = List.of(
      new EliteBlazeConfig(),
      new EliteCaveSpiderConfig(),
      new EliteCreeperConfig(),
      new EliteDrownedConfig(),
      new EliteElderGuardianConfig(),
      new EliteGuardianConfig(),
      new EliteEndermanConfig(),
      new EliteEndermiteConfig(),
      new EliteEvokerConfig(),
      new EliteHuskConfig(),
      new EliteIllusionerConfig(),
      new EliteIronGolemConfig(),
      new ElitePhantomConfig(),
      new ElitePillagerConfig(),
      new ElitePolarBearConfig(),
      new EliteRavagerConfig(),
      new EliteSilverfishConfig(),
      new EliteSkeletonConfig(),
      new EliteSpiderConfig(),
      new EliteStrayConfig(),
      new EliteVexConfig(),
      new EliteVindicatorConfig(),
      new EliteWitchConfig(),
      new EliteWitherSkeletonConfig(),
      new EliteZombieConfig(),
      new EliteGhastConfig(),
      new EliteWolfConfig(),
      new EliteEnderDragon(),
      new EliteShulkerConfig(),
      new EliteKillerBunnyConfig(),
      new EliteLlamaConfig(),
      new EliteSlimeConfig(),
      new EliteMagmaCubeConfig(),
      new EliteBoggedConfig(),
      new EliteWardenConfig(),
      new EliteGoatConfig(),
      new EliteZombiefiedPiglin(),
      new EliteZoglinConfig(),
      new ElitePiglinConfig(),
      new EliteHoglinConfig(),
      new ElitePiglinBruteConfig(),
      new EliteBeeConfig(),
      new EliteBreezeConfig(),
      new EliteWitherConfig()
   );

   public static Map<EntityType, MobPropertiesConfigFields> getMobProperties() {
      return mobProperties;
   }

   public static String defaultNameTemplate(EntityType type) {
      return MobTypeDefaults.nameTemplate(type);
   }

   public static boolean isEligible(EntityType type) {
      return type != null && type.isSpawnable() && type.getEntityClass() != null && Mob.class.isAssignableFrom(type.getEntityClass());
   }

   public static void initializeConfigs() {
      Map<EntityType, MobPropertiesConfigFields> defaults = new EnumMap<>(EntityType.class);

      for (MobPropertiesConfigFields fields : mobPropertiesConfigFieldsList) {
         defaults.put(fields.getEntityType(), fields);
      }

      Map<EntityType, MobPropertiesConfigFields> loaded = new EnumMap<>(EntityType.class);

      for (EntityType type : EntityType.values()) {
         if (isEligible(type)) {
            MobPropertiesConfigFields fields = defaults.get(type);
            if (fields == null) {
               fields = new MobPropertiesConfigFields(
                  "elite_" + type.name().toLowerCase(Locale.ROOT),
                  type,
                  false,
                  MobTypeDefaults.nameTemplate(type),
                  List.of("$player &cwas slain by $entity&c!"),
                  2.0,
                  0.0
               );
            }

            loaded.put(type, initializeConfiguration(fields));
         }
      }

      mobProperties = Collections.unmodifiableMap(loaded);
   }

   private static MobPropertiesConfigFields initializeConfiguration(MobPropertiesConfigFields mobPropertiesConfigFields) {
      File file = ConfigurationEngine.fileCreator("mobproperties", mobPropertiesConfigFields.getFileName());
      FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
      mobPropertiesConfigFields.generateConfigDefaults(fileConfiguration);
      ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
      return new MobPropertiesConfigFields(fileConfiguration, file, mobPropertiesConfigFields);
   }
}
