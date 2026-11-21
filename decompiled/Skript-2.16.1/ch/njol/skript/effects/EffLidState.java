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
import org.bukkit.block.Lidded;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Open/Close Lid")
@Description({"Open or close the lid of the block(s)."})
@Example.Examples({@Example("open the lid of {_chest}"), @Example("close the lid of {_blocks::*}")})
@Since({"2.10"})
public class EffLidState extends Effect {
   private boolean setOpen;
   private Expression<Block> blocks;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setOpen = !parseResult.hasTag("close");
      this.blocks = (Expression<Block>)exprs[0];
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (Block block : this.blocks.getArray(event)) {
         if (block.getState() instanceof Lidded lidded) {
            if (this.setOpen) {
               lidded.open();
            } else {
               lidded.close();
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.setOpen ? "open" : "close") + " lid of " + this.blocks.toString(event, debug);
   }

   static {
      Skript.registerEffect(EffLidState.class, "(open|:close) [the] lid[s] (of|for) %blocks%", "(open|:close) %blocks%'[s] lid[s]");
   }
}
