package com.viaversion.viaversion.protocol.shared_registration.def;

import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocol.shared_registration.PacketBound;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

final class TextComponentRegistrations {
   static <CU extends ClientboundPacketType> void registerComponents1_12_2(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      ctx.clientbound(ClientboundPackets1_12_1.CHAT, cr::registerComponentPacket);
      registerCombatAndTitle1_12_2(ctx, cr);
      registerComponentsFrom1_12_2(ctx, cr);
   }

   static <CU extends ClientboundPacketType> void registerComponents1_14(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      ctx.clientbound(ClientboundPackets1_12_1.CHAT, cr::registerComponentPacket);
      registerCombatAndTitle1_12_2(ctx, cr);
      registerComponentsFrom1_12_2(ctx, cr);
      ctx.clientbound(ClientboundPackets1_16_2.SET_OBJECTIVE, cr::registerSetObjective);
      ctx.clientbound(ClientboundPackets1_16_2.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
   }

   static <CU extends ClientboundPacketType> void registerComponents1_16_2(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      ctx.clientbound(ClientboundPackets1_16.CHAT, cr::registerComponentPacket);
      registerComponentsFrom1_12_2(ctx, cr);
      ctx.clientbound(ClientboundPackets1_16_2.SET_OBJECTIVE, cr::registerSetObjective);
      ctx.clientbound(ClientboundPackets1_16_2.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_12_1.PLAYER_COMBAT, jcr::registerPlayerCombat, PacketBound.REMOVED_AT_MAX);
         ctx.clientbound(ClientboundPackets1_12_1.SET_TITLES, jcr::registerTitle, PacketBound.REMOVED_AT_MAX);
      }
   }

