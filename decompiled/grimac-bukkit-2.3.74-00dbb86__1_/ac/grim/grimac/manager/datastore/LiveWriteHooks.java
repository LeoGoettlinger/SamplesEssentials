package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.checks.impl.misc.ClientBrand;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface LiveWriteHooks {
   LiveWriteHooks NOOP = new LiveWriteHooks() {
      @Override
      public void onJoin(@NotNull UUID u, @Nullable String n, long t, @NotNull SessionTracker.ClientMeta m) {
      }

      @Override
      public void onQuit(@NotNull UUID u, long t, @NotNull SessionTracker.ClientMeta m) {
      }

      @Override
      public void observeBrand(@NotNull UUID u, long t, @NotNull SessionTracker.ClientMeta m) {
      }

      @Override
      public void recordFlag(@NotNull UUID u, @NotNull AbstractCheck c, double v, @Nullable String vb, long t, @NotNull SessionTracker.ClientMeta m) {
      }

      @Override
      public void recordFlagData(@NotNull UUID u, @NotNull AbstractCheck c, double v, @Nullable byte[] d, long t, @NotNull SessionTracker.ClientMeta m) {
      }

      @Override
      public void onJoinFromUserLogin(@NotNull PlatformPlayer p, @NotNull User u, long t) {
      }

      @Override
      public void onQuitFromUserDisconnect(@NotNull User u, @Nullable GrimPlayer g, long t) {
      }

      @Override
      public void observeBrandFromCheck(@NotNull GrimPlayer g) {
      }

      @Override
      public void recordFlagFromCheck(@NotNull GrimPlayer p, @NotNull AbstractCheck c, double v, @Nullable String vb) {
      }

      @Override
      public void recordFlagDataFromCheck(@NotNull GrimPlayer p, @NotNull AbstractCheck c, double v, @Nullable byte[] d) {
      }
   };

   void onJoin(@NotNull UUID var1, @Nullable String var2, long var3, @NotNull SessionTracker.ClientMeta var5);

   void onQuit(@NotNull UUID var1, long var2, @NotNull SessionTracker.ClientMeta var4);

   void observeBrand(@NotNull UUID var1, long var2, @NotNull SessionTracker.ClientMeta var4);

   void recordFlag(@NotNull UUID var1, @NotNull AbstractCheck var2, double var3, @Nullable String var5, long var6, @NotNull SessionTracker.ClientMeta var8);

   void recordFlagData(@NotNull UUID var1, @NotNull AbstractCheck var2, double var3, @Nullable byte[] var5, long var6, @NotNull SessionTracker.ClientMeta var8);

   void onJoinFromUserLogin(@NotNull PlatformPlayer var1, @NotNull User var2, long var3);

   void onQuitFromUserDisconnect(@NotNull User var1, @Nullable GrimPlayer var2, long var3);

   void observeBrandFromCheck(@NotNull GrimPlayer var1);

   void recordFlagFromCheck(@NotNull GrimPlayer var1, @NotNull AbstractCheck var2, double var3, @Nullable String var5);

   void recordFlagDataFromCheck(@NotNull GrimPlayer var1, @NotNull AbstractCheck var2, double var3, @Nullable byte[] var5);

   @NotNull
   static SessionTracker.ClientMeta clientMetaFor(@NotNull User user, @Nullable GrimPlayer gp) {
      String grimVersion = GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
      int clientVersionPvn = user.getClientVersion().getProtocolVersion();
      String serverVersion = GrimAPI.INSTANCE.getPlatformServer().getPlatformImplementationString();
      String brand = null;
      if (gp != null) {
         ClientBrand check = gp.checkManager.getPacketCheck(ClientBrand.class);
         if (check != null && check.isHasBrand()) {
            brand = check.getBrand();
         }
      }

      return new SessionTracker.ClientMeta(grimVersion, brand, clientVersionPvn, serverVersion);
   }
}
