package com.magmaguy.elitemobs.magmacore.command.arguments;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class EntityTypeCommandArgument extends ListStringCommandArgument {
   public EntityTypeCommandArgument(List<String> validValues, String hint) {
      super(validValues, hint);
   }

   public EntityTypeCommandArgument() {
      super(new ArrayList<>(), "");
   }

   @Override
   public List<String> getSuggestions(CommandSender sender, String partialInput) {
      if (super.validValues.isEmpty()) {
         List<String> validEntities = new ArrayList<>();

         for (EntityType value : EntityType.values()) {
            if (!value.equals(EntityType.UNKNOWN)) {
               validEntities.add(value.getKey().getKey());
            }
         }

         return validEntities;
      } else {
         return super.getSuggestions(sender, partialInput);
      }
   }

   @Override
   public boolean matchesInput(String input) {
      try {
         EntityType.valueOf(input);
         return true;
      } catch (Exception var3) {
         return false;
      }
   }
}
