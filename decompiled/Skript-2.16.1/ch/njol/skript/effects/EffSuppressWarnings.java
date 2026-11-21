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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Locally Suppress Warning")
@Description({"Suppresses target warnings from the current script."})
@Example.Examples({@Example("locally suppress missing conjunction warnings"), @Example("suppress the variable save warnings")})
@Since({"2.3"})
public class EffSuppressWarnings extends Effect {
   @UnknownNullability
   private ScriptWarning warning;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!this.getParser().isActive()) {
         Skript.error("You can't suppress warnings outside of a script!");
         return false;
      } else {
         this.warning = ScriptWarning.values()[parseResult.mark];
         if (this.warning.isDeprecated()) {
            Skript.warning(this.warning.getDeprecationMessage());
         } else {
            this.getParser().getCurrentScript().suppressWarning(this.warning);
         }

         return true;
      }
   }

   @Override
   protected void execute(Event event) {
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "suppress " + this.warning.getWarningName() + " warnings";
   }

   static {
      StringBuilder warnings = new StringBuilder();
      ScriptWarning[] values = ScriptWarning.values();

      for (int i = 0; i < values.length; i++) {
         if (i != 0) {
            warnings.append('|');
         }

         warnings.append(values[i].ordinal()).append(':').append(values[i].getPattern());
      }

      Skript.registerEffect(EffSuppressWarnings.class, "[local[ly]] suppress [the] (" + warnings + ") warning[s]");
   }
}
