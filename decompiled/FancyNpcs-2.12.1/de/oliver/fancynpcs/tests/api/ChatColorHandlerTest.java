package de.oliver.fancynpcs.tests.api;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parsers;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import de.oliver.plugintests.annotations.FPTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ChatColorHandlerTest {
   @FPTest(
      name = "Test Placeholders"
   )
   public void testPlaceholders(Player player) {
      if (this.isPlaceholderAPIEnabled()) {
         String input = "Player name: %fn-test%";
         String got = PaperColor.handler().translateRaw(input, player, Parsers::placeholder);
         String expected = "Player name: ";
         if (got.equalsIgnoreCase("Player name: %fn-test%")) {
            throw new AssertionError("Placeholder not replaced: " + got);
         }
      }
   }

   private boolean isPlaceholderAPIEnabled() {
      Plugin placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
      return placeholderAPI != null && placeholderAPI.isEnabled();
   }
}
