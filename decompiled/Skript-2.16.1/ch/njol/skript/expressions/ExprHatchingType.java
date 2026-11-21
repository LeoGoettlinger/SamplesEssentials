package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.jetbrains.annotations.Nullable;

@Name("Hatching Entity Type")
@Description({"The type of the entity that will be hatched in a Player Egg Throw event."})
@Example("on player egg throw:\n\tset the hatching entity type to a primed tnt\n")
@Events({"Egg Throw"})
@Since({"2.7"})
public class ExprHatchingType extends SimpleExpression<EntityData<?>> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(PlayerEggThrowEvent.class);
   }

   @Nullable
   protected EntityData<?>[] get(Event event) {
      return !(event instanceof PlayerEggThrowEvent)
         ? new EntityData[0]
         : new EntityData[]{EntityUtils.toSkriptEntityData(((PlayerEggThrowEvent)event).getHatchingType())};
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : CollectionUtils.array(EntityData.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PlayerEggThrowEvent) {
         EntityType entityType = delta != null ? EntityUtils.toBukkitEntityType((EntityData<?>)delta[0]) : EntityType.CHICKEN;
         if (entityType.isSpawnable()) {
            ((PlayerEggThrowEvent)event).setHatchingType(entityType);
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends EntityData<?>> getReturnType() {
      return EntityData.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the hatching entity type";
   }

   static {
      Skript.registerExpression(ExprHatchingType.class, EntityData.class, ExpressionType.SIMPLE, "[the] hatching entity [type]");
   }
}