   static <CU extends ClientboundPacketType> void registerComponents1_17(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      ctx.clientbound(ClientboundPackets1_16.CHAT, cr::registerComponentPacket, PacketBound.REMOVED_AT_MAX);
      registerComponentsFrom1_12_2(ctx, cr);
      ctx.clientbound(ClientboundPackets1_17_1.SET_ACTION_BAR_TEXT, cr::registerComponentPacket, PacketBound.ADDED_AT_MIN);
      ctx.clientbound(ClientboundPackets1_17_1.SET_TITLE_TEXT, cr::registerComponentPacket, PacketBound.ADDED_AT_MIN);
      ctx.clientbound(ClientboundPackets1_17_1.SET_SUBTITLE_TEXT, cr::registerComponentPacket, PacketBound.ADDED_AT_MIN);
      ctx.clientbound(ClientboundPackets1_16_2.SET_OBJECTIVE, cr::registerSetObjective);
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_17_1.PLAYER_COMBAT_KILL, jcr::registerPlayerCombatKill, PacketBound.ADDED_AT_MIN);
      }

      ctx.clientbound(ClientboundPackets1_16_2.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
   }

   static <CU extends ClientboundPacketType> void registerComponents1_19(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      registerComponentsFrom1_12_2(ctx, cr);
      registerActionBarTitleSubtitle1_17_1(ctx, cr);
      ctx.clientbound(ClientboundPackets1_16_2.SET_OBJECTIVE, cr::registerSetObjective);
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_17_1.PLAYER_COMBAT_KILL, jcr::registerPlayerCombatKill);
      }

      ctx.clientbound(ClientboundPackets1_16_2.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
      ctx.clientbound(ClientboundPackets1_19.SYSTEM_CHAT, cr::registerComponentPacket, PacketBound.ADDED_AT_MIN);
      ctx.clientbound(ClientboundPackets1_19.SERVER_DATA, cr::registerComponentPacket, PacketBound.ADDED_AT_MIN);
   }

   static <CU extends ClientboundPacketType> void registerComponents1_20_3(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      registerComponentsFrom1_12_2(ctx, cr);
      registerActionBarTitleSubtitle1_17_1(ctx, cr);
      ctx.clientbound(ClientboundPackets1_16_2.SET_OBJECTIVE, cr::registerSetObjective);
      ctx.clientbound(ClientboundPackets1_19_1.SYSTEM_CHAT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundConfigurationPackets1_20_3.DISCONNECT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_3.DISGUISED_CHAT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_3.SET_SCORE, cr::registerSetScore1_20_3);
      ctx.clientbound(ClientboundPackets1_20_3.PLAYER_COMBAT_KILL, cr::registerPlayerCombatKill1_20);
      ctx.clientbound(ClientboundPackets1_20_5.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
      ctx.clientbound(ClientboundPackets1_19.SERVER_DATA, cr::registerComponentPacket);
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_20_3.PLAYER_CHAT, cu -> jcr.registerPlayerChat(cu, Types.VAR_INT));
         ctx.clientbound(ClientboundPackets1_20_3.PLAYER_INFO_UPDATE, jcr::registerPlayerInfoUpdate1_20_3);
      }
   }

   static <CU extends ClientboundPacketType> void registerCombatAndTitle1_12_2(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_12_1.PLAYER_COMBAT, jcr::registerPlayerCombat);
         ctx.clientbound(ClientboundPackets1_12_1.SET_TITLES, jcr::registerTitle);
      }
   }

   private static <CU extends ClientboundPacketType> void registerComponentsFrom1_12_2(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      ctx.clientbound(ClientboundPackets1_12_1.BOSS_EVENT, cr::registerBossEvent);
      ctx.clientbound(ClientboundPackets1_12_1.DISCONNECT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_12_1.TAB_LIST, cr::registerTabList);
      cr.registerLoginDisconnect();
   }

   static <CU extends ClientboundPacketType> void registerActionBarTitleSubtitle1_17_1(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      ctx.clientbound(ClientboundPackets1_17_1.SET_ACTION_BAR_TEXT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_17_1.SET_TITLE_TEXT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_17_1.SET_SUBTITLE_TEXT, cr::registerComponentPacket);
   }

   static <CU extends ClientboundPacketType> void registerComponents1_20_5(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      common1_20_5(ctx, cr);
      ctx.clientbound(ClientboundPackets1_20_5.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
      ctx.clientbound(ClientboundPackets1_20_5.DISGUISED_CHAT, cr::registerComponentPacket);
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_20_5.PLAYER_CHAT, cu -> jcr.registerPlayerChat(cu, Types.VAR_INT));
         ctx.clientbound(ClientboundPackets1_20_5.PLAYER_INFO_UPDATE, jcr::registerPlayerInfoUpdate1_20_3);
      }
   }

   private static <CU extends ClientboundPacketType> void common1_20_5(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      ctx.clientbound(ClientboundConfigurationPackets1_20_5.DISCONNECT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_5.DISCONNECT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_5.SET_ACTION_BAR_TEXT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_5.SET_TITLE_TEXT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_5.SET_SUBTITLE_TEXT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_5.BOSS_EVENT, cr::registerBossEvent);
      ctx.clientbound(ClientboundPackets1_20_5.TAB_LIST, cr::registerTabList);
      ctx.clientbound(ClientboundPackets1_20_5.SET_OBJECTIVE, cr::registerSetObjective);
      ctx.clientbound(ClientboundPackets1_20_5.SET_SCORE, cr::registerSetScore1_20_3);
      ctx.clientbound(ClientboundPackets1_20_5.SYSTEM_CHAT, cr::registerComponentPacket);
      ctx.clientbound(ClientboundPackets1_20_5.PLAYER_COMBAT_KILL, cr::registerPlayerCombatKill1_20);
      ctx.clientbound(ClientboundPackets1_20_5.SERVER_DATA, cr::registerComponentPacket);
      cr.registerLoginDisconnect();
   }

   static <CU extends ClientboundPacketType> void registerComponents1_21(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      common1_20_5(ctx, cr);
      ctx.clientbound(ClientboundPackets1_21.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
      ctx.clientbound(ClientboundPackets1_21.DISGUISED_CHAT, cr::registerDisguisedChat);
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_21.PLAYER_CHAT, cu -> jcr.registerPlayerChat(cu, ChatType.TYPE));
         ctx.clientbound(ClientboundPackets1_21.PLAYER_INFO_UPDATE, jcr::registerPlayerInfoUpdate1_20_3);
      }
   }

   static <CU extends ClientboundPacketType> void registerComponents1_21_4(RegistrationContext<CU, ?> ctx, ComponentRewriterBase<CU> cr) {
      common1_20_5(ctx, cr);
      ctx.clientbound(ClientboundPackets1_21_2.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_13);
      ctx.clientbound(ClientboundPackets1_21_2.DISGUISED_CHAT, cr::registerDisguisedChat);
      ctx.clientbound(ClientboundPackets1_21_2.PLAYER_INFO_UPDATE, cr::registerPlayerInfoUpdate1_21_4);
      if (cr instanceof JsonNBTComponentRewriter<CU> jcr) {
         ctx.clientbound(ClientboundPackets1_21_2.PLAYER_CHAT, cu -> jcr.registerPlayerChat(cu, ChatType.TYPE));
      }
   }

   static <CU extends ClientboundPacketType> void registerComponents1_21_5(RegistrationContext<CU, ?> ctx, NBTComponentRewriter<CU> cr) {
      common1_20_5(ctx, cr);
      ctx.clientbound(ClientboundPackets1_21_5.SET_PLAYER_TEAM, cr::registerSetPlayerTeam1_21_5);
      ctx.clientbound(ClientboundPackets1_21_5.DISGUISED_CHAT, cr::registerDisguisedChat);
      ctx.clientbound(ClientboundPackets1_21_5.PLAYER_INFO_UPDATE, cr::registerPlayerInfoUpdate1_21_4);
      ctx.clientbound(ClientboundPackets1_21_5.PLAYER_CHAT, cr::registerPlayerChat1_21_5);
   }

   static <CU extends ClientboundPacketType> void registerComponents26_2(RegistrationContext<CU, ?> ctx, NBTComponentRewriter<CU> cr) {
      common1_20_5(ctx, cr);
      ctx.clientbound(ClientboundPackets1_21_5.SET_PLAYER_TEAM, cr::registerSetPlayerTeam26_2);
      ctx.clientbound(ClientboundPackets1_21_5.DISGUISED_CHAT, cr::registerDisguisedChat);
      ctx.clientbound(ClientboundPackets1_21_5.PLAYER_INFO_UPDATE, cr::registerPlayerInfoUpdate1_21_4);
      ctx.clientbound(ClientboundPackets1_21_5.PLAYER_CHAT, cr::registerPlayerChat1_21_5);
   }

   static <CU extends ClientboundPacketType> void registerComponents26_3(RegistrationContext<CU, ?> ctx, NBTComponentRewriter<CU> cr) {
      common1_20_5(ctx, cr);
      ctx.clientbound(ClientboundPackets1_21_5.SET_PLAYER_TEAM, cr::registerSetPlayerTeam26_2);
      ctx.clientbound(ClientboundPackets1_21_5.DISGUISED_CHAT, cr::registerDisguisedChat);
      ctx.clientbound(ClientboundPackets1_21_5.PLAYER_INFO_UPDATE, cr::registerPlayerInfoUpdate1_21_4);
      ctx.clientbound(ClientboundPackets1_21_5.PLAYER_CHAT, cr::registerPlayerChat26_3);
   }

   @Nullable
   static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> ComponentRewriterBase<CU> text(RegistrationContext<CU, SU> ctx) {
      return (ComponentRewriterBase<CU>)ctx.protocol().getComponentRewriter();
   }

   @Nullable
   static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> NBTComponentRewriter<CU> nbtText(RegistrationContext<CU, SU> ctx) {
      return (NBTComponentRewriter<CU>)ctx.protocol().getComponentRewriter();
   }
}
