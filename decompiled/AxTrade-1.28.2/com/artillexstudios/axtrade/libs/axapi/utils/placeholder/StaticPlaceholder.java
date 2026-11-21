package com.artillexstudios.axtrade.libs.axapi.utils.placeholder;

import java.util.function.Function;
import org.bukkit.entity.Player;

public class StaticPlaceholder extends Placeholder {
   public StaticPlaceholder(Function<String, String> function) {
      super((player, string) -> function.apply(string));
   }

   @Override
   public String parse(Player player, String line) {
      return super.parse(player, line);
   }
}
