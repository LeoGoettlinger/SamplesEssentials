package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.lang.invoke.StringConcatFactory;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Creature/Entity/Player/Projectile/Villager/Powered Creeper/etc.")
@Description({"The entity involved in an event (an entity is a player, a creature or an inanimate object like ignited TNT, a dropped item or an arrow).", "You can use the specific type of the entity that's involved in the event, e.g. in a 'death of a creeper' event you can use 'the creeper' instead of 'the entity'."})
@Example.Examples({@Example("give a diamond sword of sharpness 3 to the player"), @Example("kill the creeper"), @Example("kill all powered creepers in the wolf's world"), @Example("projectile is an arrow")})
@Since({"1.0"})
public class ExprEntity extends SimpleExpression<Entity> {
   private EntityData<?> type;
   private EventValueExpression<Entity> entity;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      RetainingLogHandler log = SkriptLogger.startRetainingLog();

      try {
         if (!StringUtils.startsWithIgnoreCase(parseResult.expr, "the ") && !StringUtils.startsWithIgnoreCase(parseResult.expr, "event-")) {
            String s = parseResult.regexes.get(0).group();
            ItemType item = Aliases.parseItemType(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s));
            log.clear();
            if (item != null) {
               log.printLog();
               return false;
            }
         }

         EntityData<?> type = EntityData.parseWithoutIndefiniteArticle(
            StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(parseResult.regexes.get(0).group())
         );
         log.clear();
         log.printLog();
         if (type == null || type.isPlural().isTrue()) {
            return false;
         }

         this.type = type;
      } finally {
         log.stop();
      }

      this.entity = new EventValueExpression<>((Class<? extends Entity>)this.type.getType());
      return this.entity.init();
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return (Class<? extends Entity>)this.type.getType();
   }

   @Nullable
   protected Entity[] get(Event e) {
      Entity[] es = this.entity.getArray(e);
      return es.length != 0 && !this.type.isInstance(es[0]) ? null : es;
   }

   @Nullable
   @Override
   public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      for (Class<R> t : to) {
         if (t.equals(EntityData.class)) {
            return new SimpleLiteral<>((R)this.type, false);
         }
      }

      return super.getConvertedExpression(to);
   }

   @Override
   public boolean setTime(int time) {
      return this.entity.setTime(time);
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the " + this.type;
   }

   static {
      Skript.registerExpression(ExprEntity.class, Entity.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "[the] [event-]<.+>");
   }
}
