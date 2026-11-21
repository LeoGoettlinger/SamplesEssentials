package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Player;

@Name("Can Fly")
@Description({"Whether a player is allowed to fly."})
@Example("player can fly")
@Since({"2.3"})
public class CondCanFly extends PropertyCondition<Player> {
   public boolean check(Player player) {
      return player.getAllowFlight();
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.CAN;
   }

   @Override
   protected String getPropertyName() {
      return "fly";
   }

   static {
      register(CondCanFly.class, PropertyCondition.PropertyType.CAN, "fly", "players");
   }
}
