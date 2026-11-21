package su.nightexpress.nightcore.util.bridge;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.function.Function;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Fluid;
import org.bukkit.GameEvent;
import org.bukkit.JukeboxSong;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockType;
import org.bukkit.block.banner.PatternType;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog.Variant;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager.Type;
import org.bukkit.entity.Wolf.SoundVariant;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.Version;

public interface RegistryType<T extends Keyed> {
   RegistryType<Material> MATERIAL = type(isSpigot -> Registry.MATERIAL);
   RegistryType<Enchantment> ENCHANTMENT = type(isSpigot -> isSpigot ? forSpigot((Class<T>)Enchantment.class) : forPaper(RegistryKey.ENCHANTMENT));
   RegistryType<GameEvent> GAME_EVENT = type(isSpigot -> isSpigot ? Registry.GAME_EVENT : forPaper(RegistryKey.GAME_EVENT));
   RegistryType<StructureType> STRUCTURE_TYPE = type(isSpigot -> isSpigot ? Registry.STRUCTURE_TYPE : forPaper(RegistryKey.STRUCTURE_TYPE));
   RegistryType<PotionEffectType> MOB_EFFECT = type(isSpigot -> isSpigot ? Registry.EFFECT : forPaper(RegistryKey.MOB_EFFECT));
   RegistryType<Profession> VILLAGER_PROFESSION = type(isSpigot -> isSpigot ? Registry.VILLAGER_PROFESSION : forPaper(RegistryKey.VILLAGER_PROFESSION));
   RegistryType<Type> VILLAGER_TYPE = type(isSpigot -> isSpigot ? Registry.VILLAGER_TYPE : forPaper(RegistryKey.VILLAGER_TYPE));
   RegistryType<Attribute> ATTRIBUTE = type(isSpigot -> isSpigot ? Registry.ATTRIBUTE : forPaper(RegistryKey.ATTRIBUTE));
   RegistryType<Fluid> FLUID = type(isSpigot -> isSpigot ? Registry.FLUID : forPaper(RegistryKey.FLUID));
   RegistryType<Sound> SOUND = type(isSpigot -> isSpigot ? Registry.SOUNDS : forPaper(RegistryKey.SOUND_EVENT));
   RegistryType<Biome> BIOME = type(isSpigot -> isSpigot ? forSpigot((Class<T>)Biome.class) : forPaper(RegistryKey.BIOME));
   RegistryType<Structure> STRUCTURE = type(isSpigot -> isSpigot ? forSpigot((Class<T>)Structure.class) : forPaper(RegistryKey.STRUCTURE));
   RegistryType<TrimMaterial> TRIM_MATERIAL = type(isSpigot -> isSpigot ? forSpigot((Class<T>)TrimMaterial.class) : forPaper(RegistryKey.TRIM_MATERIAL));
   RegistryType<TrimPattern> TRIM_PATTERN = type(isSpigot -> isSpigot ? forSpigot((Class<T>)TrimPattern.class) : forPaper(RegistryKey.TRIM_PATTERN));
   RegistryType<DamageType> DAMAGE_TYPE = type(isSpigot -> isSpigot ? forSpigot((Class<T>)DamageType.class) : forPaper(RegistryKey.DAMAGE_TYPE));
   RegistryType<PatternType> BANNER_PATTERN = type(isSpigot -> isSpigot ? forSpigot((Class<T>)PatternType.class) : forPaper(RegistryKey.BANNER_PATTERN));
   RegistryType<Art> PAINTING_VARIANT = type(isSpigot -> isSpigot ? forSpigot((Class<T>)Art.class) : forPaper(RegistryKey.PAINTING_VARIANT));
   RegistryType<MusicInstrument> INSTRUMENT = type(isSpigot -> isSpigot ? forSpigot((Class<T>)MusicInstrument.class) : forPaper(RegistryKey.INSTRUMENT));
   RegistryType<org.bukkit.entity.Cat.Type> CAT_VARIANT = type(
      isSpigot -> isSpigot ? forSpigot((Class<T>)org.bukkit.entity.Cat.Type.class) : forPaper(RegistryKey.CAT_VARIANT)
   );
   RegistryType<Variant> FROG_VARIANT = type(isSpigot -> isSpigot ? forSpigot((Class<T>)Variant.class) : forPaper(RegistryKey.FROG_VARIANT));
   RegistryType<EntityType> ENTITY_TYPE = type(isSpigot -> isSpigot ? Registry.ENTITY_TYPE : forPaper(RegistryKey.ENTITY_TYPE));
   RegistryType<Particle> PARTICLE_TYPE = type(isSpigot -> isSpigot ? Registry.PARTICLE_TYPE : forPaper(RegistryKey.PARTICLE_TYPE));
   RegistryType<PotionType> POTION = type(isSpigot -> isSpigot ? Registry.POTION : forPaper(RegistryKey.POTION));

