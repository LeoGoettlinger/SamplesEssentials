package de.oliver.cloud.bukkit.parser.selector;

import de.oliver.cloud.bukkit.data.SingleEntitySelector;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.parser.ParserDescriptor;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class SingleEntitySelectorParser<C> extends SelectorUtils.EntitySelectorParser<C, SingleEntitySelector> {
   @API(
      status = Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, SingleEntitySelector> singleEntitySelectorParser() {
      return ParserDescriptor.of(new SingleEntitySelectorParser<>(), SingleEntitySelector.class);
   }

   @API(
      status = Status.STABLE,
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
      status = Status.INTERNAL,
      consumers = {"de.oliver.cloud.*"}
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
