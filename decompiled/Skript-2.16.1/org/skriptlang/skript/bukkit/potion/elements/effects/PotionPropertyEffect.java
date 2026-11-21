package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

abstract class PotionPropertyEffect extends Effect {
   private Expression<SkriptPotionEffect> potions;
   private boolean isNegated;

   public static String[] getPatterns(PotionPropertyEffect.Type type, String property) {
      return switch (type) {
         case MAKE -> new String[]{"make %skriptpotioneffects% [:not] " + property};
         case SHOW -> new String[]{
            "(show|not:hide) [the] [potion] " + property + " [(of|for) %skriptpotioneffects%]", "(show|not:hide) %skriptpotioneffects%'[s] " + property
         };
      };
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.potions = (Expression<SkriptPotionEffect>)expressions[0];
      this.isNegated = parseResult.hasTag("not");
      return SkriptPotionEffect.isChangeable(this.potions);
   }

   @Override
   protected void execute(Event event) {
      for (SkriptPotionEffect potionEffect : this.potions.getArray(event)) {
         this.modify(potionEffect, this.isNegated);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      switch (this.getPropertyType()) {
         case MAKE:
            builder.append("make", this.potions);
            if (this.isNegated) {
               builder.append("not");
            }

            builder.append(this.getPropertyName());
            break;
         case SHOW:
            if (this.isNegated) {
               builder.append("hide");
            } else {
               builder.append("show");
            }

            builder.append("the potion", this.getPropertyName(), "of", this.potions);
      }

      return builder.toString();
   }

   public abstract void modify(SkriptPotionEffect var1, boolean var2);

   public abstract PotionPropertyEffect.Type getPropertyType();

   public abstract String getPropertyName();

   public static enum Type {
      MAKE,
      SHOW;
   }
}
