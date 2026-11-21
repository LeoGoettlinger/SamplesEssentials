package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;

@Name("Redstone Block Power")
@Description({"Power of a redstone block"})
@Example("if redstone power of targeted block is 15:\n\tsend \"This block is very powerful!\"\n")
@Since({"2.5"})
public class ExprRedstoneBlockPower extends SimplePropertyExpression<Block, Long> {
   public Long convert(Block b) {
      return (long)b.getBlockPower();
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   protected String getPropertyName() {
      return "redstone power";
   }

   static {
      register(ExprRedstoneBlockPower.class, Long.class, "redstone power", "blocks");
   }
}
