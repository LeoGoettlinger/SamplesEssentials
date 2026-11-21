package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.structures.StructVariables;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.util.IndexTrackingTreeMap;

public class Variable<T> implements Expression<T>, KeyReceiverExpression<T>, KeyProviderExpression<T> {
   private static final String SINGLE_SEPARATOR_CHAR = ":";
   public static final String SEPARATOR = "::";
   public static final String LOCAL_VARIABLE_TOKEN = "_";
   public static final String EPHEMERAL_VARIABLE_TOKEN = "-";
   private static final char[] reservedTokens = new char[]{'~', '.', '+', '$', '!', '&', '^', '*'};
   @Nullable
   private final Script script;
   private final VariableString name;
   private final Class<T> superType;
   private final Class<? extends T>[] types;
   private final boolean local;
   private final boolean ephemeral;
   private final boolean list;
   @Nullable
   private final Variable<?> source;
   private final Map<Event, String[]> cache = Collections.synchronizedMap(new WeakHashMap<>());
   private Variable.ListProvider listProvider = new Variable.ShallowListProvider();

   private Variable(VariableString name, Class<? extends T>[] types, boolean local, boolean ephemeral, boolean list, @Nullable Variable<?> source) {
      assert types.length > 0;

      assert name.isSimple() || name.getMode() == StringMode.VARIABLE_NAME;

      ParserInstance parser = this.getParser();
      this.script = parser.isActive() ? parser.getCurrentScript() : null;
      this.local = local;
      this.ephemeral = ephemeral;
      this.list = list;
      this.name = name;
      this.types = types;
      this.superType = (Class<T>)Classes.getSuperClassInfo(types).getC();
      this.source = source;
   }

   public static boolean isValidVariableName(String name, boolean allowListVariable, boolean printErrors) {
      assert !name.isEmpty() : "Variable name should not be empty";

      char first = name.charAt(0);

      for (char token : reservedTokens) {
         if (first == token && printErrors) {
            Skript.warning("The character '" + token + "' is reserved at the start of variable names, and may be restricted in future versions");
         }
      }

      name = name.startsWith("_") ? name.substring("_".length()).trim() : name.trim();
      if (!allowListVariable && name.contains("::")) {
         if (printErrors) {
            Skript.error("List variables are not allowed here (error in variable {" + name + "})");
         }

         return false;
      } else if (!name.startsWith("::") && !name.endsWith("::")) {
         if (name.contains("*") && (!allowListVariable || name.indexOf("*") != name.length() - 1 || !name.endsWith("::*"))) {
            List<Integer> asterisks = new ArrayList<>();
            List<Integer> percents = new ArrayList<>();

            for (int i = 0; i < name.length(); i++) {
               char character = name.charAt(i);
               if (character == '*') {
                  asterisks.add(i);
               } else if (character == '%') {
                  percents.add(i);
               }
            }

            int count = asterisks.size();
            int index = 0;

            for (int ix = 0; ix < percents.size() && index != asterisks.size() && ix + 1 != percents.size(); ix += 2) {
               int lowerBound = percents.get(ix);

               for (int upperBound = percents.get(ix + 1);
                  index < asterisks.size() && lowerBound < asterisks.get(index) && asterisks.get(index) < upperBound;
                  index++
               ) {
                  count--;
               }
            }

            if (count != 0 && (count != 1 || !name.endsWith("::*"))) {
               if (printErrors) {
                  Skript.error(
                     "A variable's name must not contain any asterisks except at the end after '::' to denote a list variable, e.g. {variable::*} (error in variable {"
                        + name
                        + "})"
                  );
               }

               return false;
            }
         } else {
            if (name.contains("::::")) {
               if (printErrors) {
                  Skript.error("A variable's name must not contain the separator '::' multiple times in a row (error in variable {" + name + "})");
               }

               return false;
            }

            if (name.replace("::", "").contains(":") && printErrors) {
               ParserInstance parser = ParserInstance.get();
               Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
               if ((currentScript == null || !currentScript.suppressesWarning(ScriptWarning.VARIABLE_CONTAINS_COLON))
                  && !SkriptConfig.disableColonInVariableWarnings.value()) {
                  Skript.warning("If you meant to make the variable {" + name + "} a list, its name should contain '::'. Having a single ':' does nothing!");
               }
            }
         }

         return true;
      } else {
         if (printErrors) {
            Skript.error("A variable's name must neither start nor end with the separator '::' (error in variable {" + name + "})");
         }

         return false;
      }
   }

