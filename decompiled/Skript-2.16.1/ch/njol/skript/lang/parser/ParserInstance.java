package ch.njol.skript.lang.parser;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.log.HandlerList;
import ch.njol.skript.structures.StructOptions;
import ch.njol.skript.variables.HintManager;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.Experimented;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

public final class ParserInstance implements Experimented {
   private static final ThreadLocal<ParserInstance> PARSER_INSTANCES = ThreadLocal.withInitial(ParserInstance::new);
   private boolean isActive = false;
   private final ExpressionParseCache expressionParseCache = new ExpressionParseCache();
   private final LiteralParseCache literalParseCache = new LiteralParseCache();
   @Nullable
   private Script currentScript = null;
   @Nullable
   private Structure currentStructure = null;
   @Nullable
   private String currentEventName;
   @Nullable
   private Class<? extends Event>[] currentEvents = null;
   private List<TriggerSection> currentSections = new ArrayList<>();
   private Kleenean hasDelayBefore = Kleenean.FALSE;
   private final HandlerList handlers = new HandlerList();
   @Nullable
   private Node node;
   private String indentation = "";
   private final ParsingStack parsingStack = new ParsingStack();
   private HintManager hintManager = new HintManager(true);
   private static final Map<Class<? extends ParserInstance.Data>, Function<ParserInstance, ? extends ParserInstance.Data>> dataRegister = new HashMap<>();
   private final Map<Class<? extends ParserInstance.Data>, ParserInstance.Data> dataMap = new HashMap<>();

   public static ParserInstance get() {
      return PARSER_INSTANCES.get();
   }

   @Internal
   public void setInactive() {
      this.isActive = false;
      this.reset();
      this.literalParseCache.clear();
      this.setCurrentScript((Script)null);
   }

   @Internal
   public void setActive(Script script) {
      this.reset();
      this.hintManager.setActive(true);
      this.isActive = true;
      this.setCurrentScript(script);
   }

   public boolean isActive() {
      return this.isActive;
   }

   public void reset() {
      this.currentStructure = null;
      this.currentEventName = null;
      this.currentEvents = null;
      this.currentSections = new ArrayList<>();
      this.hasDelayBefore = Kleenean.FALSE;
      this.node = null;
      this.hintManager = new HintManager(this.hintManager.isActive());
      this.dataMap.clear();
      this.expressionParseCache.clear();
   }

   public ExpressionParseCache getExpressionParseCache() {
      return this.expressionParseCache;
   }

   public LiteralParseCache getLiteralParseCache() {
      return this.literalParseCache;
   }

   private void setCurrentScript(@Nullable Script currentScript) {
      if (currentScript != this.currentScript) {
         Script previous = this.currentScript;
         this.currentScript = currentScript;
         this.getDataInstances().forEach(data -> data.onCurrentScriptChange(currentScript != null ? currentScript.getConfig() : null));
         if (previous != null) {
            ScriptLoader.eventRegistry()
               .events(ParserInstance.ScriptActivityChangeEvent.class)
               .forEach(event -> event.onActivityChange(this, previous, false, currentScript));
            previous.eventRegistry()
               .events(ParserInstance.ScriptActivityChangeEvent.class)
               .forEach(event -> event.onActivityChange(this, previous, false, currentScript));
         }

         if (currentScript != null) {
            ScriptLoader.eventRegistry()
               .events(ParserInstance.ScriptActivityChangeEvent.class)
               .forEach(event -> event.onActivityChange(this, currentScript, true, previous));
            currentScript.eventRegistry()
               .events(ParserInstance.ScriptActivityChangeEvent.class)
               .forEach(event -> event.onActivityChange(this, currentScript, true, previous));
         }
      }
   }

   public Script getCurrentScript() {
      if (this.currentScript == null) {
         throw new SkriptAPIException("This ParserInstance is not currently parsing/loading something!");
      } else {
         return this.currentScript;
      }
   }

   public void setCurrentStructure(@Nullable Structure structure) {
      this.currentStructure = structure;
   }

