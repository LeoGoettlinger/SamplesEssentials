package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
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
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Age of Block/Entity")
@Description({"Returns the age or maximum age of blocks and age for entities (there in no maximum age for entities).", "For blocks, 'Age' represents the different growth stages that a crop-like block can go through. A value of 0 indicates that the crop was freshly planted, whilst a value equal to 'maximum age' indicates that the crop is ripe and ready to be harvested.", "For entities, 'Age' represents the time left for them to become adults and it's in minus increasing to be 0 which means they're adults, e.g. A baby cow needs 20 minutes to become an adult which equals to 24,000 ticks so their age will be -24000 once spawned."})
@Example.Examples({@Example("# Set targeted crop to fully grown crop\nset age of targeted block to maximum age of targeted block\n"), @Example("# Spawn a baby cow that will only need 1 minute to become an adult\nspawn a baby cow at player\nset age of last spawned entity to -1200 # in ticks = 60 seconds\n")})
@Since({"2.7"})
public class ExprAge extends SimplePropertyExpression<Object, Integer> {
   private boolean isMax;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isMax = parseResult.hasTag("max");
      this.setExpr((Expression<? extends Object>)exprs[0]);
      if (this.isMax && !this.getExpr().canReturn(Block.class)) {
         Skript.error("Cannot use 'max age' expression with entities, use just the 'age' expression instead");
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public Integer convert(Object obj) {
      if (obj instanceof Block) {
         return !(((Block)obj).getBlockData() instanceof Ageable ageable) ? null : this.isMax ? ageable.getMaximumAge() : ageable.getAge();
      } else if (obj instanceof org.bukkit.entity.Ageable) {
         return this.isMax ? null : ((org.bukkit.entity.Ageable)obj).getAge();
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return !this.isMax && mode != Changer.ChangeMode.REMOVE_ALL && mode != Changer.ChangeMode.DELETE ? CollectionUtils.array(Number.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.RESET || delta != null) {
         int newValue = mode != Changer.ChangeMode.RESET ? ((Number)delta[0]).intValue() : 0;

         for (Object obj : this.getExpr().getArray(event)) {
            Number oldValue = this.convert(obj);
            if (oldValue != null || mode == Changer.ChangeMode.RESET) {
               switch (mode) {
                  case REMOVE:
                     setAge(obj, oldValue.intValue() - newValue);
                     break;
                  case ADD:
                     setAge(obj, oldValue.intValue() + newValue);
                     break;
                  case SET:
                     setAge(obj, newValue);
                     break;
                  case RESET:
                     if (obj instanceof org.bukkit.entity.Ageable) {
                        newValue = -24000;
                     }

                     setAge(obj, newValue);
               }
            }
         }
      }
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return (this.isMax ? "maximum " : "") + "age";
   }

   private static void setAge(Object obj, int value) {
      if (obj instanceof Block block) {
         BlockData bd = block.getBlockData();
         if (bd instanceof Ageable) {
            ((Ageable)bd).setAge(Math.max(Math.min(value, ((Ageable)bd).getMaximumAge()), 0));
            block.setBlockData(bd);
         }
      } else if (obj instanceof org.bukkit.entity.Ageable) {
         ((org.bukkit.entity.Ageable)obj).setAge(value);
      }
   }

   static {
      register(ExprAge.class, Integer.class, "[:max[imum]] age", "blocks/entities");
   }
}
