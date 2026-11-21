package su.nightexpress.nightcore.util.profile;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.bukkit.OfflinePlayer;
import org.bukkit.profile.PlayerTextures;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.wrap.NightProfile;
import su.nightexpress.nightcore.util.bridge.Software;

public class PlayerProfiles {
   public static final String TEXTURES_HOST = "http://textures.minecraft.net/texture/";
   @Deprecated
   private static final Map<UUID, CachedProfile> CACHED_PROFILES = new ConcurrentHashMap<>();

   private PlayerProfiles() {
   }

   @Deprecated
   @NonNull
   private static CachedProfile cacheTemporary(@NonNull NightProfile profile) {
      return cacheProfile(profile, false, false);
   }

   @Deprecated
   @NonNull
   public static CachedProfile cacheExact(@NonNull NightProfile profile) {
      return cacheProfile(profile, true, true);
   }

   @Deprecated
   @NonNull
   private static CachedProfile cacheProfile(@NonNull NightProfile profile, boolean permanent, boolean noUpdate) {
      if (profile.getId() == null) {
         return new CachedProfile(profile, true, true);
      } else {
         CachedProfile cachedProfile = new CachedProfile(profile, permanent, noUpdate);
         CACHED_PROFILES.put(profile.getId(), cachedProfile);
         return cachedProfile;
      }
   }

   @Deprecated
   @NonNull
   private static CachedProfile queryOrCache(@NonNull UUID id, @NonNull Supplier<NightProfile> supplier) {
      CachedProfile cached = getCachedProfile(id);
      return cached != null ? cached : cacheTemporary(supplier.get());
   }

   @Deprecated
   @Nullable
   public static CachedProfile getCachedProfile(@NonNull UUID id) {
      return CACHED_PROFILES.get(id);
   }

   @Deprecated
   @NonNull
   public static Set<CachedProfile> getCachedProfiles() {
      return new HashSet<>(CACHED_PROFILES.values());
   }

   @Deprecated
   public static void clear() {
      CACHED_PROFILES.clear();
   }

   @Deprecated
   public static void purgeProfiles() {
      CACHED_PROFILES.values().removeIf(CachedProfile::isPurgeTime);
   }

   @Deprecated
   @NonNull
   public static CachedProfile getProfile(@NonNull OfflinePlayer player) {
      return queryOrCache(player.getUniqueId(), () -> Software.get().getProfile(player));
   }

   @Deprecated
   @NonNull
   public static CachedProfile createProfile(@NonNull UUID uuid) {
      return queryOrCache(uuid, () -> Software.get().createProfile(uuid));
   }

   @Deprecated
   @NonNull
   public static NightProfile createProfile(@NonNull String name) {
      return create(name);
   }

   @Deprecated
   @NonNull
   public static CachedProfile createProfile(@NonNull UUID uuid, @Nullable String name) {
      return queryOrCache(uuid, () -> Software.get().createProfile(uuid, name));
   }

   @NonNull
   public static NightProfile create(@NonNull UUID uuid) {
      return Software.get().createProfile(uuid);
   }

   @NonNull
   public static NightProfile create(@NonNull String name) {
      return Software.get().createProfile(name);
   }

   @NonNull
   public static NightProfile create(@NonNull UUID uuid, @Nullable String name) {
      return Software.get().createProfile(uuid, name);
   }

   @Deprecated
   @Nullable
   public static CachedProfile createProfileBySkinURL(@NonNull String urlData) {
      if (urlData.isBlank()) {
         return null;
      } else {
         String name = urlData.substring(0, 16);
         if (!urlData.startsWith("http://textures.minecraft.net/texture/")) {
            urlData = "http://textures.minecraft.net/texture/" + urlData;
         }

         try {
            UUID uuid = UUID.nameUUIDFromBytes(urlData.getBytes());
            CachedProfile cached = getCachedProfile(uuid);
            if (cached != null) {
               return cached;
            } else {
               NightProfile profile = Software.get().createProfile(uuid, name);
               URL url = URI.create(urlData).toURL();
               PlayerTextures textures = profile.getTextures();
               textures.setSkin(url);
               profile.setTextures(textures);
               return cacheExact(profile);
            }
         } catch (Exception var7) {
            var7.printStackTrace();
            return null;
         }
      }
   }

   @NonNull
   public static NightProfile createStaticTexturedProfile(@NonNull String url) {
      try {
         if (!url.startsWith("http://textures.minecraft.net/texture/")) {
            url = "http://textures.minecraft.net/texture/" + url;
         }

         URL skinUrl = new URI(url).toURL();
         return createStaticTexturedProfile(skinUrl);
      } catch (MalformedURLException | URISyntaxException var2) {
         throw new IllegalArgumentException("Could not create profile", var2);
      }
   }

   @NonNull
   public static NightProfile createStaticTexturedProfile(@NonNull URL skinUrl) {
      NightProfile profile = Software.get().createProfile(null, null);
      PlayerTextures textures = profile.getTextures();
      textures.setSkin(skinUrl);
      profile.setTextures(textures);
      return profile;
   }

   @Nullable
   public static String getProfileSkinURL(@NonNull NightProfile profile) {
      URL skin = profile.getTextures().getSkin();
      if (skin == null) {
         return null;
      } else {
         String raw = skin.toString();
         return raw.substring("http://textures.minecraft.net/texture/".length());
      }
   }
}
