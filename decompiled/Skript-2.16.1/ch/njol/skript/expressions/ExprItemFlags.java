package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Item Flags")
@Description({"The item flags of an item. Can be modified."})
@Example.Examples({@Example("set item flags of player's tool to hide enchants and hide attributes"), @Example("add hide potion effects to item flags of player's held item"), @Example("remove hide enchants from item flags of {legendary sword}")})
@Since({"2.10"})
public class ExprItemFlags extends PropertyExpression<ItemType, ItemFlag> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends ItemType>)exprs[0]);
      return true;
   }

   protected ItemFlag[] get(Event event, ItemType[] source) {
      Set<ItemFlag> flags = new HashSet<>();

      for (ItemType itemType : source) {
         ItemMeta meta = itemType.getItemMeta();
         flags.addAll(meta.getItemFlags());
      }

      return flags.toArray(new ItemFlag[0]);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, RESET, DELETE -> (Class[])CollectionUtils.array(ItemFlag[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ItemFlag[] flags = delta != null ? (ItemFlag[])delta : new ItemFlag[0];

      for (ItemType itemType : this.getExpr().getArray(event)) {
         ItemMeta meta = itemType.getItemMeta();
         switch (mode) {
            case SET:
               meta.removeItemFlags(ItemFlag.values());
               meta.addItemFlags(flags);
               break;
            case ADD:
               meta.addItemFlags(flags);
               break;
            case REMOVE:
               meta.removeItemFlags(flags);
               break;
            case RESET:
            case DELETE:
               meta.removeItemFlags(ItemFlag.values());
               break;
            default:
               return;
         }

         itemType.setItemMeta(meta);
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends ItemFlag> getReturnType() {
      return ItemFlag.class;
   }

   @Override
   public String toString(Event event, boolean debug) {
      return "item flags of " + this.getExpr().toString(event, debug);
   }

   static {
      register(ExprItemFlags.class, ItemFlag.class, "item flags", "itemtypes");
   }
}
