package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import java.util.function.Consumer;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Beehive Honey Level")
@Description({"The current or max honey level of a beehive.", "The max level is 5, which cannot be changed."})
@Example("set the honey level of {_beehive} to the max honey level of {_beehive}")
@Since({"2.11"})
public class ExprBeehiveHoneyLevel extends SimplePropertyExpression<Block, Integer> {
   private boolean isMax;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isMax = parseResult.hasTag("max");
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Integer convert(Block block) {
      if (block.getBlockData() instanceof Beehive beehive) {
         return this.isMax ? beehive.getMaximumHoneyLevel() : beehive.getHoneyLevel();
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.isMax) {
         return null;
      } else {
         return switch (mode) {
            case SET, ADD, REMOVE -> (Class[])CollectionUtils.array(Integer.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int value = delta != null ? (Integer)delta[0] : 0;

      Consumer<Beehive> consumer = switch (mode) {
         case SET -> beehivex -> beehivex.setHoneyLevel(Math2.fit(0, value, 5));
         case ADD -> beehivex -> {
            int current = beehivex.getHoneyLevel();
            beehivex.setHoneyLevel(Math2.fit(0, current + value, 5));
         };
         case REMOVE -> beehivex -> {
            int current = beehivex.getHoneyLevel();
            beehivex.setHoneyLevel(Math2.fit(0, current - value, 5));
         };
         default -> throw new IllegalStateException("Unexpected value: " + mode);
      };

      for (Block block : this.getExpr().getArray(event)) {
         if (block.getBlockData() instanceof Beehive beehive) {
            consumer.accept(beehive);
            block.setBlockData(beehive);
         }
      }
   }

   @Override
   public Class<Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return (this.isMax ? "maximum " : "") + "honey level";
   }

   static {
      registerDefault(ExprBeehiveHoneyLevel.class, Integer.class, "[max:max[imum]] honey level", "blocks");
   }
}
