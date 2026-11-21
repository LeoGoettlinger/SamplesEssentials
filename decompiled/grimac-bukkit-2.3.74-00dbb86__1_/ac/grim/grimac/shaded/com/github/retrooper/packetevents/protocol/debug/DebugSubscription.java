package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.debug;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface DebugSubscription<T> extends MappedEntity {
   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T value);

   public static final class Event<T> {
      private final DebugSubscription<T> subscription;
      private final T value;

      public Event(DebugSubscription<T> subscription, T value) {
         this.subscription = subscription;
         this.value = value;
      }

      public static DebugSubscription.Event<?> read(PacketWrapper<?> wrapper) {
         DebugSubscription<? super Object> subscription = wrapper.readMappedEntity(DebugSubscriptions.getRegistry());
         Object value = subscription.read(wrapper);
         return new DebugSubscription.Event<>(subscription, value);
      }

      public static <T> void write(PacketWrapper<?> wrapper, DebugSubscription.Event<T> event) {
         wrapper.writeMappedEntity(event.subscription);
         event.subscription.write(wrapper, event.value);
      }

      public DebugSubscription<T> getSubscription() {
         return this.subscription;
      }

      public T getValue() {
         return this.value;
      }
   }

   public static final class Update<T> {
      private final DebugSubscription<T> subscription;
      @Nullable
      private final T value;

      public Update(DebugSubscription<T> subscription, @Nullable T value) {
         this.subscription = subscription;
         this.value = value;
      }

      public static DebugSubscription.Update<?> read(PacketWrapper<?> wrapper) {
         DebugSubscription<? super Object> subscription = wrapper.readMappedEntity(DebugSubscriptions.getRegistry());
         Object value = wrapper.readOptional(subscription::read);
         return new DebugSubscription.Update<>(subscription, value);
      }

      public static <T> void write(PacketWrapper<?> wrapper, DebugSubscription.Update<T> event) {
         wrapper.writeMappedEntity(event.subscription);
         wrapper.writeOptional(event.value, event.subscription::write);
      }

      public DebugSubscription<T> getSubscription() {
         return this.subscription;
      }

      @Nullable
      public T getValue() {
         return this.value;
      }
   }
}
