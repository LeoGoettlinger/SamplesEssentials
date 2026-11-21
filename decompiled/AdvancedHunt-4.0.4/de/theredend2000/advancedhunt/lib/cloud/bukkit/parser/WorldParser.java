package de.theredend2000.advancedhunt.lib.cloud.bukkit.parser;

import de.theredend2000.advancedhunt.lib.cloud.bukkit.BukkitCaptionKeys;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.exception.parsing.ParserException;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.BlockingSuggestionProvider;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class WorldParser<C> implements ArgumentParser<C, World>, BlockingSuggestionProvider.Strings<C> {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, World> worldParser() {
      return ParserDescriptor.of(new WorldParser<>(), World.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, World> worldComponent() {
      return CommandComponent.<C, World>builder().parser(worldParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<World> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();
      World world = Bukkit.getWorld(input);
      return world == null ? ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext)) : ArgumentParseResult.success(world);
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return Bukkit.getWorlds().stream().<String>map(World::getName).collect(Collectors.toList());
   }

   public static final class WorldParseException extends ParserException {
      private final String input;

      public WorldParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(WorldParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }
   }
}
