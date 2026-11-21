package org.enginehub.piston.part;

import com.google.auto.value.AutoValue;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.config.ColorConfig;

@AutoValue
public abstract class NoArgCommandFlag implements CommandFlag {
   public static NoArgCommandFlag.Builder builder(char name, String description) {
      return builder(name, TextComponent.of(description));
   }

   public static NoArgCommandFlag.Builder builder(char name, Component description) {
      return new AutoValue_NoArgCommandFlag.Builder().named(name).describedBy(description);
   }

   NoArgCommandFlag() {
   }

   @Override
   public Component getTextRepresentation() {
      return ColorConfig.partWrapping().wrap(TextComponent.of("["), ColorConfig.mainText().wrap("-" + this.getName()), TextComponent.of("]"));
   }

   @com.google.auto.value.AutoValue.Builder
   public abstract static class Builder {
      public final NoArgCommandFlag.Builder named(char name) {
         return this.name(name);
      }

      abstract NoArgCommandFlag.Builder name(char var1);

      public final NoArgCommandFlag.Builder describedBy(String description) {
         return this.describedBy(TextComponent.of(description));
      }

      public final NoArgCommandFlag.Builder describedBy(Component description) {
         return this.description(description);
      }

      abstract NoArgCommandFlag.Builder description(Component var1);

      public final ArgAcceptingCommandFlag.Builder withRequiredArg() {
         NoArgCommandFlag flag = this.build();
         return ArgAcceptingCommandFlag.builder(flag.getName(), flag.getDescription());
      }

      public abstract NoArgCommandFlag build();
   }
}
