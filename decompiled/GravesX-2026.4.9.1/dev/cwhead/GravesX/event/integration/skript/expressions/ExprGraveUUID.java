package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.Builder;

@Name("GravesX Grave UUID")
@Description({"Gets the UUID of a grave."})
@Examples({"broadcast \"Grave UUID: %grave uuid of event-grave%\"", "set {_uuid} to uuid of grave event-grave"})
public class ExprGraveUUID extends SimpleExpression<String> {
   private Expression<Grave> grave;

   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      this.grave = (Expression<Grave>)expressions[0];
      return true;
   }

   @Nullable
   protected String[] get(Event event) {
      Grave graveValue = (Grave)this.grave.getSingle(event);
      return graveValue != null && graveValue.getUUID() != null ? new String[]{graveValue.getUUID().toString()} : null;
   }

   public boolean isSingle() {
      return true;
   }

   public Class<? extends String> getReturnType() {
      return String.class;
   }

   public String toString(@Nullable Event event, boolean debug) {
      return "grave uuid of " + this.grave;
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            SyntaxRegistry.EXPRESSION,
            ((Builder)org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder(ExprGraveUUID.class, String.class)
                  .addPatterns(new String[]{"[the] grave uuid of %grave%", "[the] uuid of grave %grave%", "%grave%'[s] grave uuid", "%grave%'[s] uuid"}))
               .build()
         );
   }
}
