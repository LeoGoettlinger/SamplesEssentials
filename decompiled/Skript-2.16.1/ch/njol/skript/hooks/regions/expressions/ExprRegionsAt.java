package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Regions At")
@Description({"All <a href='#region'>regions</a> at a particular <a href='./classes/#location'>location</a>.", "This expression requires a supported regions plugin to be installed."})
@Example("On click on a sign:\n\tline 1 of the clicked block is \"[region info]\"\n\tset {_regions::*} to regions at the clicked block\n\tif {_regions::*} is empty:\n\t\tmessage \"No regions exist at this sign.\"\n\telse:\n\t\tmessage \"Regions containing this sign: <gold>%{_regions::*}%<r>.\"\n")
@Since({"2.1"})
@RequiredPlugins({"Supported regions plugin"})
public class ExprRegionsAt extends SimpleExpression<Region> {
   private Expression<Location> locs;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 1) {
         Skript.warning(
            "Most regions plugins can have multiple intersecting regions at a the same location, thus it is recommended to use \"regions at ...\" instead of \"region at...\" for clarity."
         );
      }

      this.locs = Direction.combine((Expression<? extends Direction>)exprs[0], (Expression<? extends Location>)exprs[1]);
      ScriptWarning.printDeprecationWarning(
         "Skript's region syntaxes are deprecated and will be removed in a future release. For WorldGuard support, we recommend using skript-worldguard: https://github.com/SkriptLang/skript-worldguard"
      );
      return true;
   }

   @Nullable
   protected Region[] get(Event e) {
      Location[] ls = this.locs.getArray(e);
      if (ls.length == 0) {
         return new Region[0];
      } else {
         ArrayList<Region> r = new ArrayList<>();

         for (Location l : ls) {
            r.addAll(RegionsPlugin.getRegionsAt(l));
         }

         return r.toArray(new Region[r.size()]);
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends Region> getReturnType() {
      return Region.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the regions at " + this.locs.toString(e, debug);
   }

   static {
      Skript.registerExpression(ExprRegionsAt.class, Region.class, ExpressionType.PROPERTY, "[the] region(1¦s|) %direction% %locations%");
   }
}
