package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
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
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.EmptyIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Name("Elements")
@Description({"The first, last, range or a random element of a set, e.g. a list variable, or a queue.", "Asking for elements from a queue will also remove them from the queue, see the new queue expression for more information.", "See also: <a href='#ExprRandom'>random expression</a>"})
@Example.Examples({@Example("broadcast the first 3 elements of {top players::*}"), @Example("set {_last} to last element of {top players::*}"), @Example("set {_random player} to random element out of all players"), @Example("send 2nd last element of {top players::*} to player"), @Example("set {page2::*} to elements from 11 to 20 of {top players::*}"), @Example("broadcast the 1st element in {queue}"), @Example("broadcast the first 3 elements in {queue}")})
@Since({"2.0, 2.7 (relative to last element), 2.8.0 (range of elements)"})
public class ExprElement<T> extends SimpleExpression<T> implements KeyProviderExpression<T> {
   private static final Patterns<ExprElement.ElementType[]> PATTERNS = new Patterns<>(
      new Object[][]{
         {
               "[the] (first|1:last) element [out] (of|in) %objects%",
               new ExprElement.ElementType[]{ExprElement.ElementType.FIRST_ELEMENT, ExprElement.ElementType.LAST_ELEMENT}
         },
         {
               "[the] (first|1:last) %integer% elements [out] (of|in) %objects%",
               new ExprElement.ElementType[]{ExprElement.ElementType.FIRST_X_ELEMENTS, ExprElement.ElementType.LAST_X_ELEMENTS}
         },
         {"[a] random element [out] (of|in) %objects%", new ExprElement.ElementType[]{ExprElement.ElementType.RANDOM}},
         {
               "%integer% random [:distinct] elements [out] (of|in) %objects%",
               new ExprElement.ElementType[]{ExprElement.ElementType.RANDOM_ELEMENTS, ExprElement.ElementType.RANDOM_DISTINCT_ELEMENTS}
         },
         {
               "[the] %integer%(st|nd|rd|th) [1:[to] last] element [out] (of|in) %objects%",
               new ExprElement.ElementType[]{ExprElement.ElementType.ORDINAL, ExprElement.ElementType.TAIL_END_ORDINAL}
         },
         {"[the] elements (from|between) %integer% (to|and) %integer% [out] (of|in) %objects%", new ExprElement.ElementType[]{ExprElement.ElementType.RANGE}}
      }
   );
   private final Map<Event, List<String>> cache = new WeakHashMap<>();
   private Expression<? extends T> expr;
   @Nullable
   private Expression<Integer> startIndex;
   @Nullable
   private Expression<Integer> endIndex;
   private ExprElement.ElementType type;
   private boolean keyed;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      ExprElement.ElementType[] types = PATTERNS.getInfo(matchedPattern);
      this.expr = LiteralUtils.defendExpression(expressions[expressions.length - 1]);
      switch (this.type = types[parseResult.mark]) {
         case RANDOM:
         case RANDOM_DISTINCT_ELEMENTS:
         default:
            this.startIndex = null;
            break;
         case RANGE:
            this.endIndex = (Expression<Integer>)expressions[1];
         case FIRST_X_ELEMENTS:
         case LAST_X_ELEMENTS:
         case RANDOM_ELEMENTS:
         case ORDINAL:
         case TAIL_END_ORDINAL:
            this.startIndex = (Expression<Integer>)expressions[0];
      }

      if (this.type == ExprElement.ElementType.RANDOM_ELEMENTS && parseResult.hasTag("distinct")) {
         this.type = ExprElement.ElementType.RANDOM_DISTINCT_ELEMENTS;
      }

