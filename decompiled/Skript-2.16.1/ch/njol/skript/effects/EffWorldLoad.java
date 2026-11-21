package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Load World")
@Description({"Load your worlds or unload your worlds", "The load effect will create a new world if world doesn't already exist.", "When attempting to load a normal vanilla world you must define it's environment i.e \"world_nether\" must be loaded with nether environment"})
@Example.Examples({@Example("load world \"world_nether\" with environment nether"), @Example("load the world \"myCustomWorld\""), @Example("unload \"world_nether\""), @Example("unload \"world_the_end\" without saving"), @Example("unload all worlds")})
@Since({"2.8.0"})
public class EffWorldLoad extends Effect {
   private boolean save;
   private boolean load;
   private Expression<?> worlds;
   @Nullable
   private Expression<Environment> environment;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.worlds = exprs[0];
      this.load = matchedPattern == 0;
      if (this.load) {
         this.environment = (Expression<Environment>)exprs[1];
      } else {
         this.save = !parseResult.hasTag("without saving");
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      Environment environment = this.environment != null ? this.environment.getSingle(event) : null;

      for (Object world : this.worlds.getArray(event)) {
         if (this.load && world instanceof String) {
            WorldCreator worldCreator = new WorldCreator((String)world);
            if (environment != null) {
               worldCreator.environment(environment);
            }

            worldCreator.createWorld();
         } else if (!this.load && world instanceof World) {
            Bukkit.unloadWorld((World)world, this.save);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.load
         ? "load the world(s) "
            + this.worlds.toString(event, debug)
            + (this.environment == null ? "" : " with environment " + this.environment.toString(event, debug))
         : "unload the world(s) " + this.worlds.toString(event, debug) + " " + (this.save ? "with saving" : "without saving");
   }

   static {
      Skript.registerEffect(
         EffWorldLoad.class, "load [the] world[s] %strings% [with environment %-environment%]", "unload [[the] world[s]] %worlds% [:without saving]"
      );
   }
}
