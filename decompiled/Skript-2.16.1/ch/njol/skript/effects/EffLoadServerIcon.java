package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

@Name("Load Server Icon")
@Description({"Loads server icons from the given files. You can get the loaded icon using the", "<a href='#ExprLastLoadedServerIcon'>last loaded server icon</a> expression.", "Please note that the image must be 64x64 and the file path starts from the server folder."})
@Example("on load:\n\tclear {server-icons::*}\n\tloop 5 times:\n\t\tload server icon from file \"icons/%loop-number%.png\"\n\t\tadd the last loaded server icon to {server-icons::*}\n\non server list ping:\n\tset the icon to a random server icon out of {server-icons::*}\n")
@Since({"2.3"})
public class EffLoadServerIcon extends AsyncEffect {
   private Expression<String> path;
   @Nullable
   public static CachedServerIcon lastLoaded = null;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.getParser().setHasDelayBefore(Kleenean.TRUE);
      this.path = (Expression<String>)exprs[0];
      return true;
   }

   @Override
   protected void execute(Event e) {
      String pathString = this.path.getSingle(e);
      if (pathString != null) {
         Path p = Paths.get(pathString);
         if (Files.isRegularFile(p)) {
            try {
               lastLoaded = Bukkit.loadServerIcon(p.toFile());
            } catch (IllegalArgumentException | NullPointerException var5) {
            } catch (Exception var6) {
               Skript.exception(var6);
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "load server icon from file " + this.path.toString(e, debug);
   }

   static {
      Skript.registerEffect(EffLoadServerIcon.class, "load [the] server icon (from|of) [the] [image] [file] %string%");
   }
}
