package vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.data.MultiplePlayerSelector;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.PlayerParser;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;

public final class MultiplePlayerSelectorParser<C> extends SelectorUtils.PlayerSelectorParser<C, MultiplePlayerSelector> {
   private final boolean allowEmpty;

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, MultiplePlayerSelector> multiplePlayerSelectorParser() {
      return multiplePlayerSelectorParser(true);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, MultiplePlayerSelector> multiplePlayerSelectorParser(final boolean allowEmpty) {
      return ParserDescriptor.of(new MultiplePlayerSelectorParser<>(allowEmpty), MultiplePlayerSelector.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, MultiplePlayerSelector> multiplePlayerSelectorComponent() {
      return CommandComponent.<C, MultiplePlayerSelector>builder().parser(multiplePlayerSelectorParser());
   }

   @API(
      status = API.Status.STABLE,
      since = "1.8.0"
   )
   public MultiplePlayerSelectorParser(final boolean allowEmpty) {
      super(false);
      this.allowEmpty = allowEmpty;
   }

   public MultiplePlayerSelectorParser() {
      this(true);
   }

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   public MultiplePlayerSelector mapResult(@NonNull final String input, @NonNull final SelectorUtils.EntitySelectorWrapper wrapper) {
      final List<Player> players = wrapper.players();
      if (players.isEmpty() && !this.allowEmpty) {
         new SelectorUtils.SelectorParser.Thrower(NO_PLAYERS_EXCEPTION_TYPE.get()).throwIt();
      }

      return new MultiplePlayerSelector() {
         @NonNull
         @Override
         public String inputString() {
            return input;
         }

         @NonNull
         @Override
         public Collection<Player> values() {
            return Collections.unmodifiableCollection(players);
         }
      };
   }

   @Override
   protected CompletableFuture<ArgumentParseResult<MultiplePlayerSelector>> legacyParse(final CommandContext<C> commandContext, final CommandInput commandInput) {
      String input = commandInput.peekString();
      final Player player = Bukkit.getPlayer(input);
      if (player == null) {
         return CompletableFuture.completedFuture(ArgumentParseResult.failure(new PlayerParser.PlayerParseException(input, commandContext)));
      } else {
         final String pop = commandInput.readString();
         return ArgumentParseResult.successFuture(new MultiplePlayerSelector() {
            @NonNull
            @Override
            public String inputString() {
               return pop;
            }

            @NonNull
            @Override
            public Collection<Player> values() {
               return Collections.singletonList(player);
            }
         });
      }
   }
}
