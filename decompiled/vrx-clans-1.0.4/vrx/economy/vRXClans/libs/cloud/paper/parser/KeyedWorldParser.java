package vrx.economy.vRXClans.libs.cloud.paper.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CraftBukkitReflection;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.WorldParser;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestion;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProvider;

public final class KeyedWorldParser<C> implements ArgumentParser<C, World>, SuggestionProvider<C> {
   private final ArgumentParser<C, World> parser;

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, World> keyedWorldParser() {
      return ParserDescriptor.of(new KeyedWorldParser<>(), World.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, World> keyedWorldComponent() {
      return CommandComponent.<C, World>builder().parser(keyedWorldParser());
   }

   public KeyedWorldParser() {
      Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
      if (keyed != null && keyed.isAssignableFrom(World.class)) {
         this.parser = null;
      } else {
         this.parser = new WorldParser<>();
      }
   }

   @NonNull
   @Override
   public ArgumentParseResult<World> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      if (this.parser != null) {
         return this.parser.parse(commandContext, commandInput);
      } else {
         String input = commandInput.readString();
         NamespacedKey key = NamespacedKey.fromString(input);
         if (key == null) {
            return ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext));
         } else {
            World world = Bukkit.getWorld(key);
            return world == null ? ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext)) : ArgumentParseResult.success(world);
         }
      }
   }

   @NonNull
   @Override
   public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
      @NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input
   ) {
      if (this.parser != null) {
         return this.parser.suggestionProvider().suggestionsFuture(commandContext, input);
      } else {
         List<World> worlds = Bukkit.getWorlds();
         List<Suggestion> completions = new ArrayList<>(worlds.size() * 2);

         for (World world : worlds) {
            NamespacedKey key = world.getKey();
            if (input.hasRemainingInput() && key.getNamespace().equals("minecraft")) {
               completions.add(Suggestion.suggestion(key.getKey()));
            }

            completions.add(Suggestion.suggestion(key.getNamespace() + ':' + key.getKey()));
         }

         return CompletableFuture.completedFuture(completions);
      }
   }
}
