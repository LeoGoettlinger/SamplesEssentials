package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Last Death Location")
@Description({"Gets the last death location of a player, or offline player, if available.", "Can also be set, reset, and deleted if the player is online."})
@Example.Examples({@Example("set {_loc} to the last death location of player"), @Example("teleport player to last death location of (random element out of all players)")})
@Since({"2.10"})
public class ExprLastDeathLocation extends SimplePropertyExpression<OfflinePlayer, Location> {
   @Nullable
   public Location convert(OfflinePlayer offlinePlayer) {
      return offlinePlayer instanceof Player player ? player.getLastDeathLocation() : offlinePlayer.getLastDeathLocation();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, RESET, DELETE -> (Class[])CollectionUtils.array(Location.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Location loc = delta != null && delta[0] instanceof Location location ? location : null;

      for (OfflinePlayer offlinePlayer : this.getExpr().getArray(event)) {
         if (offlinePlayer instanceof Player player) {
            player.setLastDeathLocation(loc);
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return "last death location";
   }

   @Override
   public Class<? extends Location> getReturnType() {
      return Location.class;
   }

   static {
      register(ExprLastDeathLocation.class, Location.class, "[last] death location[s]", "offlineplayers");
   }
}
