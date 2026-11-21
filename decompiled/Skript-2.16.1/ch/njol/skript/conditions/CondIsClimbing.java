package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Is Climbing")
@Description({"Whether a living entity is climbing, such as a spider up a wall or a player on a ladder."})
@Example("spawn a spider at location of spawn\nwait a second\nif the last spawned spider is climbing:\n\tmessage \"The spider is now climbing!\"\n")
@RequiredPlugins({"Minecraft 1.17+"})
@Since({"2.8.0"})
public class CondIsClimbing extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity.isClimbing();
   }

   @Override
   protected String getPropertyName() {
      return "climbing";
   }

   static {
      register(CondIsClimbing.class, "climbing", "livingentities");
   }
}
