package de.theredend2000.advancedhunt.lib.cloud.bukkit.parser;

import de.theredend2000.advancedhunt.lib.cloud.bukkit.BukkitCaptionKeys;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.BukkitCommandContextKeys;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.exception.parsing.ParserException;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.BlockingSuggestionProvider;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.Suggestion;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PlayerParser<C> implements ArgumentParser<C, Player>, BlockingSuggestionProvider<C> {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Player> playerParser() {
      return ParserDescriptor.of(new PlayerParser<>(), Player.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Player> playerComponent() {
      return CommandComponent.<C, Player>builder().parser(playerParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<Player> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();
      Player player = Bukkit.getPlayer(input);
      return player == null ? ArgumentParseResult.failure(new PlayerParser.PlayerParseException(input, commandContext)) : ArgumentParseResult.success(player);
   }

   @NonNull
   @Override
   public Iterable<Suggestion> suggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
      return Bukkit.getOnlinePlayers()
         .stream()
         .filter(player -> !(bukkit instanceof Player) || ((Player)bukkit).canSee(player))
         .<String>map(OfflinePlayer::getName)
         .map(Suggestion::suggestion)
         .collect(Collectors.toList());
   }

   public static final class PlayerParseException extends ParserException {
      private final String input;

      public PlayerParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(PlayerParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }
   }
}
