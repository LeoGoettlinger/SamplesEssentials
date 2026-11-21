package org.skriptlang.skript.common.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Internal
public class QueueClassInfo extends ClassInfo<SkriptQueue> {
   public QueueClassInfo() {
      super(SkriptQueue.class, "queue");
      this.user(new String[]{"queues?"})
         .name("Queue")
         .description("A queued list of values. Entries are removed from a queue when they are queried.")
         .examples("set {queue} to a new queue", "add \"hello\" to {queue}", "broadcast the 1st element of {queue}")
         .since("2.10")
         .changer(new QueueClassInfo.QueueChanger())
         .parser(new QueueClassInfo.QueueParser())
         .serializer(new QueueClassInfo.QueueSerializer())
         .property(Property.AMOUNT, "The amount of elements in the queue.", Skript.instance(), new QueueClassInfo.QueueAmountHandler())
         .property(Property.SIZE, "The size of the queue, in element count.", Skript.instance(), new QueueClassInfo.QueueAmountHandler())
         .property(
            Property.IS_EMPTY,
            "Whether a queue is empty, i.e. whether there are no elements in the queue.",
            Skript.instance(),
            ConditionPropertyHandler.of(AbstractCollection::isEmpty)
         );
   }

   private static class QueueAmountHandler implements ExpressionPropertyHandler<SkriptQueue, Integer> {
      public Integer convert(SkriptQueue propertyHolder) {
         return propertyHolder.size();
      }

      @NotNull
      @Override
      public Class<Integer> returnType() {
         return Integer.class;
      }
   }

   private static class QueueChanger implements Changer<SkriptQueue> {
      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case ADD, REMOVE, DELETE -> new Class[]{Object.class};
            case RESET -> new Class[0];
            default -> null;
         };
      }

      public void change(SkriptQueue[] what, @Nullable Object[] delta, Changer.ChangeMode mode) {
         for (SkriptQueue queue : what) {
            switch (mode) {
               case ADD:
                  assert delta != null;

                  queue.addAll(Arrays.asList(delta));
                  break;
               case REMOVE:
                  assert delta != null;

                  queue.removeAll(Arrays.asList(delta));
                  break;
               case DELETE:
               case RESET:
                  queue.clear();
            }
         }
      }
   }

   private static class QueueParser extends Parser<SkriptQueue> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(SkriptQueue queue, int flags) {
         return Classes.toString(queue.toArray(), flags, true);
      }

      public String toVariableNameString(SkriptQueue queue) {
         return this.toString(queue, 0);
      }
   }

   private static class QueueSerializer extends Serializer<SkriptQueue> {
      public Fields serialize(SkriptQueue queue) {
         Fields fields = new Fields();
         fields.putObject("contents", queue.toArray());
         return fields;
      }

      public void deserialize(SkriptQueue queue, Fields fields) throws StreamCorruptedException {
         Object[] contents = fields.getObject("contents", Object[].class);
         queue.clear();
         if (contents != null) {
            queue.addAll(List.of(contents));
         }
      }

      @Override
      public boolean mustSyncDeserialization() {
         return false;
      }

      @Override
      protected boolean canBeInstantiated() {
         return true;
      }
   }
}
