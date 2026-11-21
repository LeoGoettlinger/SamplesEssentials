package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enchantment Bonus")
@Description({"The enchantment bonus in an enchant prepare event. This represents the number of bookshelves affecting/surrounding the enchantment table."})
@Example("on enchant:\n\tsend \"There are %enchantment bonus% bookshelves surrounding this enchantment table!\" to player\n")
@Events({"enchant prepare"})
@Since({"2.5"})
public class ExprEnchantmentBonus extends SimpleExpression<Integer> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprEnchantmentBonus.class, Integer.class
                  )
                  .addPattern("[the] enchant[ment] bonus"))
               .supplier(ExprEnchantmentBonus::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return new Class[]{PrepareItemEnchantEvent.class};
   }

   @Nullable
   protected Integer[] get(Event event) {
      return event instanceof PrepareItemEnchantEvent prepare ? new Integer[]{prepare.getEnchantmentBonus()} : null;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "enchantment bonus";
   }
}
