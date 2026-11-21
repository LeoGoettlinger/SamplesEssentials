package org.skriptlang.skript.bukkit.worldborder.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("World Border")
@Description({"Get the border of a world or a player.", "A player's world border is not persistent. Restarts, quitting, death or changing worlds will reset the border."})
@Example("set {_border} to world border of player's world")
@Since({"2.11"})
public class ExprWorldBorder extends SimplePropertyExpression<Object, WorldBorder> {
   private static final boolean USE_DEPRECATED = !Skript.methodExists(WorldBorder.class, "getWarningTimeTicks");

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprWorldBorder.class, WorldBorder.class, "world[ ]border", "worlds/players", true)
               .supplier(ExprWorldBorder::new))
            .build()
      );
   }

   @Nullable
   public WorldBorder convert(Object object) {
      if (object instanceof World world) {
         return world.getWorldBorder();
      } else {
         return object instanceof Player player ? player.getWorldBorder() : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : CollectionUtils.array(WorldBorder.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Object[] objects = this.getExpr().getArray(event);
      if (mode == Changer.ChangeMode.RESET) {
         for (Object object : objects) {
            if (object instanceof World world) {
               world.getWorldBorder().reset();
            } else if (object instanceof Player player) {
               player.setWorldBorder(null);
            }
         }
      } else {
         WorldBorder to = (WorldBorder)delta[0];

         assert to != null;

         for (Object objectx : objects) {
            if (objectx instanceof World world) {
               WorldBorder worldBorder = world.getWorldBorder();
               worldBorder.setCenter(to.getCenter());
               worldBorder.setSize(to.getSize());
               worldBorder.setDamageAmount(to.getDamageAmount());
               worldBorder.setDamageBuffer(to.getDamageBuffer());
               worldBorder.setWarningDistance(to.getWarningDistance());
               if (USE_DEPRECATED) {
                  worldBorder.setWarningTime(to.getWarningTime());
               } else {
                  worldBorder.setWarningTimeTicks(to.getWarningTimeTicks());
               }
            } else if (objectx instanceof Player player) {
               player.setWorldBorder(to);
            }
         }
      }
   }

   @Override
   public Class<? extends WorldBorder> getReturnType() {
      return WorldBorder.class;
   }

   @Override
   protected String getPropertyName() {
      return "world border";
   }
}
