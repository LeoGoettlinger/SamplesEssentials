package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import java.util.Arrays;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class ItemStackClassInfo extends ClassInfo<ItemStack> {
   public ItemStackClassInfo() {
      super(ItemStack.class, "itemstack");
      this.user(new String[]{"items?", "item ?stacks?"})
         .name("Item")
         .description(
            "An item, e.g. a stack of torches, a furnace, or a wooden sword of sharpness 2. Unlike <a href='#itemtype'>item type</a> an item can only represent exactly one item (e.g. an upside-down cobblestone stair facing west), while an item type can represent a whole range of items (e.g. any cobble stone stairs regardless of direction).",
            "You don't usually need this type except when you want to make a command that only accepts an exact item.",
            "Please note that currently 'material' is exactly the same as 'item', i.e. can have an amount & enchantments."
         )
         .usage(
            "<code>[<number> [of]] <alias> [of <enchantment> <level>]</code>, Where <alias> must be an alias that represents exactly one item (i.e cannot be a general alias like 'sword' or 'plant')"
         )
         .examples("set {_item} to type of the targeted block", "{_item} is a torch")
         .since("1.0")
         .after("number")
         .supplier(() -> Arrays.stream(Material.values()).filter(Material::isItem).<ItemStack>map(ItemStack::new).iterator())
         .parser(new ItemStackClassInfo.ItemStackParser())
         .cloner(ItemStack::clone)
         .serializer(new ConfigurationSerializer())
         .defaultExpression(new EventValueExpression<>(ItemStack.class))
         .property(Property.AMOUNT, "The number of items in this stack. Can be set.", Skript.instance(), new ItemStackClassInfo.ItemStackAmountHandler());
   }

   private static class ItemStackAmountHandler implements ExpressionPropertyHandler<ItemStack, Number> {
      public Number convert(ItemStack itemStack) {
         return itemStack.getAmount();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.SET ? new Class[]{Integer.class} : null;
      }

      public void change(ItemStack itemStack, @Nullable Object[] delta, Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.SET) {
            assert delta != null;

            itemStack.setAmount((Integer)delta[0]);
         }
      }

      @NotNull
      @Override
      public Class<Number> returnType() {
         return Number.class;
      }
   }

   private static class ItemStackParser extends Parser<ItemStack> {
      @Nullable
      public ItemStack parse(String s, ParseContext context) {
         ItemType t = Aliases.parseItemType(s);
         if (t == null) {
            return null;
         } else {
            t = t.getItem();
            if (t.numTypes() != 1) {
               Skript.error("'" + s + "' represents multiple materials");
               return null;
            } else {
               ItemStack i = t.getRandom();
               if (i == null) {
                  Skript.error("'" + s + "' cannot represent an item");
                  return null;
               } else {
                  return i;
               }
            }
         }
      }

      public String toString(ItemStack i, int flags) {
         return ItemType.toString(i, flags);
      }

      public String toVariableNameString(ItemStack i) {
         StringBuilder b = new StringBuilder("item:");
         b.append(i.getType().name());
         b.append(":").append(ItemUtils.getDamage(i));
         b.append("*").append(i.getAmount());

         for (Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
            b.append("#").append(entry.getKey().getKey());
            b.append(":").append(entry.getValue());
         }

         return b.toString();
      }
   }
}
