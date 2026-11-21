package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Amplifier")
@Description({"An expression to obtain the amplifier of a potion effect."})
@Example.Examples({@Example("set the amplifier of {_potion} to 10"), @Example("add 10 to the amplifier of the player's speed effect")})
@Since({"2.7", "2.14 (support for potion effect objects, changing)"})
public class ExprPotionAmplifier extends SimplePropertyExpression<SkriptPotionEffect, Integer> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprPotionAmplifier.class, Integer.class, "([potion] amplifier|potion tier|potion level)[s]", "skriptpotioneffects", true
               )
               .supplier(ExprPotionAmplifier::new))
            .build()
      );
   }

   public Integer convert(SkriptPotionEffect potionEffect) {
      return potionEffect.amplifier() + 1;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!SkriptPotionEffect.isChangeable(this.getExpr())) {
         return null;
      } else {
         return switch (mode) {
            case ADD, SET, REMOVE -> (Class[])CollectionUtils.array(Integer.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      int change = (Integer)delta[0];
      if (mode == Changer.ChangeMode.REMOVE) {
         change = -change;
      }

      for (SkriptPotionEffect potionEffect : this.getExpr().getArray(event)) {
         int base = mode == Changer.ChangeMode.SET ? -1 : potionEffect.amplifier();
         potionEffect.amplifier(change + base);
      }
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return "amplifier";
   }
}
