package org.skriptlang.skript.bukkit.brewing.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Brewing Results")
@Description({"The resulting items in an 'on brew complete' event."})
@Example("on brew complete:\n\tset {_results::*} to the brewing results\n")
@Since({"2.13"})
@Events({"Brewing Complete"})
public class ExprBrewingResults extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {
   private boolean delayed;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprBrewingResults.class, ItemStack.class
                  )
                  .addPatterns(new String[]{"[the] brewing results"}))
               .supplier(ExprBrewingResults::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.delayed = isDelayed.isTrue();
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(BrewEvent.class);
   }

   @Nullable
   protected ItemStack[] get(Event event) {
      return event instanceof BrewEvent brewEvent ? brewEvent.getResults().toArray(ItemStack[]::new) : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.delayed) {
         Skript.error("The 'brewing results' cannot be changed after the 'brewing complete' event has passed.");
         return null;
      } else {
         return switch (mode) {
            case SET, DELETE, ADD, REMOVE -> (Class[])CollectionUtils.array(ItemType[].class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof BrewEvent brewEvent) {
         ArrayList itemTypes = new ArrayList();
         if (delta != null) {
            for (Object object : delta) {
               if (object instanceof ItemType itemType) {
                  itemTypes.add(itemType);
               }
            }
         }

         List<ItemStack> results = brewEvent.getResults();
         switch (mode) {
            case SET:
               results.clear();
               results.addAll(itemTypes.stream().map(ItemType::getRandom).filter(itemStackx -> !Objects.isNull(itemStackx)).toList());
               break;
            case DELETE:
               results.clear();
               break;
            case ADD:
               results.addAll(itemTypes.stream().map(ItemType::getRandom).filter(itemStackx -> !Objects.isNull(itemStackx)).toList());
               break;
            case REMOVE:
               for (ItemStack itemStack : new ArrayList<>(results)) {
                  for (ItemType itemType : itemTypes) {
                     if (itemType.isOfType(itemStack)) {
                        results.remove(itemStack);
                     }
                  }
               }
         }

         if (results.size() > 3) {
            this.warning("A brewing stand can only contain 3 items; Some items will be ignored.");
         }
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends ItemStack> getReturnType() {
      return ItemStack.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the brewing results";
   }
}
