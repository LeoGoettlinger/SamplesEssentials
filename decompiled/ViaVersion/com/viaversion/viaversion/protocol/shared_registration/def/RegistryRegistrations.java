package com.viaversion.viaversion.protocol.shared_registration.def;

import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.protocol.shared_registration.PacketBound;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

final class RegistryRegistrations {
   static <CU extends ClientboundPacketType> void registerTags1_17_1(RegistrationContext<CU, ?> ctx) {
      if (ctx.protocol().getTagRewriter() instanceof TagRewriter tagRewriter) {
         ctx.clientbound(ClientboundPackets1_17_1.UPDATE_TAGS, tagRewriter::registerGeneric);
      }
   }

   static <CU extends ClientboundPacketType> void registerTags1_20_2(RegistrationContext<CU, ?> ctx) {
      if (ctx.protocol().getTagRewriter() instanceof TagRewriter tagRewriter) {
         ctx.clientbound(ClientboundPackets1_20_2.UPDATE_TAGS, tagRewriter::registerGeneric);
         ctx.clientbound(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, tagRewriter::registerGeneric, PacketBound.ADDED_AT_MIN);
      }
   }

   static <CU extends ClientboundPacketType> void registerRegistryData1_21(RegistrationContext<CU, ?> ctx) {
      if (ctx.protocol().getRegistryDataRewriter() != null) {
         ctx.clientboundHandler(ClientboundConfigurationPackets1_21.REGISTRY_DATA, ctx.protocol().getRegistryDataRewriter()::handle);
      }
   }

   static <CU extends ClientboundPacketType> void registerRecipePackets1_21_2(RegistrationContext<CU, ?> ctx) {
      RecipeDisplayRewriter<CU> rr = ctx.protocol().getRecipeRewriter();
      if (rr != null) {
         ctx.clientbound(ClientboundPackets1_21_2.UPDATE_RECIPES, rr::registerUpdateRecipes);
         ctx.clientbound(ClientboundPackets1_21_2.RECIPE_BOOK_ADD, rr::registerRecipeBookAdd, PacketBound.ADDED_AT_MIN);
         ctx.clientbound(ClientboundPackets1_21_2.PLACE_GHOST_RECIPE, rr::registerPlaceGhostRecipe);
      }
   }

   static <CU extends ClientboundPacketType> void registerStatistics1_13(RegistrationContext<CU, ?> ctx) {
      ctx.clientbound(ClientboundPackets1_13.AWARD_STATS, new StatisticsRewriter<>(ctx.protocol())::register);
   }

   static <CU extends ClientboundPacketType> void registerSounds1_10(RegistrationContext<CU, ?> ctx) {
      ctx.clientbound(ClientboundPackets1_9_3.SOUND, new SoundRewriter<>(ctx.protocol())::registerSound);
   }

   static <CU extends ClientboundPacketType> void registerSounds1_14(RegistrationContext<CU, ?> ctx) {
      SoundRewriter<CU> sr = new SoundRewriter<>(ctx.protocol());
      ctx.clientbound(ClientboundPackets1_14.SOUND, sr::registerSound);
      ctx.clientbound(ClientboundPackets1_14.SOUND_ENTITY, sr::registerSound, PacketBound.ADDED_AT_MIN);
   }

   static <CU extends ClientboundPacketType> void registerSounds1_19_3(RegistrationContext<CU, ?> ctx) {
      SoundRewriter<CU> sr = new SoundRewriter<>(ctx.protocol());
      ctx.clientbound(ClientboundPackets1_19_3.SOUND, sr::registerSound1_19_3);
      ctx.clientbound(ClientboundPackets1_19_3.SOUND_ENTITY, sr::registerSound1_19_3);
   }

   static <CU extends ClientboundPacketType> void registerCommands1_19(RegistrationContext<CU, ?> ctx) {
      if (ctx.protocol().getMappingData() != null && !Mappings.isIntIdIdentity(ctx.protocol().getMappingData().getArgumentTypeMappings())
         || ctx.protocol().getRegistryDataRewriter() != null) {
         ctx.clientbound(ClientboundPackets1_19_3.COMMANDS, new CommandRewriter1_19_4<>(ctx.protocol())::registerDeclareCommands1_19);
      }
   }

   static <CU extends ClientboundPacketType> void registerAttributes1_21(RegistrationContext<CU, ?> ctx) {
      ctx.clientbound(ClientboundPackets1_21.UPDATE_ATTRIBUTES, new AttributeRewriter<>(ctx.protocol())::register1_21);
   }
}
