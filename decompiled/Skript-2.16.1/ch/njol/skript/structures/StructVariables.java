package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.variables.Variables;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Variables")
@Description({"Used for defining variables present within a script.", "This section is not required, but it ensures that a variable has a value if it doesn't exist when the script is loaded."})
@Example.Examples({@Example("variables:\n\t{joins} = 0\n\t{balance::%player%} = 0\n"), @Example("on join:\n\tadd 1 to {joins}\n\tmessage \"Your balance is %{balance::%player%}%\"\n")})
@Since({"1.0"})
public class StructVariables extends Structure {
   public static final Structure.Priority PRIORITY = new Structure.Priority(300);

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult, @Nullable EntryContainer entryContainer) {
      SectionNode node = entryContainer.getSource();
      node.convertToEntries(0, "=");
      Script script = this.getParser().getCurrentScript();
      StructVariables.DefaultVariables existing = script.getData(StructVariables.DefaultVariables.class);
      List<NonNullPair<String, Object>> variables;
      if (existing != null && existing.hasDefaultVariables()) {
         variables = new ArrayList<>(existing.variables);
      } else {
         variables = new ArrayList<>();
      }

      for (Node n : node) {
         if (!(n instanceof EntryNode)) {
            Skript.error("Invalid line in variables structure");
         } else {
            String name = n.getKey().toLowerCase(Locale.ENGLISH);
            if (name.startsWith("{") && name.endsWith("}")) {
               name = name.substring(1, name.length() - 1);
            } else {
               Skript.warning(
                  "It is suggested to use brackets around the name of a variable. Example: {example::%player%} = 5\nExcluding brackets is deprecated, meaning this warning will become an error in the future."
               );
            }

            if (name.startsWith("_")) {
               Skript.error("'" + name + "' cannot be a local variable in default variables structure");
            } else if (!name.contains("<") && !name.contains(">")) {
               String var = name;
               name = StringUtils.replaceAll(
                  name,
                  "%(.+?)%",
                  m -> {
                     if (!m.group(1).contains("{") && !m.group(1).contains("}") && !m.group(1).contains("%")) {
                        ClassInfo<?> cix = Classes.getClassInfoFromUserInput(
                           StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group(1))
                        );
                        if (cix == null) {
                           Skript.error("Can't understand the type '" + m.group(1) + "'");
                           return null;
                        } else {
                           return "<" + cix.getCodeName() + ">";
                        }
                     } else {
                        Skript.error("'" + var + "' is not a valid name for a default variable");
                        return null;
                     }
                  }
               );
               if (name != null) {
                  if (name.contains("%")) {
                     Skript.error("Invalid use of percent signs in variable name");
                  } else {
                     ParseLogHandler log = SkriptLogger.startParseLogHandler();

                     Object o;
                     try {
                        o = Classes.parseSimple(((EntryNode)n).getValue(), Object.class, ParseContext.SCRIPT);
                        if (o == null) {
                           log.printError("Can't understand the value '" + ((EntryNode)n).getValue() + "'");
                           continue;
                        }

                        log.printLog();
                     } finally {
                        log.stop();
                     }

                     ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
                     if (ci.getSerializer() == null) {
                        Skript.error("Can't save '" + ((EntryNode)n).getValue() + "' in a variable");
                     } else {
                        if (ci.getSerializeAs() != null) {
                           ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
                           if (as == null) {
                              assert false : ci;
                              continue;
                           }

                           o = Converters.convert(o, as.getC());
                           if (o == null) {
                              Skript.error("Can't save '" + ((EntryNode)n).getValue() + "' in a variable");
                              continue;
                           }
                        }

                        variables.add(new NonNullPair<>(name, o));
                     }
                  }
               }
            } else {
               Skript.error("'" + name + "' cannot have symbol '<' or '>' within the definition");
            }
         }
      }

      script.addData(new StructVariables.DefaultVariables(variables));
      return true;
   }

   @Override
   public boolean load() {
      StructVariables.DefaultVariables data = this.getParser().getCurrentScript().getData(StructVariables.DefaultVariables.class);
      if (data == null) {
         Skript.error("Default variables data missing");
         return false;
      } else if (data.isLoaded()) {
         return true;
      } else {
         Task.callSync(() -> {
            for (NonNullPair<String, Object> pair : data.getVariables()) {
               String name = pair.getKey();
               if (Variables.getVariable(name, null, false) == null) {
                  Variables.setVariable(name, pair.getValue(), null, false);
               }
            }

            return null;
         });
         data.loaded = true;
         return true;
      }
   }

   @Override
   public void postUnload() {
      Script script = this.getParser().getCurrentScript();
      StructVariables.DefaultVariables data = script.getData(StructVariables.DefaultVariables.class);
      if (data != null) {
         for (NonNullPair<String, Object> pair : data.getVariables()) {
            String name = pair.getKey();
            if (name.contains("<") && name.contains(">")) {
               Variables.setVariable(pair.getKey(), null, null, false);
            }
         }

         script.removeData(StructVariables.DefaultVariables.class);
      }
   }

   @Override
   public Structure.Priority getPriority() {
      return PRIORITY;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "variables";
   }

   static {
      Skript.registerStructure(StructVariables.class, "variables");
   }

   public static class DefaultVariables implements ScriptData {
      private final Deque<Map<String, Class<?>[]>> hints = Queues.synchronizedDeque(new ArrayDeque());
      private final List<NonNullPair<String, Object>> variables;
      private boolean loaded;

      public DefaultVariables(Collection<NonNullPair<String, Object>> variables) {
         this.variables = ImmutableList.copyOf(variables);
      }

      public void add(String variable, Class<?>... hints) {
         if (hints != null && hints.length != 0) {
            if (!CollectionUtils.containsAll(hints, Object.class)) {
               Map<String, Class<?>[]> map = this.hints.peekFirst();
               if (map != null) {
                  map.put(variable, hints);
               }
            }
         }
      }

      public void enterScope() {
         this.hints.push(new HashMap<>());
      }

      public void exitScope() {
         this.hints.pop();
      }

      @Nullable
      public Class<?>[] get(String variable) {
         synchronized (this.hints) {
            for (Map<String, Class<?>[]> map : this.hints) {
               Class<?>[] hints = map.get(variable);
               if (hints != null && hints.length > 0) {
                  return hints;
               }
            }

            return null;
         }
      }

      public boolean hasDefaultVariables() {
         return !this.variables.isEmpty();
      }

      @Unmodifiable
      public List<NonNullPair<String, Object>> getVariables() {
         return this.variables;
      }

      private boolean isLoaded() {
         return this.loaded;
      }
   }
}
