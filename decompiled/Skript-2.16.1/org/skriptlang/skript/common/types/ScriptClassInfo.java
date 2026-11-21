package org.skriptlang.skript.common.types;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Feature;
import java.io.File;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;
import org.skriptlang.skript.lang.script.Script;

@Internal
public class ScriptClassInfo extends ClassInfo<Script> {
   public ScriptClassInfo() {
      super(Script.class, "script");
      this.user(new String[]{"scripts?"})
         .name("Script")
         .description("A script loaded by Skript.", "Disabled scripts will report as being empty since their content has not been loaded.")
         .usage("")
         .examples("the current script")
         .since("2.10")
         .parser(new ScriptClassInfo.ScriptParser())
         .property(
            Property.NAME,
            "A script's name, as text. If the experiment 'Script Reflection' is enabled, this will return the resolved name of the script, otherwise it returns the file name with path relative to the scripts folder. Cannot be changed.",
            Skript.instance(),
            new ScriptClassInfo.ScriptNameHandler()
         );
   }

   private static class ScriptNameHandler implements ExpressionPropertyHandler<Script, String> {
      private boolean useResolvedName;

      @Override
      public PropertyHandler<Script> newInstance() {
         return new ScriptClassInfo.ScriptNameHandler();
      }

      @Override
      public boolean init(Expression<?> parentExpression, ParserInstance parser) {
         this.useResolvedName = parser.hasExperiment(Feature.SCRIPT_REFLECTION);
         return true;
      }

      public String convert(Script script) {
         return this.useResolvedName ? script.name() : script.nameAndPath();
      }

      @NotNull
      @Override
      public Class<String> returnType() {
         return String.class;
      }
   }

   private static class ScriptParser extends Parser<Script> {
      final Path path = Skript.getInstance().getScriptsFolder().getAbsoluteFile().toPath();

      @Override
      public boolean canParse(ParseContext context) {
         return switch (context) {
            case PARSE, COMMAND -> true;
            default -> false;
         };
      }

      @Nullable
      public Script parse(String name, ParseContext context) {
         return switch (context) {
            case PARSE, COMMAND -> {
               File file = ScriptLoader.getScriptFromName(name);
               yield file != null && file.isFile() ? ScriptLoader.getScript(file) : null;
            }
            default -> null;
         };
      }

      public String toString(Script script, int flags) {
         File file = script.getConfig().getFile();
         return file == null ? script.getConfig().getFileName() : this.path.relativize(file.toPath().toAbsolutePath()).toString();
      }

      public String toVariableNameString(Script script) {
         return this.toString(script, 0);
      }
   }
}
