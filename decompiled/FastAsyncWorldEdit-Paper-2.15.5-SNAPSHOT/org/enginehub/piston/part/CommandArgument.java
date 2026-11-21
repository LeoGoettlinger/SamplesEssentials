package org.enginehub.piston.part;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import java.util.Collection;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.Key;

@AutoValue
public abstract class CommandArgument implements ArgAcceptingCommandPart {
   public static CommandArgument.Builder builder(TranslatableComponent name, Component description) {
      return new AutoValue_CommandArgument.Builder()
         .named(name)
         .describedBy(description)
         .defaultsTo(ImmutableList.of())
         .ofTypes(ImmutableSet.of())
         .variable(false);
   }

   public abstract boolean isVariable();

   @Override
   public final boolean isRequired() {
      return this.getDefaults().isEmpty();
   }

   @Override
   public Component getTextRepresentation() {
      com.google.common.collect.ImmutableList.Builder<Component> builder = ImmutableList.builder();
      builder.add(TextComponent.of(this.isRequired() ? "<" : "["));
      builder.add(ColorConfig.mainText().wrap(this.getArgumentName()));
      if (this.isVariable()) {
         builder.add(ColorConfig.textModifier().wrap("..."));
      }

      builder.add(TextComponent.of(this.isRequired() ? ">" : "]"));
      return ColorConfig.partWrapping().wrap(builder.build());
   }

   @com.google.auto.value.AutoValue.Builder
   public abstract static class Builder {
      public final CommandArgument.Builder named(String name) {
         return this.named(TranslatableComponent.of(name));
      }

      public final CommandArgument.Builder named(TranslatableComponent name) {
         return this.argumentName(name);
      }

      abstract CommandArgument.Builder argumentName(TranslatableComponent var1);

      public final CommandArgument.Builder describedBy(String description) {
         return this.describedBy(TextComponent.of(description));
      }

      public final CommandArgument.Builder describedBy(Component description) {
         return this.description(description);
      }

      abstract CommandArgument.Builder description(Component var1);

      public final CommandArgument.Builder defaultsTo(Iterable<String> defaults) {
         return this.defaults(defaults);
      }

      abstract CommandArgument.Builder defaults(Iterable<String> var1);

      public final CommandArgument.Builder ofTypes(Collection<Key<?>> types) {
         return this.types(types);
      }

      abstract CommandArgument.Builder types(Collection<Key<?>> var1);

      public abstract CommandArgument.Builder variable(boolean var1);

      public abstract CommandArgument build();
   }
}
