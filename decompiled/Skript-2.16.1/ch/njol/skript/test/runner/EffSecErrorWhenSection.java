package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class EffSecErrorWhenSection extends EffectSection {
   @Nullable
   private Literal<String> error;
   @Nullable
   private Expression<?> with;

   @Override
   public boolean init(
      Expression<?>[] expressions,
      int matchedPattern,
      Kleenean isDelayed,
      SkriptParser.ParseResult parseResult,
      @Nullable SectionNode sectionNode,
      @Nullable List<TriggerItem> triggerItems
   ) {
      this.error = (Literal<String>)expressions[0];
      this.with = LiteralUtils.defendExpression(expressions[1]);
      if (sectionNode != null) {
         if (this.error != null) {
            Skript.error(this.error.getSingle());
         }

         return false;
      } else {
         return this.with == null || LiteralUtils.canInitSafely(this.with);
      }
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      return super.walk(event, false);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("error");
      builder.appendIf(this.error != null, this.error);
      builder.append("when using a section");
      builder.appendIf(this.with != null, "with", this.with);
      return builder.toString();
   }

   static {
      if (TestMode.ENABLED) {
         Skript.registerSection(EffSecErrorWhenSection.class, "error [%-*string%] when using [a] section [with %-object%]");
      }
   }
}
