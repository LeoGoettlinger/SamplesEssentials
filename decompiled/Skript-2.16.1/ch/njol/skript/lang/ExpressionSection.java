package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ExpressionSection extends Section {
   protected final SectionExpression<?> expression;

   public ExpressionSection(SectionExpression<?> expression) {
      this.expression = expression;
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      Section.SectionContext context = this.getParser().getData(Section.SectionContext.class);
      if (context.sectionNode == null && this.expression.isSectionOnly()) {
         Skript.error("This expression requires a section.");
         return false;
      } else {
         boolean init;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            boolean claimedSection = context.claim(this, parseResult.expr);
            if (claimedSection) {
               init = this.expression.init(expressions, matchedPattern, isDelayed, parseResult, context.sectionNode, context.triggerItems);
               if (init) {
                  log.printLog();
                  return true;
               }

               context.unclaim(this);
               log.printError();
               return false;
            }

            if (!this.expression.isSectionOnly() && !(context.owner instanceof ExpressionSection)) {
               log.clear();
               return this.expression.init(expressions, matchedPattern, isDelayed, parseResult, null, null);
            }

            log.printError();
            init = false;
         }

         return init;
      }
   }

   @Override
   public boolean init(
      Expression<?>[] expressions,
      int matchedPattern,
      Kleenean isDelayed,
      SkriptParser.ParseResult parseResult,
      @Nullable SectionNode sectionNode,
      List<TriggerItem> triggerItems
   ) {
      return this.expression.init(expressions, matchedPattern, isDelayed, parseResult, sectionNode, triggerItems);
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      return super.walk(event, false);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.expression.toString(event, debug);
   }

   public SectionExpression<?> getAsExpression() {
      return this.expression;
   }

   @Override
   public void loadCode(SectionNode sectionNode) {
      super.loadCode(sectionNode);
   }

   @Override
   public void loadOptionalCode(SectionNode sectionNode) {
      super.loadOptionalCode(sectionNode);
   }

   public boolean runSection(Event event) {
      return TriggerItem.walk(this.first, event);
   }

   @Override
   public void setTriggerItems(List<TriggerItem> items) {
      super.setTriggerItems(items);
   }

   @SafeVarargs
   public final Trigger loadCodeTask(
      SectionNode sectionNode, String name, @Nullable Runnable beforeLoading, @Nullable Runnable afterLoading, Class<? extends Event>... events
   ) {
      return super.loadCode(sectionNode, name, beforeLoading, afterLoading, events);
   }
}
