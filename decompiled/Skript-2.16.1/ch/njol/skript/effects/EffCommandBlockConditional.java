package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Conditional / Unconditional")
@Description({"Sets whether the provided command blocks are conditional or not."})
@Example.Examples({@Example("make command block {_block} conditional"), @Example("make command block {_block} unconditional if {_block} is conditional")})
@Since({"2.10"})
public class EffCommandBlockConditional extends Effect {
   private Expression<Block> blocks;
   private boolean conditional;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.blocks = (Expression<Block>)exprs[0];
      this.conditional = !parseResult.hasTag("not");
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (Block block : this.blocks.getArray(event)) {
         if (block.getBlockData() instanceof CommandBlock cmdBlock) {
            cmdBlock.setConditional(this.conditional);
            block.setBlockData(cmdBlock);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "make command block " + this.blocks.toString(event, debug) + (this.conditional ? " " : " un") + "conditional";
   }

   static {
      Skript.registerEffect(EffCommandBlockConditional.class, "make command block[s] %blocks% [not:(un|not )]conditional");
   }
}
