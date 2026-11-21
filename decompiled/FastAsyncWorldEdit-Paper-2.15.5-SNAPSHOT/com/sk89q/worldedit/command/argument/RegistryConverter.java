package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.fluid.FluidCategory;
import com.sk89q.worldedit.world.fluid.FluidType;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.weather.WeatherType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

public final class RegistryConverter<V extends Keyed> implements ArgumentConverter<V> {
   private final Registry<V> registry;
   private final TextComponent choices;

   public static void register(CommandManager commandManager) {
      ImmutableList.of(
            BlockType.class,
            BlockCategory.class,
            ItemType.class,
            ItemCategory.class,
            BiomeType.class,
            EntityType.class,
            FluidType.class,
            FluidCategory.class,
            GameMode.class,
            WeatherType.class,
            ConfiguredFeatureType.class,
            StructureType.class,
            new Class[]{TreeType.class}
         )
         .stream()
         .map(c -> (Class)c)
         .forEach(registryType -> commandManager.registerConverter(Key.of((Class<V>)registryType), from((Class<Keyed>)registryType)));
      commandManager.registerConverter(Key.of(new TypeToken<Registry<?>>() {}), new RegistryConverter<>(Registry.REGISTRY));
   }

   private static <V extends Keyed> RegistryConverter<V> from(Class<Keyed> registryType) {
      try {
         Field registryField = registryType.getDeclaredField("REGISTRY");
         Registry<V> registry = (Registry<V>)registryField.get(null);
         return new RegistryConverter<>(registry);
      } catch (NoSuchFieldException var3) {
         throw new IllegalArgumentException("Not a registry-backed type: " + registryType.getName());
      } catch (IllegalAccessException var4) {
         throw new IllegalStateException("Registry field inaccessible on " + registryType.getName());
      }
   }

   private RegistryConverter(Registry<V> registry) {
      this.registry = registry;
      this.choices = TextComponent.of("any " + registry.getName());
   }

   @Override
   public Component describeAcceptableArguments() {
      return this.choices;
   }

   @Override
   public ConversionResult<V> convert(String argument, InjectedValueAccess injectedValueAccess) {
      V result = this.registry.get(argument.toLowerCase(Locale.ROOT));
      return (ConversionResult<V>)(result == null
         ? FailedConversion.from(new IllegalArgumentException("Not a valid " + this.registry.getName() + ": " + argument))
         : SuccessfulConversion.fromSingle(result));
   }

   @Override
   public List<String> getSuggestions(String input, InjectedValueAccess context) {
      return SuggestionHelper.getRegistrySuggestions(this.registry, input).toList();
   }
}
