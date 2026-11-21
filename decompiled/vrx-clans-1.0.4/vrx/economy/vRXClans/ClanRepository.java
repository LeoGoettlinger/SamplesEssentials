package vrx.economy.vRXClans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ClanRepository {
   private final VRXClans plugin;
   private final DatabaseManager databaseManager;

   public ClanRepository(VRXClans plugin, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
   }

   public ClanRepository.ClanDataSnapshot loadAll() throws SQLException {
      Map<String, Clan> clans = new HashMap<>();
      Map<UUID, Clan> playerClans = new HashMap<>();

      try (
         Connection connection = this.databaseManager.getConnection();
         Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM vrx_clans");
      ) {
         while (rs.next()) {
            String name = rs.getString("name");
            Clan clan = new Clan(name, UUID.randomUUID());
            clan.getMembers().clear();
            this.hydrateClan(rs, clan);
            clans.put(name, clan);
         }
      }

      try (
         Connection connection = this.databaseManager.getConnection();
         Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM vrx_members");
      ) {
         while (rs.next()) {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            String clanName = rs.getString("clan_name");
            Clan.ClanRank rank = Clan.ClanRank.valueOf(rs.getString("rank"));
            Clan clan = clans.get(clanName);
            if (clan != null) {
               clan.setRank(uuid, rank);
               playerClans.put(uuid, clan);
            }
         }
      }

      return new ClanRepository.ClanDataSnapshot(clans, playerClans);
   }

   public void saveAll(Collection<Clan> clans) throws SQLException {
      try (Connection connection = this.databaseManager.getConnection()) {
         connection.setAutoCommit(false);

         try {
            try (Statement clearMembers = connection.createStatement()) {
               clearMembers.executeUpdate("DELETE FROM vrx_members");
            }

            String replaceQuery = "REPLACE INTO vrx_clans (name, bank, level, xp, lastUpkeep, radBonus, xpBoost, hasteBoost, speedBoost, strBoost, qZombies, qDiamonds, qMoney, qZDone, qDDone, qMDone, qReset, kills, deaths, warActive, warKills, warWager, bWorld, bX, bY, bZ, bYaw, bPitch, vault, allies, rivals, bRegen, bSat, perms, rankNames, rewardLevel, tag, tagColor, homes, achievements, description, iconMaterial, motd, mail, extraMembers, extraHomes, extraVaultRows, extraAllies, extraRivals, tpDelayReduction, tpCostReduction) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (PreparedStatement ps = connection.prepareStatement(replaceQuery)) {
               for (Clan clan : clans) {
                  this.bindClan(ps, clan);
                  ps.addBatch();
               }

               ps.executeBatch();
            }

            String memberQuery = "REPLACE INTO vrx_members (uuid, clan_name, rank) VALUES (?,?,?)";

            try (PreparedStatement ps = connection.prepareStatement(memberQuery)) {
               for (Clan clan : clans) {
                  for (Entry<UUID, Clan.ClanRank> entry : clan.getMembers().entrySet()) {
                     ps.setString(1, entry.getKey().toString());
                     ps.setString(2, clan.getName());
                     ps.setString(3, entry.getValue().name());
                     ps.addBatch();
                  }
               }

               ps.executeBatch();
            }

            connection.commit();
         } catch (SQLException var28) {
            connection.rollback();
            throw var28;
         } finally {
            connection.setAutoCommit(true);
         }
      }
   }

   public void deleteClan(String clanName) throws SQLException {
      try (Connection connection = this.databaseManager.getConnection()) {
         connection.setAutoCommit(false);

         try (
            PreparedStatement clans = connection.prepareStatement("DELETE FROM vrx_clans WHERE name = ?");
            PreparedStatement members = connection.prepareStatement("DELETE FROM vrx_members WHERE clan_name = ?");
         ) {
            clans.setString(1, clanName);
            clans.executeUpdate();
            members.setString(1, clanName);
            members.executeUpdate();
            connection.commit();
         } catch (SQLException var21) {
            connection.rollback();
            throw var21;
         } finally {
            connection.setAutoCommit(true);
         }
      }
   }

   private void hydrateClan(ResultSet rs, Clan clan) throws SQLException {
      clan.setBankBalance(rs.getDouble("bank"));
      clan.setLevel(rs.getInt("level"));
      clan.setXp(rs.getDouble("xp"));

      try {
         clan.setLastRewardLevel(rs.getInt("rewardLevel"));
      } catch (SQLException var24) {
         clan.setLastRewardLevel(clan.getLevel());
      }

      clan.setLastUpkeepTime(rs.getLong("lastUpkeep"));
      clan.setPurchasedRadiusBonus(rs.getInt("radBonus"));
      clan.setXpBooster(rs.getLong("xpBoost"));
      clan.setHasteBooster(rs.getLong("hasteBoost"));
      clan.setSpeedBooster(rs.getLong("speedBoost"));
      clan.setStrengthBooster(rs.getLong("strBoost"));
      clan.setQuestZombies(rs.getInt("qZombies"));
      clan.setQuestDiamonds(rs.getInt("qDiamonds"));
      clan.setQuestMoney(rs.getDouble("qMoney"));
      clan.setQuestZombiesDone(rs.getBoolean("qZDone"));
      clan.setQuestDiamondsDone(rs.getBoolean("qDDone"));
      clan.setQuestBankDone(rs.getBoolean("qMDone"));
      clan.setLastQuestReset(rs.getLong("qReset"));
      clan.setKills(rs.getInt("kills"));
      clan.setDeaths(rs.getInt("deaths"));
      clan.setActiveWar(rs.getString("warActive"));
      clan.setWarKills(rs.getInt("warKills"));
      clan.setWarWager(rs.getDouble("warWager"));
      clan.setBaseRegen(rs.getBoolean("bRegen"));
      clan.setBaseSaturation(rs.getBoolean("bSat"));

      try {
         clan.setTag(rs.getString("tag"));
      } catch (SQLException var23) {
      }

      try {
         clan.setTagColor(rs.getString("tagColor"));
      } catch (SQLException var22) {
      }

      try {
         clan.loadHomes(rs.getString("homes"));
      } catch (SQLException var21) {
      }

      try {
         clan.loadAchievements(rs.getString("achievements"));
      } catch (SQLException var20) {
      }

      try {
         clan.setDescription(rs.getString("description"));
      } catch (SQLException var19) {
      }

      try {
         clan.setIconMaterial(rs.getString("iconMaterial"));
      } catch (SQLException var18) {
      }

      try {
         clan.setMotd(rs.getString("motd"));
      } catch (SQLException var17) {
      }

      try {
         clan.loadMail(rs.getString("mail"));
      } catch (SQLException var16) {
      }

      try {
         clan.setExtraMemberSlots(rs.getInt("extraMembers"));
      } catch (SQLException var15) {
      }

      try {
         clan.setExtraHomeSlots(rs.getInt("extraHomes"));
      } catch (SQLException var14) {
      }

      try {
         clan.setExtraVaultRows(rs.getInt("extraVaultRows"));
      } catch (SQLException var13) {
      }

      try {
         clan.setExtraAllySlots(rs.getInt("extraAllies"));
      } catch (SQLException var12) {
      }

      try {
         clan.setExtraRivalSlots(rs.getInt("extraRivals"));
      } catch (SQLException var11) {
      }

      try {
         clan.setTeleportDelayReduction(rs.getInt("tpDelayReduction"));
      } catch (SQLException var10) {
      }

      try {
         clan.setTeleportCostReductionPercent(rs.getDouble("tpCostReduction"));
      } catch (SQLException var9) {
      }

      String perms = rs.getString("perms");
      if (perms != null && !perms.isEmpty()) {
         clan.loadPerms(perms);
      }

      try {
         String rankNames = rs.getString("rankNames");
         if (rankNames != null && !rankNames.isEmpty()) {
            clan.loadRankNames(rankNames);
         }
      } catch (SQLException var8) {
      }

      String worldName = rs.getString("bWorld");
      if (worldName != null && !worldName.isEmpty()) {
         World world = Bukkit.getWorld(worldName);
         if (world != null) {
            clan.setBaseLocation(new Location(world, rs.getDouble("bX"), rs.getDouble("bY"), rs.getDouble("bZ"), rs.getFloat("bYaw"), rs.getFloat("bPitch")));
         } else {
            this.plugin.getLogger().warning("Clan base world is not loaded: " + worldName + " for clan " + clan.getName());
         }
      }

      String vaultData = rs.getString("vault");
      if (vaultData != null && !vaultData.isEmpty()) {
         clan.setCachedVaultBase64(vaultData);
      }

      String allies = rs.getString("allies");
      if (allies != null && !allies.isEmpty()) {
         clan.getAllies().addAll(Arrays.asList(allies.split(",")));
      }

      String rivals = rs.getString("rivals");
      if (rivals != null && !rivals.isEmpty()) {
         clan.getRivals().addAll(Arrays.asList(rivals.split(",")));
      }
   }

   private void bindClan(PreparedStatement ps, Clan clan) throws SQLException {
      ps.setString(1, clan.getName());
      ps.setDouble(2, clan.getBankBalance());
      ps.setInt(3, clan.getLevel());
      ps.setDouble(4, clan.getXp());
      ps.setLong(5, clan.getLastUpkeepTime());
      ps.setInt(6, clan.getPurchasedRadiusBonus());
      ps.setLong(7, clan.getXpBooster());
      ps.setLong(8, clan.getHasteBooster());
      ps.setLong(9, clan.getSpeedBooster());
      ps.setLong(10, clan.getStrengthBooster());
      ps.setInt(11, clan.getQuestZombies());
      ps.setInt(12, clan.getQuestDiamonds());
      ps.setDouble(13, clan.getQuestMoney());
      ps.setBoolean(14, clan.isQuestZombiesDone());
      ps.setBoolean(15, clan.isQuestDiamondsDone());
      ps.setBoolean(16, clan.isQuestBankDone());
      ps.setLong(17, clan.getLastQuestReset());
      ps.setInt(18, clan.getKills());
      ps.setInt(19, clan.getDeaths());
      ps.setString(20, clan.getActiveWar());
      ps.setInt(21, clan.getWarKills());
      ps.setDouble(22, clan.getWarWager());
      Location base = clan.getBaseLocation();
      if (base != null && base.getWorld() != null) {
         ps.setString(23, base.getWorld().getName());
         ps.setDouble(24, base.getX());
         ps.setDouble(25, base.getY());
         ps.setDouble(26, base.getZ());
         ps.setFloat(27, base.getYaw());
         ps.setFloat(28, base.getPitch());
      } else {
         ps.setString(23, "");
         ps.setDouble(24, 0.0);
         ps.setDouble(25, 0.0);
         ps.setDouble(26, 0.0);
         ps.setFloat(27, 0.0F);
         ps.setFloat(28, 0.0F);
      }

      ps.setString(29, Optional.ofNullable(clan.getCachedVaultBase64()).orElse(""));
      ps.setString(30, String.join(",", clan.getAllies()));
      ps.setString(31, String.join(",", clan.getRivals()));
      ps.setBoolean(32, clan.isBaseRegen());
      ps.setBoolean(33, clan.isBaseSaturation());
      ps.setString(34, clan.serializePerms());
      ps.setString(35, clan.serializeRankNames());
      ps.setInt(36, clan.getLastRewardLevel());
      ps.setString(37, clan.getTag());
      ps.setString(38, clan.getTagColor());
      ps.setString(39, clan.serializeHomes());
      ps.setString(40, clan.serializeAchievements());
      ps.setString(41, clan.getDescription());
      ps.setString(42, clan.getIconMaterial());
      ps.setString(43, clan.getMotd());
      ps.setString(44, clan.serializeMail());
      ps.setInt(45, clan.getExtraMemberSlots());
      ps.setInt(46, clan.getExtraHomeSlots());
      ps.setInt(47, clan.getExtraVaultRows());
      ps.setInt(48, clan.getExtraAllySlots());
      ps.setInt(49, clan.getExtraRivalSlots());
      ps.setInt(50, clan.getTeleportDelayReduction());
      ps.setDouble(51, clan.getTeleportCostReductionPercent());
   }

   public void insertLog(ClanLogEntry entry) throws SQLException {
      String sql = "INSERT INTO vrx_clan_logs (timestamp, clan_name, type, actor, target, amount, message) VALUES (?,?,?,?,?,?,?)";

      try (
         Connection connection = this.databaseManager.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql);
      ) {
         ps.setLong(1, entry.timestamp());
         ps.setString(2, entry.clanName());
         ps.setString(3, entry.type().name());
         ps.setString(4, entry.actor());
         ps.setString(5, entry.target());
         ps.setDouble(6, entry.amount());
         ps.setString(7, entry.message());
         ps.executeUpdate();
      }
   }

   public List<ClanLogEntry> loadLogs(String clanName, int limit) throws SQLException {
      String sql = "SELECT timestamp, clan_name, type, actor, target, amount, message FROM vrx_clan_logs WHERE clan_name = ? ORDER BY timestamp DESC LIMIT ?";
      List<ClanLogEntry> result = new ArrayList<>();

      try (
         Connection connection = this.databaseManager.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql);
      ) {
         ps.setString(1, clanName);
         ps.setInt(2, Math.max(1, Math.min(limit, 100)));

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               result.add(
                  new ClanLogEntry(
                     rs.getLong("timestamp"),
                     rs.getString("clan_name"),
                     ClanLogEntry.LogType.valueOf(rs.getString("type")),
                     rs.getString("actor"),
                     rs.getString("target"),
                     rs.getDouble("amount"),
                     rs.getString("message")
                  )
               );
            }
         }
      }

      return result;
   }

   public void insertWarHistory(WarHistoryEntry entry) throws SQLException {
      String sql = "INSERT INTO vrx_war_history (timestamp, winner, loser, clan_a, clan_b, winner_kills, loser_kills, pot, duration_millis) VALUES (?,?,?,?,?,?,?,?,?)";

      try (
         Connection connection = this.databaseManager.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql);
      ) {
         ps.setLong(1, entry.timestamp());
         ps.setString(2, entry.winner());
         ps.setString(3, entry.loser());
         ps.setString(4, entry.clanA());
         ps.setString(5, entry.clanB());
         ps.setInt(6, entry.winnerKills());
         ps.setInt(7, entry.loserKills());
         ps.setDouble(8, entry.pot());
         ps.setLong(9, entry.durationMillis());
         ps.executeUpdate();
      }
   }

   public List<WarHistoryEntry> loadWarHistory(String clanName, int limit) throws SQLException {
      String sql = "SELECT timestamp, winner, loser, clan_a, clan_b, winner_kills, loser_kills, pot, duration_millis FROM vrx_war_history WHERE clan_a = ? OR clan_b = ? ORDER BY timestamp DESC LIMIT ?";
      List<WarHistoryEntry> result = new ArrayList<>();

      try (
         Connection connection = this.databaseManager.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql);
      ) {
         ps.setString(1, clanName);
         ps.setString(2, clanName);
         ps.setInt(3, Math.max(1, Math.min(limit, 100)));

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               result.add(
                  new WarHistoryEntry(
                     rs.getLong("timestamp"),
                     rs.getString("winner"),
                     rs.getString("loser"),
                     rs.getString("clan_a"),
                     rs.getString("clan_b"),
                     rs.getInt("winner_kills"),
                     rs.getInt("loser_kills"),
                     rs.getDouble("pot"),
                     rs.getLong("duration_millis")
                  )
               );
            }
         }
      }

      return result;
   }

   public record ClanDataSnapshot(Map<String, Clan> clans, Map<UUID, Clan> playerClans) {
   }
}