   @Nullable
   public Structure getCurrentStructure() {
      return this.currentStructure;
   }

   public boolean isCurrentStructure(Class<? extends Structure> structureClass) {
      return structureClass.isInstance(this.currentStructure);
   }

   @SafeVarargs
   public final boolean isCurrentStructure(Class<? extends Structure>... structureClasses) {
      for (Class<? extends Structure> structureClass : structureClasses) {
         if (this.isCurrentStructure(structureClass)) {
            return true;
         }
      }

      return false;
   }

   public void setCurrentEventName(@Nullable String currentEventName) {
      this.currentEventName = currentEventName;
   }

   @Nullable
   public String getCurrentEventName() {
      return this.currentEventName;
   }

   public void setCurrentEvents(@Nullable Class<? extends Event>[] currentEvents) {
      this.currentEvents = currentEvents;
      this.getDataInstances().forEach(data -> data.onCurrentEventsChange(currentEvents));
   }

   @SafeVarargs
   public final void setCurrentEvent(String name, @Nullable Class<? extends Event>... events) {
      this.currentEventName = name;
      this.setCurrentEvents(events);
      this.setHasDelayBefore(Kleenean.FALSE);
   }

   public void deleteCurrentEvent() {
      this.currentEventName = null;
      this.setCurrentEvents(null);
      this.setHasDelayBefore(Kleenean.FALSE);
   }

   @Nullable
   public Class<? extends Event>[] getCurrentEvents() {
      return this.currentEvents;
   }

   public boolean isCurrentEvent(Class<? extends Event> event) {
      if (this.currentEvents == null) {
         return false;
      } else {
         for (Class<? extends Event> currentEvent : this.currentEvents) {
            if (event.isAssignableFrom(currentEvent)) {
               return true;
            }
         }

         return false;
      }
   }

   @SafeVarargs
   public final boolean isCurrentEvent(Class<? extends Event>... events) {
      for (Class<? extends Event> event : events) {
         if (this.isCurrentEvent(event)) {
            return true;
         }
      }

      return false;
   }

   public void setCurrentSections(List<TriggerSection> currentSections) {
      this.currentSections = currentSections;
   }

   public List<TriggerSection> getCurrentSections() {
      return this.currentSections;
   }

   @Nullable
   public <T extends TriggerSection> T getCurrentSection(Class<T> sectionClass) {
      int i = this.currentSections.size();

      while (i-- > 0) {
         TriggerSection triggerSection = this.currentSections.get(i);
         if (sectionClass.isInstance(triggerSection)) {
            return (T)triggerSection;
         }
      }

      return null;
   }

   @NotNull
   public <T extends TriggerSection> List<T> getCurrentSections(Class<T> sectionClass) {
      List<T> list = new ArrayList<>();

      for (TriggerSection triggerSection : this.currentSections) {
         if (sectionClass.isInstance(triggerSection)) {
            list.add((T)triggerSection);
         }
      }

      return list;
   }

   public List<TriggerSection> getSectionsUntil(TriggerSection section) {
      return new ArrayList<>(this.currentSections.subList(this.currentSections.indexOf(section) + 1, this.currentSections.size()));
   }

   public List<TriggerSection> getSections(int levels) {
      Preconditions.checkArgument(levels > 0, "Depth must be at least 1");
      return new ArrayList<>(this.currentSections.subList(Math.max(this.currentSections.size() - levels, 0), this.currentSections.size()));
   }

   public List<TriggerSection> getSections(int levels, Class<? extends TriggerSection> type) {
      Preconditions.checkArgument(levels > 0, "Depth must be at least 1");
      List<? extends TriggerSection> sections = this.getCurrentSections(type);
      if (sections.isEmpty()) {
         return new ArrayList<>();
      } else {
         TriggerSection section = sections.get(Math.max(sections.size() - levels, 0));
         return new ArrayList<>(this.currentSections.subList(this.currentSections.indexOf(section), this.currentSections.size()));
      }
   }