      this.keyed = KeyProviderExpression.canReturnKeys(this.expr);
      return LiteralUtils.canInitSafely(this.expr);
   }

   @Nullable
   @Override
   protected T[] get(Event event) {
      if (this.keyed) {
         KeyedValue.UnzippedKeyValues<T> unzipped = KeyedValue.unzip(this.keyedIterator(event));
         this.cache.put(event, unzipped.keys());
         T[] empty = (T[])((Object[])Array.newInstance(this.getReturnType(), 0));
         return (T[])unzipped.values().toArray(empty);
      } else {
         Iterator<? extends T> iterator;
         if (this.expr.isSingle()) {
            T single = (T)this.expr.getSingle(event);
            if (single instanceof SkriptQueue queue) {
               return this.getFromQueue(event, queue);
            }

            iterator = this.transformIterator(event, Iterators.singletonIterator(single));
         } else {
            iterator = this.transformIterator(event, this.expr.iterator(event));
         }

         assert iterator != null;

         return (T[])Iterators.toArray(iterator, this.getReturnType());
      }
   }

   @NotNull
   @Override
   public String[] getArrayKeys(Event event) throws IllegalStateException {
      if (!this.keyed) {
         throw new UnsupportedOperationException();
      } else if (!this.cache.containsKey(event)) {
         throw new SkriptAPIException("Cannot call getArrayKeys() before calling getArray() or getAll()");
      } else {
         return this.cache.remove(event).toArray(new String[0]);
      }
   }

   @Nullable
   @Override
   public Iterator<? extends T> iterator(Event event) {
      if (this.expr.isSingle()) {
         T single = (T)this.expr.getSingle(event);
         return (Iterator<? extends T>)(single instanceof SkriptQueue queue
            ? Optional.ofNullable((T)this.getFromQueue(event, queue)).<Iterator<? extends T>>map(Iterators::forArray).orElse(null)
            : Iterators.singletonIterator(single));
      } else {
         Iterator<? extends T> iterator = this.expr.iterator(event);
         return this.transformIterator(event, iterator);
      }
   }

   @Override
   public Iterator<KeyedValue<T>> keyedIterator(Event event) {
      if (!this.keyed) {
         throw new UnsupportedOperationException();
      } else {
         Iterator<KeyedValue<T>> iterator = (Iterator<KeyedValue<T>>)((KeyProviderExpression)this.expr).keyedIterator(event);
         return this.transformIterator(event, iterator);
      }
   }

   private <A> Iterator<A> transformIterator(Event event, @Nullable Iterator<A> iterator) {
      if (iterator != null && iterator.hasNext()) {
         Integer startIndex = 0;
         Integer endIndex = 0;
         if (this.startIndex != null) {
            startIndex = this.startIndex.getSingle(event);
            if (startIndex == null || startIndex <= 0 && this.type != ExprElement.ElementType.RANGE) {
               return EmptyIterator.get();
            }
         }

         if (this.endIndex != null) {
            endIndex = this.endIndex.getSingle(event);
            if (endIndex == null) {
               return EmptyIterator.get();
            }
         }

         return this.type.apply(iterator, startIndex, endIndex);
      } else {
         return EmptyIterator.get();
      }
   }

   @Nullable
   private T[] getFromQueue(Event event, SkriptQueue queue) {
      if (queue == null) {
         return null;
      } else {
         Integer startIndex = 0;
         Integer endIndex = 0;
         if (this.startIndex != null) {
            startIndex = this.startIndex.getSingle(event);
            if (startIndex == null || startIndex <= 0 && this.type != ExprElement.ElementType.RANGE) {
               return null;
            }
         }

         if (this.endIndex != null) {
            endIndex = this.endIndex.getSingle(event);
            if (endIndex == null) {
               return null;
            }
         }
         return (T[])(switch (this.type) {
            case FIRST_ELEMENT -> CollectionUtils.array(queue.pollFirst());
            case LAST_ELEMENT -> CollectionUtils.array(queue.pollLast());
            case FIRST_X_ELEMENTS -> CollectionUtils.array(queue.removeRangeSafely(0, startIndex));
            case LAST_X_ELEMENTS -> CollectionUtils.array(queue.removeRangeSafely(queue.size() - startIndex, queue.size()));
            case RANDOM -> {
               Random random = ThreadLocalRandom.current();
               yield CollectionUtils.array(queue.removeSafely(random.nextInt(0, queue.size())));
            }
            case RANDOM_ELEMENTS, RANDOM_DISTINCT_ELEMENTS -> {
               Random random = ThreadLocalRandom.current();
               T[] elements = (T[])((Object[])Array.newInstance(this.getReturnType(), Math.min(startIndex, queue.size())));

               for (int i = 0; i < elements.length; i++) {
                  elements[i] = (T)queue.removeSafely(random.nextInt(0, queue.size()));
               }

               yield elements;
            }
            case ORDINAL -> CollectionUtils.array(queue.removeSafely(startIndex - 1));
            case TAIL_END_ORDINAL -> CollectionUtils.array(queue.removeSafely(queue.size() - startIndex));
            case RANGE -> {
               boolean reverse = startIndex > endIndex;
               T[] elements = (T[])CollectionUtils.array(queue.removeRangeSafely(Math.min(startIndex, endIndex) - 1, Math.max(startIndex, endIndex)));
               if (reverse) {
                  ArrayUtils.reverse(elements);
               }

               yield elements;
            }
         });
      }
   }

   @Override
   public boolean canReturnKeys() {
      return !this.keyed ? false : ((KeyProviderExpression)this.expr).canReturnKeys();
   }

   @Override
   public boolean areKeysRecommended() {
      return !this.keyed ? false : ((KeyProviderExpression)this.expr).areKeysRecommended();
   }

   @Nullable
   @Override
   public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      Expression<? extends R> convExpr = this.expr.getConvertedExpression(to);
      if (convExpr == null) {
         return null;
      } else {
         ExprElement<R> exprElement = new ExprElement();
         exprElement.expr = convExpr;
         exprElement.startIndex = this.startIndex;
         exprElement.endIndex = this.endIndex;
         exprElement.type = this.type;
         return exprElement;
      }
   }

   @Override
   public boolean isSingle() {
      return switch (this.type) {
         case FIRST_X_ELEMENTS, LAST_X_ELEMENTS, RANDOM_ELEMENTS, RANDOM_DISTINCT_ELEMENTS, RANGE -> false;
         default -> true;
      };
   }

   @Override
   public Class<? extends T> getReturnType() {
      Class<? extends T> returnType = this.expr.getReturnType();
      return returnType == SkriptQueue.class ? Object.class : returnType;
   }

   @Override
   public Class<? extends T>[] possibleReturnTypes() {
      Class<? extends T> returnType = this.expr.getReturnType();
      return returnType == SkriptQueue.class ? new Class[]{Object.class} : this.expr.possibleReturnTypes();
   }

   @Override
   public boolean canReturn(Class<?> returnType) {
      return returnType == SkriptQueue.class || this.expr.canReturn(returnType);
   }

   @Override
   public Expression<? extends T> simplify() {
      return (Expression<? extends T>)(!(this.expr instanceof Literal)
            || this.type == ExprElement.ElementType.RANDOM
            || this.startIndex != null && !(this.startIndex instanceof Literal)
            || this.endIndex != null && !(this.endIndex instanceof Literal)
         ? this
         : SimplifiedLiteral.fromExpression(this));
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String prefix;
      switch (this.type) {
         case FIRST_ELEMENT:
            prefix = "the first";
            break;
         case LAST_ELEMENT:
            prefix = "the last";
            break;
         case FIRST_X_ELEMENTS:
            assert this.startIndex != null;

            prefix = "the first " + this.startIndex.toString(event, debug);
            break;
         case LAST_X_ELEMENTS:
            assert this.startIndex != null;

            prefix = "the last " + this.startIndex.toString(event, debug);
            break;
         case RANDOM:
            prefix = "a random";
            break;
         case RANDOM_ELEMENTS:
            assert this.startIndex != null;

            prefix = this.startIndex.toString(event, debug) + " random";
            break;
         case RANDOM_DISTINCT_ELEMENTS:
            assert this.startIndex != null;

            prefix = this.startIndex.toString(event, debug) + " distinct random";
            break;
         case ORDINAL:
         case TAIL_END_ORDINAL:
            assert this.startIndex != null;

            prefix = "the ";
            if (this.startIndex instanceof Literal) {
               Integer integer = ((Literal)this.startIndex).getSingle();
               if (integer == null) {
                  prefix = prefix + this.startIndex.toString(event, debug) + "th";
               } else {
                  prefix = prefix + StringUtils.fancyOrderNumber(integer);
               }
            } else {
               prefix = prefix + this.startIndex.toString(event, debug) + "th";
            }

            if (this.type == ExprElement.ElementType.TAIL_END_ORDINAL) {
               prefix = prefix + " last";
            }
            break;
         case RANGE:
            if ($assertionsDisabled || this.startIndex != null && this.endIndex != null) {
               return "the elements from "
                  + this.startIndex.toString(event, debug)
                  + " to "
                  + this.endIndex.toString(event, debug)
                  + " of "
                  + this.expr.toString(event, debug);
            }

            throw new AssertionError();
         default:
            throw new IllegalStateException();
      }

      return prefix + (this.isSingle() ? " element" : " elements") + " of " + this.expr.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprElement.class, Object.class, ExpressionType.PROPERTY, PATTERNS.getPatterns());
   }

   private static enum ElementType {
      FIRST_ELEMENT(iterator -> Iterators.limit(iterator, 1)),
      LAST_ELEMENT(iterator -> Iterators.singletonIterator(Iterators.getLast(iterator))),
      FIRST_X_ELEMENTS(Iterators::limit),
      LAST_X_ELEMENTS((iterator, index) -> {
         Object[] array = Iterators.toArray(iterator, Object.class);
         index = Math.min(index, array.length);
         return Iterators.forArray(CollectionUtils.subarray(array, array.length - index, array.length));
      }),
      RANDOM(iterator -> {
         Object[] array = Iterators.toArray(iterator, Object.class);
         if (array.length == 0) {
            return EmptyIterator.get();
         } else {
            Object element = CollectionUtils.getRandom(array);
            return Iterators.singletonIterator(element);
         }
      }),
      RANDOM_ELEMENTS(
         (iterator, amount) -> {
            Object[] array = Iterators.toArray(iterator, Object.class);
            return (Iterator<?>)(array.length == 0
               ? EmptyIterator.get()
               : IntStream.range(0, amount).mapToObj(ignored -> CollectionUtils.getRandom(array)).iterator());
         }
      ),
      RANDOM_DISTINCT_ELEMENTS((iterator, amount) -> {
         List<Object> remaining = Lists.newArrayList(iterator);
         if (remaining.isEmpty()) {
            return EmptyIterator.get();
         } else {
            Random random = ThreadLocalRandom.current();
            return IntStream.range(0, Math.min(amount, remaining.size())).mapToObj(ignored -> {
               int index = random.nextInt(remaining.size());
               Object element = remaining.get(index);
               remaining.remove(index);
               return element;
            }).iterator();
         }
      }),
      ORDINAL((iterator, index) -> {
         Iterators.advance(iterator, index - 1);
         return (Iterator<?>)(!iterator.hasNext() ? EmptyIterator.get() : Iterators.singletonIterator(iterator.next()));
      }),
      TAIL_END_ORDINAL((iterator, index) -> {
         Object[] array = Iterators.toArray(iterator, Object.class);
         return (Iterator<?>)(index > array.length ? EmptyIterator.get() : Iterators.singletonIterator(array[array.length - index]));
      }),
      RANGE((iterator, startIndex, endIndex) -> {
         boolean reverse = startIndex > endIndex;
         int from = Math.max(Math.min(startIndex, endIndex) - 1, 0);
         int to = Math.max(Math.max(startIndex, endIndex), 0);
         if (reverse) {
            Object[] array = Iterators.toArray(iterator, Object.class);
            Object[] elements = CollectionUtils.subarray(array, from, to);
            ArrayUtils.reverse(elements);
            return Iterators.forArray(elements);
         } else {
            Iterators.advance(iterator, from);
            return Iterators.limit(iterator, to - from);
         }
      });

      private final TriFunction<Iterator<?>, Integer, Integer, Iterator<?>> function;

      private ElementType(Function<Iterator<?>, Iterator<?>> function) {
         this.function = (it, start, end) -> function.apply(it);
      }

      private ElementType(BiFunction<Iterator<?>, Integer, Iterator<?>> function) {
         this.function = (it, start, end) -> function.apply(it, start);
      }

      private ElementType(TriFunction<Iterator<?>, Integer, Integer, Iterator<?>> function) {
         this.function = function;
      }

      public <T> Iterator<T> apply(Iterator<T> iterator, int startIndex, int endIndex) {
         return (Iterator<T>)this.function.apply(iterator, startIndex, endIndex);
      }
   }
}
