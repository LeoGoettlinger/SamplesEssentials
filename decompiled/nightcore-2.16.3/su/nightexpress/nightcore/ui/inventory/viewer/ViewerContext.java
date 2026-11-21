package su.nightexpress.nightcore.ui.inventory.viewer;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ViewerContext {
   protected final MenuViewer viewer;
   protected final Object object;

   public ViewerContext(@NonNull MenuViewer viewer, @Nullable Object object) {
      this.viewer = viewer;
      this.object = object;
   }

   public boolean hasObject() {
      return this.object != null;
   }

   @NonNull
   public Player getPlayer() {
      return this.viewer.getPlayer();
   }

   @NonNull
   public MenuViewer getViewer() {
      return this.viewer;
   }

   @Nullable
   public Object getObject() {
      return this.object;
   }

   @NonNull
   public Optional<Object> object() {
      return Optional.ofNullable(this.object);
   }

   @NonNull
   public <T> Optional<T> object(@NonNull Class<T> type) {
      return this.object().filter(type::isInstance).map(type::cast);
   }

   @NonNull
   public <T> T getObject(@NonNull Class<T> type) {
      if (this.object == null) {
         throw new NullPointerException("Object is null! Check #hasObject before calling this method");
      } else {
         return this.object(type)
            .orElseThrow(
               () -> new IllegalArgumentException(
                  "Viewer's object is defined as %s, but %s was expected"
                     .formatted(this.object().map(Object::getClass).map(Class::getSimpleName), type.getSimpleName())
               )
            );
      }
   }
}
