package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Script")
@Description({"The current script, or a script from its (file) name.", "If the script is enabled or disabled (or reloaded) this reference will become invalid.", "Therefore, it is recommended to obtain a script reference <em>when needed</em>."})
@Example.Examples({@Example("on script load:\n\tbroadcast \"Loaded %the current script%\"\n"), @Example("on script load:\n\tset {running::%script%} to true\n"), @Example("on script unload:\n\tset {running::%script%} to false\n"), @Example("set {script} to the script named \"weather.sk\""), @Example("loop the scripts in directory \"quests/\":\n\tenable loop-value\n")})
@Since({"2.0"})
public class ExprScript extends SimpleExpression<Script> {
   @Nullable
   private Script script;
   @Nullable
   private Expression<String> name;
   private boolean isDirectory;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isDirectory = matchedPattern == 2;
      if (matchedPattern == 0) {
         ParserInstance parser = this.getParser();
         if (!parser.isActive()) {
            Skript.error("'the current script' can only be used in a script.");
            return false;
         }

         this.script = parser.getCurrentScript();
      } else {
         this.name = (Expression<String>)exprs[0];
      }

      return true;
   }

   protected Script[] get(Event event) {
      if (this.script != null) {
         return new Script[]{this.script};
      } else {
         assert this.name != null;

         if (this.isDirectory) {
            String string = this.name.getSingle(event);
            if (string == null) {
               return new Script[0];
            } else {
               File folder = new File(Skript.getInstance().getScriptsFolder(), string);
               List<Script> scripts = new ArrayList<>();
               if (!folder.isDirectory()) {
                  return new Script[0];
               } else {
                  this.getScripts(folder, scripts);
                  return scripts.toArray(new Script[0]);
               }
            }
         } else {
            return this.name.stream(event).map(ScriptLoader::getScriptFromName).map(ExprScript::getHandle).filter(Objects::nonNull).toArray(Script[]::new);
         }
      }
   }

   private void getScripts(File folder, List<Script> scripts) {
      File[] files = folder.listFiles();
      if (files != null) {
         FileFilter loaded = ScriptLoader.getLoadedScriptsFilter();
         FileFilter disabled = ScriptLoader.getDisabledScriptsFilter();
         FileFilter filter = f -> loaded.accept(f) || disabled.accept(f);

         for (File file : files) {
            if (file.isDirectory()) {
               this.getScripts(file, scripts);
            } else if (filter.accept(file)) {
               Script handle = getHandle(file);
               if (handle != null) {
                  scripts.add(handle);
               }
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.script != null || this.name != null && this.name.isSingle() && !this.isDirectory;
   }

   @Override
   public Class<? extends Script> getReturnType() {
      return Script.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (this.script != null) {
         return "the current script";
      } else {
         assert this.name != null;

         if (this.isDirectory) {
            return "the scripts in directory " + this.name.toString(event, debug);
         } else {
            return this.name.isSingle() ? "the script named " + this.name.toString(event, debug) : "the scripts named " + this.name.toString(event, debug);
         }
      }
   }

   @Nullable
   static Script getHandle(@Nullable File file) {
      if (file != null && !file.isDirectory()) {
         Script script = ScriptLoader.getScript(file);
         return script != null ? script : ScriptLoader.createDummyScript(file.getName(), file);
      } else {
         return null;
      }
   }

   static {
      Skript.registerExpression(
         ExprScript.class,
         Script.class,
         ExpressionType.SIMPLE,
         "[the] [current] script",
         "[the] script[s] [named] %strings%",
         "[the] scripts in [directory|folder] %string%"
      );
   }
}
