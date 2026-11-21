package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;

@Name("Is Conditional")
@Description({"Checks whether a command block is conditional or not."})
@Example("if {_block} is conditional:\n\tmake {_block} unconditional\n")
@Since({"2.10"})
public class CondIsCommandBlockConditional extends PropertyCondition<Block> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Block>)exprs[0]);
      this.setNegated(parseResult.hasTag("un") ^ matchedPattern == 1);
      return true;
   }

   public boolean check(Block block) {
      return block.getBlockData() instanceof CommandBlock cmdBlock ? cmdBlock.isConditional() : false;
   }

   @Override
   protected String getPropertyName() {
      return "conditional";
   }

   static {
      register(CondIsCommandBlockConditional.class, "[:un]conditional", "blocks");
   }
}
