package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Is Full")
@Description({"Checks to see if the an entity block storage (i.e beehive) is full."})
@Example("if the entity storage of {_beehive} is full:\n\trelease the entity storage of {_beehive}\n")
@Since({"2.11"})
public class CondEntityStorageIsFull extends Condition {
   private Expression<Block> blocks;

   @Override
   public boolean init(Expression<?>[] exrps, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setNegated(matchedPattern >= 2);
      this.blocks = (Expression<Block>)exrps[0];
      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.blocks
         .check(event, block -> block.getState() instanceof EntityBlockStorage<?> blockStorage ? blockStorage.isFull() : false, this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("the entity storage of", this.blocks);
      if (this.blocks.isSingle()) {
         builder.append("is");
      } else {
         builder.append("are");
      }

      if (this.isNegated()) {
         builder.append("not");
      }

      builder.append("full");
      return builder.toString();
   }

   static {
      Skript.registerCondition(
         CondEntityStorageIsFull.class,
         Condition.ConditionType.PROPERTY,
         "[the] entity storage of %blocks% (is|are) full",
         "%blocks%'[s] entity storage (is|are) full",
         "[the] entity storage of %blocks% (isn't|is not|aren't|are not) full",
         "%blocks%'[s] entity storage (isn't|is not|aren't|are not) full"
      );
   }
}