   public boolean isCurrentSection(Class<? extends TriggerSection> sectionClass) {
      for (TriggerSection triggerSection : this.currentSections) {
         if (sectionClass.isInstance(triggerSection)) {
            return true;
         }
      }

      return false;
   }

   @SafeVarargs
   public final boolean isCurrentSection(Class<? extends TriggerSection>... sectionClasses) {
      for (Class<? extends TriggerSection> sectionClass : sectionClasses) {
         if (this.isCurrentSection(sectionClass)) {
            return true;
         }
      }

      return false;
   }

   public void setHasDelayBefore(Kleenean hasDelayBefore) {
      this.hasDelayBefore = hasDelayBefore;
   }

   public Kleenean getHasDelayBefore() {
      return this.hasDelayBefore;
   }

   public HandlerList getHandlers() {
      return this.handlers;
   }

   public void setNode(@Nullable Node node) {
      this.node = node != null && node.getParent() != null ? node : null;
   }

   @Nullable
   public Node getNode() {
      return this.node;
   }

   public void setIndentation(String indentation) {
      this.indentation = indentation;
   }

   public String getIndentation() {
      return this.indentation;
   }

   public ParsingStack getParsingStack() {
      return this.parsingStack;
   }

   @Override
   public boolean hasExperiment(String featureName) {
      return this.isActive() && Skript.experiments().isUsing(this.getCurrentScript(), featureName);
   }

   @Override
   public boolean hasExperiment(Experiment experiment) {
      return this.isActive() && Skript.experiments().isUsing(this.getCurrentScript(), experiment);
   }

   @Internal
   public void addExperiment(Experiment experiment) {
      Script script = this.getCurrentScript();
      ExperimentSet set = script.getData(ExperimentSet.class, () -> new ExperimentSet());
      set.add(experiment);
   }

   @Internal
   public void removeExperiment(Experiment experiment) {
      Script script = this.getCurrentScript();
      ExperimentSet set = script.getData(ExperimentSet.class);
      if (set != null) {
         set.remove(experiment);
      }
   }

   public Experimented experimentSnapshot() {
      if (!this.isActive()) {
         return new ExperimentSet();
      } else {
         Script script = this.getCurrentScript();
         ExperimentSet set = script.getData(ExperimentSet.class);
         return set == null ? new ExperimentSet() : new ExperimentSet(set);
      }
   }

   public ExperimentSet getExperimentSet() {
      if (!this.isActive()) {
         return new ExperimentSet();
      } else {
         Script script = this.getCurrentScript();
         ExperimentSet set = script.getData(ExperimentSet.class);
         return set == null ? new ExperimentSet() : set;
      }
   }

   @Experimental
   public HintManager getHintManager() {
      return this.hintManager;
   }

   public static <T extends ParserInstance.Data> void registerData(Class<T> dataClass, Function<ParserInstance, T> dataFunction) {
      dataRegister.put(dataClass, dataFunction);
   }

   public static boolean isRegistered(Class<? extends ParserInstance.Data> dataClass) {
      return dataRegister.containsKey(dataClass);
   }

   @NotNull
   public <T extends ParserInstance.Data> T getData(Class<T> dataClass) {
      if (this.dataMap.containsKey(dataClass)) {
         return (T)this.dataMap.get(dataClass);
      } else if (dataRegister.containsKey(dataClass)) {
         T data = (T)dataRegister.get(dataClass).apply(this);
         this.dataMap.put(dataClass, data);
         return data;
      } else {
         assert false;

         return null;
      }
   }

   @NotNull
   private List<? extends ParserInstance.Data> getDataInstances() {
      List<ParserInstance.Data> dataList = new ArrayList<>();

      for (Class<? extends ParserInstance.Data> dataClass : dataRegister.keySet()) {
         ParserInstance.Data data = this.getData(dataClass);
         dataList.add(data);
      }

      return dataList;
   }

