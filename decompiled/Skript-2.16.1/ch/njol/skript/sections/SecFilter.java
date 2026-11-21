package ch.njol.skript.sections;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Filter")
@Description({"Filters a variable list based on the supplied conditions. Unlike the filter expression, this effect maintains the indices of the filtered list.", "It also supports filtering based on meeting any of the given criteria, rather than all, like multi-line if statements."})
@Example.Examples({@Example("set {_a::*} to integers between -10 and 10"), @Example("filter {_a::*} to match:\n\tinput is a number\n\tmod(input, 2) = 0\n\tinput > 0\nsend {_a::*} # sends 2, 4, 6, 8, and 10\n")})
@Since({"2.10"})
public class SecFilter extends Section implements InputSource {
   @UnknownNullability
   private Variable<?> unfilteredObjects;
   private final List<Condition> conditions = new ArrayList<>();
   private boolean isAny;
   @Nullable
   private Object currentValue;
   @UnknownNullability
   private String currentIndex;
   private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

   @Override
   public boolean init(
      Expression<?>[] expressions,
      int matchedPattern,
      Kleenean isDelayed,
      SkriptParser.ParseResult parseResult,
      SectionNode sectionNode,
      List<TriggerItem> triggerItems
   ) {
      if (!expressions[0].isSingle() && expressions[0] instanceof Variable) {
         this.unfilteredObjects = (Variable<?>)expressions[0];
         this.isAny = parseResult.hasTag("any");
         ParserInstance parser = this.getParser();
         if (sectionNode.isEmpty()) {
            Skript.error("filter sections must contain at least one condition");
            return false;
         } else {
            InputSource.InputData inputData = this.getParser().getData(InputSource.InputData.class);
            InputSource originalSource = inputData.getSource();
            inputData.setSource(this);

            try {
               for (Node childNode : sectionNode) {
                  if (!(childNode instanceof SimpleNode)) {
                     Skript.error("Filter sections may not contain other sections");
                     return false;
                  }

                  String childKey = childNode.getKey();
                  if (childKey != null) {
                     childKey = ScriptLoader.replaceOptions(childKey);
                     parser.setNode(childNode);
                     Condition condition = Condition.parse(childKey, "Can't understand this condition: '" + childKey + "'");
                     parser.setNode(sectionNode);
                     if (condition == null) {
                        return false;
                     }

                     this.conditions.add(condition);
                  }
               }

               return true;
            } finally {
               inputData.setSource(originalSource);
            }
         }
      } else {
         Skript.error("You can only filter list variables!");
         return false;
      }
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      String varName = this.unfilteredObjects.getName().toString(event);
      String varSubName = StringUtils.substring(varName, 0, -1);
      boolean local = this.unfilteredObjects.isLocal();
      Map<String, Object> rawVariable = (Map<String, Object>)this.unfilteredObjects.getRaw(event);
      if (rawVariable == null) {
         return this.getNext();
      } else {
         int initialSize = rawVariable.size();
         List<Pair<String, Object>> toKeep = new ArrayList<>();
         List<String> toRemove = new ArrayList<>();
         Iterator<Pair<String, Object>> variableIterator = Variables.getVariableIterator(varName, local, event);
         Stream<Pair<String, Object>> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(variableIterator, 16), false);
         if (this.isAny) {
            stream.forEach(pair -> {
               this.currentValue = pair.getValue();
               this.currentIndex = pair.getKey();
               if (this.conditions.stream().anyMatch(c -> c.check(event))) {
                  toKeep.add((Pair<String, Object>)pair);
               } else {
                  toRemove.add(pair.getKey());
               }
            });
         } else {
            stream.forEach(pair -> {
               this.currentValue = pair.getValue();
               this.currentIndex = pair.getKey();
               if (this.conditions.stream().allMatch(c -> c.check(event))) {
                  toKeep.add((Pair<String, Object>)pair);
               } else {
                  toRemove.add(pair.getKey());
               }
            });
         }

         if (toKeep.size() < initialSize / 2) {
            Variables.deleteVariable(varName, event, local);

            for (Pair<String, Object> pair : toKeep) {
               Variables.setVariable(varSubName + pair.getKey(), pair.getValue(), event, local);
            }
         } else {
            for (String index : toRemove) {
               Variables.setVariable(varSubName + index, null, event, local);
            }
         }

         return this.getNext();
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

   @UnknownNullability
   @Override
   public String getCurrentIndex() {
      return this.currentIndex;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "filter " + this.unfilteredObjects.toString(event, debug) + " to match " + (this.isAny ? "any" : "all");
   }

   static {
      Skript.registerSection(SecFilter.class, "filter %~objects% to match [:any|all]");
      if (!ParserInstance.isRegistered(InputSource.InputData.class)) {
         ParserInstance.registerData(InputSource.InputData.class, InputSource.InputData::new);
      }
   }
}
