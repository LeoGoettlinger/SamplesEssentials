package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

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
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enchant Item")
@Description({"The enchant item in an enchant prepare event or enchant event.\nIt can be modified, but enchantments will still be applied in the enchant event.\n"})
@Example.Examples({@Example("on enchant:\n\tset the enchanted item to a diamond chestplate\n"), @Example("on enchant prepare:\n\tset the enchant item to a wooden sword\n")})
@Events({"enchant prepare", "enchant"})
@Since({"2.5"})
public class ExprEnchantItem extends SimpleExpression<ItemType> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprEnchantItem.class, ItemType.class
                  )
                  .addPatterns(new String[]{"[the] enchant[:ed] item"}))
               .supplier(ExprEnchantItem::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!parseResult.hasTag("ed")) {
         ScriptWarning.printDeprecationWarning("The 'enchant item' form of the enchanted item expression is deprecated, please use 'enchanted item'!");
      }

      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(EnchantItemEvent.class, PrepareItemEnchantEvent.class);
   }

   protected ItemType[] get(Event event) {
      return new ItemType[]{new ItemType(switch (event) {
         case PrepareItemEnchantEvent prepare -> prepare.getItem();
         case EnchantItemEvent enchant -> enchant.getItem();
         case null, default -> throw new IllegalStateException("Unsupported event " + event);
      })};
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(ItemType.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      ItemType item = (ItemType)delta[0];
      switch (event) {
         case EnchantItemEvent enchant:
            ItemStack created = item.getRandom();
            enchant.setItem(created != null ? created : ItemStack.empty());
            break;
         case PrepareItemEnchantEvent prepare:
            ItemStack existing = prepare.getItem();
            Material target = existing.getType();
            boolean mayChangeType = false;

            for (Material candidate : item.getMaterials()) {
               mayChangeType |= candidate != target;
            }

            if (mayChangeType) {
               this.warning(
                  "Changing the item type of the enchant item in a prepare item enchant event is deprecated for removal and will not be supported in the future."
               );
               existing.setType(item.getMaterial());
            }

            existing.setItemMeta(item.getItemMeta());
            existing.setAmount(item.getAmount());
            break;
         default:
            throw new AssertionError("unreachable");
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "enchanted item";
   }
}