   @Nullable
   public static <T> Variable<T> newInstance(String name, Class<? extends T>[] types) {
      name = name.trim();
      if (!isValidVariableName(name, true, true)) {
         return null;
      } else {
         VariableString variableString = VariableString.newInstance(name.startsWith("_") ? name.substring("_".length()).trim() : name, StringMode.VARIABLE_NAME);
         if (variableString == null) {
            return null;
         } else {
            boolean isLocal = name.startsWith("_");
            boolean isEphemeral = name.startsWith("-");
            boolean isPlural = name.endsWith("::*");
            ParserInstance parser = ParserInstance.get();
            Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
            if (currentScript != null
               && !SkriptConfig.disableVariableStartingWithExpressionWarnings.value()
               && !currentScript.suppressesWarning(ScriptWarning.VARIABLE_STARTS_WITH_EXPRESSION)) {
               String strippedName = name;
               if (isLocal) {
                  strippedName = name.substring("_".length());
               } else if (isEphemeral) {
                  strippedName = name.substring("-".length());
               }

               if (strippedName.startsWith("%")) {
                  Skript.warning(
                     "Starting a variable's name with an expression is discouraged ({"
                        + name
                        + "}). You could prefix it with the script's name: {"
                        + StringUtils.substring(currentScript.getConfig().getFileName(), 0, -3)
                        + "::"
                        + name
                        + "}"
                  );
               }
            }

            if (isLocal && variableString.isSimple()) {
               Set<Class<?>> hints = parser.getHintManager().get(variableString.toString(null));
               if (!hints.isEmpty()) {
                  if (types[0] == Object.class) {
                     return new Variable<>(variableString, hints.toArray(new Class[0]), true, isEphemeral, isPlural, null);
                  }

                  List<Class<? extends T>> potentialTypes = new ArrayList<>();

                  for (Class<? extends T> type : types) {
                     if (hints.stream().anyMatch(hint -> type.isAssignableFrom((Class<?>)hint) || Converters.converterExists((Class<?>)hint, type))) {
                        potentialTypes.add(type);
                     }
                  }

                  if (!potentialTypes.isEmpty()) {
                     return new Variable<>(variableString, potentialTypes.toArray(Class[]::new), true, isEphemeral, isPlural, null);
                  }

                  ClassInfo<?>[] infos = new ClassInfo[types.length];

                  for (int i = 0; i < types.length; i++) {
                     infos[i] = Classes.getSuperClassInfo(types[i]);
                  }

                  ClassInfo<?>[] hintInfos = hints.stream().map(Classes::getSuperClassInfo).toArray(ClassInfo[]::new);
                  String isTypes = Utils.a(Classes.toString(hintInfos, false));
                  String notTypes = Utils.a(Classes.toString(infos, false));
                  Skript.error("Expected variable '{_" + variableString.toString(null) + "}' to be " + notTypes + ", but it is " + isTypes);
                  return null;
               }
            }

            return new Variable<>(variableString, types, isLocal, isEphemeral, isPlural, null);
         }
      }
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      throw new UnsupportedOperationException();
   }

   public boolean isLocal() {
      return this.local;
   }

   public boolean isEphemeral() {
      return this.ephemeral;
   }

   public boolean isList() {
      return this.list;
   }

   @Override
   public boolean isSingle() {
      return !this.list;
   }

   @Override
   public Class<? extends T> getReturnType() {
      return this.superType;
   }

