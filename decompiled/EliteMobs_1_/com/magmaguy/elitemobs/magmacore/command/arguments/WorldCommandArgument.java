package com.magmaguy.elitemobs.magmacore.command.arguments;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;

public class WorldCommandArgument extends ListStringCommandArgument {
   public WorldCommandArgument(List<String> validValues, String hint) {
      super(validValues, hint);
   }

   public WorldCommandArgument(String hint) {
      super(new ArrayList<>(), hint);
   }

   @Override
   public List<String> getSuggestions(CommandSender sender, String partialInput) {
      return super.validValues.isEmpty() ? Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).toList() : super.getSuggestions(sender, partialInput);
   }
}
