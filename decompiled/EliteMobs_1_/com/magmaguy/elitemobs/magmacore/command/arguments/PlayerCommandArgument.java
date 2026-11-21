package com.magmaguy.elitemobs.magmacore.command.arguments;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommandArgument implements ICommandArgument {
   @Override
   public String hint() {
      return "";
   }

   @Override
   public boolean matchesInput(String input) {
      return this.getPlayerByName(input) != null;
   }

   @Override
   public List<String> literals() {
      return Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).toList();
   }

   @Override
   public List<String> getSuggestions(CommandSender sender, String partialInput) {
      return Bukkit.getOnlinePlayers()
         .stream()
         .filter(target -> !(sender instanceof Player viewer && !viewer.canSee(target)))
         .<String>map(Player::getName)
         .filter(name -> name.regionMatches(true, 0, partialInput, 0, partialInput.length()))
         .toList();
   }

   @Override
   public boolean isLiteral() {
      return false;
   }

   private Player getPlayerByName(String name) {
      return Bukkit.getPlayerExact(name);
   }
}
