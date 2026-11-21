package vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector;

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.data.SinglePlayerSelector;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.PlayerParser;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;

public final class SinglePlayerSelectorParser<C> extends SelectorUtils.PlayerSelectorParser<C, SinglePlayerSelector> {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, SinglePlayerSelector> singlePlayerSelectorParser() {
      return ParserDescriptor.of(new SinglePlayerSelectorParser<>(), SinglePlayerSelector.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, SinglePlayerSelector> singlePlayerSelectorComponent() {
      return CommandComponent.<C, SinglePlayerSelector>builder().parser(singlePlayerSelectorParser());
   }

   public SinglePlayerSelectorParser() {
      super(true);
   }

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   public SinglePlayerSelector mapResult(@NonNull final String input, @NonNull final SelectorUtils.EntitySelectorWrapper wrapper) {
      final Player player = wrapper.singlePlayer();
      return new SinglePlayerSelector() {
         @NonNull
         public Player single() {
            return player;
         }

         @NonNull
         @Override
         public String inputString() {
            return input;
         }
      };
   }

   @Override
   protected CompletableFuture<ArgumentParseResult<SinglePlayerSelector>> legacyParse(final CommandContext<C> commandContext, final CommandInput commandInput) {
      String input = commandInput.peekString();
      final Player player = Bukkit.getPlayer(input);
      if (player == null) {
         return CompletableFuture.completedFuture(ArgumentParseResult.failure(new PlayerParser.PlayerParseException(input, commandContext)));
      } else {
         final String pop = commandInput.readString();
         return ArgumentParseResult.successFuture(new SinglePlayerSelector() {
            @NonNull
            public Player single() {
               return player;
            }

            @NonNull
            @Override
            public String inputString() {
               return pop;
            }
         });
      }
   }
}
