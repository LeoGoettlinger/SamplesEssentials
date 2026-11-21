package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class InventoryClassInfo extends ClassInfo<Inventory> {
   public InventoryClassInfo() {
      super(Inventory.class, "inventory");
      this.user(new String[]{"inventor(y|ies)"})
         .name("Inventory")
         .description(
            "An inventory of a <a href='#player'>player</a> or <a href='#block'>block</a>. Inventories have many effects and conditions regarding the items contained.",
            "An inventory has a fixed amount of <a href='#slot'>slots</a> which represent a specific place in the inventory, e.g. the <a href='#ExprArmorSlot'>helmet slot</a> for players (Please note that slot support is still very limited but will be improved eventually)."
         )
         .usage("")
         .examples("")
         .since("1.0")
         .defaultExpression(new EventValueExpression<>(Inventory.class))
         .parser(new InventoryClassInfo.InventoryParser())
         .changer(new InventoryClassInfo.InventoryChanger())
         .property(Property.CONTAINS, "Inventories can contain items.", Skript.instance(), new InventoryClassInfo.InventoryContainsHandler())
         .property(Property.NAME, "The name of the inventory. Can be set or reset.", Skript.instance(), new InventoryClassInfo.InventoryNameHandler())
         .property(Property.DISPLAY_NAME, "The name of the inventory. Can be set or reset.", Skript.instance(), new InventoryClassInfo.InventoryNameHandler())
         .property(Property.VIEWERS, "The viewers of the inventory.", Skript.instance(), new InventoryClassInfo.InventoryViewersHandler())
         .property(
            Property.IS_EMPTY,
            "Whether the inventory contains no items (all slots contain air).",
            Skript.instance(),
            ConditionPropertyHandler.of(inventory -> {
               for (ItemStack s : inventory.getContents()) {
                  if (s != null && s.getType() != Material.AIR) {
                     return false;
                  }
               }

               return true;
            })
         );
   }

   public static class InventoryChanger implements Changer<Inventory> {
      private final Material[] cachedMaterials = Material.values();

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.RESET) {
            return null;
         } else if (mode == Changer.ChangeMode.REMOVE_ALL) {
            return CollectionUtils.array(ItemType[].class);
         } else {
            return mode == Changer.ChangeMode.SET
               ? CollectionUtils.array(ItemType[].class, Inventory.class)
               : CollectionUtils.array(ItemType[].class, Inventory[].class);
         }
      }

      public void change(Inventory[] inventories, @Nullable Object[] delta, Changer.ChangeMode mode) {
         for (Inventory inventory : inventories) {
            assert inventory != null;

            switch (mode) {
               case DELETE:
                  inventory.clear();
                  break;
               case SET:
                  inventory.clear();
               case ADD:
                  assert delta != null;

                  if (!(delta instanceof ItemStack[] items)) {
                     for (Object d : delta) {
                        if (d instanceof ItemStack stack) {
                           new ItemType(stack).addTo(inventory);
                        } else if (d instanceof ItemType itemType) {
                           itemType.addTo(inventory);
                        } else if (d instanceof Block block) {
                           new ItemType(block).addTo(inventory);
                        } else {
                           Skript.error("Can't " + d.toString() + " to an inventory!");
                        }
                     }
                     break;
                  }

                  if (items.length > 36) {
                     return;
                  }

                  Object[] var20 = delta;
                  int var24 = delta.length;
                  int var27 = 0;

                  for (; var27 < var24; var27++) {
                     Object dx = var20[var27];
                     if (dx instanceof Inventory) {
                        for (ItemStack itemStack : (Inventory)dx) {
                           if (itemStack != null) {
                              inventory.addItem(new ItemStack[]{itemStack});
                           }
                        }
                     } else {
                        ((ItemType)dx).addTo(inventory);
                     }
                  }
                  break;
               case REMOVE:
               case REMOVE_ALL:
                  assert delta != null;

                  if (delta.length == this.cachedMaterials.length) {
                     boolean equal = true;

                     for (int i = 0; i < delta.length; i++) {
                        if (!(delta[i] instanceof ItemType itemType)) {
                           equal = false;
                           break;
                        }

                        if (itemType.getMaterial() != this.cachedMaterials[i]) {
                           equal = false;
                           break;
                        }
                     }

                     if (equal) {
                        inventory.clear();
                        break;
                     }
                  }

                  for (Object dx : delta) {
                     if (dx instanceof Inventory itemStacks) {
                        assert mode == Changer.ChangeMode.REMOVE;

                        for (ItemStack itemStackx : itemStacks) {
                           if (itemStackx != null) {
                              inventory.removeItem(new ItemStack[]{itemStackx});
                           }
                        }
                     } else if (mode == Changer.ChangeMode.REMOVE) {
                        ((ItemType)dx).removeFrom(inventory);
                     } else {
                        ((ItemType)dx).removeAll(inventory);
                     }
                  }
                  break;
               case RESET:
                  assert false;
            }

            if (inventory.getHolder() instanceof Player player) {
               player.updateInventory();
            }
         }
      }
   }

   private static class InventoryContainsHandler implements ContainsHandler<Inventory, Object> {
      public boolean contains(Inventory container, Object element) {
         if (element instanceof ItemType type) {
            return type.isContainedIn(container);
         } else {
            return element instanceof ItemStack stack ? container.containsAtLeast(stack, stack.getAmount()) : false;
         }
      }

      @Override
      public Class<?>[] elementTypes() {
         return new Class[]{ItemType.class, ItemStack.class};
      }
   }

   private static class InventoryNameHandler implements ExpressionPropertyHandler<Inventory, Component> {
      public Component convert(Event event, Inventory inventory) {
         return event instanceof InventoryEvent inventoryEvent && inventoryEvent.getInventory() == inventory
            ? inventoryEvent.getView().title()
            : this.convert(inventory);
      }

      @Nullable
      public Component convert(Inventory inventory) {
         return inventory.getViewers().isEmpty() ? null : ((HumanEntity)inventory.getViewers().getFirst()).getOpenInventory().title();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : new Class[]{Component.class};
      }

      public void change(Inventory inventory, @Nullable Object[] delta, Changer.ChangeMode mode) {
         Component name = delta != null && delta.length != 0 ? (Component)delta[0] : null;
         if (!inventory.getViewers().isEmpty()) {
            List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
            InventoryType type = inventory.getType();
            if (type.isCreatable()) {
               if (name == null) {
                  name = type.defaultTitle();
               }

               Inventory copy;
               if (type == InventoryType.CHEST) {
                  copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), name);
               } else {
                  copy = Bukkit.createInventory(inventory.getHolder(), type, name);
               }

               copy.setContents(inventory.getContents());
               viewers.forEach(viewer -> viewer.openInventory(copy));
            }
         }
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }

   private static class InventoryParser extends Parser<Inventory> {
      @Nullable
      public Inventory parse(String s, ParseContext context) {
         return null;
      }

      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(Inventory i, int flags) {
         return "inventory of " + Classes.toString(i.getHolder());
      }

      public String getDebugMessage(Inventory i) {
         return "inventory of " + Classes.getDebugMessage(i.getHolder());
      }

      public String toVariableNameString(Inventory i) {
         return "inventory of " + Classes.toString(i.getHolder(), StringMode.VARIABLE_NAME);
      }
   }

   private static class InventoryViewersHandler implements ExpressionPropertyHandler<Inventory, Object> {
      public Object convert(Inventory inventory) {
         return inventory.getViewers().stream().filter(viewer -> viewer instanceof Player).toArray(Player[]::new);
      }

      @NotNull
      @Override
      public Class<Object> returnType() {
         return Player.class;
      }
   }
}
