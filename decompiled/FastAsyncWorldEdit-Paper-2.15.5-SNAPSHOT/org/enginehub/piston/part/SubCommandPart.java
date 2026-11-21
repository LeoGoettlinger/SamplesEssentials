package org.enginehub.piston.part;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import java.util.Collection;
import org.enginehub.piston.Command;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.util.ComponentHelper;

@AutoValue
public abstract class SubCommandPart implements ArgConsumingCommandPart {
   public static SubCommandPart.Builder builder(TranslatableComponent name, Component description) {
      return new AutoValue_SubCommandPart.Builder().named(name).describedBy(description);
   }

   public abstract ImmutableList<Command> getCommands();

   @Override
   public Component getTextRepresentation() {
      com.google.common.collect.ImmutableList.Builder<Component> builder = ImmutableList.builder();
      builder.add(TextComponent.of(this.isRequired() ? "<" : "["));
      builder.addAll(this.getCommands().stream().map(Command::getName).map(ColorConfig.mainText()::wrap).collect(ComponentHelper.joiningWithBar()).children());
      builder.add(TextComponent.of(this.isRequired() ? ">" : "]"));
      return ColorConfig.partWrapping().wrap(builder.build());
   }

   @com.google.auto.value.AutoValue.Builder
   public abstract static class Builder {
      public final SubCommandPart.Builder named(TranslatableComponent name) {
         return this.argumentName(name);
      }

      abstract SubCommandPart.Builder argumentName(TranslatableComponent var1);

      public final SubCommandPart.Builder describedBy(String description) {
         return this.describedBy(TextComponent.of(description));
      }

      public final SubCommandPart.Builder describedBy(Component description) {
         return this.description(description);
      }

      abstract SubCommandPart.Builder description(Component var1);

      public final SubCommandPart.Builder withCommands(Collection<Command> commands) {
         return this.commands(commands);
      }

      abstract SubCommandPart.Builder commands(Collection<Command> var1);

      public final SubCommandPart.Builder required() {
         return this.required(true);
      }

      public final SubCommandPart.Builder optional() {
         return this.required(false);
      }

      abstract SubCommandPart.Builder required(boolean var1);

      public abstract SubCommandPart build();
   }
}
