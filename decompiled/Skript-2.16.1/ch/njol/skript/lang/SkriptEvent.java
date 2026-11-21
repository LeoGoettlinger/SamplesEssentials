package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.structures.StructEvent;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.iterator.ConsumingIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public abstract class SkriptEvent extends Structure {
   public static final Structure.Priority PRIORITY = new Structure.Priority(600);
   private String expr;
   private SectionNode source;
   @Nullable
   protected EventPriority eventPriority;
   @Nullable
   protected SkriptEvent.ListeningBehavior listeningBehavior;
   protected boolean supportsListeningBehavior;
   private SkriptEventInfo<?> skriptEventInfo;
   protected Trigger trigger;

   @Override
   public final boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult, @Nullable EntryContainer entryContainer) {
      this.expr = parseResult.expr;
      StructEvent.EventData eventData = this.getParser().getData(StructEvent.EventData.class);
      EventPriority priority = eventData.getPriority();
      if (priority != null && !this.isEventPrioritySupported()) {
         Skript.error("This event doesn't support event priority");
         return false;
      } else {
         this.eventPriority = priority;
         SyntaxElementInfo<? extends Structure> syntaxElementInfo = this.getParser().getData(Structure.StructureData.class).getStructureInfo();
         if (!(syntaxElementInfo instanceof SkriptEventInfo)) {
            throw new IllegalStateException();
         } else {
            this.skriptEventInfo = (SkriptEventInfo<?>)syntaxElementInfo;

            assert entryContainer != null;

            this.source = entryContainer.getSource();
            this.listeningBehavior = eventData.getListenerBehavior();
            if (!this.init(args, matchedPattern, parseResult)) {
               return false;
            } else {
               for (Class<? extends Event> eventClass : this.getEventClasses()) {
                  if (Cancellable.class.isAssignableFrom(eventClass)) {
                     this.supportsListeningBehavior = true;
                     break;
                  }
               }

               if (this.listeningBehavior != null && !this.isListeningBehaviorSupported()) {
                  String eventName = this.skriptEventInfo.name.toLowerCase(Locale.ENGLISH);
                  Skript.error(Utils.A(eventName) + " event does not support listening for cancelled or uncancelled events.");
                  return false;
               } else {
                  return true;
               }
            }
         }
      }
   }

   public abstract boolean init(Literal<?>[] var1, int var2, SkriptParser.ParseResult var3);

   @Override
   public boolean preLoad() {
      return super.preLoad();
   }

   @Override
   public boolean load() {
      if (!this.shouldLoadEvent()) {
         return false;
      } else {
         if (Skript.debug() || this.source.debug()) {
            Skript.debug(this.expr + " (" + this + "):");
         }

         Class<? extends Event>[] eventClasses = this.getEventClasses();

         try {
            this.getParser().setCurrentEvent(this.skriptEventInfo.getName().toLowerCase(Locale.ENGLISH), eventClasses);
            List<TriggerItem> items = ScriptLoader.loadItems(this.source);
            Script script = this.getParser().getCurrentScript();
            this.trigger = new Trigger(script, this.expr, this, items);
            int lineNumber = this.source.getLine();
            this.trigger.setLineNumber(lineNumber);
            this.trigger.setDebugLabel(script + ": line " + lineNumber);
         } finally {
            this.getParser().deleteCurrentEvent();
         }

         return true;
      }
   }

   @Override
   public boolean postLoad() {
      SkriptEventHandler.registerBukkitEvents(this.trigger, this.getEventClasses());
      return true;
   }

   @Override
   public void unload() {
      SkriptEventHandler.unregisterBukkitEvents(this.trigger);
   }

   @Override
   public void postUnload() {
      super.postUnload();
   }

   @Override
   public Structure.Priority getPriority() {
      return PRIORITY;
   }

   public abstract boolean check(Event var1);

   public boolean shouldLoadEvent() {
      return true;
   }

   public Class<? extends Event>[] getEventClasses() {
      return this.skriptEventInfo.events;
   }

   public EventPriority getEventPriority() {
      return this.eventPriority != null ? this.eventPriority : SkriptConfig.defaultEventPriority.value();
   }

   public boolean isEventPrioritySupported() {
      return true;
   }

   public SkriptEvent.ListeningBehavior getListeningBehavior() {
      return this.listeningBehavior != null ? this.listeningBehavior : this.skriptEventInfo.getListeningBehavior();
   }

   public boolean isListeningBehaviorSupported() {
      return this.supportsListeningBehavior;
   }

   public boolean canExecuteAsynchronously() {
      return false;
   }

   public static String fixPattern(String pattern) {
      return BukkitSyntaxInfos.fixPattern(pattern);
   }

   @Nullable
   public static SkriptEvent parse(String expr, SectionNode sectionNode, @Nullable String defaultError) {
      ParserInstance.get().getData(Structure.StructureData.class).node = sectionNode;
      Iterator<BukkitSyntaxInfos.Event<?>> iterator = Skript.instance().syntaxRegistry().syntaxes(BukkitSyntaxInfos.Event.KEY).iterator();
      Iterator<BukkitSyntaxInfos.Event<?>> var9 = new ConsumingIterator<>(
         iterator,
         info -> ParserInstance.get().getData(Structure.StructureData.class).structureInfo = SyntaxElementInfo.fromModern(
            (SyntaxInfo<? extends SkriptEvent>)info
         )
      );

      Object var6;
      try (ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler()) {
         SkriptEvent event = SkriptParser.parseStatic(expr, var9, ParseContext.EVENT, defaultError);
         if (event != null) {
            parseLogHandler.printLog();
            return event;
         }

         parseLogHandler.printError();
         var6 = null;
      }

      return (SkriptEvent)var6;
   }

   public static enum ListeningBehavior {
      UNCANCELLED,
      CANCELLED,
      ANY;

      public boolean matches(boolean cancelled) {
         switch (this) {
            case UNCANCELLED:
               return !cancelled;
            case CANCELLED:
               return cancelled;
            case ANY:
               return true;
            default:
               assert false;

               return false;
         }
      }
   }
}
