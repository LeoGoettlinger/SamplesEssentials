package vrx.economy.vRXClans;

import java.time.Instant;

public record ClanLogEntry(long timestamp, String clanName, ClanLogEntry.LogType type, String actor, String target, double amount, String message) {
   public static ClanLogEntry now(String clanName, ClanLogEntry.LogType type, String actor, String target, double amount, String message) {
      return new ClanLogEntry(Instant.now().toEpochMilli(), clanName, type, actor, target, amount, message);
   }

   public static enum LogType {
      BANK_DEPOSIT,
      BANK_WITHDRAW,
      MEMBER_KICK,
      MEMBER_PROMOTE,
      MEMBER_DEMOTE,
      WAR_REQUEST,
      WAR_START,
      WAR_KILL,
      WAR_END,
      DIPLOMACY_ALLY,
      DIPLOMACY_RIVAL,
      RANK_RENAME,
      LEVEL_REWARD,
      TAG_UPDATE,
      HOME_SET,
      HOME_DELETE,
      ACHIEVEMENT_UNLOCK,
      ADMIN_EDIT,
      SHOP_UPGRADE,
      MAIL_SEND,
      MOTD_UPDATE,
      PROFILE_UPDATE,
      APPLICATION_CREATE,
      APPLICATION_ACCEPT,
      APPLICATION_DENY;
   }
}
