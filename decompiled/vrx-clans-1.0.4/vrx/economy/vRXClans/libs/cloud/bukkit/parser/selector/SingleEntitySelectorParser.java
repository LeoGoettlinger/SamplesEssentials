package vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector;

import org.apiguardian.api.API;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.data.SingleEntitySelector;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;

public final class SingleEntitySelectorParser<C> extends SelectorUtils.EntitySelectorParser<C, SingleEntitySelector> {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, SingleEntitySelector> singleEntitySelectorParser() {
      return ParserDescriptor.of(new SingleEntitySelectorParser<>(), SingleEntitySelector.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, SingleEntitySelector> singleEntitySelectorComponent() {
      return CommandComponent.<C, SingleEntitySelector>builder().parser(singleEntitySelectorParser());
   }

   public SingleEntitySelectorParser() {
      super(true);
   }

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   public SingleEntitySelector mapResult(@NonNull final String input, @NonNull final SelectorUtils.EntitySelectorWrapper wrapper) {
      final Entity entity = wrapper.singleEntity();
      return new SingleEntitySelector() {
         @NonNull
         public Entity single() {
            return entity;
         }

         @NonNull
         @Override
         public String inputString() {
            return input;
         }
      };
   }
}
