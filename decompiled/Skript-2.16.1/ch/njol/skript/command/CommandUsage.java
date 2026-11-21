package ch.njol.skript.command;

import ch.njol.skript.lang.VariableString;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

public class CommandUsage {
   private final VariableString usage;
   private final String defaultUsage;

   public CommandUsage(@Nullable VariableString usage, String defaultUsage) {
      if (usage == null) {
         defaultUsage = VariableString.quote(defaultUsage);
         usage = VariableString.newInstance(defaultUsage);

         assert usage != null;
      }

      this.usage = usage;
      this.defaultUsage = defaultUsage;
   }

   public VariableString getRawUsage() {
      return this.usage;
   }

   public String getUsage() {
      return this.getUsage(null);
   }

   public String getUsage(@Nullable Event event) {
      return event == null && !this.usage.isSimple() ? this.defaultUsage : this.usage.toString(event);
   }

   public Component getUsageComponent(@Nullable Event event) {
      return TextComponentParser.instance().parse(this.getUsage(event));
   }

   @Override
   public String toString() {
      return this.getUsage();
   }
}
