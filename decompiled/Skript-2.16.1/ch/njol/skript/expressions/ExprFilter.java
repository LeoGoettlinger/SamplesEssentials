package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.WeakHashMap;
import java.util.stream.StreamSupport;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

@Name("Filter")
@Description({"Filters a list based on a condition. ", "For example, if you ran 'broadcast \"something\" and \"something else\" where [string input is \"something\"]', ", "only \"something\" would be broadcast as it is the only string that matched the condition."})
@Example.Examples({@Example("send \"congrats on being staff!\" to all players where [player input has permission \"staff\"]"), @Example("loop (all blocks in radius 5 of player) where [block input is not air]:")})
@Since({"2.2-dev36, 2.10 (parenthesis pattern)"})
public class ExprFilter extends SimpleExpression<Object> implements InputSource, KeyProviderExpression<Object> {
   private final Map<Event, List<String>> cache = new WeakHashMap<>();
   private boolean keyed;
   @UnknownNullability
   private Condition filterCondition;
   @UnknownNullability
   private String unparsedCondition;
   @UnknownNullability
   private Expression<?> unfilteredObjects;
   private final Set<ExprInput<?>> dependentInputs = new HashSet<>();
   @Nullable
   private Object currentValue;
   @UnknownNullability
   private String currentIndex;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.unfilteredObjects = LiteralUtils.defendExpression(expressions[0]);
      if (!this.unfilteredObjects.isSingle() && LiteralUtils.canInitSafely(this.unfilteredObjects)) {
         this.keyed = KeyProviderExpression.canReturnKeys(this.unfilteredObjects);
         this.unparsedCondition = parseResult.regexes.get(0).group();
         InputSource.InputData inputData = this.getParser().getData(InputSource.InputData.class);
         InputSource originalSource = inputData.getSource();
         inputData.setSource(this);
         this.filterCondition = Condition.parse(this.unparsedCondition, "Can't understand this condition: " + this.unparsedCondition);
         inputData.setSource(originalSource);
         return this.filterCondition != null;
      } else {
         return false;
      }
   }

   @NotNull
   @Override
   public Iterator<?> iterator(Event event) {
      if (this.keyed) {
         return Iterators.transform(this.keyedIterator(event), KeyedValue::value);
      } else {
         this.currentIndex = null;
         Iterator<?> unfilteredObjectIterator = this.unfilteredObjects.iterator(event);
         return (Iterator<?>)(unfilteredObjectIterator == null ? Collections.emptyIterator() : Iterators.filter(unfilteredObjectIterator, candidateObject -> {
            this.currentValue = candidateObject;
            return this.filterCondition.check(event);
         }));
      }
   }

   @Override
   public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
      Iterator<KeyedValue<Object>> keyedIterator = ((KeyProviderExpression)this.unfilteredObjects).keyedIterator(event);
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(keyedIterator, 16), false).filter(keyedValue -> {
         this.currentValue = keyedValue.value();
         this.currentIndex = keyedValue.key();
         return this.filterCondition.check(event);
      }).iterator();
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      if (!this.keyed) {
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
      return this.keyed;
   }

   @Override
   public boolean areKeysRecommended() {
      return KeyProviderExpression.areKeysRecommended(this.unfilteredObjects);
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<?> getReturnType() {
      return this.unfilteredObjects.getReturnType();
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.unfilteredObjects.possibleReturnTypes();
   }

   @Override
   public boolean canReturn(Class<?> returnType) {
      return this.unfilteredObjects.canReturn(returnType);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.unfilteredObjects.toString(event, debug) + " that match [" + this.unparsedCondition + "]";
   }

   private boolean matchesAnySpecifiedTypes(String candidateString) {
      for (ExprInput<?> dependentInput : this.dependentInputs) {
         ClassInfo<?> specifiedType = dependentInput.getSpecifiedType();
         if (specifiedType == null) {
            return false;
         }

         if (specifiedType.matchesUserInput(candidateString)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isLoopOf(String candidateString) {
      return this.unfilteredObjects.isLoopOf(candidateString) || this.matchesAnySpecifiedTypes(candidateString);
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

   static {
      Skript.registerExpression(
         ExprFilter.class, Object.class, ExpressionType.COMBINED, "%objects% (where|that match) \\[<.+>\\]", "%objects% (where|that match) \\(<.+>\\)"
      );
      if (!ParserInstance.isRegistered(InputSource.InputData.class)) {
         ParserInstance.registerData(InputSource.InputData.class, InputSource.InputData::new);
      }
   }
}
