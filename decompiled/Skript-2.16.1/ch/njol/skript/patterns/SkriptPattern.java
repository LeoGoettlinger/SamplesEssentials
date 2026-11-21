package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class SkriptPattern {
   private final PatternElement first;
   private final int expressionAmount;
   private final Keyword[] keywords;
   private final int minLength;
   @Nullable
   private List<TypePatternElement> types;

   public SkriptPattern(PatternElement first, int expressionAmount) {
      this.first = first;
      this.expressionAmount = expressionAmount;
      this.keywords = Keyword.buildKeywords(first);
      this.minLength = Keyword.computeMinLength(first);
   }

   @Nullable
   public MatchResult match(String expr, int flags, ParseContext parseContext) {
      String lowerExpr = expr.toLowerCase(Locale.ENGLISH);
      if (lowerExpr.length() < this.minLength) {
         return null;
      } else {
         for (Keyword keyword : this.keywords) {
            if (!keyword.isPresent(lowerExpr)) {
               return null;
            }
         }

         expr = expr.trim();
         MatchResult matchResult = new MatchResult();
         matchResult.source = this;
         matchResult.expr = expr;
         matchResult.expressions = new Expression[this.expressionAmount];
         matchResult.parseContext = parseContext;
         matchResult.flags = flags;
         return this.first.match(expr, matchResult);
      }
   }

   @Nullable
   public MatchResult match(String expr) {
      return this.match(expr, 3, ParseContext.DEFAULT);
   }

   public int countTypes() {
      return this.expressionAmount;
   }

   public int countNonNullTypes() {
      return countNonNullTypes(this.first);
   }

   private static int countNonNullTypes(PatternElement patternElement) {
      int count = 0;

      while (patternElement != null) {
         switch (patternElement) {
            case ChoicePatternElement choicePatternElement:
               int max = 0;

               for (PatternElement component : choicePatternElement.getPatternElements()) {
                  int componentCount = countNonNullTypes(component);
                  if (componentCount > max) {
                     max = componentCount;
                  }
               }

               count += max;
               break;
            case GroupPatternElement groupPatternElement:
               count += countNonNullTypes(groupPatternElement.getPatternElement());
               break;
            case OptionalPatternElement optionalPatternElement:
               count += countNonNullTypes(optionalPatternElement.getPatternElement());
               break;
            case TypePatternElement ignored:
               count++;
               break;
            default:
         }

         patternElement = patternElement.originalNext;
      }

      return count;
   }

   public <T extends PatternElement> List<T> getElements(Class<T> type) {
      if (type == TypePatternElement.class) {
         if (this.types == null) {
            this.types = ImmutableList.copyOf(getElements(TypePatternElement.class, this.first, new ArrayList<>()));
         }

         return (List<T>)this.types;
      } else {
         return getElements(type, this.first, new ArrayList<>());
      }
   }

   private static <T extends PatternElement> List<T> getElements(Class<T> type, PatternElement element, List<T> elements) {
      while (element != null) {
         if (element instanceof ChoicePatternElement choicePatternElement) {
            choicePatternElement.getPatternElements().forEach(e -> getElements(type, e, elements));
         } else if (element instanceof GroupPatternElement groupPatternElement) {
            getElements(type, groupPatternElement.getPatternElement(), elements);
         } else if (element instanceof OptionalPatternElement optionalPatternElement) {
            getElements(type, optionalPatternElement.getPatternElement(), elements);
         } else if (type.isInstance(element)) {
            elements.add((T)element);
         }

         element = element.originalNext;
      }

      return elements;
   }

   public String toString(SkriptPattern.StringificationProperties properties) {
      return this.first.toFullString(properties);
   }

   @Override
   public String toString() {
      return this.toString(SkriptPattern.StringificationProperties.DEFAULT);
   }

   public interface StringificationProperties {
      SkriptPattern.StringificationProperties DEFAULT = builder().build();

      static SkriptPattern.StringificationProperties.Builder builder() {
         return new SkriptPattern.StringificationPropertiesImpl.BuilderImpl();
      }

      boolean excludeParseTags();

      boolean excludeTypeFlags();

      public interface Builder {
         SkriptPattern.StringificationProperties.Builder excludeParseTags();

         SkriptPattern.StringificationProperties.Builder excludeTypeFlags();

         SkriptPattern.StringificationProperties build();
      }
   }

   private record StringificationPropertiesImpl(boolean excludeParseTags, boolean excludeTypeFlags) implements SkriptPattern.StringificationProperties {
      private static class BuilderImpl implements SkriptPattern.StringificationProperties.Builder {
         private boolean excludeParseTags = false;
         private boolean excludeTypeFlags = false;

         @Override
         public SkriptPattern.StringificationProperties.Builder excludeParseTags() {
            this.excludeParseTags = true;
            return this;
         }

         @Override
         public SkriptPattern.StringificationProperties.Builder excludeTypeFlags() {
            this.excludeTypeFlags = true;
            return this;
         }

         @Override
         public SkriptPattern.StringificationProperties build() {
            return new SkriptPattern.StringificationPropertiesImpl(this.excludeParseTags, this.excludeTypeFlags);
         }
      }
   }
}
