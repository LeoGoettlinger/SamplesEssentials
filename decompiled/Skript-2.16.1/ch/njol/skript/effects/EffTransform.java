package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Transform List")
@Description({"Transforms (or 'maps') a list's values using a given expression. This is akin to looping over the list and setting each value to a modified version of itself.", "Evaluates the given expression for each element in the list, replacing the original element with the expression's result.", "If the given expression returns a single value, the indices of the list will not change. If the expression returns multiple values, then then indices will be reset as a single index cannot contain multiple values.", "Only variable lists can be transformed with this effect. For other lists, see the transform expression."})
@Example.Examples({@Example("set {_a::*} to 1, 2, and 3\ntransform {_a::*} using input * 2\n# {_a::*} is now 2, 4, and 6\n"), @Example("# get a list of the sizes of all clans without manually looping\nset {_clan-sizes::*} to indices of {clans::*}\ntransform {_clan-sizes::*} using {clans::%input%::size}\n"), @Example("# set all existing values of a list to 0:\ntransform {_list::*} with 0\n")})
@Since({"2.10"})
@Keywords({"input"})
public class EffTransform extends Effect implements InputSource {
   @UnknownNullability
   private Expression<?> mappingExpr;
   @UnknownNullability
   private Variable<?> unmappedObjects;
   private final Set<ExprInput<?>> dependentInputs = new HashSet<>();
   @Nullable
   private Object currentValue;
   @UnknownNullability
   private String currentIndex;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (parseResult.regexes.isEmpty()) {
         return false;
      } else if (!expressions[0].isSingle() && expressions[0] instanceof Variable<?> variable) {
         this.unmappedObjects = variable;
         String var7 = parseResult.regexes.get(0).group();
         this.mappingExpr = this.parseExpression(var7, this.getParser(), 3);
         if (this.mappingExpr == null) {
            return false;
         } else {
            if (HintManager.canUseHints(variable)) {
               this.getParser().getHintManager().set(variable, this.mappingExpr.possibleReturnTypes());
            }

            return true;
         }
      } else {
         Skript.error("You can only transform list variables!");
         return false;
      }
   }

   @Override
   protected void execute(Event event) {
      Map<String, Object> mappedValues = new HashMap<>();

      assert this.mappingExpr != null;

      boolean isSingle = this.mappingExpr.isSingle();
      String varName = this.unmappedObjects.getName().toString(event);
      String varSubName = StringUtils.substring(varName, 0, -1);
      boolean local = this.unmappedObjects.isLocal();
      int i = 1;
      Iterator<? extends KeyedValue<?>> it = this.unmappedObjects.keyedIterator(event);

      while (it.hasNext()) {
         KeyedValue<?> keyedValue = (KeyedValue<?>)it.next();
         this.currentIndex = keyedValue.key();
         this.currentValue = keyedValue.value();
         if (isSingle) {
            mappedValues.put(this.currentIndex, this.mappingExpr.getSingle(event));
         } else {
            for (Object value : this.mappingExpr.getArray(event)) {
               mappedValues.put(String.valueOf(i++), value);
               mappedValues.putIfAbsent(this.currentIndex, null);
            }
         }
      }

      for (Entry<String, Object> pair : mappedValues.entrySet()) {
         Variables.setVariable(varSubName + pair.getKey(), pair.getValue(), event, local);
      }
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
      return true;
   }

   @UnknownNullability
   @Override
   public String getCurrentIndex() {
      return this.currentIndex;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "transform " + this.unmappedObjects.toString(event, debug) + " using " + this.mappingExpr.toString(event, debug);
   }

   static {
      Skript.registerEffect(EffTransform.class, "(transform|map) %~objects% (using|with) <.+>");
      if (!ParserInstance.isRegistered(InputSource.InputData.class)) {
         ParserInstance.registerData(InputSource.InputData.class, InputSource.InputData::new);
      }
   }
}
