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
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import java.io.File;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Is Script Loaded")
@Description({"Check if the current script, or another script, is currently loaded."})
@Example.Examples({@Example("script is loaded"), @Example("script \"example.sk\" is loaded")})
@Since({"2.2-dev31"})
public class CondScriptLoaded extends Condition {
   @Nullable
   private Expression<String> scripts;
   @Nullable
   private Script currentScript;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.scripts = (Expression<String>)exprs[0];
      ParserInstance parser = this.getParser();
      if (this.scripts == null) {
         if (!parser.isActive()) {
            Skript.error("The condition 'script loaded' requires a script name argument when used outside of script files");
            return false;
         }

         this.currentScript = parser.getCurrentScript();
      }

      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.scripts == null ? ScriptLoader.getLoadedScripts().contains(this.currentScript) ^ this.isNegated() : this.scripts.check(event, scriptName -> {
         File scriptFile = ScriptLoader.getScriptFromName(scriptName);
         if (scriptFile == null) {
            return false;
         } else {
            Script script = ScriptLoader.getScript(scriptFile);
            return script != null && ScriptLoader.getLoadedScripts().contains(script);
         }
      }, this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String scriptName = this.scripts == null ? "script" : (this.scripts.isSingle() ? "script" : "scripts " + this.scripts.toString(event, debug));
      return this.scripts != null && !this.scripts.isSingle()
         ? scriptName + (this.isNegated() ? " aren't" : " are") + " loaded"
         : scriptName + (this.isNegated() ? " isn't" : " is") + " loaded";
   }

   static {
      Skript.registerCondition(CondScriptLoaded.class, "script[s] [%-strings%] (is|are) loaded", "script[s] [%-strings%] (isn't|is not|aren't|are not) loaded");
   }
}
