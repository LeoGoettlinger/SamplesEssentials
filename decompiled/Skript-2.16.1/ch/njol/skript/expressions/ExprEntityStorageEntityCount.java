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
import ch.njol.util.coll.CollectionUtils;
import java.util.function.Consumer;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Entity Count")
@Description({"The current number of entities stored inside an entity block storage (i.e. beehive).", "The maximum amount of entities an entity block storage can hold."})
@Example.Examples({@Example("broadcast the stored entity count of {_beehive}"), @Example("set the maximum entity count of {_beehive} to 20")})
@Since({"2.11"})
public class ExprEntityStorageEntityCount extends SimplePropertyExpression<Block, Integer> {
   private boolean withMax;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.withMax = parseResult.hasTag("max");
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Integer convert(Block block) {
      if (block.getState() instanceof EntityBlockStorage<?> blockStorage) {
         return this.withMax ? blockStorage.getMaxEntities() : blockStorage.getEntityCount();
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.withMax) {
         return switch (mode) {
            case SET, DELETE, RESET, ADD, REMOVE -> (Class[])CollectionUtils.array(Integer.class);
            default -> null;
         };
      } else {
         return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int value = delta != null ? (Integer)delta[0] : 0;

      Consumer<EntityBlockStorage<?>> consumer = switch (mode) {
         case SET, DELETE -> blockStoragex -> blockStoragex.setMaxEntities(value);
         case RESET -> blockStoragex -> {
            if (blockStoragex instanceof Beehive) {
               blockStoragex.setMaxEntities(3);
            } else {
               blockStoragex.setMaxEntities(1);
            }
         };
         case ADD -> blockStoragex -> {
            int current = blockStoragex.getMaxEntities();
            blockStoragex.setMaxEntities(current + value);
         };
         case REMOVE -> blockStoragex -> {
            int current = blockStoragex.getMaxEntities();
            blockStoragex.setMaxEntities(current - value);
         };
         default -> throw new IllegalStateException("Unexpected value: " + mode);
      };

      for (Block block : this.getExpr().getArray(event)) {
         if (block.getState() instanceof EntityBlockStorage<?> blockStorage) {
            consumer.accept(blockStorage);
            blockStorage.update(true, false);
         }
      }
   }

   @Override
   public Class<Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return (this.withMax ? "maximum " : "") + "stored entity count";
   }

   static {
      registerDefault(ExprEntityStorageEntityCount.class, Integer.class, "[max:max[imum]] [stored] entity count", "blocks");
   }
}
