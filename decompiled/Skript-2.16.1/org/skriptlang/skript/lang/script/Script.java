package org.skriptlang.skript.lang.script;

import ch.njol.skript.config.Config;
import ch.njol.skript.lang.util.common.AnyNamed;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.util.Validated;
import org.skriptlang.skript.util.event.EventRegistry;

public final class Script implements Validated, AnyNamed {
   private final Config config;
   private final List<Structure> structures;
   private final Set<ScriptWarning> suppressedWarnings = new HashSet<>(ScriptWarning.values().length);
   private final Map<Class<? extends ScriptData>, ScriptData> scriptData = new ConcurrentHashMap<>(5);
   private final EventRegistry<Script.Event> eventRegistry = new EventRegistry<>();

   @Internal
   public Script(Config config, List<Structure> structures) {
      this.config = config;
      this.structures = structures;
   }

   public Config getConfig() {
      return this.config;
   }

   @Unmodifiable
   public List<Structure> getStructures() {
      return Collections.unmodifiableList(this.structures);
   }

   public void suppressWarning(ScriptWarning warning) {
      this.suppressedWarnings.add(warning);
   }

   public void allowWarning(ScriptWarning warning) {
      this.suppressedWarnings.remove(warning);
   }

   public boolean suppressesWarning(ScriptWarning warning) {
      return this.suppressedWarnings.contains(warning);
   }

   @Experimental
   public void addData(ScriptData data) {
      this.scriptData.put((Class<? extends ScriptData>)data.getClass(), data);
   }

   @Experimental
   public void removeData(Class<? extends ScriptData> dataType) {
      this.scriptData.remove(dataType);
   }

   @Experimental
   public void clearData() {
      this.scriptData.clear();
   }

   @Experimental
   @Nullable
   public <Type extends ScriptData> Type getData(Class<Type> dataType) {
      return (Type)this.scriptData.get(dataType);
   }

   @Experimental
   public <Value extends ScriptData> Value getData(Class<? extends Value> dataType, Supplier<Value> mapper) {
      return (Value)this.scriptData.computeIfAbsent(dataType, clazz -> mapper.get());
   }

   @Override
   public String name() {
      return this.config.name();
   }

   public String nameAndPath() {
      String name = this.config.getFileName();
      if (name == null) {
         return null;
      } else {
         return name.contains(".") ? name.substring(0, name.lastIndexOf(46)) : name;
      }
   }

   public EventRegistry<Script.Event> eventRegistry() {
      return this.eventRegistry;
   }

   @Override
   public void invalidate() {
      this.config.invalidate();
   }

   @Override
   public boolean valid() {
      if (!this.config.valid()) {
         return false;
      } else {
         File file = this.config.getFile();
         return file == null || file.exists();
      }
   }

   public interface Event extends org.skriptlang.skript.util.event.Event {
   }
}
