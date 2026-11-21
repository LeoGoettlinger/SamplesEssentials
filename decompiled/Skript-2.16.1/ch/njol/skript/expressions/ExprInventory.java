package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Inventory")
@Description({"The inventory of a block or player. You can usually omit this expression and can directly add or remove items to/from blocks or players."})
@Example.Examples({@Example("add a plank to the player's inventory"), @Example("clear the player's inventory"), @Example("remove 5 wool from the inventory of the clicked block")})
@Since({"1.0"})
public class ExprInventory extends SimpleExpression<Object> {
   private boolean inLoop;
   private Expression<?> holders;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (exprs[0].getSource() instanceof ExprItemsIn) {
         return false;
      } else {
         Node n = SkriptLogger.getNode();
         this.inLoop = n != null && ("loop " + parseResult.expr).equals(n.getKey());
         this.holders = exprs[0];
         return true;
      }
   }

   @Override
   protected Object[] get(Event e) {
      List<Inventory> inventories = new ArrayList<>();

      for (final Object holder : this.holders.getArray(e)) {
         if (holder instanceof InventoryHolder) {
            inventories.add(((InventoryHolder)holder).getInventory());
         } else if (holder instanceof ItemType) {
            final ItemMeta meta = ((ItemType)holder).getItemMeta();
            if (meta instanceof BlockStateMeta) {
               final BlockState state = ((BlockStateMeta)meta).getBlockState();
               if (state instanceof Container) {
                  final Inventory underlyingInv = ((Container)state).getInventory();
                  Inventory proxy = (Inventory)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Inventory.class}, new InvocationHandler() {
                     @Override
                     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object returnValue = method.invoke(underlyingInv, args);
                        boolean updateSucceeded = state.update();
                        if (updateSucceeded) {
                           ((BlockStateMeta)meta).setBlockState(state);
                           ((ItemType)holder).setItemMeta(meta);
                        }

                        return returnValue;
                     }
                  });
                  inventories.add(proxy);
               }
            }
         }
      }

      final Inventory[] invArray = inventories.toArray(new Inventory[0]);
      if (this.inLoop) {
         ExprItemsIn expr = new ExprItemsIn();
         expr.init(new Expression[]{new SimpleExpression() {
            @Override
            protected Object[] get(Event e) {
               return invArray;
            }

            @Override
            public boolean isSingle() {
               return invArray.length == 1;
            }

            @Override
            public Class<?> getReturnType() {
               return Inventory.class;
            }

            @Override
            public String toString(@Nullable Event e, boolean debug) {
               return "loop of inventory expression";
            }

            @Override
            public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
               return true;
            }
         }}, 0, Kleenean.FALSE, null);
         return expr.get(e);
      } else {
         return invArray;
      }
   }

   @Override
   public boolean isSingle() {
      return !this.inLoop && this.holders.isSingle();
   }

   @Override
   public Class<?> getReturnType() {
      return this.inLoop ? Slot.class : Inventory.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "inventor" + (this.holders.isSingle() ? "y" : "ies") + " of " + this.holders.toString(e, debug);
   }

   static {
      PropertyExpression.register(ExprInventory.class, Object.class, "inventor(y|ies)", "inventoryholders/itemtypes");
   }
}
