package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Warden;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Warden Anger Level")
@Description({"The anger level a warden feels towards an entity.", "A warden can be angry towards multiple entities with different anger levels.", "If an entity reaches an anger level of 80+, the warden will pursue it.", "Anger level maxes out at 150."})
@Example.Examples({@Example("set the anger level of last spawned warden towards player to 20"), @Example("clear the last spawned warden's anger level towards player")})
@Since({"2.11"})
public class ExprWardenEntityAnger extends SimpleExpression<Integer> {
   private Expression<LivingEntity> wardens;
   private Expression<LivingEntity> targets;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.wardens = (Expression<LivingEntity>)exprs[0];
      this.targets = (Expression<LivingEntity>)exprs[1];
      return true;
   }

   @Nullable
   protected Integer[] get(Event event) {
      List<Integer> list = new ArrayList<>();
      Entity[] entities = (Entity[])this.targets.getArray(event);

      for (LivingEntity livingEntity : this.wardens.getArray(event)) {
         if (livingEntity instanceof Warden warden) {
            for (Entity entity : entities) {
               list.add(warden.getAnger(entity));
            }
         }
      }

      return list.toArray(new Integer[0]);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE, ADD, REMOVE -> (Class[])CollectionUtils.array(Integer.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int value = delta != null ? (Integer)delta[0] : 0;

      BiConsumer<Warden, Entity> consumer = switch (mode) {
         case SET -> (wardenx, entityx) -> wardenx.setAnger(entityx, Math2.fit(0, value, 150));
         case DELETE -> Warden::clearAnger;
         case ADD -> (wardenx, entityx) -> {
            int current = wardenx.getAnger(entityx);
            int newValue = Math2.fit(0, current + value, 150);
            wardenx.setAnger(entityx, newValue);
         };
         case REMOVE -> (wardenx, entityx) -> {
            int current = wardenx.getAnger(entityx);
            int newValue = Math2.fit(0, current - value, 150);
            wardenx.setAnger(entityx, newValue);
         };
         default -> throw new IllegalStateException("Unexpected value: " + mode);
      };
      Entity[] entities = (Entity[])this.targets.getArray(event);

      for (LivingEntity livingEntity : this.wardens.getArray(event)) {
         if (livingEntity instanceof Warden warden) {
            for (Entity entity : entities) {
               consumer.accept(warden, entity);
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.wardens.isSingle() && this.targets.isSingle();
   }

   @Override
   public Class<Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the anger level of " + this.wardens.toString(event, debug) + " towards " + this.targets.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprWardenEntityAnger.class,
         Integer.class,
         ExpressionType.COMBINED,
         "[the] anger level [of] %livingentities% towards %livingentities%",
         "%livingentities%'[s] anger level towards %livingentities%"
      );
   }
}
