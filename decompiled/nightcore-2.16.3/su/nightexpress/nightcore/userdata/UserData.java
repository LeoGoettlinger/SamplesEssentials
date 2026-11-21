package su.nightexpress.nightcore.userdata;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.wrap.NightProfile;
import su.nightexpress.nightcore.db.state.StatefulData;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.profile.PlayerProfiles;

public class UserData extends StatefulData {
   private final UUID id;
   private String name;
   private String lastSkinUrl;
   private long lastSeen;
   private NightProfile profile;

   public UserData(@NonNull UUID id, @NonNull String name) {
      this.id = id;
      this.setName(name);
   }

   @NonNull
   public static UserData of(@NonNull Player player) {
      NightProfile profile = Players.getProfile(player);
      URL skin = profile.getTextures().getSkin();
      String lastSkinUrl = skin == null ? null : skin.toString();
      UserData data = new UserData(player.getUniqueId(), player.getName());
      data.setLastSkinUrl(lastSkinUrl);
      data.updateLastSeen();
      data.refreshProfile();
      return data;
   }

   public void update(@NonNull Player player) {
      NightProfile onlineProfile = Players.getProfile(player);
      this.update(onlineProfile);
   }

   public void update(@NonNull NightProfile profile) {
      this.profile = profile;
      String profileName = profile.getName();
      if (profileName != null && !this.name.equalsIgnoreCase(profileName)) {
         this.setName(profileName);
         this.markDirty();
      }

      URL profileSkin = profile.getTextures().getSkin();
      if (profileSkin != null) {
         this.lastSkinUrl = profileSkin.toString();
         this.markDirty();
      }
   }

   public void refreshProfile() {
      this.profile = PlayerProfiles.create(this.id, this.name);
      if (this.lastSkinUrl != null) {
         try {
            PlayerTextures textures = this.profile.getTextures();
            textures.setSkin(new URI(this.lastSkinUrl).toURL());
            this.profile.setTextures(textures);
         } catch (URISyntaxException | MalformedURLException var2) {
            var2.printStackTrace();
         }
      }
   }

   @NonNull
   public NightProfile getEffectiveProfile() {
      if (this.profile == null) {
         this.refreshProfile();
      }

      return this.profile;
   }

   public void updateLastSeen() {
      this.setLastSeen(System.currentTimeMillis());
   }

   @Nullable
   public Player getPlayer() {
      return Players.getPlayer(this.id);
   }

   @NonNull
   public Optional<Player> player() {
      return Players.findById(this.id);
   }

   @Nullable
   public NightProfile getProfile() {
      return this.profile;
   }

   @NonNull
   public UUID getId() {
      return this.id;
   }

   @NonNull
   public String getName() {
      return this.name;
   }

   public void setName(@NonNull String name) {
      this.name = name;
   }

   @Nullable
   public String getLastSkinUrl() {
      return this.lastSkinUrl;
   }

   public void setLastSkinUrl(@Nullable String lastSkinUrl) {
      this.lastSkinUrl = lastSkinUrl;
   }

   public long getLastSeen() {
      return this.lastSeen;
   }

   public void setLastSeen(long lastSeen) {
      this.lastSeen = lastSeen;
   }
}
