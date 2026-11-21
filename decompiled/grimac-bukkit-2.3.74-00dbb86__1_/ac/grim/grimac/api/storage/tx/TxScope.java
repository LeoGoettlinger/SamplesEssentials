package ac.grim.grimac.api.storage.tx;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;

@ApiStatus.Experimental
public sealed interface TxScope permits TxScope.Player, TxScope.Server, TxScope.Custom {
   @NotNull
   String tag();

   public record Custom(@NotNull String value) implements TxScope {
      @NotNull
      @Override
      public String tag() {
         return this.value;
      }
   }

   public record Player(@NotNull UUID uuid) implements TxScope {
      @NotNull
      @Override
      public String tag() {
         return "player:" + this.uuid;
      }
   }

   public record Server(@NotNull String name) implements TxScope {
      @NotNull
      @Override
      public String tag() {
         return "server:" + this.name;
      }
   }
}
