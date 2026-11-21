package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
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
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.jetbrains.annotations.Nullable;

@Name("Hanging Entity/Remover")
@Description({"Returns the hanging entity or remover in hanging <a href='#break_mine'>break</a> and <a href='#place'>place</a> events."})
@Example("on break of item frame:\n\tif item of hanging entity is diamond pickaxe:\n\t\tcancel event\n\t\tif hanging remover is a player:\n\t\t\tsend \"You can't break that item frame!\" to hanging remover\n")
@Since({"2.6.2"})
public class ExprHanging extends SimpleExpression<Entity> implements EventRestrictedSyntax {
   private boolean isRemover;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isRemover = parseResult.hasTag("remover");
      if (this.isRemover && !this.getParser().isCurrentEvent(HangingBreakEvent.class)) {
         Skript.error("The expression 'hanging remover' can only be used in break event");
         return false;
      } else {
         return true;
      }
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(HangingBreakEvent.class, HangingPlaceEvent.class);
   }

   @Nullable
   public Entity[] get(Event e) {
      if (!(e instanceof HangingEvent)) {
         return null;
      } else {
         Entity entity = null;
         if (!this.isRemover) {
            entity = ((HangingEvent)e).getEntity();
         } else if (e instanceof HangingBreakByEntityEvent) {
            entity = ((HangingBreakByEntityEvent)e).getRemover();
         }

         return new Entity[]{entity};
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "hanging " + (this.isRemover ? "remover" : "entity");
   }

   static {
      Skript.registerExpression(ExprHanging.class, Entity.class, ExpressionType.SIMPLE, "[the] hanging (entity|:remover)");
   }
}
