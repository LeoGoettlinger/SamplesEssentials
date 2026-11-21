package ch.njol.skript.command;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import java.lang.invoke.StringConcatFactory;
import java.util.WeakHashMap;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class Argument<T> {
   @Nullable
   private final String name;
   @Nullable
   private final Expression<? extends T> def;
   private final ClassInfo<T> type;
   private final boolean single;
   private final int index;
   private final boolean optional;
   private transient WeakHashMap<Event, T[]> current = new WeakHashMap<>();

   private Argument(@Nullable String name, @Nullable Expression<? extends T> def, ClassInfo<T> type, boolean single, int index, boolean optional) {
      this.name = name;
      this.def = def;
      this.type = type;
      this.single = single;
      this.index = index;
      this.optional = optional;
   }

   @Nullable
   public static <T> Argument<T> newInstance(@Nullable String name, ClassInfo<T> type, @Nullable String def, int index, boolean single, boolean forceOptional) {
      if (name != null && !Variable.isValidVariableName(name, false, false)) {
         Skript.error("An argument's name must be a valid variable name, and cannot be a list variable.");
         return null;
      } else {
         Expression<? extends T> d = null;
         if (def == null) {
            return new Argument<>(name, d, type, single, index, def != null || forceOptional);
         } else if (def.startsWith("%") && def.endsWith("%")) {
            RetainingLogHandler log = SkriptLogger.startRetainingLog();

            try {
               d = new SkriptParser(
                     StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(def.substring(1, def.length() - 1)),
                     1,
                     ParseContext.COMMAND
                  )
                  .parseExpression(type.getC());
               if (d != null) {
                  log.printLog();
                  return new Argument<>(name, d, type, single, index, def != null || forceOptional);
               } else {
                  log.printErrors("Can't understand this expression: " + def);
                  return null;
               }
            } finally {
               log.stop();
            }
         } else {
            RetainingLogHandler log = SkriptLogger.startRetainingLog();

            try {
               if (type.getC() == String.class) {
                  if (def.startsWith("\"") && def.endsWith("\"")) {
                     d = VariableString.newInstance(
                        StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(def.substring(1, def.length() - 1))
                     );
                  } else {
                     d = new SimpleLiteral<>((T)def, false);
                  }
               } else {
                  d = new SkriptParser(def, 2, ParseContext.DEFAULT).parseExpression(type.getC());
               }

               if (d != null) {
                  log.printLog();
                  return new Argument<>(name, d, type, single, index, def != null || forceOptional);
               } else {
                  log.printErrors("Can't understand this expression: '" + def + "'");
                  return null;
               }
            } finally {
               log.stop();
            }
         }
      }
   }

   @Override
   public String toString() {
      Expression<? extends T> def = this.def;
      return "<"
         + (this.name != null ? this.name + ": " : "")
         + Utils.toEnglishPlural(this.type.getCodeName(), !this.single)
         + (def == null ? "" : " = " + def.toString())
         + ">";
   }

   public boolean isOptional() {
      return this.optional;
   }

   public void setToDefault(ScriptCommandEvent event) {
      if (this.def != null) {
         this.set(event, this.def.getArray(event));
      }
   }

   public void set(ScriptCommandEvent e, Object[] o) {
      if (!this.type.getC().isAssignableFrom(o.getClass().getComponentType())) {
         throw new IllegalArgumentException();
      } else {
         this.current.put(e, o);
         String name = this.name;
         if (name != null) {
            if (this.single) {
               if (o.length > 0) {
                  Variables.setVariable(name, o[0], e, true);
               }
            } else {
               for (int i = 0; i < o.length; i++) {
                  Variables.setVariable(name + "::" + (i + 1), o[i], e, true);
               }
            }
         }
      }
   }

   @Nullable
   public T[] getCurrent(Event e) {
      return (T[])((Object[])this.current.get(e));
   }

   @Nullable
   public String getName() {
      return this.name;
   }

   public Class<T> getType() {
      return this.type.getC();
   }

   public int getIndex() {
      return this.index;
   }

   public boolean isSingle() {
      return this.single;
   }
}
