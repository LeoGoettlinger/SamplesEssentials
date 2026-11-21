package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("View Distance")
@Description({"The view distance of a world or a player.", "The view distance of a player is the distance in chunks sent by the server to the player. This has nothing to do with client side view distance settings.", "View distance is capped between 2 to 32 chunks."})
@Example.Examples({@Example("set view distance of player to 10"), @Example("add 50 to the view distance of world \"world\""), @Example("reset the view distance of player"), @Example("clear the view distance of world \"world\"")})
@Since({"2.4, 2.11 (worlds)"})
public class ExprViewDistance extends SimplePropertyExpression<Object, Integer> {
   private static final boolean SUPPORTS_SETTER = Skript.methodExists(Player.class, "setViewDistance", int.class);
   private static final boolean RUNNING_1_21 = Skript.isRunningMinecraft(1, 21, 0);

   @Nullable
   public Integer convert(Object object) {
      if (object instanceof Player player) {
         return player.getViewDistance();
      } else {
         return object instanceof World world ? world.getViewDistance() : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (SUPPORTS_SETTER) {
         return switch (mode) {
            case SET, DELETE, RESET, ADD, REMOVE -> (Class[])CollectionUtils.array(Integer.class);
            default -> null;
         };
      } else {
         Skript.error("'view distance' requires a Paper server to change.");
         return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int value = 2;
      if (mode == Changer.ChangeMode.RESET) {
         value = Bukkit.getViewDistance();
      } else if (delta != null) {
         value = (Integer)delta[0];
         if (mode == Changer.ChangeMode.REMOVE) {
            value = -value;
         }
      }

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof Player player) {
            this.changeViewDistance(mode, value, player::getViewDistance, player::setViewDistance);
         } else if (RUNNING_1_21 && object instanceof World world) {
            this.changeViewDistance(mode, value, world::getViewDistance, world::setViewDistance);
         }
      }
   }

   private void changeViewDistance(Changer.ChangeMode mode, int value, Supplier<Integer> getter, Consumer<Integer> setter) {
      setter.accept(Math2.fit(2, switch (mode) {
         case SET, DELETE, RESET -> value;
         case ADD, REMOVE -> getter.get() + value;
         default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
      }, 32));
   }

   @Override
   public Class<Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return "view distance";
   }

   static {
      register(ExprViewDistance.class, Integer.class, "view distance[s]", "players/worlds");
   }
}
