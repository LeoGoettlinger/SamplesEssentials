package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Compass Target")
@Description({"The location a player's compass is pointing at.", "As of Minecraft 1.21.4, the compass is controlled by the resource pack and by default will not point to this compass target when used outside of the overworld dimension."})
@Example("# make all player's compasses target a player stored in {compass::target::%player%}\nevery 5 seconds:\n\tloop all players:\n\t\tset the loop-player's compass target to location of {compass::target::%%loop-player%}\n")
@Since({"2.0"})
public class ExprCompassTarget extends SimplePropertyExpression<Player, Location> {
   @Nullable
   public Location convert(Player p) {
      return p.getCompassTarget();
   }

   @Override
   public Class<Location> getReturnType() {
      return Location.class;
   }

   @Override
   protected String getPropertyName() {
      return "compass target";
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : new Class[]{Location.class};
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      for (Player p : this.getExpr().getArray(e)) {
         p.setCompassTarget(delta == null ? p.getWorld().getSpawnLocation() : (Location)delta[0]);
      }
   }

   static {
      register(ExprCompassTarget.class, Location.class, "compass target", "players");
   }
}
