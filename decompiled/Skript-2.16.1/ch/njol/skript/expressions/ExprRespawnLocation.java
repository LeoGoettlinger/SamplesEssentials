package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

@Name("Respawn location")
@Description({"The location that a player should respawn at. This is used within the respawn event."})
@Example("on respawn:\n\tset respawn location to {example::spawn}\n")
@Since({"2.2-dev35"})
public class ExprRespawnLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(PlayerRespawnEvent.class);
   }

   @Nullable
   protected Location[] get(Event event) {
      return !(event instanceof PlayerRespawnEvent) ? null : CollectionUtils.array(((PlayerRespawnEvent)event).getRespawnLocation());
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Location> getReturnType() {
      return Location.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the respawn location " + (event != null ? ": " + ((PlayerRespawnEvent)event).getRespawnLocation() : "");
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Location.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PlayerRespawnEvent) {
         if (delta != null) {
            ((PlayerRespawnEvent)event).setRespawnLocation((Location)delta[0]);
         }
      }
   }

   static {
      Skript.registerExpression(ExprRespawnLocation.class, Location.class, ExpressionType.SIMPLE, "[the] respawn location");
   }
}
