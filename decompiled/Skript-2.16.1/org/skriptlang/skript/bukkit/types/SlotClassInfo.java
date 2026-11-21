package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.slot.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class SlotClassInfo extends ClassInfo<Slot> {
   public SlotClassInfo() {
      super(Slot.class, "slot");
      this.user(new String[]{"(inventory )?slots?"})
         .name("Slot")
         .description(
            "Represents a single slot of an <a href='#inventory'>inventory</a>. Notable slots are the <a href='#ExprArmorSlot'>armour slots</a> and <a href='./expressions/#ExprFurnaceSlot'>furnace slots</a>. ",
            "The most important property that distinguishes a slot from an <a href='#itemstack'>item</a> is its ability to be changed, e.g. it can be set, deleted, enchanted, etc. (Some item expressions can be changed as well, e.g. items stored in variables. For that matter: slots are never saved to variables, only the items they represent at the time when the variable is set).",
            "Please note that <a href='#ExprTool'>tool</a> can be regarded a slot, but it can actually change it's position, i.e. doesn't represent always the same slot."
         )
         .usage("")
         .examples(
            "set tool of player to dirt",
            "delete helmet of the victim",
            "set the color of the player's tool to green",
            "enchant the player's chestplate with projectile protection 5"
         )
         .since("")
         .defaultExpression(new EventValueExpression<>(Slot.class))
         .changer(new SlotClassInfo.SlotChanger())
         .parser(new SlotClassInfo.SlotParser())
         .serializeAs(ItemStack.class)
         .property(
            Property.NAME,
            "The custom name of the item in the slot, if it has one. Can be set or reset.",
            Skript.instance(),
            new SlotClassInfo.SlotNameHandler()
         )
         .property(
            Property.DISPLAY_NAME,
            "The custom name of the item in the slot, if it has one. Can be set or reset.",
            Skript.instance(),
            new SlotClassInfo.SlotNameHandler()
         )
         .property(Property.AMOUNT, "The amount of items in the slot's stack. Can be set.", Skript.instance(), new SlotClassInfo.SlotAmountHandler())
         .property(Property.IS_EMPTY, "Whether this slot does not contain a (non-air) item.", Skript.instance(), ConditionPropertyHandler.of(slot -> {
            ItemStack item = slot.getItem();
            return item == null || item.getType() == Material.AIR;
         }));
   }

   private static class SlotAmountHandler implements ExpressionPropertyHandler<Slot, Number> {
      public Number convert(Slot slot) {
         return slot.getAmount();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.SET ? new Class[]{Integer.class} : null;
      }

      public void change(Slot slot, @Nullable Object[] delta, Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.SET) {
            assert delta != null;

            slot.setAmount((Integer)delta[0]);
         }
      }

      @NotNull
      @Override
      public Class<Number> returnType() {
         return Number.class;
      }
   }

   public static class SlotChanger implements Changer<Slot> {
      @Nullable
      @Override
      public Class<Object>[] acceptChange(Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.RESET) {
            return null;
         } else {
            return mode == Changer.ChangeMode.SET ? new Class[]{ItemType[].class, ItemStack[].class} : new Class[]{ItemType.class, ItemStack.class};
         }
      }

      public void change(Slot[] slots, @Nullable Object[] deltas, Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.SET) {
            if (deltas != null) {
               if (deltas.length == 1) {
                  Object delta = deltas[0];

                  for (Slot slot : slots) {
                     slot.setItem(delta instanceof ItemStack ? (ItemStack)delta : ((ItemType)delta).getItem().getRandom());
                  }
               } else if (deltas.length == slots.length) {
                  for (int i = 0; i < slots.length; i++) {
                     Object delta = deltas[i];
                     slots[i].setItem(delta instanceof ItemStack ? (ItemStack)delta : ((ItemType)delta).getItem().getRandom());
                  }
               }
            }
         } else {
            Object delta = deltas == null ? null : deltas[0];

            for (Slot slot : slots) {
               switch (mode) {
                  case ADD:
                     assert delta != null;

                     if (delta instanceof ItemStack) {
                        ItemStack i = slot.getItem();
                        if (i == null || i.getType() == Material.AIR || ItemUtils.itemStacksEqual(i, (ItemStack)delta)) {
                           if (i != null && i.getType() != Material.AIR) {
                              i.setAmount(Math.min(i.getAmount() + ((ItemStack)delta).getAmount(), i.getMaxStackSize()));
                              slot.setItem(i);
                           } else {
                              slot.setItem((ItemStack)delta);
                           }
                        }
                     } else {
                        slot.setItem(((ItemType)delta).getItem().addTo(slot.getItem()));
                     }
                     break;
                  case REMOVE:
                  case REMOVE_ALL:
                     assert delta != null;

                     if (delta instanceof ItemStack) {
                        ItemStack i = slot.getItem();
                        if (i != null && ItemUtils.itemStacksEqual(i, (ItemStack)delta)) {
                           int a = mode == Changer.ChangeMode.REMOVE_ALL ? 0 : i.getAmount() - ((ItemStack)delta).getAmount();
                           if (a <= 0) {
                              slot.setItem(null);
                           } else {
                              i.setAmount(a);
                              slot.setItem(i);
                           }
                        }
                     } else if (mode == Changer.ChangeMode.REMOVE) {
                        slot.setItem(((ItemType)delta).removeFrom(slot.getItem()));
                     } else {
                        slot.setItem(((ItemType)delta).removeAll(slot.getItem()));
                     }
                     break;
                  case DELETE:
                     slot.setItem(null);
                     break;
                  case RESET:
                     assert false;
               }
            }
         }
      }
   }

   private static class SlotNameHandler implements ExpressionPropertyHandler<Slot, Component> {
      public Component convert(Slot slot) {
         ItemStack stack = slot.getItem();
         if (stack != null && stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            return meta.hasDisplayName() ? meta.displayName() : null;
         } else {
            return null;
         }
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case DELETE, RESET, SET -> new Class[]{Component.class};
            default -> null;
         };
      }

      public void change(Slot named, @Nullable Object[] delta, Changer.ChangeMode mode) {
         Component name = delta == null ? null : (Component)delta[0];
         ItemStack stack = named.getItem();
         if (stack != null && !ItemUtils.isAir(stack.getType())) {
            ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(stack.getType());
            meta.displayName(name);
            stack.setItemMeta(meta);
            named.setItem(stack);
         }
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }

   private static class SlotParser extends Parser<Slot> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(Slot o, int flags) {
         ItemStack i = o.getItem();
         return i == null ? new ItemType(Material.AIR).toString(flags) : ItemType.toString(i, flags);
      }

      public String toVariableNameString(Slot o) {
         return "slot:" + o.toString();
      }
   }
}
