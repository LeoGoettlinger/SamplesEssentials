package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.Brushable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Brushing Stage")
@Description({"Represents how far the block has been uncovered.\nThe only blocks that can currently be \"brushed\" are Suspicious Gravel and Suspicious Sand.\n0 means the block is untouched, the max (usually 3) means nearly fulled brushed.\nResetting this value will set it to 0.\n"})
@Example.Examples({@Example("# prevent dusting past level 1\non player change block:\n\tif dusting progress of future event-blockdata > 1:\n\t\tcancel event\n"), @Example("# draw particles when dusting is complete!\non player change block:\n\tif brushing progress of event-block is max brushing stage of event-block:\n\t\tdraw 20 totem of undying particles at event-block\n")})
@Since({"2.12"})
@Keywords({"brush", "brushing", "dusting"})
public class ExprDustedStage extends PropertyExpression<Object, Integer> {
   private boolean isMax;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Object>)exprs[0]);
      this.isMax = parseResult.hasTag("max");
      return true;
   }

   @Nullable
   protected Integer[] get(Event event, Object[] source) {
      return this.get(source, obj -> {
         Brushable brushable = this.getBrushable(obj);
         return brushable != null ? this.isMax ? brushable.getMaximumDusted() : brushable.getDusted() : null;
      });
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.isMax) {
         Skript.error("Attempting to modify the max dusted stage is not supported.");
         return null;
      } else {
         return switch (mode) {
            case SET, ADD, REMOVE, RESET -> (Class[])CollectionUtils.array(Integer.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (!this.isMax) {
         int value = delta == null ? 0 : (Integer)delta[0];

         for (Object obj : this.getExpr().getArray(event)) {
            Brushable brushable = this.getBrushable(obj);
            if (brushable != null) {
               int currentValue = brushable.getDusted();
               int maxValue = brushable.getMaximumDusted();

               long newValue = switch (mode) {
                  case SET, RESET -> value;
                  case ADD -> Math2.addClamped(currentValue, value);
                  case REMOVE -> Math2.addClamped(currentValue, -value);
                  default -> throw new IllegalArgumentException("Change mode " + mode + " is not valid for ExprDustedStage!");
               };
               brushable.setDusted(Math.clamp(newValue, 0, maxValue));
               if (obj instanceof Block block) {
                  block.setBlockData(brushable);
               }
            }
         }
      }
   }

   @Nullable
   private Brushable getBrushable(Object obj) {
      if (obj instanceof Block block) {
         if (block.getBlockData() instanceof Brushable brushable) {
            return brushable;
         }
      } else if (obj instanceof Brushable brushable) {
         return brushable;
      }

      return null;
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.getExpr().toString(event, debug) + "'s " + (this.isMax ? "maximum " : "") + " dusted stage";
   }

   static {
      register(ExprDustedStage.class, Integer.class, "[:max[imum]] (dust|brush)[ed|ing] (value|stage|progress[ion])", "blocks/blockdatas");
   }
}
