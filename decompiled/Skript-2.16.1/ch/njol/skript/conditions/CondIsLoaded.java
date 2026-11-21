package ch.njol.skript.conditions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Is Loaded")
@Description({"Checks whether a world, chunk or script is loaded.", "'chunk at 1, 1' uses chunk coordinates, which are location coords divided by 16."})
@Example.Examples({@Example("if chunk at {home::%player's uuid%} is loaded:"), @Example("if chunk 1, 10 in world \"world\" is loaded:"), @Example("if world(\"lobby\") is loaded:"), @Example("if script named \"MyScript.sk\" is loaded:")})
@Since({"2.3, 2.5 (revamp with chunk at location/coords), 2.10 (Scripts)"})
public class CondIsLoaded extends Condition {
   private Expression<Location> locations;
   private Expression<Number> x;
   private Expression<Number> z;
   private Expression<?> objects;
   private int pattern;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.pattern = matchedPattern;
      switch (this.pattern) {
         case 0:
            this.locations = Direction.combine((Expression<? extends Direction>)exprs[0], (Expression<? extends Location>)exprs[1]);
            break;
         case 1:
            this.x = (Expression<Number>)exprs[0];
            this.z = (Expression<Number>)exprs[1];
            this.objects = exprs[2];
            break;
         case 2:
         case 3:
         case 4:
            this.objects = exprs[0];
      }

      this.setNegated(parseResult.mark == 1);
      return true;
   }

   @Override
   public boolean check(Event e) {
      return switch (this.pattern) {
         case 0 -> this.locations.check(e, location -> {
            World world = location.getWorld();
            return world != null ? world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4) : false;
         }, this.isNegated());
         case 1 -> this.objects.check(e, object -> {
            if (object instanceof World world) {
               Number x = this.x.getSingle(e);
               Number z = this.z.getSingle(e);
               return x != null && z != null ? world.isChunkLoaded(x.intValue(), z.intValue()) : false;
            } else {
               return false;
            }
         }, this.isNegated());
         case 2, 4 -> this.objects.check(e, object -> {
            if (object instanceof World world) {
               return Bukkit.getWorld(world.getName()) != null;
            } else {
               return object instanceof Script script ? ScriptLoader.getLoadedScripts().contains(script) : false;
            }
         }, this.isNegated());
         case 3 -> this.objects.check(e, ScriptLoader.getLoadedScripts()::contains, this.isNegated());
         default -> false;
      };
   }

   @Override
   public String toString(@Nullable Event e, boolean d) {
      String neg = this.isNegated() ? " not " : " ";

      return switch (this.pattern) {
         case 0 -> "chunk[s] at " + this.locations.toString(e, d) + (this.locations.isSingle() ? " is" : " are") + neg + "loaded";
         case 1 -> "chunk at " + this.x.toString(e, d) + ", " + this.z.toString(e, d) + " in " + this.objects.toString(e, d) + ") is" + neg + "loaded";
         default -> this.objects.toString(e, d) + (this.objects.isSingle() ? " is" : " are") + neg + "loaded";
         case 3 -> "scripts " + this.objects.toString(e, d) + (this.objects.isSingle() ? " is" : " are") + neg + "loaded";
         case 4 -> "worlds " + this.objects.toString(e, d) + (this.objects.isSingle() ? " is" : " are") + neg + "loaded";
      };
   }

   static {
      Skript.registerCondition(
         CondIsLoaded.class,
         "chunk[s] %directions% [%locations%] (is|are)[(1¦(n't| not))] loaded",
         "chunk [at] %number%, %number% (in|of) [world] %world% is[(1¦(n't| not))] loaded",
         "%scripts/worlds% (is|are)[1:(n't| not)] loaded",
         "script[s] %scripts% (is|are)[1:(n't| not)] loaded",
         "world[s] %worlds% (is|are)[1:(n't| not)] loaded"
      );
   }
}
