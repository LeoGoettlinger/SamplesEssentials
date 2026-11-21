package org.lushplugins.lushrewards.utils.placeholder;

import org.bukkit.entity.Player;

public class RegexPlaceholder extends Placeholder {
   private final LocalPlaceholders.PlaceholderFunction method;

   public RegexPlaceholder(String content, LocalPlaceholders.PlaceholderFunction method) {
      super(content);
      this.method = method;
   }

   @Override
   boolean matches(String string) {
      return string.matches(this.content);
   }

   @Override
   public String parse(String[] params, Player player) {
      try {
         return this.method.apply(params, player);
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public RegexPlaceholder addChild(Placeholder placeholder) {
      super.addChild(placeholder);
      return this;
   }
}
