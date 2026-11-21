package de.theredend2000.advancedhunt.lib.kyori.adventure.text.object;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

record PlayerHeadObjectContentsImpl(
   @Nullable String name, @Nullable UUID id, List<PlayerHeadObjectContents.ProfileProperty> profileProperties, boolean hat, @Nullable Key texture
) implements PlayerHeadObjectContents {
   PlayerHeadObjectContentsImpl(
      @Nullable String name, @Nullable UUID id, List<PlayerHeadObjectContents.ProfileProperty> profileProperties, boolean hat, @Nullable Key texture
   ) {
      checkValidName(name);
      this.name = name;
      this.id = id;
      this.profileProperties = profileProperties;
      this.hat = hat;
      this.texture = texture;
   }

   @Override
   public PlayerHeadObjectContents.Builder toBuilder() {
      return new PlayerHeadObjectContentsImpl.BuilderImpl(this);
   }

   static void checkValidName(@Nullable final String name) {
      if (name != null) {
         if (!PlayerHeadObjectContents.isValidName(name)) {
            throw new IllegalArgumentException("Invalid player name: " + name);
         }
      }
   }

   static final class BuilderImpl implements PlayerHeadObjectContents.Builder {
      @Nullable
      private String name;
      @Nullable
      private UUID id;
      private final List<PlayerHeadObjectContents.ProfileProperty> properties = new ArrayList<>();
      private boolean hat = true;
      @Nullable
      private Key texture;

      BuilderImpl() {
      }

      BuilderImpl(final PlayerHeadObjectContentsImpl playerHeadObjectContents) {
         this.name = playerHeadObjectContents.name;
         this.id = playerHeadObjectContents.id;
         this.properties.addAll(playerHeadObjectContents.profileProperties);
         this.hat = playerHeadObjectContents.hat;
         this.texture = playerHeadObjectContents.texture;
      }

      @Override
      public PlayerHeadObjectContents.Builder name(@Nullable final String name) {
         PlayerHeadObjectContentsImpl.checkValidName(name);
         this.name = name;
         return this;
      }

      @Override
      public PlayerHeadObjectContents.Builder id(@Nullable final UUID id) {
         this.id = id;
         return this;
      }

      @Override
      public PlayerHeadObjectContents.Builder profileProperty(final PlayerHeadObjectContents.ProfileProperty property) {
         this.properties.add(Objects.requireNonNull(property, "property"));
         return this;
      }

      @Override
      public PlayerHeadObjectContents.Builder profileProperties(final Collection<? extends PlayerHeadObjectContents.ProfileProperty> properties) {
         for (PlayerHeadObjectContents.ProfileProperty property : Objects.requireNonNull(properties, "properties")) {
            this.profileProperty(property);
         }

         return this;
      }

      private void clearProfile() {
         this.name = null;
         this.id = null;
         this.properties.clear();
         this.texture = null;
      }

      @Override
      public PlayerHeadObjectContents.Builder skin(final PlayerHeadObjectContents.SkinSource skinSource) {
         this.clearProfile();
         Objects.requireNonNull(skinSource, "skinSource").applySkinToPlayerHeadContents(this);
         return this;
      }

      @Override
      public PlayerHeadObjectContents.Builder hat(final boolean hat) {
         this.hat = hat;
         return this;
      }

      @Override
      public PlayerHeadObjectContents.Builder texture(@Nullable final Key texture) {
         this.texture = texture;
         return this;
      }

      @Override
      public PlayerHeadObjectContents build() {
         return new PlayerHeadObjectContentsImpl(this.name, this.id, List.copyOf(this.properties), this.hat, this.texture);
      }
   }

   record ProfilePropertyImpl(String name, String value, @Nullable String signature) implements PlayerHeadObjectContents.ProfileProperty {
   }
}
