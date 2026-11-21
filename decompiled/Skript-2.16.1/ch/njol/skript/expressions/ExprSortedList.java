package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.EmptyIterator;
import java.lang.reflect.Array;
import java.util.Iterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

@Name("Sorted List")
@Description({"Sorts given list in natural order. All objects in list must be comparable; if they're not, this expression will return nothing."})
@Example.Examples({@Example("set {_sorted::*} to sorted {_players::*}"), @Example("command /leaderboard:\n\ttrigger:\n\t\tloop reversed sorted {most-kills::*}:\n\t\t\tsend \"%loop-counter%. %loop-index% with %loop-value% kills\" to sender\n")})
@Since({"2.2-dev19, 2.14 (retain indices when looping)"})
public class ExprSortedList extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {
   private Expression<?> list;
   private boolean keyed;

   public ExprSortedList() {
   }

   public ExprSortedList(Expression<?> list) {
      this.list = list;
      this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.list = LiteralUtils.defendExpression(expressions[0]);
      if (this.list.isSingle()) {
         Skript.error("A single object cannot be sorted.");
         return false;
      } else {
         this.keyed = KeyedIterableExpression.canIterateWithKeys(this.list);
         return LiteralUtils.canInitSafely(this.list);
      }
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      try {
         return this.list.stream(event).sorted(ExprSortedList::compare).toArray();
      } catch (ClassCastException | IllegalArgumentException var3) {
         return (Object[])Array.newInstance(this.getReturnType(), 0);
      }
   }

   @Override
   public boolean canIterateWithKeys() {
      return this.keyed;
   }

   @Override
   public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
      if (!this.keyed) {
         throw new UnsupportedOperationException();
      } else {
         try {
            return ((KeyedIterableExpression)this.list).keyedStream(event).sorted((a, b) -> compare(a.value(), b.value())).iterator();
         } catch (ClassCastException | IllegalArgumentException var3) {
            return EmptyIterator.get();
         }
      }
   }

   public static <A, B> int compare(A a, B b) throws IllegalArgumentException, ClassCastException {
      if (a instanceof String && b instanceof String) {
         return Relation.get(((String)a).compareToIgnoreCase((String)b)).getRelation();
      } else {
         Comparator<A, B> comparator = Comparators.getComparator((Class<A>)a.getClass(), (Class<B>)b.getClass());
         if (comparator != null && comparator.supportsOrdering()) {
            return comparator.compare(a, b).getRelation();
         } else if (!(a instanceof Comparable)) {
            throw new IllegalArgumentException("Cannot compare " + a.getClass());
         } else {
            return ((Comparable)a).compareTo(b);
         }
      }
   }

   @SafeVarargs
   @Nullable
   @Override
   public final <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      if (CollectionUtils.containsSuperclass(to, this.getReturnType())) {
         return this;
      } else {
         Expression<? extends R> convertedList = this.list.getConvertedExpression(to);
         return convertedList != null ? new ExprSortedList(convertedList) : null;
      }
   }

   @Override
   public Class<?> getReturnType() {
      return this.list.getReturnType();
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.list.possibleReturnTypes();
   }

   @Override
   public boolean canReturn(Class<?> returnType) {
      return this.list.canReturn(returnType);
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public boolean isIndexLoop(String input) {
      if (!this.keyed) {
         throw new IllegalStateException();
      } else {
         return ((KeyedIterableExpression)this.list).isIndexLoop(input);
      }
   }

   @Override
   public boolean isLoopOf(String input) {
      return this.list.isLoopOf(input);
   }

   @Override
   public Expression<?> simplify() {
      return (Expression<?>)(this.list instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "sorted " + this.list.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprSortedList.class, Object.class, ExpressionType.PROPERTY, "sorted %objects%");
   }
}
