package com.artillexstudios.axrankmenu.libs.axapi.placeholders;

import com.artillexstudios.axrankmenu.libs.axapi.placeholders.exception.PlaceholderParameterNotInContextException;
import com.artillexstudios.axrankmenu.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Optionals;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PaperPlaceholderHandler extends PlaceholderHandler {
   public static String parseWithPlaceholderAPI(String line, PlaceholderParameters parameters) {
      String newLine = parse(line, parameters);
      if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
         try {
            newLine = PlaceholderAPI.setPlaceholders(Optionals.orElse(parameters.getByName("player"), parameters.resolve(Player.class)), newLine);
         } catch (PlaceholderParameterNotInContextException var4) {
            return newLine;
         }
      }

      return newLine;
   }
}
