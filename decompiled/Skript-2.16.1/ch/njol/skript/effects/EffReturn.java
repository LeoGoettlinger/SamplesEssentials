package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ReturnHandler;
import ch.njol.skript.lang.SectionExitHandler;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Return")
@Description({"Makes a trigger or a section (e.g. a function) return a value"})
@Example.Examples({@Example("function double(i: number) :: number:\n\treturn 2 * {_i}\n"), @Example("function divide(i: number) returns number:\n\treturn {_i} / 2\n")})
@Since({"2.2, 2.8.0 (returns aliases)"})
public class EffReturn extends Effect {
   @UnknownNullability
   private ReturnHandler<?> handler;
   @UnknownNullability
   private Expression<?> value;
   @UnknownNullability
   private List<SectionExitHandler> sectionsToExit;
   private int breakLevels;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      ParserInstance parser = this.getParser();
      this.handler = parser.getData(ReturnHandler.ReturnHandlerStack.class).getCurrentHandler();
      if (this.handler == null) {
         Skript.error("The return statement cannot be used here");
         return false;
      } else if (!isDelayed.isFalse()) {
         Skript.error(
            "A return statement after a delay is useless, as the calling trigger will resume when the delay starts (and won't get any returned value)"
         );
         return false;
      } else {
         Class<?> returnType = this.handler.returnValueType();
         if (returnType == null) {
            Skript.error(this.handler + " doesn't return any value. Please use 'stop' or 'exit' if you want to stop the trigger.");
            return false;
         } else {
            RetainingLogHandler log = SkriptLogger.startRetainingLog();

            Expression<?> convertedExpr;
            try {
               convertedExpr = exprs[0].getConvertedExpression(returnType);
               if (convertedExpr == null) {
                  String typeName = Classes.getSuperClassInfo(returnType).getName().withIndefiniteArticle();
                  log.printErrors(this.handler + " is declared to return " + typeName + ", but " + exprs[0].toString(null, false) + " is not of that type.");
                  return false;
               }

               log.printLog();
            } finally {
               log.stop();
            }

            if (this.handler.isSingleReturnValue() && !convertedExpr.isSingle()) {
               String typeName = Classes.getSuperClassInfo(returnType).getName().getSingular();
               Skript.error(this.handler + " is defined to only return a single " + typeName + ", but this return statement can return multiple values.");
               return false;
            } else {
               this.value = convertedExpr;
               List<TriggerSection> innerSections = parser.getSectionsUntil((TriggerSection)this.handler);
               innerSections.add(0, (TriggerSection)this.handler);
               this.breakLevels = innerSections.size();
               if (parser.getCurrentSections().size() < this.breakLevels) {
                  this.breakLevels = -1;
               }

               this.sectionsToExit = innerSections.stream().filter(SectionExitHandler.class::isInstance).map(SectionExitHandler.class::cast).toList();
               return true;
            }
         }
      }
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      this.debug(event, false);
      this.handler.returnValues(event, this.value);

      for (SectionExitHandler section : this.sectionsToExit) {
         section.exit(event);
      }

      return ((TriggerSection)this.handler).getNext();
   }

   @Override
   protected void execute(Event event) {
      assert false;
   }

   @Override
   public ExecutionIntent executionIntent() {
      return (ExecutionIntent)(this.breakLevels == -1 ? ExecutionIntent.stopTrigger() : ExecutionIntent.stopSections(this.breakLevels));
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "return " + this.value.toString(event, debug);
   }

   static {
      Skript.registerEffect(EffReturn.class, "return %objects%");
      ParserInstance.registerData(ReturnHandler.ReturnHandlerStack.class, ReturnHandler.ReturnHandlerStack::new);
   }
}
