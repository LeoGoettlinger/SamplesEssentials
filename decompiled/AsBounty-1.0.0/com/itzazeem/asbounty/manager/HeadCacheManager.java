package com.itzazeem.asbounty.manager;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.itzazeem.asbounty.AsBounty;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HeadCacheManager {
   private final AsBounty plugin;

   public HeadCacheManager(AsBounty plugin) {
      this.plugin = plugin;
      this.updateAllOnlinePlayers();
   }

   public void cacheHead(UUID uuid, String name, String base64) {
      CompletableFuture.runAsync(
         () -> {
            try (PreparedStatement stmt = this.plugin
                  .getHeadDatabaseConnection()
                  .prepareStatement("INSERT OR REPLACE INTO player_heads (uuid, name, head_base64, last_updated) VALUES (?, ?, ?, ?)")) {
               stmt.setString(1, uuid.toString());
               stmt.setString(2, name);
               stmt.setString(3, base64);
               stmt.setLong(4, System.currentTimeMillis());
               stmt.execute();
            } catch (SQLException var9) {
            }
         }
      );
   }

   public CompletableFuture<String> getHeadBase64(UUID uuid) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            String var4;
            try (PreparedStatement stmt = this.plugin.getHeadDatabaseConnection().prepareStatement("SELECT head_base64 FROM player_heads WHERE uuid = ?")) {
               stmt.setString(1, uuid.toString());

               try (ResultSet rs = stmt.executeQuery()) {
                  if (!rs.next()) {
                     return null;
                  }

                  var4 = rs.getString("head_base64");
               }
            }

            return var4;
         } catch (SQLException var10) {
            var10.printStackTrace();
            return null;
         }
      });
   }

   public CompletableFuture<String> getPlayerName(UUID uuid) {
      return CompletableFuture.supplyAsync(() -> {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && player.isOnline()) {
            return player.getName();
         } else {
            try {
               String var5;
               try (PreparedStatement stmt = this.plugin.getHeadDatabaseConnection().prepareStatement("SELECT name FROM player_heads WHERE uuid = ?")) {
                  stmt.setString(1, uuid.toString());

                  try (ResultSet rs = stmt.executeQuery()) {
                     if (!rs.next()) {
                        return null;
                     }

                     var5 = rs.getString("name");
                  }
               }

               return var5;
            } catch (SQLException var11) {
               var11.printStackTrace();
               return null;
            }
         }
      });
   }

   public String getPlayerNameSync(UUID uuid) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null && player.isOnline()) {
         return player.getName();
      } else {
         try {
            String var5;
            try (PreparedStatement stmt = this.plugin.getHeadDatabaseConnection().prepareStatement("SELECT name FROM player_heads WHERE uuid = ?")) {
               stmt.setString(1, uuid.toString());

               try (ResultSet rs = stmt.executeQuery()) {
                  if (!rs.next()) {
                     return null;
                  }

                  var5 = rs.getString("name");
               }
            }

            return var5;
         } catch (SQLException var11) {
            var11.printStackTrace();
            return null;
         }
      }
   }

   public CompletableFuture<String> getOrFetchHeadBase64(UUID uuid, String playerName) {
      return this.getHeadBase64(uuid)
         .thenCompose(cached -> cached != null && !cached.isEmpty() ? CompletableFuture.completedFuture(cached) : this.fetchHeadFromMinecraft(uuid, playerName));
   }

   private CompletableFuture<String> fetchHeadFromMinecraft(UUID uuid, String playerName) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            PlayerProfile profile = Bukkit.createProfile(uuid, playerName);
            profile.update().join();

            for (ProfileProperty property : profile.getProperties()) {
               if (property.getName().equals("textures")) {
                  String base64 = property.getValue();
                  if (base64 != null && !base64.isEmpty()) {
                     this.cacheHead(uuid, playerName, base64);
                     return base64;
                  }
               }
            }
         } catch (Exception var7) {
         }

         return null;
      });
   }

   public String fetchHeadSync(UUID uuid, String playerName) {
      try {
         PlayerProfile profile = Bukkit.createProfile(uuid, playerName);
         profile.update().join();

         for (ProfileProperty property : profile.getProperties()) {
            if (property.getName().equals("textures")) {
               String base64 = property.getValue();
               if (base64 != null && !base64.isEmpty()) {
                  this.cacheHead(uuid, playerName, base64);
                  return base64;
               }
            }
         }
      } catch (Exception var7) {
      }

      return null;
   }

   public void updateAllOnlinePlayers() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.fetchHeadFromMinecraft(player.getUniqueId(), player.getName());
      }
   }
}
