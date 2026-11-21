package de.oliver.cloud.bukkit.parser;

import de.oliver.cloud.bukkit.BukkitCaptionKeys;
import de.oliver.cloud.caption.CaptionVariable;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.exception.parsing.ParserException;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.BlockingSuggestionProvider;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class WorldParser<C> implements ArgumentParser<C, World>, BlockingSuggestionProvider.Strings<C> {
   @API(
      status = Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, World> worldParser() {
      return ParserDescriptor.of(new WorldParser<>(), World.class);
   }

   @API(
      status = Status.STABLE,
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
