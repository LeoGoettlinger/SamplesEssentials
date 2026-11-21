package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Player;

@Name("Has Client Weather")
@Description({"Checks whether the given players have a custom client weather"})
@Example("if the player has custom weather:\n\tmessage \"Your custom weather is %player's weather%\"\n")
@Since({"2.3"})
public class CondHasClientWeather extends PropertyCondition<Player> {
   public boolean check(Player player) {
      return player.getPlayerWeather() != null;
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.HAVE;
   }

   @Override
   protected String getPropertyName() {
      return "custom weather set";
   }

   static {
      register(CondHasClientWeather.class, PropertyCondition.PropertyType.HAVE, "[a] (client|custom) weather [set]", "players");
   }
}
