package org.lushplugins.lushrewards.utils.placeholder;

import java.util.List;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;

public class TimePlaceholder extends Placeholder {
   private final TimePlaceholder.TimePlaceholderFunction method;

   public TimePlaceholder(String content, TimePlaceholder.TimePlaceholderFunction method) {
      super(content);
      this.method = method;
   }

   @Override
   boolean matches(String string) {
      return string.startsWith(this.content)
         || string.startsWith(this.content + "_minutes")
         || string.startsWith(this.content + "_hours")
         || string.startsWith(this.content + "_seconds");
   }

   @Override
   public String parse(String[] params, Player player) {
      try {
         Integer seconds = this.method.apply(params, player);
         if (seconds == null) {
            return null;
         } else {
            List<String> paramsList = List.of(params);
            if (paramsList.contains("seconds")) {
               return String.valueOf(seconds);
            } else {
               return paramsList.contains("hours")
                  ? String.valueOf((int)Math.floor(seconds.intValue() / 60.0 / 60.0))
                  : String.valueOf((int)Math.floor(seconds.intValue() / 60.0));
            }
         }
      } catch (Exception var5) {
         LushRewards.getInstance().log(Level.WARNING, "Caught error whilst parsing time placeholder:", var5);
         return null;
      }
   }

   public TimePlaceholder addChild(Placeholder placeholder) {
      super.addChild(placeholder);
      return this;
   }

   @FunctionalInterface
   public interface TimePlaceholderFunction {
      Integer apply(String[] var1, Player var2);
   }
}
