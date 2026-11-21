package net.essentialsx.api.v2.services.mail;

import java.util.UUID;

public class MailMessage {
   private final boolean read;
   private final boolean legacy;
   private final String senderName;
   private final UUID senderId;
   private final long timestamp;
   private final long expire;
   private final String message;

   public MailMessage(boolean read, boolean legacy, String sender, UUID uuid, long timestamp, long expire, String message) {
      this.read = read;
      this.legacy = legacy;
      this.senderName = sender;
      this.senderId = uuid;
      this.timestamp = timestamp;
      this.expire = expire;
      this.message = message;
   }

   public boolean isRead() {
      return this.read;
   }

   public boolean isLegacy() {
      return this.legacy;
   }

   public String getSenderUsername() {
      return this.senderName;
   }

   public UUID getSenderUUID() {
      return this.senderId;
   }

   public long getTimeSent() {
      return this.timestamp;
   }

   public long getTimeExpire() {
      return this.expire;
   }

   public String getMessage() {
      return this.message;
   }

   public boolean isExpired() {
      return this.getTimeExpire() == 0L ? false : System.currentTimeMillis() >= this.getTimeExpire();
   }
}
