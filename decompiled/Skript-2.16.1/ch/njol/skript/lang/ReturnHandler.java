package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.Deque;
import java.util.LinkedList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

public interface ReturnHandler<T> {
   @NonExtendable
   default void loadReturnableSectionCode(SectionNode node) {
      if (!(this instanceof Section)) {
         throw new SkriptAPIException("loadReturnableSectionCode called on a non-section object");
      } else {
         ParserInstance parser = ParserInstance.get();
         ReturnHandler.ReturnHandlerStack stack = parser.getData(ReturnHandler.ReturnHandlerStack.class);
         stack.push(this);
         Section section = (Section)this;

         try {
            section.loadCode(node);
         } finally {
            stack.pop();
         }
      }
   }

   @NonExtendable
   default ReturnableTrigger<T> loadReturnableSectionCode(SectionNode node, String name, Class<? extends Event>[] events) {
      if (!(this instanceof Section)) {
         throw new SkriptAPIException("loadReturnableSectionCode called on a non-section object");
      } else {
         ParserInstance parser = ParserInstance.get();
         ParserInstance.Backup parserBackup = parser.backup();
         parser.reset();
         parser.setCurrentEvent(name, events);
         SkriptEvent skriptEvent = new SectionSkriptEvent(name, (Section)this);
         parser.setCurrentStructure(skriptEvent);
         ReturnHandler.ReturnHandlerStack stack = parser.getData(ReturnHandler.ReturnHandlerStack.class);

         ReturnableTrigger var8;
         try {
            var8 = new ReturnableTrigger<>(this, parser.getCurrentScript(), name, skriptEvent, trigger -> {
               stack.push(trigger);
               return ScriptLoader.loadItems(node);
            });
         } finally {
            stack.pop();
            parser.restoreBackup(parserBackup);
         }

         return var8;
      }
   }

   @NonExtendable
   default ReturnableTrigger<T> loadReturnableTrigger(SectionNode node, String name, SkriptEvent event) {
      ParserInstance parser = ParserInstance.get();
      ReturnHandler.ReturnHandlerStack stack = parser.getData(ReturnHandler.ReturnHandlerStack.class);

      ReturnableTrigger var6;
      try {
         var6 = new ReturnableTrigger<>(this, parser.getCurrentScript(), name, event, trigger -> {
            stack.push(trigger);
            return ScriptLoader.loadItems(node);
         });
      } finally {
         stack.pop();
      }

      return var6;
   }

   void returnValues(Event var1, Expression<? extends T> var2);

   boolean isSingleReturnValue();

   @Nullable
   Class<? extends T> returnValueType();

   public static class ReturnHandlerStack extends ParserInstance.Data {
      private final Deque<ReturnHandler<?>> stack = new LinkedList<>();

      public ReturnHandlerStack(ParserInstance parserInstance) {
         super(parserInstance);
      }

      public Deque<ReturnHandler<?>> getStack() {
         return this.stack;
      }

      @Nullable
      public ReturnHandler<?> getCurrentHandler() {
         return this.stack.peek();
      }

      public void push(ReturnHandler<?> handler) {
         this.stack.push(handler);
      }

      public ReturnHandler<?> pop() {
         return this.stack.pop();
      }
   }
}
