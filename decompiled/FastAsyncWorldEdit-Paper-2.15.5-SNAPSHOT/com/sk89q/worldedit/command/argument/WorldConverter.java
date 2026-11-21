package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

public class WorldConverter implements ArgumentConverter<World> {
   public static final WorldConverter WORLD_CONVERTER = new WorldConverter();
   private final TextComponent choices = TextComponent.of("any world");

   public static void register(CommandManager commandManager) {
      commandManager.registerConverter(Key.of(World.class), WORLD_CONVERTER);
   }

   private WorldConverter() {
   }

   @Override
   public Component describeAcceptableArguments() {
      return this.choices;
   }

   private Stream<? extends World> getWorlds() {
      return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds().stream();
   }

   @Override
   public List<String> getSuggestions(String input, InjectedValueAccess context) {
      return this.getWorlds().map(World::id).filter(world -> world.startsWith(input)).collect(Collectors.toList());
   }

   @Override
   public ConversionResult<World> convert(String s, InjectedValueAccess injectedValueAccess) {
      World result = this.getWorlds().filter(world -> world.id().equals(s)).findAny().orElse(null);
      return (ConversionResult<World>)(result == null
         ? FailedConversion.from(new IllegalArgumentException("Not a valid world: " + s))
         : SuccessfulConversion.fromSingle(result));
   }
}
