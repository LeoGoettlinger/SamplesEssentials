package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;

@Name("Beehive Is Sedated")
@Description({"Checks if a beehive is sedated from a nearby campfire."})
@Example("if {_beehive} is sedated:")
@Since({"2.11"})
public class CondIsSedated extends PropertyCondition<Block> {
   public boolean check(Block block) {
      return block.getState() instanceof Beehive beehive ? beehive.isSedated() : false;
   }

   @Override
   protected String getPropertyName() {
      return "sedated";
   }

   static {
      PropertyCondition.register(CondIsSedated.class, PropertyCondition.PropertyType.BE, "sedated", "blocks");
   }
}
