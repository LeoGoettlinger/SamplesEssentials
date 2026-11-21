package com.artillexstudios.axafkzone.libs.axapi.utils.placeholder;

import java.util.function.BiFunction;
import org.bukkit.entity.Player;

public class Placeholder {
   private final BiFunction<Player, String, String> function;

   public Placeholder(BiFunction<Player, String, String> function) {
      this.function = function;
   }

   public String parse(Player player, String line) {
      return this.function.apply(player, line);
   }
}