   @NonNull
   Registry<T> getRegistry();

   private static <T extends Keyed> RegistryType<T> type(@NonNull Function<Boolean, Registry<T>> function) {
      Registry<T> registry = function.apply(Version.isSpigot());
      return () -> registry;
   }

   private static <T extends Keyed> Registry<T> forSpigot(@NonNull Class<T> clazz) {
      return Bukkit.getRegistry(clazz);
   }

   private static <T extends Keyed> Registry<T> forPaper(RegistryKey<T> key) {
      return RegistryAccess.registryAccess().getRegistry(key);
   }

   public static class MC_1_21 {
      public static final RegistryType<BlockType> BLOCK = RegistryType.type(isSpigot -> isSpigot ? Registry.BLOCK : RegistryType.forPaper(RegistryKey.BLOCK));
      public static final RegistryType<ItemType> ITEM = RegistryType.type(isSpigot -> isSpigot ? Registry.ITEM : RegistryType.forPaper(RegistryKey.ITEM));
      public static final RegistryType<JukeboxSong> JUKEBOX_SONG = RegistryType.type(
         isSpigot -> isSpigot ? RegistryType.forSpigot((Class<T>)JukeboxSong.class) : RegistryType.forPaper(RegistryKey.JUKEBOX_SONG)
      );
      public static final RegistryType<org.bukkit.map.MapCursor.Type> MAP_DECORATION_TYPE = RegistryType.type(
         isSpigot -> isSpigot ? Registry.MAP_DECORATION_TYPE : RegistryType.forPaper(RegistryKey.MAP_DECORATION_TYPE)
      );
      public static final RegistryType<org.bukkit.entity.Wolf.Variant> WOLF_VARIANT = RegistryType.type(
         isSpigot -> isSpigot ? RegistryType.forSpigot((Class<T>)org.bukkit.entity.Wolf.Variant.class) : RegistryType.forPaper(RegistryKey.WOLF_VARIANT)
      );
      public static final RegistryType<MenuType> MENU = RegistryType.type(isSpigot -> isSpigot ? Registry.MENU : RegistryType.forPaper(RegistryKey.MENU));
   }

   public static class MC_1_21_5 {
      public static final RegistryType<org.bukkit.entity.Chicken.Variant> CHICKEN_VARIANT = RegistryType.type(
         isSpigot -> isSpigot ? RegistryType.forSpigot((Class<T>)org.bukkit.entity.Chicken.Variant.class) : RegistryType.forPaper(RegistryKey.CHICKEN_VARIANT)
      );
      public static final RegistryType<org.bukkit.entity.Cow.Variant> COW_VARIANT = RegistryType.type(
         isSpigot -> isSpigot ? RegistryType.forSpigot((Class<T>)org.bukkit.entity.Cow.Variant.class) : RegistryType.forPaper(RegistryKey.COW_VARIANT)
      );
      public static final RegistryType<org.bukkit.entity.Pig.Variant> PIG_VARIANT = RegistryType.type(
         isSpigot -> isSpigot ? RegistryType.forSpigot((Class<T>)org.bukkit.entity.Pig.Variant.class) : RegistryType.forPaper(RegistryKey.PIG_VARIANT)
      );
   }

   public static class Paper {
      public static final RegistryType<DataComponentType> DATA_COMPONENT_TYPE = RegistryType.type(
         isSpigot -> RegistryType.forPaper(RegistryKey.DATA_COMPONENT_TYPE)
      );
      public static final RegistryType<MemoryKey<?>> MEMORY_MODULE_TYPE = RegistryType.type(isSpigot -> RegistryType.forPaper(RegistryKey.MEMORY_MODULE_TYPE));
      public static final RegistryType<SoundVariant> WOLF_SOUND_VARIANT = RegistryType.type(isSpigot -> RegistryType.forPaper(RegistryKey.WOLF_SOUND_VARIANT));
   }
}
