package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Player;

@Name("Is Blocking")
@Description({"Checks whether a player is blocking with their shield."})
@Example("on damage of player:\n\tvictim is blocking\n\tdamage attacker by 0.5 hearts\n")
@Since({"unknown (before 2.1)"})
public class CondIsBlocking extends PropertyCondition<Player> {
   public boolean check(Player player) {
      return player.isBlocking();
   }

   @Override
   protected String getPropertyName() {
      return "blocking";
   }

   static {
      register(CondIsBlocking.class, "(blocking|defending) [with [a] shield]", "players");
   }
}
