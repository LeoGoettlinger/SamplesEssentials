package vrx.economy.vRXClans.libs.cloud.bukkit.parser;

import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCommandContextKeys;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.BlockingSuggestionProvider;

public final class OfflinePlayerParser<C> implements ArgumentParser<C, OfflinePlayer>, BlockingSuggestionProvider.Strings<C> {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, OfflinePlayer> offlinePlayerParser() {
      return ParserDescriptor.of(new OfflinePlayerParser<>(), OfflinePlayer.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, OfflinePlayer> offlinePlayerComponent() {
      return CommandComponent.<C, OfflinePlayer>builder().parser(offlinePlayerParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<OfflinePlayer> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();
      if (input.length() > 16) {
         return ArgumentParseResult.failure(new OfflinePlayerParser.OfflinePlayerParseException(input, commandContext));
      } else {
         OfflinePlayer player;
         try {
            player = Bukkit.getOfflinePlayer(input);
         } catch (Exception var6) {
            return ArgumentParseResult.failure(new OfflinePlayerParser.OfflinePlayerParseException(input, commandContext));
         }

         return ArgumentParseResult.success(player);
      }
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
      return Bukkit.getOnlinePlayers()
         .stream()
         .filter(player -> !(bukkit instanceof Player) || ((Player)bukkit).canSee(player))
         .<String>map(OfflinePlayer::getName)
         .collect(Collectors.toList());
   }

   public static final class OfflinePlayerParseException extends ParserException {
      private final String input;

      public OfflinePlayerParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(OfflinePlayerParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }
   }
}
