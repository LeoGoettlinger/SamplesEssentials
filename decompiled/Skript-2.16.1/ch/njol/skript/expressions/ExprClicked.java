package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.lang.reflect.Array;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Clicked Block/Entity/Inventory/Slot")
@Description({"The clicked block, entity, inventory, inventory slot, inventory click type or inventory action."})
@Example("message \"You clicked on a %type of clicked entity%!\"\nif the clicked block is a chest:\n\tshow the inventory of the clicked block to the player\n")
@Since({"1.0, 2.2-dev35 (more clickable things)"})
@Events({"click", "inventory click"})
public class ExprClicked extends SimpleExpression<Object> {
   @Nullable
   private EntityData<?> entityType;
   @Nullable
   private ItemType itemType;
   private ExprClicked.ClickableType clickable = ExprClicked.ClickableType.BLOCK_AND_ITEMS;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.clickable = ExprClicked.ClickableType.getClickable(parseResult.mark);
      switch (this.clickable) {
         case ENCHANT_BUTTON:
            if (!this.getParser().isCurrentEvent(EnchantItemEvent.class)) {
               Skript.error("The expression 'clicked enchantment button' is only usable in an enchant event.");
               return false;
            }
            break;
         case BLOCK_AND_ITEMS:
            Object type = exprs[0] == null ? null : ((Literal)exprs[0]).getSingle();
            if (type instanceof EntityData) {
               this.entityType = (EntityData<?>)type;
               if (!this.getParser().isCurrentEvent(PlayerInteractEntityEvent.class) && !this.getParser().isCurrentEvent(PlayerInteractAtEntityEvent.class)) {
                  Skript.error("The expression 'clicked entity' may only be used in a click event");
                  return false;
               }
            } else {
               this.itemType = (ItemType)type;
               if (!this.getParser().isCurrentEvent(PlayerInteractEvent.class)) {
                  Skript.error("The expression 'clicked block' may only be used in a click event");
                  return false;
               }
            }
            break;
         case SLOT:
         case INVENTORY:
         case TYPE:
         case ACTION:
            if (!this.getParser().isCurrentEvent(InventoryClickEvent.class)) {
               Skript.error("The expression '" + this.clickable.getName() + "' may only be used in an inventory click event");
               return false;
            }
      }

      return true;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Object> getReturnType() {
      return (Class<? extends Object>)(this.clickable != ExprClicked.ClickableType.BLOCK_AND_ITEMS
         ? this.clickable.getClickableClass()
         : (this.entityType != null ? this.entityType.getType() : Block.class));
   }

   @Nullable
   @Override
   protected Object[] get(Event e) {
      if (!(e instanceof InventoryClickEvent)
         && this.clickable != ExprClicked.ClickableType.BLOCK_AND_ITEMS
         && this.clickable != ExprClicked.ClickableType.ENCHANT_BUTTON) {
         return null;
      } else {
         switch (this.clickable) {
            case ENCHANT_BUTTON:
               if (e instanceof EnchantItemEvent) {
                  return new Number[]{((EnchantItemEvent)e).whichButton() + 1};
               }
               break;
            case BLOCK_AND_ITEMS:
               if (e instanceof PlayerInteractEvent) {
                  if (this.entityType != null) {
                     return null;
                  }

                  Block block = ((PlayerInteractEvent)e).getClickedBlock();
                  if (this.itemType == null) {
                     return new Block[]{block};
                  }

                  assert this.itemType != null;

                  if (this.itemType.isOfType(block)) {
                     return new Block[]{block};
                  }

                  return null;
               }

               if (e instanceof PlayerInteractEntityEvent) {
                  if (this.entityType == null) {
                     return null;
                  }

                  Entity entity = ((PlayerInteractEntityEvent)e).getRightClicked();

                  assert this.entityType != null;

                  if (this.entityType.isInstance(entity)) {
                     assert this.entityType != null;

                     Entity[] one = (Entity[])Array.newInstance(this.entityType.getType(), 1);
                     one[0] = entity;
                     return one;
                  }

                  return null;
               }
               break;
            case SLOT:
               Inventory invi = ((InventoryClickEvent)e).getClickedInventory();
               InventoryClickEvent event = (InventoryClickEvent)e;
               if (invi != null) {
                  return CollectionUtils.array(new InventorySlot(invi, event.getSlot(), event.getRawSlot()));
               }
               break;
            case INVENTORY:
               return new Inventory[]{((InventoryClickEvent)e).getClickedInventory()};
            case TYPE:
               return new ClickType[]{((InventoryClickEvent)e).getClick()};
            case ACTION:
               return new InventoryAction[]{((InventoryClickEvent)e).getAction()};
         }

         return null;
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the "
         + (
            this.clickable != ExprClicked.ClickableType.BLOCK_AND_ITEMS
               ? this.clickable.getName()
               : "clicked " + (this.entityType != null ? this.entityType : (this.itemType != null ? this.itemType : "block"))
         );
   }

   static {
      Skript.registerExpression(
         ExprClicked.class,
         Object.class,
         ExpressionType.SIMPLE,
         "[the] ("
            + ExprClicked.ClickableType.ENCHANT_BUTTON.getSyntax(false)
            + ExprClicked.ClickableType.BLOCK_AND_ITEMS.getSyntax(false)
            + ExprClicked.ClickableType.SLOT.getSyntax(false)
            + ExprClicked.ClickableType.INVENTORY.getSyntax(false)
            + ExprClicked.ClickableType.TYPE.getSyntax(false)
            + ExprClicked.ClickableType.ACTION.getSyntax(true)
            + ")"
      );
   }

   private static enum ClickableType {
      ENCHANT_BUTTON(1, Number.class, "clicked enchantment button", "clicked [enchant[ment]] (button|option)"),
      BLOCK_AND_ITEMS(2, Block.class, "clicked block/itemtype/entity", "clicked (block|%-*itemtype/entitydata%)"),
      SLOT(3, Slot.class, "clicked slot", "clicked slot"),
      INVENTORY(4, Inventory.class, "clicked inventory", "clicked inventory"),
      TYPE(5, ClickType.class, "click type", "click (type|action)"),
      ACTION(6, InventoryAction.class, "inventory action", "inventory action");

      private String name;
      private String syntax;
      private Class<?> c;
      private int value;

      private ClickableType(int value, Class<?> c, String name, String syntax) {
         this.syntax = syntax;
         this.value = value;
         this.c = c;
         this.name = name;
      }

      public int getValue() {
         return this.value;
      }

      public Class<?> getClickableClass() {
         return this.c;
      }

      public String getName() {
         return this.name;
      }

      public String getSyntax(boolean last) {
         return this.value + "¦" + this.syntax + (!last ? "|" : "");
      }

      public static ExprClicked.ClickableType getClickable(int num) {
         for (ExprClicked.ClickableType clickable : values()) {
            if (clickable.getValue() == num) {
               return clickable;
            }
         }

         return BLOCK_AND_ITEMS;
      }
   }
}
