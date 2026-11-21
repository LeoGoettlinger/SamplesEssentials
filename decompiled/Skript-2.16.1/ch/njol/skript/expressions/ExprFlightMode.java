package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Flight Mode")
@Description({"Whether the player(s) are allowed to fly. Use <a href=#EffMakeFly>Make Fly</a> effect to force player(s) to fly."})
@Example.Examples({@Example("set flight mode of player to true"), @Example("send \"%flying state of all players%\"")})
@Since({"2.2-dev34"})
public class ExprFlightMode extends SimplePropertyExpression<Player, Boolean> {
   public Boolean convert(Player player) {
      return player.getAllowFlight();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : CollectionUtils.array(Boolean.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      boolean state = mode != Changer.ChangeMode.RESET && delta != null && (Boolean)delta[0];

      for (Player player : this.getExpr().getArray(event)) {
         player.setAllowFlight(state);
      }
   }

   @Override
   protected String getPropertyName() {
      return "flight mode";
   }

   @Override
   public Class<Boolean> getReturnType() {
      return Boolean.class;
   }

   static {
      register(ExprFlightMode.class, Boolean.class, "fl(y[ing]|ight) (mode|state)", "players");
   }
}
