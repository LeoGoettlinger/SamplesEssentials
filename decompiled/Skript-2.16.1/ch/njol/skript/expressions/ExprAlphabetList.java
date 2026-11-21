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
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Alphabetical Sort")
@Description({"Sorts given strings in alphabetical order."})
@Example("set {_list::*} to alphabetically sorted {_strings::*}")
@Since({"2.2-dev18b, 2.14 (retain indices when looping)"})
public class ExprAlphabetList extends SimpleExpression<String> implements KeyedIterableExpression<String> {
   private Expression<String> texts;
   private boolean keyed;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.texts = (Expression<String>)expressions[0];
      if (this.texts.isSingle()) {
         Skript.error("A single string cannot be sorted.");
         return false;
      } else {
         this.keyed = KeyedIterableExpression.canIterateWithKeys(this.texts);
         return true;
      }
   }

   @Nullable
   protected String[] get(Event event) {
      String[] sorted = this.texts.getArray(event);
      Arrays.sort((Object[])sorted);
      return sorted;
   }

   @Override
   public boolean canIterateWithKeys() {
      return this.keyed;
   }

   @Override
   public Iterator<KeyedValue<String>> keyedIterator(Event event) {
      if (!this.keyed) {
         throw new UnsupportedOperationException();
      } else {
         Iterator<KeyedValue<String>> iterator = ((KeyedIterableExpression)this.texts).keyedIterator(event);
         KeyedValue<String>[] array = (KeyedValue<String>[])Iterators.toArray(iterator, KeyedValue.class);
         Arrays.sort(array, Comparator.comparing(KeyedValue::value));
         return Iterators.forArray(array);
      }
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
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
         return ((KeyedIterableExpression)this.texts).isIndexLoop(input);
      }
   }

   @Override
   public boolean isLoopOf(String input) {
      return this.texts.isLoopOf(input);
   }

   @Override
   public Expression<? extends String> simplify() {
      return (Expression<? extends String>)(this.texts instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "alphabetically sorted " + this.texts.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprAlphabetList.class, String.class, ExpressionType.COMBINED, "alphabetically sorted %strings%");
   }
}
