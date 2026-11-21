package com.skyblockexp.ezrtp.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.bukkit.Bukkit;

public class MySqlRtpUsageStorage implements RtpUsageStorage {
   private final String url;
   private final String user;
   private final String password;

   public MySqlRtpUsageStorage(String url, String user, String password) {
      this.url = url;
      this.user = user;
      this.password = password;
      this.setupTables();
   }

   private Connection getConnection() throws SQLException {
      return DriverManager.getConnection(this.url, this.user, this.password);
   }

   private void setupTables() {
      try (
         Connection conn = this.getConnection();
         Statement st = conn.createStatement();
      ) {
         st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS ezrtp_usage (player VARCHAR(36), world VARCHAR(64), period VARCHAR(16), lastRtp BIGINT, usage INT, PRIMARY KEY(player, world, period))"
         );
      } catch (SQLException var9) {
         Bukkit.getLogger().warning("[EzRTP] Failed to setup MySQL tables: " + var9.getMessage());
      }
   }

   @Override
   public long getLastRtpTime(UUID player, String world) {
      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT lastRtp FROM ezrtp_usage WHERE player=? AND world=? AND period='global'");
      ) {
         ps.setString(1, player.toString());
         ps.setString(2, world);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
         }
      } catch (SQLException var14) {
         Bukkit.getLogger().warning("[EzRTP] MySQL getLastRtpTime: " + var14.getMessage());
         return 0L;
      }
   }

   @Override
   public void setLastRtpTime(UUID player, String world, long time) {
      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO ezrtp_usage (player, world, period, lastRtp, usage) VALUES (?, ?, 'global', ?, 0) ON DUPLICATE KEY UPDATE lastRtp=?"
         );
      ) {
         ps.setString(1, player.toString());
         ps.setString(2, world);
         ps.setLong(3, time);
         ps.setLong(4, time);
         ps.executeUpdate();
      } catch (SQLException var13) {
         Bukkit.getLogger().warning("[EzRTP] MySQL setLastRtpTime: " + var13.getMessage());
      }
   }

   @Override
   public int getUsageCount(UUID player, String world, String period) {
      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT usage FROM ezrtp_usage WHERE player=? AND world=? AND period=?");
      ) {
         ps.setString(1, player.toString());
         ps.setString(2, world);
         ps.setString(3, period);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
         }
      } catch (SQLException var15) {
         Bukkit.getLogger().warning("[EzRTP] MySQL getUsageCount: " + var15.getMessage());
         return 0;
      }
   }

   @Override
   public void incrementUsage(UUID player, String world, String period) {
      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO ezrtp_usage (player, world, period, lastRtp, usage) VALUES (?, ?, ?, 0, 1) ON DUPLICATE KEY UPDATE usage=usage+1"
         );
      ) {
         ps.setString(1, player.toString());
         ps.setString(2, world);
         ps.setString(3, period);
         ps.executeUpdate();
      } catch (SQLException var12) {
         Bukkit.getLogger().warning("[EzRTP] MySQL incrementUsage: " + var12.getMessage());
      }
   }

   @Override
   public void resetUsage(UUID player, String world, String period) {
      String sql;
      if (player == null && world == null) {
         sql = "UPDATE ezrtp_usage SET usage=0 WHERE period=?";
      } else if (player != null && world == null) {
         sql = "UPDATE ezrtp_usage SET usage=0 WHERE player=? AND period=?";
      } else if (player == null && world != null) {
         sql = "UPDATE ezrtp_usage SET usage=0 WHERE world=? AND period=?";
      } else {
         sql = "UPDATE ezrtp_usage SET usage=0 WHERE player=? AND world=? AND period=?";
      }

      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
      ) {
         int idx = 1;
         if (player != null && world != null) {
            ps.setString(idx++, player.toString());
            ps.setString(idx++, world);
         } else if (player != null) {
            ps.setString(idx++, player.toString());
         } else if (world != null) {
            ps.setString(idx++, world);
         }

         ps.setString(idx, period);
         ps.executeUpdate();
      } catch (SQLException var13) {
         Bukkit.getLogger().warning("[EzRTP] MySQL resetUsage: " + var13.getMessage());
      }
   }

   @Override
   public void save() {
   }

   @Override
   public void reload() {
   }
}
