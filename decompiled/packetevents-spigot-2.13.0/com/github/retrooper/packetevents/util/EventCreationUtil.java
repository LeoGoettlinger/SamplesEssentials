package com.github.retrooper.packetevents.util;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketConfigReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketConfigSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketHandshakeReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketHandshakeSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketLoginReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketLoginSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.event.simple.PacketStatusReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketStatusSendEvent;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.protocol.player.User;

public class EventCreationUtil {
   public static PacketReceiveEvent createReceiveEvent(Object channel, User user, Object player, Object buffer, boolean autoProtocolTranslation) throws PacketProcessException {
      switch (user.getDecoderState()) {
         case HANDSHAKING:
            return new PacketHandshakeReceiveEvent(channel, user, player, buffer, autoProtocolTranslation);
         case STATUS:
            return new PacketStatusReceiveEvent(channel, user, player, buffer, autoProtocolTranslation);
         case LOGIN:
            return new PacketLoginReceiveEvent(channel, user, player, buffer, autoProtocolTranslation);
         case PLAY:
            return new PacketPlayReceiveEvent(channel, user, player, buffer, autoProtocolTranslation);
         case CONFIGURATION:
            return new PacketConfigReceiveEvent(channel, user, player, buffer, autoProtocolTranslation);
         default:
            throw new RuntimeException("Unknown connection state " + user.getDecoderState() + "!");
      }
   }

   public static PacketSendEvent createSendEvent(Object channel, User user, Object player, Object buffer, boolean autoProtocolTranslation) throws PacketProcessException {
      switch (user.getEncoderState()) {
         case HANDSHAKING:
            return new PacketHandshakeSendEvent(channel, user, player, buffer, autoProtocolTranslation);
         case STATUS:
            return new PacketStatusSendEvent(channel, user, player, buffer, autoProtocolTranslation);
         case LOGIN:
            return new PacketLoginSendEvent(channel, user, player, buffer, autoProtocolTranslation);
         case PLAY:
            return new PacketPlaySendEvent(channel, user, player, buffer, autoProtocolTranslation);
         case CONFIGURATION:
            return new PacketConfigSendEvent(channel, user, player, buffer, autoProtocolTranslation);
         default:
            throw new RuntimeException("Unknown connection state " + user.getEncoderState() + "!");
      }
   }
}
