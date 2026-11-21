package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.util.LegacyComponent;
import com.github.retrooper.packetevents.util.ColorUtil;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerTeams extends PacketWrapper<WrapperPlayServerTeams> {
   private String teamName;
   private WrapperPlayServerTeams.TeamMode teamMode;
   private Collection<String> players;
   private Optional<WrapperPlayServerTeams.ScoreBoardTeamInfo> teamInfo;

   public WrapperPlayServerTeams(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerTeams(
      String teamName, WrapperPlayServerTeams.TeamMode teamMode, @Nullable WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo, String... entities
   ) {
      this(teamName, teamMode, teamInfo, Arrays.asList(entities));
   }

   public WrapperPlayServerTeams(
      String teamName, WrapperPlayServerTeams.TeamMode teamMode, @Nullable WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo, Collection<String> entities
   ) {
      super(PacketType.Play.Server.TEAMS);
      this.teamName = teamName;
      this.teamMode = teamMode;
      this.players = entities;
      this.teamInfo = Optional.ofNullable(teamInfo);
   }

   @Deprecated
   public WrapperPlayServerTeams(
      String teamName, WrapperPlayServerTeams.TeamMode teamMode, Optional<WrapperPlayServerTeams.ScoreBoardTeamInfo> teamInfo, String... entities
   ) {
      this(teamName, teamMode, teamInfo, Arrays.asList(entities));
   }

   @Deprecated
   public WrapperPlayServerTeams(
      String teamName, WrapperPlayServerTeams.TeamMode teamMode, Optional<WrapperPlayServerTeams.ScoreBoardTeamInfo> teamInfo, Collection<String> entities
   ) {
      super(PacketType.Play.Server.TEAMS);
      this.teamName = teamName;
      this.teamMode = teamMode;
      this.players = entities;
      this.teamInfo = teamInfo;
   }

   @Override
   public void read() {
      int teamNameLimit = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18) ? 32767 : 16;
      this.teamName = this.readString(teamNameLimit);
      this.teamMode = WrapperPlayServerTeams.TeamMode.values()[this.readByte()];
      WrapperPlayServerTeams.ScoreBoardTeamInfo info = null;
      if (this.teamMode == WrapperPlayServerTeams.TeamMode.CREATE || this.teamMode == WrapperPlayServerTeams.TeamMode.UPDATE) {
         info = WrapperPlayServerTeams.ScoreBoardTeamInfo.read(this);
      }

      this.teamInfo = Optional.ofNullable(info);
      this.players = new ArrayList<>();
      if (this.teamMode == WrapperPlayServerTeams.TeamMode.CREATE
         || this.teamMode == WrapperPlayServerTeams.TeamMode.ADD_ENTITIES
         || this.teamMode == WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES) {
         int size;
         if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            size = this.readShort();
         } else {
            size = this.readVarInt();
         }

         for (int i = 0; i < size; i++) {
            this.players.add(this.readString(40));
         }
      }
   }

   @Override
   public void write() {
      int teamNameLimit = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18) ? 32767 : 16;
      this.writeString(this.teamName, teamNameLimit);
      this.writeByte(this.teamMode.ordinal());
      if (this.teamMode == WrapperPlayServerTeams.TeamMode.CREATE || this.teamMode == WrapperPlayServerTeams.TeamMode.UPDATE) {
         WrapperPlayServerTeams.ScoreBoardTeamInfo info = this.teamInfo.orElse(WrapperPlayServerTeams.ScoreBoardTeamInfo.EMPTY);
         WrapperPlayServerTeams.ScoreBoardTeamInfo.write(this, info);
      }

      if (this.teamMode == WrapperPlayServerTeams.TeamMode.CREATE
         || this.teamMode == WrapperPlayServerTeams.TeamMode.ADD_ENTITIES
         || this.teamMode == WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES) {
         if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            this.writeShort(this.players.size());
         } else {
            this.writeVarInt(this.players.size());
         }

         for (String playerName : this.players) {
            this.writeString(playerName, 40);
         }
      }
   }

   public void copy(WrapperPlayServerTeams wrapper) {
      this.teamName = wrapper.teamName;
      this.teamMode = wrapper.teamMode;
      this.players = wrapper.players;
      this.teamInfo = wrapper.teamInfo;
   }

   public String getTeamName() {
      return this.teamName;
   }

   public void setTeamName(String teamName) {
      this.teamName = teamName;
   }

   public WrapperPlayServerTeams.TeamMode getTeamMode() {
      return this.teamMode;
   }

   public void setTeamMode(WrapperPlayServerTeams.TeamMode teamMode) {
      this.teamMode = teamMode;
   }

   public Collection<String> getPlayers() {
      return this.players;
   }

   public void setPlayers(Collection<String> players) {
      this.players = players;
   }

   public Optional<WrapperPlayServerTeams.ScoreBoardTeamInfo> getTeamInfo() {
      return this.teamInfo;
   }

   public void setTeamInfo(@Nullable WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo) {
      this.teamInfo = Optional.ofNullable(teamInfo);
   }

   public static enum CollisionRule {
      ALWAYS("always"),
      NEVER("never"),
      PUSH_OTHER_TEAMS("pushOtherTeams"),
      PUSH_OWN_TEAM("pushOwnTeam");

      private final String id;

      private CollisionRule(String id) {
         this.id = id;
      }

      @Nullable
      public static WrapperPlayServerTeams.CollisionRule fromID(String id) {
         for (WrapperPlayServerTeams.CollisionRule value : values()) {
            if (value.id.equalsIgnoreCase(id)) {
               return value;
            }
         }

         return null;
      }

      public String getId() {
         return this.id;
      }
   }

   public static enum NameTagVisibility {
      ALWAYS("always"),
      NEVER("never"),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam");

      private final String id;

      private NameTagVisibility(String id) {
         this.id = id;
      }

      @Nullable
      public static WrapperPlayServerTeams.NameTagVisibility fromID(String id) {
         for (WrapperPlayServerTeams.NameTagVisibility value : values()) {
            if (value.id.equalsIgnoreCase(id)) {
               return value;
            }
         }

         return null;
      }

      public String getId() {
         return this.id;
      }
   }

   public static enum OptionData {
      NONE((byte)0),
      FRIENDLY_FIRE((byte)1),
      FRIENDLY_CAN_SEE_INVISIBLE((byte)2),
      ALL((byte)3);

      private static final WrapperPlayServerTeams.OptionData[] VALUES = values();
      private final byte byteValue;

      private OptionData(byte value) {
         this.byteValue = value;
      }

      public byte getByteValue() {
         return this.byteValue;
      }

      @Nullable
      public static WrapperPlayServerTeams.OptionData fromValue(byte value) {
         for (WrapperPlayServerTeams.OptionData data : VALUES) {
            if (data.getByteValue() == value) {
               return data;
            }
         }

         return null;
      }
   }

   public static class ScoreBoardTeamInfo {
      private static final WrapperPlayServerTeams.ScoreBoardTeamInfo EMPTY = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
         LegacyComponent.empty(),
         LegacyComponent.empty(),
         LegacyComponent.empty(),
         WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
         WrapperPlayServerTeams.CollisionRule.ALWAYS,
         NamedTextColor.WHITE,
         WrapperPlayServerTeams.OptionData.NONE
      );
      private LegacyComponent displayName;
      private LegacyComponent prefix;
      private LegacyComponent suffix;
      private WrapperPlayServerTeams.NameTagVisibility tagVisibility;
      private WrapperPlayServerTeams.CollisionRule collisionRule;
      private NamedTextColor color;
      private WrapperPlayServerTeams.OptionData optionData;

      public ScoreBoardTeamInfo(
         Component displayName,
         @Nullable Component prefix,
         @Nullable Component suffix,
         WrapperPlayServerTeams.NameTagVisibility tagVisibility,
         WrapperPlayServerTeams.CollisionRule collisionRule,
         NamedTextColor color,
         WrapperPlayServerTeams.OptionData optionData
      ) {
         this(
            LegacyComponent.wrapOrEmpty(displayName),
            LegacyComponent.wrapOrEmpty(prefix),
            LegacyComponent.wrapOrEmpty(suffix),
            tagVisibility,
            collisionRule,
            color,
            optionData
         );
      }

      public ScoreBoardTeamInfo(
         LegacyComponent displayName,
         LegacyComponent prefix,
         LegacyComponent suffix,
         WrapperPlayServerTeams.NameTagVisibility tagVisibility,
         WrapperPlayServerTeams.CollisionRule collisionRule,
         NamedTextColor color,
         WrapperPlayServerTeams.OptionData optionData
      ) {
         this.displayName = displayName;
         this.prefix = prefix;
         this.suffix = suffix;
         this.tagVisibility = tagVisibility;
         this.collisionRule = collisionRule;
         this.color = color;
         this.optionData = optionData;
      }

      public static WrapperPlayServerTeams.ScoreBoardTeamInfo read(PacketWrapper<?> wrapper) {
         if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_13)) {
            LegacyComponent displayName = new LegacyComponent(wrapper.readString(32));
            LegacyComponent prefix = new LegacyComponent(wrapper.readString(32));
            LegacyComponent suffix = new LegacyComponent(wrapper.readString(32));
            WrapperPlayServerTeams.OptionData optionData = wrapper.readEnum(WrapperPlayServerTeams.OptionData.values());
            WrapperPlayServerTeams.NameTagVisibility tagVisibility = WrapperPlayServerTeams.NameTagVisibility.ALWAYS;
            WrapperPlayServerTeams.CollisionRule collisionRule = WrapperPlayServerTeams.CollisionRule.ALWAYS;
            NamedTextColor color = NamedTextColor.WHITE;
            if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_8)) {
               tagVisibility = WrapperPlayServerTeams.NameTagVisibility.fromID(wrapper.readString(32));
               if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
                  collisionRule = WrapperPlayServerTeams.CollisionRule.fromID(wrapper.readString(32));
               }

               if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
                  int colorId = wrapper.readVarInt();
                  if (colorId == 21) {
                     colorId = -1;
                  }

                  color = ColorUtil.fromId(colorId);
               } else {
                  color = ColorUtil.fromId(wrapper.readByte());
               }
            }

            return new WrapperPlayServerTeams.ScoreBoardTeamInfo(displayName, prefix, suffix, tagVisibility, collisionRule, color, optionData);
         } else {
            LegacyComponent displayName = LegacyComponent.wrapOrEmpty(wrapper.readComponent());
            if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_2)) {
               WrapperPlayServerTeams.OptionData optionData = wrapper.readEnum(WrapperPlayServerTeams.OptionData.values());
               WrapperPlayServerTeams.NameTagVisibility nameTagVisibility;
               WrapperPlayServerTeams.CollisionRule collisionRule;
               if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
                  nameTagVisibility = wrapper.readEnum(WrapperPlayServerTeams.NameTagVisibility.class);
                  collisionRule = wrapper.readEnum(WrapperPlayServerTeams.CollisionRule.class);
               } else {
                  nameTagVisibility = WrapperPlayServerTeams.NameTagVisibility.fromID(wrapper.readString(40));
                  collisionRule = WrapperPlayServerTeams.CollisionRule.fromID(wrapper.readString(40));
               }

               NamedTextColor color = ColorUtil.fromId(wrapper.readByte());
               LegacyComponent prefix = LegacyComponent.wrapOrEmpty(wrapper.readComponent());
               LegacyComponent suffix = LegacyComponent.wrapOrEmpty(wrapper.readComponent());
               return new WrapperPlayServerTeams.ScoreBoardTeamInfo(displayName, prefix, suffix, nameTagVisibility, collisionRule, color, optionData);
            } else {
               LegacyComponent prefix = LegacyComponent.wrapOrEmpty(wrapper.readComponent());
               LegacyComponent suffix = LegacyComponent.wrapOrEmpty(wrapper.readComponent());
               WrapperPlayServerTeams.NameTagVisibility nameTagVisibility = wrapper.readEnum(WrapperPlayServerTeams.NameTagVisibility.class);
               WrapperPlayServerTeams.CollisionRule collisionRule = wrapper.readEnum(WrapperPlayServerTeams.CollisionRule.class);
               NamedTextColor color = wrapper.readOptional(ew -> ColorUtil.fromId(ew.readVarInt()));
               WrapperPlayServerTeams.OptionData optionData = wrapper.readEnum(WrapperPlayServerTeams.OptionData.values());
               return new WrapperPlayServerTeams.ScoreBoardTeamInfo(displayName, prefix, suffix, nameTagVisibility, collisionRule, color, optionData);
            }
         }
      }

      public static void write(PacketWrapper<?> wrapper, WrapperPlayServerTeams.ScoreBoardTeamInfo info) {
         if (wrapper.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
            wrapper.writeString(info.displayName.getLegacy());
            wrapper.writeString(info.prefix.getLegacy());
            wrapper.writeString(info.suffix.getLegacy());
            wrapper.writeEnum(info.optionData);
            if (wrapper.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
               wrapper.writeString(WrapperPlayServerTeams.NameTagVisibility.ALWAYS.getId(), 32);
               wrapper.writeByte(15);
            } else {
               wrapper.writeString(info.tagVisibility.getId(), 32);
               if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
                  wrapper.writeString(info.collisionRule.getId(), 32);
               }

               wrapper.writeByte(ColorUtil.getId(info.color));
            }
         } else {
            wrapper.writeComponent(info.displayName.getComponent());
            if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_2)) {
               wrapper.writeComponent(info.prefix.getComponent());
               wrapper.writeComponent(info.suffix.getComponent());
               wrapper.writeEnum(info.tagVisibility);
               wrapper.writeEnum(info.collisionRule);
               wrapper.writeOptional(info.color, (ew, c) -> ew.writeVarInt(ColorUtil.getId(c)));
               wrapper.writeEnum(info.optionData);
            } else {
               wrapper.writeEnum(info.optionData);
               if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
                  wrapper.writeEnum(info.tagVisibility);
                  wrapper.writeEnum(info.collisionRule);
               } else {
                  wrapper.writeString(info.tagVisibility.getId());
                  wrapper.writeString(info.collisionRule.getId());
               }

               if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
                  int colorId = ColorUtil.getId(info.color);
                  if (colorId < 0) {
                     colorId = 21;
                  }

                  wrapper.writeVarInt(colorId);
               } else {
                  wrapper.writeByte(ColorUtil.getId(info.color));
               }

               wrapper.writeComponent(info.prefix.getComponent());
               wrapper.writeComponent(info.suffix.getComponent());
            }
         }
      }

      public LegacyComponent getLegacyDisplayName() {
         return this.displayName;
      }

      public void setLegacyDisplayName(LegacyComponent component) {
         this.displayName = component;
      }

      public Component getDisplayName() {
         return this.displayName.getComponent();
      }

      public void setDisplayName(Component displayName) {
         this.displayName = new LegacyComponent(displayName);
      }

      public LegacyComponent getLegacyPrefix() {
         return this.prefix;
      }

      public void setLegacyPrefix(LegacyComponent component) {
         this.prefix = component;
      }

      public Component getPrefix() {
         return this.prefix.getComponent();
      }

      public void setPrefix(Component prefix) {
         this.prefix = new LegacyComponent(prefix);
      }

      public LegacyComponent getLegacySuffix() {
         return this.suffix;
      }

      public void setLegacySuffix(LegacyComponent component) {
         this.suffix = component;
      }

      public Component getSuffix() {
         return this.suffix.getComponent();
      }

      public void setSuffix(Component suffix) {
         this.suffix = new LegacyComponent(suffix);
      }

      public WrapperPlayServerTeams.NameTagVisibility getTagVisibility() {
         return this.tagVisibility;
      }

      public void setTagVisibility(WrapperPlayServerTeams.NameTagVisibility tagVisibility) {
         this.tagVisibility = tagVisibility;
      }

      public WrapperPlayServerTeams.CollisionRule getCollisionRule() {
         return this.collisionRule;
      }

      public void setCollisionRule(WrapperPlayServerTeams.CollisionRule collisionRule) {
         this.collisionRule = collisionRule;
      }

      public NamedTextColor getColor() {
         return this.color;
      }

      public void setColor(NamedTextColor color) {
         this.color = color;
      }

      public WrapperPlayServerTeams.OptionData getOptionData() {
         return this.optionData;
      }

      public void setOptionData(WrapperPlayServerTeams.OptionData optionData) {
         this.optionData = optionData;
      }
   }

   public static enum TeamMode {
      CREATE,
      REMOVE,
      UPDATE,
      ADD_ENTITIES,
      REMOVE_ENTITIES;
   }
}
