package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public abstract class TriggerSection extends TriggerItem {
   @Nullable
   protected TriggerItem first;
   @Nullable
   protected TriggerItem last;

   protected TriggerSection(List<TriggerItem> items) {
      this.setTriggerItems(items);
   }

   protected TriggerSection(SectionNode node) {
      ParserInstance parser = ParserInstance.get();
      List<TriggerSection> previousSections = parser.getCurrentSections();
      List<TriggerSection> sections = new ArrayList<>(previousSections);
      sections.add(this);
      parser.setCurrentSections(sections);

      try {
         this.setTriggerItems(ScriptLoader.loadItems(node));
      } finally {
         parser.setCurrentSections(previousSections);
      }
   }

   protected TriggerSection() {
   }

   protected void setTriggerItems(List<TriggerItem> items) {
      if (!items.isEmpty()) {
         this.first = items.get(0);
         this.last = items.get(items.size() - 1);
         this.last.setNext(this.getNext());

         for (TriggerItem item : items) {
            item.setParent(this);
         }
      }
   }

   public TriggerSection setNext(@Nullable TriggerItem next) {
      super.setNext(next);
      if (this.last != null) {
         this.last.setNext(next);
      }

      return this;
   }

   public TriggerSection setParent(@Nullable TriggerSection parent) {
      super.setParent(parent);
      return this;
   }

   @Override
   protected final boolean run(Event event) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   protected abstract TriggerItem walk(Event var1);

   @Nullable
   protected final TriggerItem walk(Event event, boolean run) {
      this.debug(event, run);
      return run && this.first != null ? this.first : this.getNext();
   }

   @Nullable
   protected ExecutionIntent triggerExecutionIntent() {
      for (TriggerItem current = this.first; current != null; current = current.getActualNext()) {
         ExecutionIntent executionIntent = current.executionIntent();
         if (executionIntent != null) {
            return executionIntent.use();
         }

         if (current == this.last) {
            break;
         }
      }

      return null;
   }
}
