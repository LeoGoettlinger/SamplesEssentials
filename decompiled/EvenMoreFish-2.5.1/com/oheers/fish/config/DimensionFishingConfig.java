package com.oheers.fish.config;

import com.oheers.fish.api.config.serializer.SoundSerializer;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.requirement.RequirementContext;
import java.util.List;
import java.util.function.Predicate;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.dimensionfishing.config.DimensionFishingConfigProvider;
import org.evenmorefish.fish.libs.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DimensionFishingConfig implements DimensionFishingConfigProvider {
   private static final DimensionFishingConfig INSTANCE = new DimensionFishingConfig();
   private ParticleFactory lavaLureParticles = new ParticleFactory();
   private ParticleFactory voidLureParticles = new ParticleFactory();

   private DimensionFishingConfig() {
   }

   public static DimensionFishingConfig getInstance() {
      return INSTANCE;
   }

   public void reload() {
      YamlDocument config = MainConfig.getInstance().getConfig();
      this.lavaLureParticles = new ParticleFactory(config.getMapList("dimension-fishing.lava.lure-particles"));
      this.voidLureParticles = new ParticleFactory(config.getMapList("dimension-fishing.void.lure-particles"));
   }

   @Override
   public boolean isLavaEnabled() {
      return MainConfig.getInstance().getConfig().getBoolean("dimension-fishing.lava.enabled", false);
   }

   @Override
   public boolean isVoidEnabled() {
      return MainConfig.getInstance().getConfig().getBoolean("dimension-fishing.void.enabled", false);
   }

   @NonNull
   @Override
   public List<String> getLavaAllowedWorlds() {
      return MainConfig.getInstance().getConfig().getStringList("dimension-fishing.lava.allowed-worlds");
   }

   @NonNull
   @Override
   public List<String> getVoidAllowedWorlds() {
      return MainConfig.getInstance().getConfig().getStringList("dimension-fishing.void.allowed-worlds");
   }

   @NonNull
   @Override
   public Sound getLavaFishingSwallowSound() {
      String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.lava.swallow-sound");
      Sound sound = SoundSerializer.get().deserialize(soundString);
      return sound != null ? sound : (Sound)Sound.sound().type(org.bukkit.Sound.BLOCK_LAVA_EXTINGUISH).build();
   }

   @NonNull
   @Override
   public Sound getVoidFishingSwallowSound() {
      String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.void.swallow-sound");
      Sound sound = SoundSerializer.get().deserialize(soundString);
      return sound != null ? sound : (Sound)Sound.sound().type(org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT).build();
   }

   @NonNull
   @Override
   public Sound getLavaFishingBiteSound() {
      String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.lava.bite-sound");
      Sound sound = SoundSerializer.get().deserialize(soundString);
      return sound != null ? sound : (Sound)Sound.sound().type(org.bukkit.Sound.ENTITY_FISHING_BOBBER_SPLASH).volume(0.25F).pitch(0.5F).build();
   }

   @NonNull
   @Override
   public Sound getVoidFishingBiteSound() {
      String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.void.bite-sound");
      Sound sound = SoundSerializer.get().deserialize(soundString);
      return sound != null ? sound : (Sound)Sound.sound().type(org.bukkit.Sound.ENTITY_FOX_BITE).volume(0.25F).pitch(0.1F).build();
   }

   @Nullable
   @Override
   public String getLavaFishingPermission() {
      String permission = MainConfig.getInstance().getConfig().getString("dimension-fishing.lava.permission", null);
      return "[]".equals(permission) ? null : permission;
   }

   @Nullable
   @Override
   public String getVoidFishingPermission() {
      String permission = MainConfig.getInstance().getConfig().getString("dimension-fishing.void.permission", null);
      return "[]".equals(permission) ? null : permission;
   }

   @NonNull
   @Override
   public ParticleFactory getLavaFishingLureParticles() {
      return this.lavaLureParticles;
   }

   @NonNull
   @Override
   public ParticleFactory getVoidFishingLureParticles() {
      return this.voidLureParticles;
   }

   @NonNull
   @Override
   public Predicate<Player> getLavaPredicate() {
      Section section = MainConfig.getInstance().getConfig().getSection("dimension-fishing.lava.requirements");
      return player -> {
         RequirementContext context = RequirementContext.player(player);
         return new Requirement(section).check(context);
      };
   }

   @NonNull
   @Override
   public Predicate<Player> getVoidPredicate() {
      Section section = MainConfig.getInstance().getConfig().getSection("dimension-fishing.void.requirements");
      return player -> {
         RequirementContext context = RequirementContext.player(player);
         return new Requirement(section).check(context);
      };
   }
}
