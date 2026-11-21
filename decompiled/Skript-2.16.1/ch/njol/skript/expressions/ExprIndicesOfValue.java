package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Indices of Value")
@Description({"Get the first, last or all positions of a character (or text) in another text using 'positions of %texts% in %text%'. Nothing is returned when the value does not occur in the text. Positions range from 1 to the <a href='#ExprIndicesOf'>length</a> of the text (inclusive).", "", "Using 'indices/positions of %objects% in %objects%', you can get the indices or positions of a list where the value at that index is the provided value. Indices are only supported for keyed expressions (e.g. variable lists) and will return the string indices of the given value. Positions can be used with any list and will return the numerical position of the value in the list, counting up from 1. Additionally, nothing is returned if the value is not found in the list.", "", "Whether string comparison is case-sensitive or not can be configured in Skript's config file."})
@Example.Examples({@Example("set {_first} to the first position of \"@\" in the text argument\nif {_s} contains \"abc\":\n\tset {_s} to the first (position of \"abc\" in {_s} + 3) characters of {_s}\n\t# removes everything after the first \"abc\" from {_s}\n"), @Example("set {_list::*} to 1, 2, 3, 1, 2, 3\nset {_indices::*} to indices of the value 1 in {_list::*}\n# {_indices::*} is now \"1\" and \"4\"\n\nset {_indices::*} to all indices of the value 2 in {_list::*}\n# {_indices::*} is now \"2\" and \"5\"\n\nset {_positions::*} to all positions of the value 3 in {_list::*}\n# {_positions::*} is now 3 and 6\n"), @Example("set {_otherlist::bar} to 100\nset {_otherlist::hello} to \"hi\"\nset {_otherlist::burb} to 100\nset {_otherlist::tud} to \"hi\"\nset {_otherlist::foo} to 100\n\nset {_indices::*} to the first index of the value 100 in {_otherlist::*}\n# {_indices::*} is now \"bar\"\n\nset {_indices::*} to the last index of the value 100 in {_otherlist::*}\n# {_indices::*} is now \"foo\"\n\nset {_positions::*} to all positions of the value 100 in {_otherlist::*}\n# {_positions::*} is now 1, 3 and 5\n\nset {_positions::*} to all positions of the value \"hi\" in {_otherlist::*}\n# {_positions::*} is now 2 and 4\n")})
@Since({"2.1, 2.12 (indices, positions of list)"})
public class ExprIndicesOfValue extends SimpleExpression<Object> {
   private ExprIndicesOfValue.IndexType indexType;
   private boolean position;
   private boolean string;
   private Expression<?> needle;
   private Expression<?> haystack;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (exprs[1].isSingle() && matchedPattern > 0) {
         Skript.error("'" + exprs[1] + "' can only ever have one value at most, thus the 'indices of x in list' expression has no effect.");
         return false;
      } else if (!KeyProviderExpression.canReturnKeys(exprs[1]) && matchedPattern == 2) {
         Skript.error("'" + exprs[1] + "' is not a keyed expression. You can only get the indices of a keyed expression.");
         return false;
      } else {
         this.indexType = ExprIndicesOfValue.IndexType.values()[parseResult.mark == 0 ? 0 : parseResult.mark - 1];
         if (parseResult.mark == 0 && parseResult.hasTag("mult")) {
            this.indexType = ExprIndicesOfValue.IndexType.ALL;
         }

         this.position = matchedPattern <= 1;
         this.string = matchedPattern == 0;
         this.needle = LiteralUtils.defendExpression(exprs[0]);
         this.haystack = exprs[1];
         return LiteralUtils.canInitSafely(this.needle);
      }
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      Object[] needle = this.needle.getAll(event);
      if (needle.length == 0) {
         return (Object[])(this.position ? new Long[0] : new String[0]);
      } else if (!this.position) {
         assert this.haystack instanceof KeyProviderExpression;

         return this.getIndices((KeyProviderExpression<?>)this.haystack, needle, event);
      } else if (!this.string) {
         return this.getListPositions(this.haystack, needle, event);
      } else {
         String haystack = (String)this.haystack.getSingle(event);
         return haystack == null ? new Long[0] : this.getStringPositions(haystack, (String[])needle);
      }
   }

   private Long[] getStringPositions(String haystack, String[] needles) {
      boolean caseSensitive = SkriptConfig.caseSensitive.value();
      List<Long> positions = new ArrayList<>();

      for (String needle : needles) {
         long position = StringUtils.indexOf(haystack, needle, caseSensitive);
         if (position != -1L) {
            switch (this.indexType) {
               case FIRST:
                  positions.add(position + 1L);
                  break;
               case LAST:
                  positions.add((long)StringUtils.lastIndexOf(haystack, needle, caseSensitive));
                  break;
               case ALL:
                  while (true) {
                     positions.add(position + 1L);
                     position = StringUtils.indexOf(haystack, needle, (int)position + 1, caseSensitive);
                     if (position == -1L) {
                        break;
                     }
                  }
            }
         }
      }

      return positions.toArray(Long[]::new);
   }

   private <Item, Index, Value> Index[] getMatches(
      Iterator<Item> haystackIterator,
      Value[] needles,
      Function<Item, Value> valueMapper,
      BiFunction<Item, Long, Index> indexMapper,
      IntFunction<Index[]> arrayFactory
   ) {
      boolean caseSensitive = SkriptConfig.caseSensitive.value();
      List<Index>[] results = new List[needles.length];
      long index = 1L;

      for (boolean shouldBreak = false; haystackIterator.hasNext(); index++) {
         Item item = haystackIterator.next();

         for (int i = 0; i < needles.length; i++) {
            Object needle = needles[i];
            if (this.equals(valueMapper.apply(item), needle, caseSensitive)) {
               Index mappedIndex = indexMapper.apply(item, index);
               switch (this.indexType) {
                  case FIRST:
                  case LAST:
                     results[i] = Collections.singletonList(mappedIndex);
                     break;
                  case ALL:
                     if (results[i] == null) {
                        results[i] = new ArrayList<>();
                     }

                     results[i].add(mappedIndex);
               }
            }
         }

         if (this.indexType == ExprIndicesOfValue.IndexType.FIRST && !ArrayUtils.contains(results, null)) {
            break;
         }
      }

      return Arrays.stream(results).filter(Objects::nonNull).flatMap(Collection::stream).toArray(arrayFactory);
   }

   private Long[] getListPositions(Expression<?> haystack, Object[] needles, Event event) {
      Iterator<?> haystackIterator = haystack.iterator(event);
      return haystackIterator == null ? new Long[0] : this.getMatches(haystackIterator, needles, item -> item, (item, index) -> index, Long[]::new);
   }

   private String[] getIndices(KeyProviderExpression<?> haystack, Object[] needles, Event event) {
      Iterator<? extends KeyedValue<?>> haystackIterator = haystack.keyedIterator(event);
      return haystackIterator == null
         ? new String[0]
         : this.getMatches(haystackIterator, needles, KeyedValue::value, (item, index) -> item.key(), String[]::new);
   }

   private boolean equals(Object key, Object value, boolean caseSensitive) {
      return key instanceof String keyString && value instanceof String valueString
         ? StringUtils.equals(keyString, valueString, caseSensitive)
         : key.equals(value);
   }

   @Override
   public boolean isSingle() {
      return (this.indexType == ExprIndicesOfValue.IndexType.FIRST || this.indexType == ExprIndicesOfValue.IndexType.LAST) && this.needle.isSingle();
   }

   @Override
   public Class<?> getReturnType() {
      return this.position ? Long.class : String.class;
   }

   @Override
   public Expression<?> simplify() {
      return (Expression<?>)(this.position && this.string && this.needle instanceof Literal && this.haystack instanceof Literal
         ? SimplifiedLiteral.fromExpression(this)
         : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append(this.indexType.name().toLowerCase(Locale.ENGLISH));
      if (this.position) {
         builder.append("positions");
      } else {
         builder.append("indices");
      }

      builder.append("of value", this.needle, "in", this.haystack);
      return builder.toString();
   }

   static {
      Skript.registerExpression(
         ExprIndicesOfValue.class,
         Object.class,
         ExpressionType.COMBINED,
         "[the] [1:first|2:last|3:all] (position[mult:s]|mult:indices|index[mult:es]) of [[the] value] %strings% in %string%",
         "[the] [1:first|2:last|3:all] position[mult:s] of [[the] value] %objects% in %~objects%",
         "[the] [1:first|2:last|3:all] (mult:indices|index[mult:es]) of [[the] value] %objects% in %~objects%"
      );
   }

   private static enum IndexType {
      FIRST,
      LAST,
      ALL;
   }
}
