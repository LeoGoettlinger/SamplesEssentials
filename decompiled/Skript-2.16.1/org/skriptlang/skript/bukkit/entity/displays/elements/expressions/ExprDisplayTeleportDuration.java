package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Teleport Duration")
@Description({"The teleport duration of displays is the amount of time it takes to get between locations.", "0 means that updates are applied immediately.", "1 means that the display entity will move from current position to the updated one over one tick.", "Higher values spread the movement over multiple ticks. Max of 59 ticks."})
@Example("set teleport duration of the last spawned text display to 2 ticks\nteleport last spawned text display to {_location}\nwait 2 ticks\nmessage \"display entity has arrived at %{_location}%\"\n")
@RequiredPlugins({"Spigot 1.20.4+"})
@Since({"2.10"})
public class ExprDisplayTeleportDuration extends SimplePropertyExpression<Display, Timespan> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprDisplayTeleportDuration.class, Timespan.class, "teleport[ation] duration[s]", "displays", true)
               .supplier(ExprDisplayTeleportDuration::new))
            .build()
      );
   }

   @Nullable
   public Timespan convert(Display display) {
      return new Timespan(Timespan.TimePeriod.TICK, display.getTeleportDuration());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET -> (Class[])CollectionUtils.array(Timespan.class);
         case RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Display[] displays = this.getExpr().getArray(event);
      long ticks = delta == null ? 0L : ((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK);
      switch (mode) {
         case REMOVE:
            ticks = -ticks;
         case ADD:
            for (Display display : displays) {
               int value = (int)Math2.fit(0L, display.getTeleportDuration() + ticks, 59L);
               display.setTeleportDuration(value);
            }
            break;
         case SET:
         case RESET:
            ticks = Math2.fit(0L, ticks, 59L);

            for (Display display : displays) {
               display.setTeleportDuration((int)ticks);
            }
      }
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "teleport duration";
   }
}
