package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.experiments.QueueExperimentSyntax;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Name("De-queue Queue (Experimental)")
@Description({"Requires the <code>using queues</code> experimental feature flag to be enabled.\n\nUnrolls a queue into a regular list of values, which can be stored in a list variable.\nThe order of the list will be the same as the order of the elements in the queue.\nIf a list variable is set to this, it will use numerical indices.\nThe original queue will not be changed."})
@Example("set {queue} to a new queue\nadd \"hello\" and \"there\" to {queue}\nset {list::*} to dequeued {queue}\n")
@Since({"2.10 (experimental)"})
public class ExprDequeuedQueue extends SimpleExpression<Object> implements QueueExperimentSyntax {
   private Expression<SkriptQueue> queue;

   @Override
   public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
      this.queue = (Expression<SkriptQueue>)expressions[0];
      return true;
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      SkriptQueue queue = this.queue.getSingle(event);
      return queue == null ? null : queue.toArray();
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<Object> getReturnType() {
      return Object.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "dequeued " + this.queue.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprDequeuedQueue.class, Object.class, ExpressionType.COMBINED, "(de|un)queued %queue%", "unrolled %queue%");
   }
}
