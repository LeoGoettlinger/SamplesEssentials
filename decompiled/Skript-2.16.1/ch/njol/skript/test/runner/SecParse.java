package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Parse Section")
@Description({"Parse code inside this section and use 'parse logs' to grab any logs from it."})
@NoDoc
public class SecParse extends Section {
   private String[] logs;

   @Override
   public boolean init(
      Expression<?>[] exprs,
      int matchedPattern,
      Kleenean isDelayed,
      SkriptParser.ParseResult parseResult,
      SectionNode sectionNode,
      List<TriggerItem> triggerItems
   ) {
      if (Iterables.size(sectionNode) == 0) {
         Skript.error("A parse section must contain code");
         return false;
      } else {
         RetainingLogHandler handler = SkriptLogger.startRetainingLog();
         boolean inParseSection = this.getParser().isCurrentSection(SecParse.class);
         this.loadCode(sectionNode);
         if (!inParseSection) {
            this.logs = handler.getLog().stream().map(LogEntry::getMessage).toArray(String[]::new);
         }

         handler.stop();
         return true;
      }
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      ExprParseLogs.lastLogs = this.logs;
      return this.walk(event, false);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "parse";
   }

   static {
      Skript.registerSection(SecParse.class, "parse");
   }
}
