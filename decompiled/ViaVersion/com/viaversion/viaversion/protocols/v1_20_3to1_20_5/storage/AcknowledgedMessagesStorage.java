package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import java.util.BitSet;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class AcknowledgedMessagesStorage {
   private static final int MAX_HISTORY = 20;
   private static final int MINIMUM_DELAYED_ACK_COUNT = 20;
   private static final BitSet DUMMY_LAST_SEEN_MESSAGES = new BitSet();
   private Boolean secureChatEnforced;
   private AcknowledgedMessagesStorage.ChatSession chatSession;
   private BitSet lastSeenMessages = new BitSet();
   private int delayedAckCount;

   public int updateFromMessage(int ackCount, BitSet lastSeenMessages) {
      int delayedAckCount = this.delayedAckCount;
      this.delayedAckCount = 0;
      this.lastSeenMessages = lastSeenMessages;
      return ackCount + delayedAckCount;
   }

   public int accumulateAckCount(int ackCount) {
      this.delayedAckCount += ackCount;
      int ackCountToForward = this.delayedAckCount - 20;
      if (ackCountToForward >= 20) {
         this.lastSeenMessages = DUMMY_LAST_SEEN_MESSAGES;
         this.delayedAckCount = 20;
         return ackCountToForward;
      } else {
         return 0;
      }
   }

   public BitSet createSpoofedAck() {
      return this.lastSeenMessages;
   }

   public void setSecureChatEnforced(boolean secureChatEnforced) {
      this.secureChatEnforced = secureChatEnforced;
   }

   @Nullable
   public Boolean secureChatEnforced() {
      return this.secureChatEnforced;
   }

   public boolean isSecureChatEnforced() {
      return this.secureChatEnforced == null || this.secureChatEnforced;
   }

   public void queueChatSession(UUID sessionId, ProfileKey profileKey) {
      this.chatSession = new AcknowledgedMessagesStorage.ChatSession(sessionId, profileKey);
   }

   public void sendQueuedChatSession(PacketWrapper wrapper) {
      if (this.chatSession != null) {
         PacketWrapper chatSessionUpdate = wrapper.create(ServerboundPackets1_20_3.CHAT_SESSION_UPDATE);
         chatSessionUpdate.write(Types.UUID, this.chatSession.sessionId());
         chatSessionUpdate.write(Types.PROFILE_KEY, this.chatSession.profileKey());
         chatSessionUpdate.sendToServer(Protocol1_20_3To1_20_5.class);
         this.chatSession = null;
      }
   }

   public void clear() {
      this.lastSeenMessages = new BitSet();
      this.delayedAckCount = 0;
   }

   public record ChatSession(UUID sessionId, ProfileKey profileKey) {
   }
}
