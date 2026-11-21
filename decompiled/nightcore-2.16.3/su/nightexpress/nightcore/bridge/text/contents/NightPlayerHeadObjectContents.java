package su.nightexpress.nightcore.bridge.text.contents;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.text.adapter.ObjectContentsAdapter;

public class NightPlayerHeadObjectContents implements NightObjectContents {
   public static final boolean DEFAULT_HAT = true;
   @Nullable
   private final String name;
   @Nullable
   private final UUID id;
   private final List<NightPlayerHeadObjectContents.NightProfileProperty> properties;
   private final boolean hat;
   @Nullable
   private final NightKey texture;

   NightPlayerHeadObjectContents(
      @Nullable String name,
      @Nullable UUID id,
      @NonNull List<NightPlayerHeadObjectContents.NightProfileProperty> properties,
      boolean hat,
      @Nullable NightKey texture
   ) {
      this.name = name;
      this.id = id;
      if (properties.isEmpty()) {
         this.properties = Collections.emptyList();
      } else {
         this.properties = List.copyOf(properties);
      }

      this.hat = hat;
      this.texture = texture;
   }

   @NonNull
   public static NightPlayerHeadObjectContents.NightProfileProperty property(@NonNull String name, @NonNull String value) {
      return new NightPlayerHeadObjectContents.NightProfileProperty(name, value, null);
   }

   @NonNull
   public static NightPlayerHeadObjectContents.NightProfileProperty property(@NonNull String name, @NonNull String value, @Nullable String signature) {
      return new NightPlayerHeadObjectContents.NightProfileProperty(name, value, signature);
   }

   @NonNull
   @Override
   public <T> T adapt(@NonNull ObjectContentsAdapter<T> adapter) {
      return adapter.adaptContents(this);
   }

   @Nullable
   public String name() {
      return this.name;
   }

   @Nullable
   public UUID id() {
      return this.id;
   }

   @NonNull
   public List<NightPlayerHeadObjectContents.NightProfileProperty> profileProperties() {
      return this.properties;
   }

   public boolean hat() {
      return this.hat;
   }

   @Nullable
   public NightKey texture() {
      return this.texture;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof NightPlayerHeadObjectContents that)
         ? false
         : this.hat == that.hat
            && Objects.equals(this.name, that.name)
            && Objects.equals(this.id, that.id)
            && Objects.equals(this.properties, that.properties)
            && Objects.equals(this.texture, that.texture);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.id, this.properties, this.hat, this.texture);
   }

   @Override
   public String toString() {
      return "NightPlayerHeadObjectContents{name='"
         + this.name
         + "', id="
         + this.id
         + ", properties="
         + this.properties
         + ", hat="
         + this.hat
         + ", texture="
         + this.texture
         + "}";
   }

   public static final class NightProfileProperty {
      private final String name;
      private final String value;
      @Nullable
      private final String signature;

      NightProfileProperty(@NonNull String name, @NonNull String value, @Nullable String signature) {
         this.name = name;
         this.value = value;
         this.signature = signature;
      }

      @NonNull
      public String name() {
         return this.name;
      }

      @NonNull
      public String value() {
         return this.value;
      }

      @Nullable
      public String signature() {
         return this.signature;
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof NightPlayerHeadObjectContents.NightProfileProperty that)
            ? false
            : Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value) && Objects.equals(this.signature, that.signature);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.name, this.value, this.signature);
      }

      @Override
      public String toString() {
         return "ProfilePropertyImpl{name='" + this.name + "', value='" + this.value + "', signature='" + this.signature + "'}";
      }
   }
}
