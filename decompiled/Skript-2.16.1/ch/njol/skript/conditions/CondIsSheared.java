package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import io.papermc.paper.entity.Shearable;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

@Name("Entity Is Sheared")
@Description({"Checks whether entities are sheared."})
@Example("if targeted entity of player is sheared:\n\tsend \"This entity has nothing left to shear!\" to player\n")
@Since({"2.8.0"})
public class CondIsSheared extends PropertyCondition<LivingEntity> {
   private static final boolean INTERFACE_METHOD = Skript.classExists("io.papermc.paper.entity.Shearable");

   public boolean check(LivingEntity entity) {
      if (entity instanceof Cow) {
         return entity.getEntitySpawnReason() == SpawnReason.SHEARED;
      } else if (INTERFACE_METHOD) {
         return entity instanceof Shearable shearable ? !shearable.readyToBeSheared() : false;
      } else if (entity instanceof Sheep sheep) {
         return sheep.isSheared();
      } else {
         return entity instanceof Snowman snowman ? snowman.isDerp() : false;
      }
   }

   @Override
   protected String getPropertyName() {
      return "sheared";
   }

   static {
      register(CondIsSheared.class, "(sheared|shorn)", "livingentities");
   }
}
