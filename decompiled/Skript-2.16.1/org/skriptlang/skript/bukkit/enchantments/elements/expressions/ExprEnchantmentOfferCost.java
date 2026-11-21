package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enchantment Offer Cost")
@Description({"The cost of an enchantment offer. This is displayed to the right of an enchantment offer.", "If the cost is changed, it will always be at least 1.", "This changes how many levels are required to enchant, but does not change the number of levels removed.", "To change the number of levels removed, use the enchant event."})
@Example("set cost of enchantment offer 1 to 50")
@Since({"2.5"})
public class ExprEnchantmentOfferCost extends SimplePropertyExpression<EnchantmentOffer, Long> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprEnchantmentOfferCost.class, Long.class, "[enchant[ment]] cost", "enchantmentoffers", false)
               .supplier(ExprEnchantmentOfferCost::new))
            .build()
      );
   }

   public Long convert(EnchantmentOffer offer) {
      return (long)offer.getCost();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.REMOVE && mode != Changer.ChangeMode.REMOVE_ALL && mode != Changer.ChangeMode.RESET
         ? CollectionUtils.array(Number.class)
         : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      EnchantmentOffer[] offers = this.getExpr().getArray(event);
      if (offers.length != 0 && delta != null) {
         long cost = ((Number)delta[0]).longValue();
         if (cost >= 1L) {
            for (EnchantmentOffer offer : offers) {
               switch (mode) {
                  case SET:
                     offer.setCost(Math2.clampToInt(cost));
                     break;
                  case ADD:
                     long change = Math2.addClamped(offer.getCost(), cost);
                     if (change < 1L) {
                        return;
                     }

                     offer.setCost(Math2.clampToInt(change));
                     break;
                  case REMOVE:
                     long change = Math2.addClamped(offer.getCost(), -cost);
                     if (change < 1L) {
                        return;
                     }

                     offer.setCost(Math2.clampToInt(change));
                     break;
                  default:
                     assert false;
               }
            }
         }
      }
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   protected String getPropertyName() {
      return "enchantment cost";
   }
}
