package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class Statement extends TriggerItem implements SyntaxElement {
   @Nullable
   public static Statement parse(String input, String defaultError) {
      return parse(input, null, defaultError);
   }

   @Nullable
   public static Statement parse(String input, @Nullable List<TriggerItem> items, String defaultError) {
      return parse(input, defaultError, null, items);
   }

   @Nullable
   public static Statement parse(String input, @Nullable String defaultError, @Nullable SectionNode node, @Nullable List<TriggerItem> items) {
      var wrappedIterator;
      try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
         final Section.SectionContext sectionContext = ParserInstance.get().getData(Section.SectionContext.class);
         EffFunctionCall functionCall;
         if (node != null) {
            functionCall = sectionContext.modify(
               node,
               items,
               () -> {
                  EffFunctionCall parsed = EffFunctionCall.parse(input);
                  if (parsed != null && !sectionContext.claimed()) {
                     Skript.error(
                        "The line '" + input + "' is a valid function call but cannot function as a section (:) because there is no parameter to manage it."
                     );
                     return null;
                  } else {
                     return parsed;
                  }
               }
            );
         } else {
            functionCall = EffFunctionCall.parse(input);
         }

         if (functionCall != null) {
            log.printLog();
            return functionCall;
         }

         if (log.hasError()) {
            log.printError();
            return null;
         }

         log.clear();
         EffectSection section = EffectSection.parse(input, null, node, false, items);
         if (section != null) {
            log.printLog();
            return new EffectSectionEffect(section);
         }

         log.clear();
         final Iterator<SyntaxInfo<? extends Statement>> iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.STATEMENT).iterator();
         Statement statement;
         if (node != null) {
            wrappedIterator = new Iterator<Object>() {
               @Override
               public boolean hasNext() {
                  return iterator.hasNext();
               }

               public SyntaxInfo<? extends Statement> next() {
                  sectionContext.owner = null;
                  return iterator.next();
               }
            };
            statement = sectionContext.modify(
               node,
               items,
               () -> {
                  Statement parsed = SkriptParser.parse(input, wrappedIterator, defaultError);
                  if (parsed != null && !sectionContext.claimed()) {
                     Skript.error(
                        "The line '"
                           + input
                           + "' is a valid statement but cannot function as a section (:) because there is no syntax in the line to manage it."
                     );
                     return null;
                  } else {
                     return parsed;
                  }
               }
            );
         } else {
            statement = sectionContext.modify(null, null, () -> SkriptParser.parse(input, iterator, defaultError));
         }

         if (statement != null) {
            log.printLog();
            return statement;
         }

         log.printError();
         wrappedIterator = null;
      }

      return wrappedIterator;
   }
}
