package dev.vatten.baserad.commands;

import dev.vatten.baserad.VattenPlayer;
import java.util.List;
import java.util.Locale;
import lombok.Generated;

public abstract class Command {
   protected final String name;
   protected final List<String> aliases;

   public Command(String name, List<String> aliases) {
      this.name = name;
      this.aliases = aliases.stream().map(s -> s.toLowerCase(Locale.ENGLISH)).toList();
   }

   public abstract void execute(VattenPlayer var1, String[] var2);

   public List<String> onTabComplete(VattenPlayer player, String[] args) {
      return List.of();
   }

   public String permission() {
      return "fancytags.command";
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public List<String> getAliases() {
      return this.aliases;
   }
}
