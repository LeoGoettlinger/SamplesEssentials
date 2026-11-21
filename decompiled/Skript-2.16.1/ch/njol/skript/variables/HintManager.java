package ch.njol.skript.variables;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.slot.Slot;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public class HintManager {
   private final LinkedList<HintManager.Scope> typeHints = new LinkedList<>();
   private boolean isActive;

   public HintManager(boolean active) {
      this.isActive = active;
   }

   public void setActive(boolean active) {
      this.isActive = active;
   }

   public boolean isActive() {
      ParserInstance parser = ParserInstance.get();
      return this.isActive && parser.isActive() && parser.hasExperiment(Feature.TYPE_HINTS);
   }

   public void enterScope(boolean isSection) {
      this.typeHints.push(new HintManager.Scope(new HashMap<>(), isSection));
      if (this.typeHints.size() > 1) {
         this.mergeScope(1, 0, false);
      }
   }

   public void exitScope() {
      if (this.typeHints.size() > 1) {
         this.mergeScope(0, 1, false);
      }

      this.typeHints.pop();
   }

   public void clearScope(int level, boolean sectionOnly) {
      if (level >= 0 && level <= this.typeHints.size() - 1) {
         if (!sectionOnly) {
            this.typeHints.get(level).hintMap().clear();
         } else {
            int currentLevel = 0;
            Iterator<HintManager.Scope> iterator = this.typeHints.iterator();

            while (iterator.hasNext()) {
               HintManager.Scope scope = iterator.next();
               if (scope.isSection()) {
                  if (currentLevel == level) {
                     iterator.remove();
                     return;
                  }

                  currentLevel++;
               }
            }

            throw new IndexOutOfBoundsException("Section scope level " + level + " is out of bounds (expected 0-" + currentLevel + ")");
         }
      } else {
         throw new IndexOutOfBoundsException("Scope level " + level + " is out of bounds (expected 0-" + (this.typeHints.size() - 1) + ")");
      }
   }

   public void mergeScope(int from, int to, boolean sectionOnly) {
      int expectedSize = this.typeHints.size() - 1;
      if (from < 0 || from > expectedSize) {
         throw new IndexOutOfBoundsException("'from' scope level " + from + " is out of bounds (expected 0-" + expectedSize + ")");
      } else if (to >= 0 && to <= expectedSize) {
         HintManager.Scope fromScope = null;
         HintManager.Scope toScope = null;
         if (sectionOnly) {
            int currentLevel = 0;

            for (HintManager.Scope scope : this.typeHints) {
               if (scope.isSection()) {
                  if (currentLevel == from) {
                     fromScope = scope;
                  }

                  if (currentLevel == to) {
                     toScope = scope;
                  }

                  if (fromScope != null && toScope != null) {
                     break;
                  }

                  currentLevel++;
               }
            }

            if (fromScope == null) {
               throw new IndexOutOfBoundsException("'from' section scope level " + from + " is out of bounds (expected 0-" + currentLevel + ")");
            }

            if (toScope == null) {
               throw new IndexOutOfBoundsException("'to' section scope level " + to + " is out of bounds (expected 0-" + currentLevel + ")");
            }
         } else {
            fromScope = this.typeHints.get(from);
            toScope = this.typeHints.get(to);
         }

         mergeHints(fromScope.hintMap(), toScope.hintMap());
      } else {
         throw new IndexOutOfBoundsException("'to' scope level " + to + " is out of bounds (expected 0-" + expectedSize + ")");
      }
   }

   private static void mergeHints(Map<String, Set<Class<?>>> from, Map<String, Set<Class<?>>> to) {
      for (Entry<String, Set<Class<?>>> entry : from.entrySet()) {
         to.computeIfAbsent(entry.getKey(), key -> new HashSet<>()).addAll(entry.getValue());
      }
   }

   public void set(Variable<?> variable, Class<?>... hints) {
      checkCanUseHints(variable);
      this.set(variable.getName().toString(null), hints);
   }

   public void set(String variableName, Class<?>... hints) {
      if (!this.areHintsUnavailable()) {
         this.delete_i(variableName);
         if (hints.length != 0) {
            this.add_i(variableName, Set.of(hints));
         }
      }
   }

   public void delete(Variable<?> variable) {
      checkCanUseHints(variable);
      this.delete(variable.getName().toString(null));
   }

   public void delete(String variableName) {
      if (!this.areHintsUnavailable()) {
         this.delete_i(variableName);
      }
   }

   private void delete_i(String variableName) {
      if (SkriptConfig.caseInsensitiveVariables.value()) {
         variableName = variableName.toLowerCase(Locale.ENGLISH);
      }

      this.typeHints.getFirst().hintMap().remove(variableName);
      if (variableName.endsWith("::*")) {
         String prefix = variableName.substring(0, variableName.length() - 1);
         this.typeHints.getFirst().hintMap().keySet().removeIf(key -> key.startsWith(prefix));
      }
   }

   public void add(Variable<?> variable, Class<?>... hints) {
      checkCanUseHints(variable);
      this.add(variable.getName().toString(null), hints);
   }

   public void add(String variableName, Class<?>... hints) {
      if (!this.areHintsUnavailable()) {
         this.add_i(variableName, Set.of(hints));
      }
   }

   private void add_i(String variableName, Set<Class<?>> hintSet) {
      if (SkriptConfig.caseInsensitiveVariables.value()) {
         variableName = variableName.toLowerCase(Locale.ENGLISH);
      }

      if (hintSet.contains(Slot.class)) {
         hintSet = new HashSet<>(hintSet);
         hintSet.add(ItemStack.class);
      }

      this.typeHints.getFirst().hintMap().computeIfAbsent(variableName, key -> new HashSet<>()).addAll(hintSet);
      if (!variableName.isEmpty() && variableName.charAt(variableName.length() - 1) != '*') {
         int listEnd = variableName.lastIndexOf("::");
         if (listEnd != -1) {
            String listVariableName = variableName.substring(0, listEnd + "::".length()) + "*";
            this.typeHints.getFirst().hintMap().computeIfAbsent(listVariableName, key -> new HashSet<>()).addAll(hintSet);
         }
      }
   }

   public void remove(Variable<?> variable, Class<?>... hints) {
      checkCanUseHints(variable);
      this.remove(variable.getName().toString(null), hints);
   }

   public void remove(String variableName, Class<?>... hints) {
      if (!this.areHintsUnavailable()) {
         if (SkriptConfig.caseInsensitiveVariables.value()) {
            variableName = variableName.toLowerCase(Locale.ENGLISH);
         }

         Set<Class<?>> hintSet = this.typeHints.getFirst().hintMap().get(variableName);
         if (hintSet != null) {
            for (Class<?> hint : hints) {
               hintSet.remove(hint);
            }

            if (hintSet.isEmpty()) {
               this.delete_i(variableName);
            }
         }
      }
   }

   @Unmodifiable
   public Set<Class<?>> get(Variable<?> variable) {
      checkCanUseHints(variable);
      return this.get(variable.getName().toString(null));
   }

   @Unmodifiable
   public Set<Class<?>> get(String variableName) {
      if (this.areHintsUnavailable()) {
         return ImmutableSet.of();
      } else {
         if (SkriptConfig.caseInsensitiveVariables.value()) {
            variableName = variableName.toLowerCase(Locale.ENGLISH);
         }

         Set<Class<?>> hintSet = this.typeHints.getFirst().hintMap().get(variableName);
         return hintSet != null ? ImmutableSet.copyOf(hintSet) : ImmutableSet.of();
      }
   }

   public HintManager.Backup backup() {
      return new HintManager.Backup(this);
   }

   public void restore(HintManager.Backup backup) {
      this.typeHints.set(0, backup.scope);
   }

   private boolean areHintsUnavailable() {
      if (!this.isActive()) {
         return true;
      } else if (this.typeHints.isEmpty()) {
         if (SkriptLogger.debug()) {
            SkriptLogger.LOGGER.warning("Attempted to use type hints outside of any scope");
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean canUseHints(Variable<?> variable) {
      return variable.isLocal() && variable.getName().isSimple();
   }

   private static void checkCanUseHints(Variable<?> variable) {
      if (!canUseHints(variable)) {
         throw new IllegalArgumentException("Variables must be local and have a simple name to have hints");
      }
   }

   public static final class Backup {
      private final HintManager.Scope scope;

      private Backup(HintManager source) {
         this.scope = new HintManager.Scope(new HashMap<>(), source.typeHints.getFirst().isSection);
         HintManager.mergeHints(source.typeHints.getFirst().hintMap(), this.scope.hintMap());
      }
   }

   private record Scope(Map<String, Set<Class<?>>> hintMap, boolean isSection) {
   }
}
