package org.enginehub.piston.part;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import java.util.Collection;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.Key;

@AutoValue
public abstract class ArgAcceptingCommandFlag implements CommandFlag, ArgAcceptingCommandPart {
   public static ArgAcceptingCommandFlag.Builder builder(char name, Component description) {
      return new AutoValue_ArgAcceptingCommandFlag.Builder().named(name).describedBy(description).defaultsTo(ImmutableList.of()).ofTypes(ImmutableList.of());
   }

   ArgAcceptingCommandFlag() {
   }

   @Override
   public Component getTextRepresentation() {
      return ColorConfig.partWrapping()
         .wrap(
            TextComponent.of("["),
            ColorConfig.mainText().wrap("-" + this.getName()),
            TextComponent.space(),
            TextComponent.of("<"),
            ColorConfig.mainText().wrap(this.getArgumentName()),
            TextComponent.of(">"),
            TextComponent.of("]")
         );
   }

   @com.google.auto.value.AutoValue.Builder
   public abstract static class Builder {
      public final ArgAcceptingCommandFlag.Builder named(char name) {
         return this.name(name);
      }

      abstract ArgAcceptingCommandFlag.Builder name(char var1);

      public final ArgAcceptingCommandFlag.Builder describedBy(String description) {
         return this.describedBy(TextComponent.of(description));
      }

      public final ArgAcceptingCommandFlag.Builder describedBy(Component description) {
         return this.description(description);
      }

      abstract ArgAcceptingCommandFlag.Builder description(Component var1);

      public final ArgAcceptingCommandFlag.Builder defaultsTo(Iterable<String> defaults) {
         return this.defaults(defaults);
      }

      abstract ArgAcceptingCommandFlag.Builder defaults(Iterable<String> var1);

      public final ArgAcceptingCommandFlag.Builder ofTypes(Collection<Key<?>> types) {
         return this.types(types);
      }

      abstract ArgAcceptingCommandFlag.Builder types(Collection<Key<?>> var1);

      public final ArgAcceptingCommandFlag.Builder argNamed(String name) {
         return this.argNamed(TranslatableComponent.of(name));
      }

      public final ArgAcceptingCommandFlag.Builder argNamed(TranslatableComponent name) {
         return this.argumentName(name);
      }

      abstract ArgAcceptingCommandFlag.Builder argumentName(TranslatableComponent var1);

      public abstract ArgAcceptingCommandFlag build();
   }
}