   public ParserInstance.Backup backup() {
      if (!this.isActive()) {
         throw new SkriptAPIException("Backups may only be created from active ParserInstances");
      } else {
         return new ParserInstance.Backup(this);
      }
   }

   public void restoreBackup(ParserInstance.Backup backup) {
      backup.apply(this);
   }

   @Deprecated(
      since = "2.7.0",
      forRemoval = true
   )
   public HashMap<String, String> getCurrentOptions() {
      if (!this.isActive()) {
         return new HashMap<>(0);
      } else {
         StructOptions.OptionsData data = this.getCurrentScript().getData(StructOptions.OptionsData.class);
         return data == null ? new HashMap<>(0) : new HashMap<>(data.getOptions());
      }
   }

   @Deprecated(
      since = "2.7.0",
      forRemoval = true
   )
   @Nullable
   public SkriptEvent getCurrentSkriptEvent() {
      return this.getCurrentStructure() instanceof SkriptEvent event ? event : null;
   }

   @Deprecated(
      since = "2.7.0",
      forRemoval = true
   )
   public void setCurrentSkriptEvent(@Nullable SkriptEvent currentSkriptEvent) {
      this.setCurrentStructure(currentSkriptEvent);
   }

   @Deprecated(
      since = "2.7.0",
      forRemoval = true
   )
   public void deleteCurrentSkriptEvent() {
      this.setCurrentStructure(null);
   }

   @Deprecated(
      since = "2.7.0",
      forRemoval = true
   )
   public void setCurrentScript(@Nullable Config currentScript) {
      if (currentScript != null) {
         File file = currentScript.getFile();
         if (file != null) {
            Script script = ScriptLoader.getScript(file);
            if (script != null) {
               this.setActive(script);
            }
         }
      }
   }

   public static class Backup {
      private final Script currentScript;
      @Nullable
      private final Structure currentStructure;
      @Nullable
      private final String currentEventName;
      @Nullable
      private final Class<? extends Event>[] currentEvents;
      private final List<TriggerSection> currentSections;
      private final Kleenean hasDelayBefore;
      private final HintManager hintManager;
      private final Map<Class<? extends ParserInstance.Data>, ParserInstance.Data> dataMap;

      private Backup(ParserInstance parser) {
         this.currentScript = parser.currentScript;
         this.currentStructure = parser.currentStructure;
         this.currentEventName = parser.currentEventName != null ? parser.currentEventName : null;
         this.currentEvents = parser.currentEvents != null ? Arrays.copyOf(parser.currentEvents, parser.currentEvents.length) : null;
         this.currentSections = new ArrayList<>(parser.currentSections);
         this.hasDelayBefore = parser.hasDelayBefore;
         this.hintManager = parser.hintManager;
         this.dataMap = new HashMap<>(parser.dataMap);
      }

      private void apply(ParserInstance parser) {
         parser.setCurrentScript(this.currentScript);
         parser.currentStructure = this.currentStructure;
         parser.currentEventName = this.currentEventName;
         parser.currentEvents = this.currentEvents;
         parser.currentSections = this.currentSections;
         parser.hasDelayBefore = this.hasDelayBefore;
         parser.hintManager = this.hintManager;
         parser.dataMap.clear();
         parser.dataMap.putAll(this.dataMap);
      }
   }

   public abstract static class Data {
      private final ParserInstance parserInstance;

      public Data(ParserInstance parserInstance) {
         this.parserInstance = parserInstance;
      }

      protected final ParserInstance getParser() {
         return this.parserInstance;
      }

      @Deprecated(
         since = "2.11.0",
         forRemoval = true
      )
      public void onCurrentScriptChange(@Nullable Config currentScript) {
      }

      public void onCurrentEventsChange(@Nullable Class<? extends Event>[] currentEvents) {
      }
   }

   @FunctionalInterface
   public interface ScriptActivityChangeEvent extends ScriptLoader.LoaderEvent, Script.Event {
      void onActivityChange(ParserInstance var1, Script var2, boolean var3, @Nullable Script var4);
   }
}
