package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Buried Item")
@Description({"Represents the item that is uncovered when dusting.", "The only blocks that can currently be \"dusted\" are Suspicious Gravel and Suspicious Sand."})
@Example.Examples({@Example("send target block's brushable item"), @Example("set {_gravel}'s brushable item to emerald")})
@Since({"2.12"})
@RequiredPlugins({"Minecraft 1.20+"})
public class ExprBrushableItem extends SimplePropertyExpression<Block, ItemStack> {
   private static final boolean SUPPORTS_DUSTING = Skript.classExists("org.bukkit.block.BrushableBlock");

   @Nullable
   public ItemStack convert(Block block) {
      return block.getState() instanceof BrushableBlock brushableBlock ? brushableBlock.getItem() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.DELETE ? null : CollectionUtils.array(ItemStack.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE) {
         ItemStack newItem = mode == Changer.ChangeMode.SET ? (ItemStack)delta[0] : null;

         for (Block block : this.getExpr().getArray(event)) {
            BlockState state = block.getState();
            if (state instanceof BrushableBlock brushableBlock) {
               brushableBlock.setItem(newItem);
               state.update();
            }
         }
      }
   }

   @Override
   public Class<? extends ItemStack> getReturnType() {
      return ItemStack.class;
   }

   @Override
   protected String getPropertyName() {
      return "brushable item";
   }

   static {
      if (SUPPORTS_DUSTING) {
         register(ExprBrushableItem.class, ItemStack.class, "(brushable|buried) item", "blocks");
      }
   }
}
