package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Minimum/Maximum Enchantment Level")
@Description({"The minimum or maximum allowed level in Minecraft of a particular <a href='#enchantment'>enchantment</a>.\n\nThe minimum starting level is 1 for all existing enchantments as of 26.1.2.\n"})
@Example.Examples({@Example("set {_maximum} to the maximum enchantment level of sharpness\nif the level of sharpness of the player's tool is greater than {_maximum}:\n\tsend \"<gold>Your tool's sharpness level was capped out at the maximum allowed level.</gold>\"\n\tset the level of sharpness of the player's tool to {_maximum}\n"), @Example("set {_min} to the minimum enchantment level of sharpness\nset {_max} to the maximum enchantment level of sharpness\nloop integers between {_min} and {_max}:\n\tset slot loop-counter of {_gui} to enchanted book named \"Sharpness %loop-value%\" with lore \"<reset>Click to enchant!\"\n")})
@Since({"2.16"})
public class ExprMinMaxEnchantmentLevel extends SimplePropertyExpression<Enchantment, Integer> {
   private boolean maximum = false;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprMinMaxEnchantmentLevel.class, Integer.class, "((:max|min)[imum]|starting) enchant[ment] level", "enchantments", true
               )
               .supplier(ExprMinMaxEnchantmentLevel::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.maximum = parseResult.hasTag("max");
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Integer convert(Enchantment from) {
      return this.maximum ? from.getMaxLevel() : from.getStartLevel();
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return (this.maximum ? "maximum" : "minimum") + " enchantment level";
   }
}
