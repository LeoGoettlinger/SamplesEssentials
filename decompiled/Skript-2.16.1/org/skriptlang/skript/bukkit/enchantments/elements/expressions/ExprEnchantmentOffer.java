package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enchantment Offer")
@Description({"The enchantment offer in enchant prepare events."})
@Example("on enchant prepare:\n\tsend \"Your enchantment offers are: %the enchantment offers%\" to player\n")
@Since({"2.5"})
@Events({"enchant prepare"})
public class ExprEnchantmentOffer extends SimpleExpression<EnchantmentOffer> implements EventRestrictedSyntax {
   private Expression<Integer> offerNumber;
   private boolean all;
   private final Random rand = new Random();

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprEnchantmentOffer.class, EnchantmentOffer.class
                     )
                     .addPatterns(
                        new String[]{
                           "[all [[of] the]|the] enchant[ment] offers", "enchant[ment] offer[s] %integers%", "[the] %integer%(st|nd|rd|th) enchant[ment] offer"
                        }
                     ))
                  .supplier(ExprEnchantmentOffer::new))
               .priority(SyntaxInfo.SIMPLE))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 0) {
         this.all = true;
      } else {
         this.offerNumber = (Expression<Integer>)exprs[0];
         this.all = false;
      }

      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(PrepareItemEnchantEvent.class);
   }

   @Nullable
   protected EnchantmentOffer[] get(Event event) {
      if (event instanceof PrepareItemEnchantEvent enchant) {
         if (this.all) {
            return enchant.getOffers();
         } else if (this.offerNumber == null) {
            return new EnchantmentOffer[0];
         } else if (this.offerNumber.isSingle()) {
            Integer offer = this.offerNumber.getSingle(event);
            if (offer == null) {
               return new EnchantmentOffer[0];
            } else {
               return offer >= 1 && offer <= enchant.getOffers().length ? new EnchantmentOffer[]{enchant.getOffers()[offer - 1]} : new EnchantmentOffer[0];
            }
         } else {
            List<EnchantmentOffer> offers = new ArrayList<>();

            for (Integer index : this.offerNumber.getArray(event)) {
               if (index >= 1 && index <= enchant.getOffers().length) {
                  offers.add(enchant.getOffers()[index - 1]);
               }
            }

            return offers.toArray(new EnchantmentOffer[0]);
         }
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.DELETE ? null : CollectionUtils.array(EnchantmentType.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null || mode == Changer.ChangeMode.DELETE;

      EnchantmentType type = mode != Changer.ChangeMode.DELETE ? (EnchantmentType)delta[0] : null;
      if (event instanceof PrepareItemEnchantEvent prepareEvent) {
         switch (mode) {
            case SET:
               assert type != null;

               Integer[] indices = this.all ? new Integer[]{1, 2, 3} : this.offerNumber.getArray(prepareEvent);

               for (Integer indexx : indices) {
                  int slot = indexx - 1;
                  if (slot >= 0 && slot < prepareEvent.getOffers().length) {
                     EnchantmentOffer offer = prepareEvent.getOffers()[slot];
                     if (offer == null) {
                        offer = new EnchantmentOffer(type.getType(), type.getLevel(), this.getCost(slot + 1, prepareEvent.getEnchantmentBonus()));
                        prepareEvent.getOffers()[slot] = offer;
                     } else {
                        offer.setEnchantment(type.getType());
                        offer.setEnchantmentLevel(type.getLevel());
                     }
                  }
               }
               break;
            case DELETE:
               if (this.all) {
                  Arrays.fill(prepareEvent.getOffers(), null);
               } else {
                  for (Integer index : this.offerNumber.getArray(prepareEvent)) {
                     int slot = index - 1;
                     if (slot >= 0 && slot < prepareEvent.getOffers().length) {
                        prepareEvent.getOffers()[slot] = null;
                     }
                  }
               }
               break;
            default:
               assert false;
         }
      }
   }

   @Override
   public boolean isSingle() {
      return !this.all && this.offerNumber.isSingle();
   }

   @Override
   public Class<? extends EnchantmentOffer> getReturnType() {
      return EnchantmentOffer.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.all ? "the enchantment offers" : "enchantment offer " + this.offerNumber.toString(event, debug);
   }

   public int getCost(int slot, int bookshelves) {
      int base = this.rand.nextInt(8) + 1 + bookshelves / 2 + this.rand.nextInt(bookshelves + 1);

      return switch (slot) {
         case 1 -> Math.max(base / 3, 1);
         case 2 -> base * 2 / 3 + 1;
         case 3 -> Math.max(base, bookshelves * 2);
         default -> 1;
      };
   }
}
