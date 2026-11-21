package su.nightexpress.nightcore.bridge.paper;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.wrap.NightProfile;

public class PaperProfile implements NightProfile {
   private final PlayerProfile backend;

   public PaperProfile(@NonNull PlayerProfile backend) {
      this.backend = backend;
   }

   @NonNull
   public PlayerProfile getBackend() {
      return this.backend;
   }

   @Override
   public void apply(@NonNull SkullMeta meta) {
      meta.setPlayerProfile(this.backend);
   }

   @Nullable
   @Override
   public UUID getId() {
      return this.backend.getId();
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
      return this.backend.update().thenApplyAsync(PaperProfile::new);
   }

   @Override
   public String toString() {
      return "PaperProfile{backend=" + this.backend + "}";
   }
}
