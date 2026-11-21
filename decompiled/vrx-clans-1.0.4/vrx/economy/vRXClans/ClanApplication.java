package vrx.economy.vRXClans;

import java.util.UUID;

public record ClanApplication(UUID applicantUuid, String applicantName, String clanName, long createdAt, long expiresAt) {
   public boolean expired() {
      return System.currentTimeMillis() > this.expiresAt;
   }
}
