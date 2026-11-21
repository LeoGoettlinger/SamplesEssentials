package com.magmaguy.elitemobs.magmacore.command.arguments;

import java.util.List;
import lombok.Generated;
import org.bukkit.command.CommandSender;

public class LiteralCommandArgument implements ICommandArgument {
   private final String literal;

   public LiteralCommandArgument(String literal) {
      this.literal = literal;
   }

   @Override
   public String hint() {
      return "";
   }

   @Override
   public boolean matchesInput(String input) {
      return this.literal.equalsIgnoreCase(input);
   }

   @Override
   public List<String> literals() {
      return List.of(this.literal);
   }

   @Override
   public List<String> getSuggestions(CommandSender sender, String partialInput) {
      return this.literal.toLowerCase().startsWith(partialInput.toLowerCase()) ? List.of(this.literal) : List.of();
   }

   @Override
   public boolean isLiteral() {
      return true;
   }

   @Generated
   public String getLiteral() {
      return this.literal;
   }
}