   @Override
   public Class<? extends T>[] possibleReturnTypes() {
      return Arrays.copyOf(this.types, this.types.length);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      StringBuilder stringBuilder = new StringBuilder().append("{");
      if (this.local) {
         stringBuilder.append("_");
      }

      stringBuilder.append(StringUtils.substring(this.name.toString(event, debug), 1, -1)).append("}");
      if (debug) {
         stringBuilder.append(" (");
         if (event != null) {
            stringBuilder.append(Classes.toString(this.get(event))).append(", ");
         }

         stringBuilder.append("as ").append(this.superType.getName()).append(")");
      }

      return stringBuilder.toString();
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   public <R> Variable<R> getConvertedExpression(Class<R>... to) {
      boolean converterExists = this.superType == Object.class;
      if (!converterExists) {
         for (Class<?> type : this.types) {
            if (Converters.converterExists(type, to)) {
               converterExists = true;
               break;
            }
         }
      }

      return !converterExists ? null : new Variable<>(this.name, to, this.local, this.ephemeral, this.list, this);
   }

   @Nullable
   public Object getRaw(Event event) {
      StructVariables.DefaultVariables data = this.script == null ? null : this.script.getData(StructVariables.DefaultVariables.class);
      if (data != null) {
         data.enterScope();
      }

      Object var7;
      try {
         String name = this.name.toString(event);
         if (name.endsWith("::*") != this.list) {
            return null;
         }

         Object value = !this.list
            ? convertIfOldPlayer(name, this.local, event, Variables.getVariable(name, event, this.local))
            : Variables.getVariable(name, event, this.local);
         if (value != null) {
            return value;
         }

         if (data == null || !data.hasDefaultVariables()) {
            return null;
         }

         Iterator var5 = this.name.getDefaultVariableNames(name, event).iterator();

         do {
            if (!var5.hasNext()) {
               return null;
            }

            String typeHint = (String)var5.next();
            value = Variables.getVariable(typeHint, event, false);
         } while (value == null);

         var7 = value;
      } finally {
         if (data != null) {
            data.exitScope();
         }
      }

      return var7;
   }

   @Nullable
   private Object get(Event event) {
      Object rawValue = this.getRaw(event);
      if (!this.list) {
         return rawValue;
      } else {
         KeyedValue<?>[] values = this.listProvider.getValues(event);
         return KeyedValue.unzip(values).values().toArray();
      }
   }

   @Nullable
   public static <T> T convertIfOldPlayer(String key, boolean local, Event event, @Nullable T object) {
      if (SkriptConfig.enablePlayerVariableFix.value() && object instanceof Player oldPlayer && !oldPlayer.isValid() && oldPlayer.isOnline()) {
         Player newPlayer = Bukkit.getPlayer(oldPlayer.getUniqueId());
         Variables.setVariable(key, newPlayer, event, local);
         return (T)newPlayer;
      } else {
         return object;
      }
   }

   @Override
   public Iterator<KeyedValue<T>> keyedIterator(Event event) {
      if (!this.list) {
         throw new SkriptAPIException("Invalid call to keyedIterator");
      } else {
         Iterator<KeyedValue<?>> iterator = Iterators.forArray(this.listProvider.getValues(event));
         Iterator<KeyedValue<T>> transformed = Iterators.transform(iterator, value -> {
            assert value != null;

            T converted = Converters.convert(value.value(), this.types);
            return converted == null ? null : new KeyedValue(value.key(), converted);
         });
         return Iterators.filter(transformed, Objects::nonNull);
      }
   }

   public Iterator<Pair<String, Object>> variablesIterator(Event event) {
      if (!this.list) {
         throw new SkriptAPIException("Looping a non-list variable");
      } else {
         return Variables.getVariableIterator(this.name.toString(event), this.local, event);
      }
   }

   @Nullable
   @Override
   public Iterator<T> iterator(Event event) {
      if (!this.list) {
         T value = this.getSingle(event);
         return value != null ? new SingleItemIterator<>(value) : null;
      } else {
         return Iterators.transform(this.keyedIterator(event), KeyedValue::value);
      }
   }

   @Nullable
   private T getConverted(Event event) {
      assert !this.list;

      return Converters.convert(this.get(event), this.types);
   }

   private T[] getConvertedArray(Event event) {
      assert this.list;

      KeyedValue<Object>[] values = (KeyedValue<Object>[])this.listProvider.getValues(event);
      KeyedValue<T>[] mappedValues = KeyedValue.map(values, value -> Converters.convert(value, this.types));
      mappedValues = (KeyedValue<T>[])ArrayUtils.removeAllOccurrences(mappedValues, null);
      KeyedValue.UnzippedKeyValues<T> unzipped = KeyedValue.unzip(mappedValues);
      this.cache.put(event, unzipped.keys().toArray(new String[0]));
      return (T[])unzipped.values().toArray((T[])((Object[])Array.newInstance(this.superType, 0)));
   }

   private void set(Event event, @Nullable Object value) {
      Variables.setVariable(this.name.toString(event), value, event, this.local);
   }

   private void setIndex(Event event, String index, @Nullable Object value) {
      assert this.list;

      String name = this.name.toString(event);

      assert name.endsWith("::*") : name + "; " + this.name;

      Variables.setVariable(name.substring(0, name.length() - 1) + index, value, event, this.local);
   }

   public int size(Event event) {
      Preconditions.checkState(this.list, "Cannot get the size of a single variable");
      Map<?, ?> map = (Map<?, ?>)this.getRaw(event);
      if (map == null) {
         return 0;
      } else {
         int size = map.size();
         if (map.containsKey(null)) {
            size--;
         }

         if (map instanceof IndexTrackingTreeMap<?> indexTrackingMap) {
            for (String sublistIndex : indexTrackingMap.mapIndices()) {
               if (!((Map)map.get(sublistIndex)).containsKey(null)) {
                  size--;
               }
            }

            return size;
         } else {
            for (Object value : map.values()) {
               if (value instanceof Map<?, ?> sublist && !sublist.containsKey(null)) {
                  size--;
               }
            }

            return size;
         }
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return !this.list && mode == Changer.ChangeMode.SET ? CollectionUtils.array(Object.class) : CollectionUtils.array(Object[].class);
   }

   @Override
   public void change(Event event, @NotNull Object[] delta, Changer.ChangeMode mode, @NotNull String[] keys) {
      if (!this.list) {
         this.change(event, delta, mode);
      } else if (mode != Changer.ChangeMode.SET) {
         this.change(event, delta, mode);
      } else {
         assert delta.length == keys.length;

         this.set(event, null);
         int length = Math.min(delta.length, keys.length);

         for (int index = 0; index < length; index++) {
            Object value = delta[index];
            String key = keys[index];
            if (value instanceof Object[] array) {
               for (int j = 0; j < array.length; j++) {
                  this.setIndex(event, key + "::" + (j + 1), array[j]);
               }
            } else {
               this.setIndex(event, key, value);
            }
         }
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      switch (mode) {
         case DELETE:
            this.set(event, null);
            break;
         case SET:
            assert delta != null;

            if (this.list) {
               this.set(event, null);
               int i = 1;

               for (Object valuexx : delta) {
                  if (valuexx instanceof Object[]) {
                     for (int j = 0; j < ((Object[])valuexx).length; j++) {
                        this.setIndex(event, i + "::" + (j + 1), ((Object[])valuexx)[j]);
                     }
                  } else {
                     this.setIndex(event, i + "", valuexx);
                  }

                  i++;
               }
            } else if (delta.length > 0) {
               this.set(event, delta[0]);
            }
            break;
         case RESET:
            Object rawValue = this.getRaw(event);
            if (rawValue == null) {
               return;
            }

            for (Object values : rawValue instanceof Map ? ((Map)rawValue).values() : Arrays.asList(rawValue)) {
               Class<?> type = values.getClass();

               assert type != null;

               ClassInfo<?> classInfo = Classes.getSuperClassInfo(type);
               Changer<?> changer = classInfo.getChanger();
               if (changer != null && changer.acceptChange(Changer.ChangeMode.RESET) != null) {
                  Object[] valueArray = (Object[])Array.newInstance(values.getClass(), 1);
                  valueArray[0] = values;
                  changer.change(valueArray, null, Changer.ChangeMode.RESET);
               }
            }
            break;
         case ADD:
         case REMOVE:
         case REMOVE_ALL:
            assert delta != null;

            if (this.list) {
               Map<String, Object> map = (Map<String, Object>)this.getRaw(event);
               if (mode == Changer.ChangeMode.REMOVE) {
                  if (map == null) {
                     return;
                  }

                  Set<String> toRemove = new HashSet<>();

                  for (Object value : delta) {
                     for (Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        if (key != null && !toRemove.contains(key) && Relation.EQUAL.isImpliedBy(Comparators.compare(entry.getValue(), value))) {
                           toRemove.add(key);
                           break;
                        }
                     }
                  }

                  for (String index : toRemove) {
                     assert index != null;

                     this.setIndex(event, index, null);
                  }
               } else if (mode == Changer.ChangeMode.REMOVE_ALL) {
                  if (map == null) {
                     return;
                  }

                  Set<String> toRemove = new HashSet<>();

                  for (Entry<String, Object> i : map.entrySet()) {
                     for (Object value : delta) {
                        if (Relation.EQUAL.isImpliedBy(Comparators.compare(i.getValue(), value))) {
                           toRemove.add(i.getKey());
                        }
                     }
                  }

                  for (String index : toRemove) {
                     assert index != null;

                     this.setIndex(event, index, null);
                  }
               } else {
                  assert mode == Changer.ChangeMode.ADD;

                  if (map instanceof IndexTrackingTreeMap<Object> indexTrackingMap) {
                     for (Object valuex : delta) {
                        int index = indexTrackingMap.nextOpenIndex();
                        this.setIndex(event, String.valueOf(index), valuex);
                     }

                     return;
                  }

                  int i = 1;

                  for (Object valuex : delta) {
                     if (map != null) {
                        while (map.containsKey(i + "")) {
                           i++;
                        }
                     }

                     this.setIndex(event, i + "", valuex);
                     i++;
                  }
               }
            } else {
               Object originalValue = this.get(event);
               Class<?> clazz = originalValue == null ? null : originalValue.getClass();
               Operator operator = mode == Changer.ChangeMode.ADD ? Operator.ADDITION : Operator.SUBTRACTION;
               if (clazz != null && Arithmetics.getOperations(operator, clazz).isEmpty()) {
                  Changer<?> changer;
                  Class<?>[] classes;
                  if ((changer = Classes.getSuperClassInfo(clazz).getChanger()) != null && (classes = changer.acceptChange(mode)) != null) {
                     Object[] originalValueArray = (Object[])Array.newInstance(originalValue.getClass(), 1);
                     originalValueArray[0] = originalValue;
                     Class<?>[] classes2 = new Class[classes.length];

                     for (int i = 0; i < classes.length; i++) {
                        classes2[i] = classes[i].isArray() ? classes[i].getComponentType() : classes[i];
                     }

                     ArrayList<Object> convertedDelta = new ArrayList<>();

                     for (Object valuex : delta) {
                        Object convertedValue = Converters.convert(valuex, classes2);
                        if (convertedValue != null) {
                           convertedDelta.add(convertedValue);
                        }
                     }

                     Changer.ChangerUtils.change(changer, originalValueArray, convertedDelta.toArray(), mode);
                  }
               } else {
                  boolean changed = false;

                  for (Object newValue : delta) {
                     OperationInfo info = Arithmetics.getOperationInfo(operator, clazz != null ? clazz : newValue.getClass(), newValue.getClass());
                     if (info != null) {
                        Object valuexx = originalValue == null ? Arithmetics.getDefaultValue(info.left()) : originalValue;
                        if (valuexx != null) {
                           originalValue = info.operation().calculate(valuexx, newValue);
                           changed = true;
                        }
                     }
                  }

                  if (changed) {
                     this.set(event, originalValue);
                  }
               }
            }
      }
   }

   @Override
   public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
      this.changeInPlace(event, changeFunction);
   }

   @Override
   public <R> void changeInPlace(Event event, Function<T, R> changeFunction) {
      if (!this.list) {
         T value = this.getSingle(event);
         if (value != null) {
            this.set(event, changeFunction.apply(value));
         }
      } else {
         this.keyedIterator(event).forEachRemaining(keyedValue -> {
            String index = keyedValue.key();
            Object newValue = changeFunction.apply(keyedValue.value());
            this.setIndex(event, index, newValue);
         });
      }
   }

   @Nullable
   @Override
   public T getSingle(Event event) {
      if (this.list) {
         throw new SkriptAPIException("Invalid call to getSingle");
      } else {
         return this.getConverted(event);
      }
   }

   @NotNull
   @Override
   public String[] getArrayKeys(Event event) throws SkriptAPIException {
      if (!this.list) {
         throw new SkriptAPIException("Invalid call to getArrayKeys on non-list");
      } else if (!this.cache.containsKey(event)) {
         throw new IllegalStateException();
      } else {
         return this.cache.remove(event);
      }
   }

   @NotNull
   @Override
   public String[] getAllKeys(Event event) {
      return this.getArrayKeys(event);
   }

   @Override
   public boolean canReturnKeys() {
      return this.list;
   }

   @Override
   public boolean areKeysRecommended() {
      return false;
   }

   @Override
   public boolean returnNestedStructures(boolean nested) {
      if (!this.canReturnKeys()) {
         return false;
      } else {
         this.listProvider = (Variable.ListProvider)(nested ? new Variable.RecursiveListProvider() : new Variable.ShallowListProvider());
         return true;
      }
   }

   @Override
   public boolean returnsNestedStructures() {
      return this.listProvider.getClass() == Variable.RecursiveListProvider.class;
   }

   @Override
   public T[] getArray(Event event) {
      return this.getAll(event);
   }

   @Override
   public T[] getAll(Event event) {
      if (this.list) {
         return this.getConvertedArray(event);
      } else {
         T value = this.getConverted(event);
         if (value == null) {
            return (T[])((Object[])Array.newInstance(this.superType, 0));
         } else {
            T[] valueArray = (T[])((Object[])Array.newInstance(this.superType, 1));
            valueArray[0] = value;
            return valueArray;
         }
      }
   }

   @Override
   public boolean isLoopOf(String input) {
      return KeyProviderExpression.super.isLoopOf(input)
         || input.equalsIgnoreCase("var")
         || input.equalsIgnoreCase("variable")
         || input.equalsIgnoreCase("value");
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
      return SimpleExpression.check(this.getAll(event), checker, negated, this.getAnd());
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker) {
      return SimpleExpression.check(this.getAll(event), checker, false, this.getAnd());
   }

   public VariableString getName() {
      return this.name;
   }

   @Override
   public boolean getAnd() {
      return true;
   }

   @Override
   public boolean setTime(int time) {
      return false;
   }

   @Override
   public int getTime() {
      return 0;
   }

   @Override
   public boolean isDefault() {
      return false;
   }

   @Override
   public Expression<?> getSource() {
      Variable<?> source = this.source;
      return source == null ? this : source;
   }

   @Override
   public Expression<? extends T> simplify() {
      return this;
   }

   @Override
   public boolean supportsLoopPeeking() {
      return true;
   }

   private interface ListProvider {
      KeyedValue<?>[] getValues(Event var1);
   }

   class RecursiveListProvider implements Variable.ListProvider {
      @Override
      public KeyedValue<?>[] getValues(Event event) {
         if (!Variable.this.list) {
            throw new SkriptAPIException("Invalid call to getValues on non-list variable");
         } else {
            Object rawValue = Variable.this.getRaw(event);
            if (rawValue == null) {
               return new KeyedValue[0];
            } else {
               List<KeyedValue<?>> keyedValues = new ArrayList<>();
               String name = StringUtils.substring(Variable.this.name.toString(event), 0, -1);
               this.getValuesRecursive(event, (Map<?, ?>)rawValue, name, "", keyedValues);
               return keyedValues.toArray(new KeyedValue[0]);
            }
         }
      }

      private void getValuesRecursive(Event event, Map<?, ?> variable, String root, String prefix, List<KeyedValue<?>> values) {
         for (Entry<String, ?> entry : variable.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
               String relativeKey = prefix + entry.getKey();
               String absoluteKey = root + relativeKey;
               Object value;
               if (entry.getValue() instanceof Map<?, ?> sublist) {
                  this.getValuesRecursive(event, (Map<?, ?>)entry.getValue(), root, relativeKey + "::", values);
                  value = sublist.get(null);
               } else {
                  value = entry.getValue();
               }

               value = Variable.convertIfOldPlayer(absoluteKey, Variable.this.local, event, value);
               if (value != null) {
                  values.add(new KeyedValue<>(relativeKey, value));
               }
            }
         }
      }
   }

   class ShallowListProvider implements Variable.ListProvider {
      @Override
      public KeyedValue<?>[] getValues(Event event) {
         if (!Variable.this.list) {
            throw new SkriptAPIException("Invalid call to getValues on non-list variable");
         } else {
            Object rawValue = Variable.this.getRaw(event);
            if (rawValue == null) {
               return new KeyedValue[0];
            } else {
               List<KeyedValue<?>> keyedValues = new ArrayList<>();
               String name = StringUtils.substring(Variable.this.name.toString(event), 0, -1);

               for (Entry<String, ?> variable : ((Map)rawValue).entrySet()) {
                  if (variable.getKey() != null && variable.getValue() != null) {
                     Object value;
                     if (variable.getValue() instanceof Map<?, ?> sublist) {
                        value = sublist.get(null);
                     } else {
                        value = variable.getValue();
                     }

                     value = Variable.convertIfOldPlayer(name + variable.getKey(), Variable.this.local, event, value);
                     if (value != null) {
                        keyedValues.add(new KeyedValue<>(variable.getKey(), value));
                     }
                  }
               }

               return keyedValues.toArray(new KeyedValue[0]);
            }
         }
      }
   }
}
