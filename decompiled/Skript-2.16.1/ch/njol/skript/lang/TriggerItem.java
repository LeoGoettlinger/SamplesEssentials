package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;
import java.io.File;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.lang.script.Script;

public abstract class TriggerItem implements Debuggable {
   @Nullable
   protected TriggerSection parent = null;
   @Nullable
   private TriggerItem next = null;
   private static final String INDENT = "  ";
   @Nullable
   private String indentation = null;

   protected TriggerItem() {
   }

   protected TriggerItem(TriggerSection parent) {
      this.parent = parent;
   }

   @Nullable
   protected TriggerItem walk(Event event) {
      if (this.run(event)) {
         this.debug(event, true);
         return this.next;
      } else {
         this.debug(event, false);
         TriggerSection parent = this.parent;
         return parent == null ? null : parent.getNext();
      }
   }

   protected abstract boolean run(Event var1);

   public static boolean walk(TriggerItem start, Event event) {
      TriggerItem triggerItem = start;

      try {
         while (triggerItem != null) {
            triggerItem = triggerItem.walk(event);
         }

         return true;
      } catch (StackOverflowError var8) {
         Trigger trigger = start.getTrigger();
         String scriptName = "<unknown>";
         if (trigger != null) {
            Script script = trigger.getScript();
            if (script != null) {
               File scriptFile = script.getConfig().getFile();
               if (scriptFile != null) {
                  scriptName = scriptFile.getName();
               }
            }
         }

         Skript.adminBroadcast("<red>The script '<gold>" + scriptName + "<red>' infinitely (or excessively) repeated itself!");
         if (Skript.debug()) {
            var8.printStackTrace();
         }
      } catch (Exception var9) {
         if (var9.getStackTrace().length != 0) {
            Skript.exception(var9, triggerItem);
         }
      } catch (Throwable var10) {
         Skript.markErrored();
         throw var10;
      }

      return false;
   }

   @Nullable
   public ExecutionIntent executionIntent() {
      return null;
   }

   public String getIndentation() {
      if (this.indentation == null) {
         int level = 0;
         TriggerItem triggerItem = this;

         while ((triggerItem = triggerItem.parent) != null) {
            level++;
         }

         this.indentation = StringUtils.multiply("  ", level);
      }

      return this.indentation;
   }

   protected final void debug(Event event, boolean run) {
      if (Skript.debug()) {
         Skript.debug(TextComponentParser.instance().escape(this.getIndentation() + (run ? "" : "-") + this.toString(event, true)));
      }
   }

   @Override
   public final String toString() {
      return this.toString(null, false);
   }

   public TriggerItem setParent(@Nullable TriggerSection parent) {
      this.parent = parent;
      return this;
   }

   @Nullable
   public final TriggerSection getParent() {
      return this.parent;
   }

   @Nullable
   public final Trigger getTrigger() {
      TriggerItem triggerItem = this;

      while (triggerItem != null && !(triggerItem instanceof Trigger)) {
         triggerItem = triggerItem.getParent();
      }

      return (Trigger)triggerItem;
   }

   public TriggerItem setNext(@Nullable TriggerItem next) {
      this.next = next;
      return this;
   }

   @Nullable
   public TriggerItem getNext() {
      return this.next;
   }

   @Nullable
   public TriggerItem getActualNext() {
      return this.next;
   }
}
