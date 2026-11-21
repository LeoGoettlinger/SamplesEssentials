package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.experiments.QueueExperimentSyntax;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Name("Queue Start/End (Experimental)")
@Description({"Requires the <code>using queues</code> experimental feature flag to be enabled.\n\nThe first or last element in a queue. Asking for this does <b>not</b> remove the element from the queue.\n\nThis is designed for use with the <code>add</code> changer: to add or remove elements from the start or the end of the queue.\n"})
@Example("set {queue} to a new queue\nadd \"hello\" to {queue}\nadd \"foo\" to the start of {queue}\nbroadcast the first element of {queue} # foo\nbroadcast the first element of {queue} # hello\n# queue is now empty")
@Since({"2.10 (experimental)"})
public class ExprQueueStartEnd extends SimplePropertyExpression<SkriptQueue, Object> implements QueueExperimentSyntax {
   private boolean start;

   @Override
   public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
      this.start = result.hasTag("start");
      return super.init(expressions, pattern, delayed, result);
   }

   @Nullable
   public Object convert(SkriptQueue from) {
      return this.start ? from.peekFirst() : from.peekLast();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET -> new Class[]{Object.class};
         case DELETE -> new Class[0];
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      SkriptQueue queue = this.getExpr().getSingle(event);
      if (queue != null) {
         if (this.start) {
            switch (mode) {
               case ADD:
                  queue.addAll(0, Arrays.asList(delta));
                  break;
               case REMOVE:
                  for (Object object : delta) {
                     queue.removeFirstOccurrence(object);
                  }
                  break;
               case SET:
                  assert delta != null && delta.length > 0;

                  queue.removeFirst();
                  queue.addFirst(delta[0]);
                  break;
               case DELETE:
                  queue.removeFirst();
            }
         } else {
            switch (mode) {
               case ADD:
                  queue.addAll(Arrays.asList(delta));
                  break;
               case REMOVE:
                  for (Object object : delta) {
                     queue.removeLastOccurrence(object);
                  }
                  break;
               case SET:
                  assert delta != null && delta.length > 0;

                  queue.removeLast();
                  queue.addLast(delta[0]);
                  break;
               case DELETE:
                  queue.removeLast();
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<?> getReturnType() {
      return Object.class;
   }

   @Override
   protected String getPropertyName() {
      return this.start ? "start" : "end";
   }

   static {
      register(ExprQueueStartEnd.class, Object.class, "(:start|end)", "queue");
   }
}
