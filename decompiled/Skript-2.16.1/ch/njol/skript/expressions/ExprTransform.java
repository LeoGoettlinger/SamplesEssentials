package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.WeakHashMap;
import java.util.stream.StreamSupport;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

@Name("Transformed List")
@Description({"Transforms (or 'maps') a list's values using a given expression. This is akin to looping over the list and getting a modified version of each value.", "If the given expression returns a single value, the indices of the list will not change. If the expression returns multiple values, then then indices will be reset as a single index cannot contain multiple values."})
@Example.Examples({@Example("set {_a::*} to (1, 2, and 3) transformed using (input * 2 - 1, input * 2)\n# {_a::*} is now 1, 2, 3, 4, 5, and 6\n"), @Example("# get a list of the sizes of all clans without manually looping\nset {_clan-sizes::*} to keyed {clans::*} transformed using [{clans::%input index%::size}]\n# using the 'keyed' expression retains the indices of the clans list\n")})
@Since({"2.10"})
@Keywords({"input"})
public class ExprTransform extends SimpleExpression<Object> implements InputSource, KeyProviderExpression<Object> {
   private final Map<Event, List<String>> cache = new WeakHashMap<>();
   private boolean keyed;
   @UnknownNullability
   private Expression<?> mappingExpr;
   @Nullable
   private ClassInfo<?> returnClassInfo;
   @UnknownNullability
   private Expression<?> unmappedObjects;
   private final Set<ExprInput<?>> dependentInputs = new HashSet<>();
   @Nullable
   private Object currentValue;
   @UnknownNullability
   private String currentIndex;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.unmappedObjects = LiteralUtils.defendExpression(expressions[0]);
      if (!this.unmappedObjects.isSingle() && LiteralUtils.canInitSafely(this.unmappedObjects)) {
         this.keyed = KeyProviderExpression.canReturnKeys(this.unmappedObjects);
         if (!parseResult.regexes.isEmpty()) {
            String unparsedExpression = parseResult.regexes.get(0).group();

            assert unparsedExpression != null;

            this.mappingExpr = this.parseExpression(unparsedExpression, this.getParser(), 3);
            return this.mappingExpr != null;
         } else {
            this.returnClassInfo = Classes.getExactClassInfo(this.mappingExpr.getReturnType());
            return true;
         }
      } else {
         return false;
      }
   }

   @NotNull
   @Override
   public Iterator<?> iterator(Event event) {
      if (this.hasIndices()) {
         Iterator<? extends KeyedValue<?>> iterator = ((KeyProviderExpression)this.unmappedObjects).keyedIterator(event);
         return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 16), false).flatMap(keyedValue -> {
            this.currentValue = keyedValue.value();
            this.currentIndex = keyedValue.key();
            return this.mappingExpr.stream(event);
         }).iterator();
      } else {
         this.currentIndex = null;
         Iterator<?> unfilteredObjectIterator = this.unmappedObjects.iterator(event);
         return unfilteredObjectIterator == null
            ? Collections.emptyIterator()
            : StreamSupport.stream(Spliterators.spliteratorUnknownSize(unfilteredObjectIterator, 16), false).flatMap(value -> {
               this.currentValue = value;
               return this.mappingExpr.stream(event);
            }).iterator();
      }
   }

   @Override
   public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
      Iterator<? extends KeyedValue<?>> iterator = ((KeyProviderExpression)this.unmappedObjects).keyedIterator(event);
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 16), false).map(keyedValue -> {
         this.currentValue = keyedValue.value();
         this.currentIndex = keyedValue.key();
         Object mappedValue = this.mappingExpr.getSingle(event);
         return mappedValue != null ? keyedValue.withValue(mappedValue) : null;
      }).filter(Objects::nonNull).iterator();
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      if (!this.canReturnKeys()) {
         return Converters.convertStrictly(Iterators.toArray(this.iterator(event), Object.class), (Class<Object>)this.getReturnType());
      } else {
         KeyedValue.UnzippedKeyValues<Object> unzipped = KeyedValue.unzip(this.keyedIterator(event));
         this.cache.put(event, unzipped.keys());
         return Converters.convertStrictly(unzipped.values().toArray(), (Class<Object>)this.getReturnType());
      }
   }

   @NotNull
   @Override
   public String[] getArrayKeys(Event event) throws IllegalStateException {
      if (!this.cache.containsKey(event)) {
         throw new IllegalStateException();
      } else {
         return this.cache.remove(event).toArray(new String[0]);
      }
   }

   @Override
   public boolean canReturnKeys() {
      return this.hasIndices() && this.mappingExpr.isSingle();
   }

   @Override
   public boolean areKeysRecommended() {
      return KeyProviderExpression.areKeysRecommended(this.mappingExpr);
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<?> getReturnType() {
      return this.mappingExpr.getReturnType();
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.mappingExpr.possibleReturnTypes();
   }

   @Override
   public boolean canReturn(Class<?> returnType) {
      return this.mappingExpr.canReturn(returnType);
   }

   @Override
   public boolean isLoopOf(String candidateString) {
      return KeyProviderExpression.super.isLoopOf(candidateString) || this.mappingExpr.isLoopOf(candidateString) || this.matchesReturnType(candidateString);
   }

   private boolean matchesReturnType(String candidateString) {
      return this.returnClassInfo == null ? false : this.returnClassInfo.matchesUserInput(candidateString);
   }

   @Override
   public Set<ExprInput<?>> getDependentInputs() {
      return this.dependentInputs;
   }

   @Nullable
   @Override
   public Object getCurrentValue() {
      return this.currentValue;
   }

   @Override
   public boolean hasIndices() {
      return this.keyed;
   }

   @UnknownNullability
   @Override
   public String getCurrentIndex() {
      return this.currentIndex;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.unmappedObjects.toString(event, debug) + " transformed using " + this.mappingExpr.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprTransform.class,
         Object.class,
         ExpressionType.PATTERN_MATCHES_EVERYTHING,
         "%objects% (transformed|mapped) (using|with) \\[<.+>\\]",
         "%objects% (transformed|mapped) (using|with) \\(<.+>\\)"
      );
      if (!ParserInstance.isRegistered(InputSource.InputData.class)) {
         ParserInstance.registerData(InputSource.InputData.class, InputSource.InputData::new);
      }
   }
}
