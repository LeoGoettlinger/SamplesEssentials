package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import org.bukkit.event.Event;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Loot")
@Description({"The loot that will be generated in a 'loot generate' event."})
@Example("on loot generate:\n\tchance of 10%\n\tadd 64 diamonds to loot\n\tsend \"You hit the jackpot!!\"\n")
@Since({"2.7"})
public class ExprLoot extends SimpleExpression<ItemStack> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(ExprLoot.class, ItemStack.class)
                  .addPatterns(new String[]{"[the] loot"}))
               .supplier(ExprLoot::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!this.getParser().isCurrentEvent(LootGenerateEvent.class)) {
         Skript.error("The 'loot' expression can only be used in a 'loot generate' event");
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   protected ItemStack[] get(Event event) {
      return event instanceof LootGenerateEvent lootEvent ? lootEvent.getLoot().toArray(new ItemStack[0]) : new ItemStack[0];
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case DELETE, ADD, REMOVE, SET -> (Class[])CollectionUtils.array(ItemStack[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof LootGenerateEvent lootEvent) {
         ArrayList items = null;
         if (delta != null) {
            items = new ArrayList(delta.length);

            for (Object item : delta) {
               items.add((ItemStack)item);
            }
         }

         switch (mode) {
            case DELETE:
               lootEvent.getLoot().clear();
               break;
            case ADD:
               lootEvent.getLoot().addAll(items);
               break;
            case REMOVE:
               lootEvent.getLoot().removeAll(items);
               break;
            case SET:
               lootEvent.setLoot(items);
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
      return "the loot";
   }
}
