package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Items")
@Description({"Items or blocks of a specific type, useful for looping."})
@Example.Examples({@Example("loop tag values of tag \"diamond_ores\" and tag values of tag \"oak_logs\":\n\tblock contains loop-item\n\tmessage \"Theres at least one %loop-item% in this block\"\n"), @Example("drop all blocks at the player # drops one of every block at the player")})
@Since({"1.0 pre-5"})
public class ExprItems extends SimpleExpression<ItemType> {
   private static final ItemType[] ALL_BLOCKS = Arrays.stream(Material.values())
      .filter(material -> !material.isLegacy() && material.isBlock())
      .map(ItemType::new)
      .toArray(ItemType[]::new);
   @Nullable
   private Expression<ItemType> itemTypeExpr;
   private boolean items;
   private ItemType[] buffer = null;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.items = matchedPattern == 3;
      this.itemTypeExpr = (Expression<ItemType>)(matchedPattern != 0 && matchedPattern != 1 ? exprs[0] : null);
      if (this.itemTypeExpr instanceof Literal) {
         for (ItemType itemType : ((Literal)this.itemTypeExpr).getAll()) {
            itemType.setAll(true);
         }
      }

      return true;
   }

   @Nullable
   protected ItemType[] get(Event event) {
      if (this.buffer != null) {
         return this.buffer;
      } else {
         List<ItemType> items = new ArrayList<>();
         this.iterator(event).forEachRemaining(items::add);
         ItemType[] itemTypes = items.toArray(new ItemType[0]);
         if (this.itemTypeExpr instanceof Literal) {
            this.buffer = itemTypes;
         }

         return itemTypes;
      }
   }

   @Nullable
   @Override
   public Iterator<ItemType> iterator(Event event) {
      if (!this.items && this.itemTypeExpr == null) {
         return Arrays.stream(ALL_BLOCKS).map(ItemType::clone).iterator();
      } else {
         Iterable<ItemStack> itemStackIterable = Iterables.concat(this.itemTypeExpr.stream(event).map(ItemType::getAll).toArray(Iterable[]::new));
         return this.items
            ? StreamSupport.stream(itemStackIterable.spliterator(), false).map(ItemType::new).iterator()
            : StreamSupport.stream(itemStackIterable.spliterator(), false).filter(itemStack -> itemStack.getType().isBlock()).map(ItemType::new).iterator();
      }
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "all of the " + (this.items ? "items" : "blocks") + (this.itemTypeExpr != null ? " of type " + this.itemTypeExpr.toString(event, debug) : "");
   }

   @Override
   public boolean isLoopOf(String input) {
      return this.items ? input.equals("item") : input.equals("block");
   }

   static {
      Skript.registerExpression(
         ExprItems.class,
         ItemType.class,
         ExpressionType.COMBINED,
         "[all [[of] the]|the] block[[ ]type]s",
         "every block[[ ]type]",
         "[all [[of] the]|the|every] block[s] of type[s] %itemtypes%",
         "[all [[of] the]|the|every] item[s] of type[s] %itemtypes%"
      );
   }
}
