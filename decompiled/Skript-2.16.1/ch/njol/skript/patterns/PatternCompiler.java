package ch.njol.skript.patterns;

import ch.njol.skript.lang.SkriptParser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;

public class PatternCompiler {
   private static PatternElement getEmpty() {
      return new LiteralPatternElement("");
   }

   public static SkriptPattern compile(String pattern) throws MalformedPatternException {
      AtomicInteger atomicInteger = new AtomicInteger(0);

      try {
         PatternElement first = compile(pattern, atomicInteger);
         return new SkriptPattern(first, atomicInteger.get());
      } catch (MalformedPatternException var3) {
         throw var3;
      } catch (RuntimeException var4) {
         throw new MalformedPatternException(pattern, "caught exception while compiling pattern", var4);
      }
   }

   static PatternElement compile(String pattern, AtomicInteger expressionOffset) {
      StringBuilder literalBuilder = new StringBuilder();
      PatternElement first = null;

      for (int i = 0; i < pattern.length(); i++) {
         char c = pattern.charAt(i);
         if (c == '[') {
            if (!literalBuilder.isEmpty()) {
               first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
               literalBuilder = new StringBuilder();
            }

            int end = SkriptParser.nextBracket(pattern, ']', c, i + 1, true);
            PatternElement patternElement = compile(pattern.substring(i + 1, end), expressionOffset);
            first = appendElement(first, new OptionalPatternElement(patternElement));
            i = end;
         } else if (c == '(') {
            if (!literalBuilder.isEmpty()) {
               first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
               literalBuilder = new StringBuilder();
            }

            int end = SkriptParser.nextBracket(pattern, ')', c, i + 1, true);
            PatternElement patternElement = compile(pattern.substring(i + 1, end), expressionOffset);
            first = appendElement(first, new GroupPatternElement(patternElement));
            i = end;
         } else if (c == '|') {
            if (!literalBuilder.isEmpty()) {
               first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
               literalBuilder = new StringBuilder();
            }

            PatternElement prevFirst = first;
            if (!(first instanceof ChoicePatternElement choicePatternElement)) {
               first = choicePatternElement = new ChoicePatternElement();
               choicePatternElement.add(prevFirst != null ? prevFirst : getEmpty());
            }

            choicePatternElement.add(getEmpty());
         } else if (c == 166 || c == ':') {
            String tag = literalBuilder.toString();
            literalBuilder = new StringBuilder();
            ParseTagPatternElement parseTagPatternElement;
            if (c == 166) {
               int mark;
               try {
                  mark = Integer.parseInt(tag);
               } catch (NumberFormatException var11) {
                  throw new MalformedPatternException(pattern, "invalid parse mark at " + i, var11);
               }

               parseTagPatternElement = new ParseTagPatternElement(mark);
            } else {
               parseTagPatternElement = new ParseTagPatternElement(tag);
            }

            first = appendElement(first, parseTagPatternElement);
         } else if (c == '%') {
            if (!literalBuilder.isEmpty()) {
               first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
               literalBuilder = new StringBuilder();
            }

            int end = pattern.indexOf(37, i + 1);
            if (end == -1) {
               throw new MalformedPatternException(pattern, "single percentage sign at " + i);
            }

            int exprOffset = expressionOffset.getAndIncrement();
            TypePatternElement typePatternElement = TypePatternElement.fromString(pattern.substring(i + 1, end), exprOffset);
            first = appendElement(first, typePatternElement);
            i = end;
         } else if (c == '<') {
            if (!literalBuilder.isEmpty()) {
               first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
               literalBuilder = new StringBuilder();
            }

            int end = pattern.indexOf(62, i + 1);
            if (end == -1) {
               throw new MalformedPatternException(pattern, "missing closing regex bracket '>' at " + i);
            }

            Pattern regexPattern;
            try {
               regexPattern = Pattern.compile(pattern.substring(i + 1, end));
            } catch (PatternSyntaxException var10) {
               throw new MalformedPatternException(pattern, "invalid regex <" + pattern.substring(i + 1, end) + "> at " + i, var10);
            }

            first = appendElement(first, new RegexPatternElement(regexPattern));
            i = end;
         } else if (c == '\\') {
            literalBuilder.append(pattern.charAt(++i));
         } else {
            literalBuilder.append(c);
         }
      }

      if (!literalBuilder.isEmpty()) {
         first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
      }

      return first == null ? getEmpty() : first;
   }

   private static PatternElement appendElement(@Nullable PatternElement first, PatternElement next) {
      if (first == null || first instanceof LiteralPatternElement literalPatternElement && first.next == null && literalPatternElement.isEmpty()) {
         return next;
      } else if (first instanceof ChoicePatternElement choicePatternElement) {
         PatternElement last = choicePatternElement.getLast();
         choicePatternElement.setLast(appendElement(last, next));
         return first;
      } else {
         PatternElement last = first;

         while (last.next != null) {
            last = last.next;
         }

         last.setNext(next);
         last.originalNext = next;
         return first;
      }
   }
}
