package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Block Data")
@Description({"Get the <a href='#blockdata'>block data</a> associated with a block.", "This data can also be used to set blocks."})
@Example.Examples({@Example("set {_data} to block data of target block"), @Example("set block at player to {_data}"), @Example("set block data of target block to oak_stairs[facing=south;waterlogged=true]")})
@Since({"2.5, 2.5.2 (set), 2.10 (block displays)"})
public class ExprBlockData extends SimplePropertyExpression<Object, BlockData> {
   @Nullable
   public BlockData convert(Object object) {
      if (object instanceof Block block) {
         return block.getBlockData();
      } else if (object instanceof BlockDisplay blockDisplay) {
         return blockDisplay.getBlock();
      } else {
         return object instanceof FallingBlock fallingBlock ? fallingBlock.getBlockData() : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(BlockData.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      BlockData blockData = (BlockData)delta[0];

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof Block block) {
            block.setBlockData(blockData);
         } else if (object instanceof BlockDisplay blockDisplay) {
            blockDisplay.setBlock(blockData);
         } else if (object instanceof FallingBlock fallingBlock) {
            fallingBlock.setBlockData(blockData);
         }
      }
   }

   @Override
   public Class<? extends BlockData> getReturnType() {
      return BlockData.class;
   }

   @Override
   protected String getPropertyName() {
      return "block data";
   }

   static {
      register(ExprBlockData.class, BlockData.class, "block[ ]data", "blocks/displays/entities");
   }
}
