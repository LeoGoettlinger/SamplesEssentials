package su.nightexpress.nightcore.bridge.spigot;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.wrap.NightProfile;

public class SpigotProfile implements NightProfile {
   private final PlayerProfile backend;

   public SpigotProfile(PlayerProfile backend) {
      this.backend = backend;
   }

   @Override
   public void apply(@NonNull SkullMeta meta) {
      if (this.backend == null || !this.backend.getTextures().isEmpty()) {
         meta.setOwnerProfile(this.backend);
      }
   }

   @Nullable
   @Override
   public UUID getId() {
      return this.backend.getUniqueId();
   }

   @NonNull
   @Override
   public Optional<UUID> id() {
      return Optional.ofNullable(this.getId());
   }

   @Nullable
   @Override
   public String getName() {
      return this.backend.getName();
   }

   @NonNull
   @Override
   public Optional<String> name() {
      return Optional.ofNullable(this.getName());
   }

   @NonNull
   @Override
   public PlayerTextures getTextures() {
      return this.backend.getTextures();
   }

   @Override
   public void setTextures(@Nullable PlayerTextures textures) {
      this.backend.setTextures(textures);
   }

   @Override
   public boolean isComplete() {
      return this.backend.isComplete();
   }

   @NonNull
   @Override
   public CompletableFuture<NightProfile> update() {
      return this.backend.update().thenApplyAsync(SpigotProfile::new);
   }

   @Override
   public String toString() {
      return "SpigotProfile{backend=" + this.backend + "}";
   }
}
