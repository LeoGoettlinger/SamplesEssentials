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
import java.util.Iterator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.jetbrains.annotations.Nullable;

@Name("Affected Entities")
@Description({"The affected entities in the <a href='#aoe_cloud_effect'>area cloud effect</a> event."})
@Example("on area cloud effect:\n\tloop affected entities:\n\t\tif loop-value is a player:\n\t\t\tsend \"WARNING: you've step on an area effect cloud!\" to loop-value\n")
@Since({"2.4"})
public class ExprAffectedEntities extends SimpleExpression<LivingEntity> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(AreaEffectCloudApplyEvent.class);
   }

   @Nullable
   protected LivingEntity[] get(Event event) {
      return event instanceof AreaEffectCloudApplyEvent areaEvent ? areaEvent.getAffectedEntities().toArray(new LivingEntity[0]) : null;
   }

   @Nullable
   @Override
   public Iterator<? extends LivingEntity> iterator(Event event) {
      return event instanceof AreaEffectCloudApplyEvent areaEvent ? areaEvent.getAffectedEntities().iterator() : super.iterator(event);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, SET, DELETE, REMOVE -> (Class[])CollectionUtils.array(LivingEntity[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof AreaEffectCloudApplyEvent areaEvent) {
         switch (mode) {
            case SET:
               areaEvent.getAffectedEntities().clear();
            case ADD:
               for (Object entity : delta) {
                  areaEvent.getAffectedEntities().add((LivingEntity)entity);
               }
               break;
            case DELETE:
            case RESET:
               areaEvent.getAffectedEntities().clear();
               break;
            case REMOVE:
               for (Object entity : delta) {
                  areaEvent.getAffectedEntities().remove((LivingEntity)entity);
               }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public boolean isDefault() {
      return true;
   }

   @Override
   public Class<? extends LivingEntity> getReturnType() {
      return LivingEntity.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the affected entities";
   }

   static {
      Skript.registerExpression(ExprAffectedEntities.class, LivingEntity.class, ExpressionType.SIMPLE, "[the] affected entities");
   }
}
