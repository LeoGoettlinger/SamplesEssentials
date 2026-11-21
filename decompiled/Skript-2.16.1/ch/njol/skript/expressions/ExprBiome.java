package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Biome")
@Description({"The biome at a certain location. Please note that biomes are only defined for x/z-columns", "(i.e. the <a href='#ExprAltitude'>altitude</a> (y-coordinate) doesn't matter), up until Minecraft 1.15.x.", "As of Minecraft 1.16, biomes are now 3D (per block vs column)."})
@Example("# damage player in deserts constantly\nevery real minute:\n\tloop all players:\n\t\tbiome at loop-player is desert\n\t\tdamage the loop-player by 1\n")
@Since({"1.4.4, 2.6.1 (3D biomes)"})
public class ExprBiome extends PropertyExpression<Location, Biome> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr(
         (Expression<? extends Location>)(matchedPattern == 1
            ? exprs[0]
            : Direction.combine((Expression<? extends Direction>)exprs[0], (Expression<? extends Location>)exprs[1]))
      );
      return true;
   }

   protected Biome[] get(Event event, Location[] source) {
      return this.get(source, location -> location.getBlock().getBiome());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? new Class[]{Biome.class} : super.acceptChange(mode);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode != Changer.ChangeMode.SET) {
         super.change(event, delta, mode);
      } else {
         assert delta != null;

         Biome biome = (Biome)delta[0];

         for (Location location : this.getExpr().getArray(event)) {
            location.getBlock().setBiome(biome);
         }
      }
   }

   @Override
   public Class<? extends Biome> getReturnType() {
      return Biome.class;
   }

   @Override
   public Expression<? extends Biome> simplify() {
      return (Expression<? extends Biome>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the biome at " + this.getExpr().toString(event, debug);
   }

   @Override
   public boolean setTime(int time) {
      super.setTime(time, this.getExpr(), new Class[0]);
      return true;
   }

   static {
      Skript.registerExpression(ExprBiome.class, Biome.class, ExpressionType.PROPERTY, "[the] biome [(of|%direction%) %locations%]", "%locations%'[s] biome");
   }
}
