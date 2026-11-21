package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.EnumSet;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public class WrapperPlayServerBossBar extends PacketWrapper<WrapperPlayServerBossBar> {
   private UUID uuid;
   private WrapperPlayServerBossBar.Action action;
   private Component title;
   private float health;
   private BossBar.Color color;
   private BossBar.Overlay overlay;
   private EnumSet<BossBar.Flag> flags;

   public WrapperPlayServerBossBar(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerBossBar(UUID uuid, WrapperPlayServerBossBar.Action action) {
      super(PacketType.Play.Server.BOSS_BAR);
      this.uuid = uuid;
      this.action = action;
   }

   @Override
   public void read() {
      this.uuid = this.readUUID();
      this.action = this.readEnum(WrapperPlayServerBossBar.Action.class);
      switch (this.action) {
         case ADD:
            this.title = this.readComponent();
            this.health = this.readFloat();
            this.color = this.readEnum(BossBar.Color.class);
            this.overlay = this.readEnum(BossBar.Overlay.class);
            this.flags = this.getFlagsFromBytes(this.readUnsignedByte());
         case REMOVE:
         default:
            break;
         case UPDATE_HEALTH:
            this.health = this.readFloat();
            break;
         case UPDATE_TITLE:
            this.title = this.readComponent();
            break;
         case UPDATE_STYLE:
            this.color = this.readEnum(BossBar.Color.class);
            this.overlay = this.readEnum(BossBar.Overlay.class);
            break;
         case UPDATE_FLAGS:
            this.flags = this.getFlagsFromBytes(this.readUnsignedByte());
      }
   }

   @Override
   public void write() {
      this.writeUUID(this.uuid);
      this.writeEnum(this.action);
      switch (this.action) {
         case ADD:
            this.writeComponent(this.title);
            this.writeFloat(this.health);
            this.writeEnum(this.color);
            this.writeEnum(this.overlay);
            this.writeByte(this.convertFlagsToBytes());
         case REMOVE:
         default:
            break;
         case UPDATE_HEALTH:
            this.writeFloat(this.health);
            break;
         case UPDATE_TITLE:
            this.writeComponent(this.title);
            break;
         case UPDATE_STYLE:
            this.writeEnum(this.color);
            this.writeEnum(this.overlay);
            break;
         case UPDATE_FLAGS:
            this.writeByte(this.convertFlagsToBytes());
      }
   }

   public void copy(WrapperPlayServerBossBar wrapper) {
      this.uuid = wrapper.uuid;
      this.action = wrapper.action;
      this.title = wrapper.title;
      this.health = wrapper.health;
      this.color = wrapper.color;
      this.overlay = wrapper.overlay;
      this.flags = wrapper.flags;
   }

   private EnumSet<BossBar.Flag> getFlagsFromBytes(short b) {
      EnumSet<BossBar.Flag> list = EnumSet.noneOf(BossBar.Flag.class);
      if ((b & 1) != 0) {
         list.add(BossBar.Flag.DARKEN_SCREEN);
      }

      if ((b & 2) != 0) {
         list.add(BossBar.Flag.PLAY_BOSS_MUSIC);
      }

      if ((b & 4) != 0) {
         list.add(BossBar.Flag.CREATE_WORLD_FOG);
      }

      return list;
   }

   private byte convertFlagsToBytes() {
      int bitmask = 0;

      for (BossBar.Flag flag : this.flags) {
         int id;
         switch (flag) {
            case DARKEN_SCREEN:
               id = 1;
               break;
            case PLAY_BOSS_MUSIC:
               id = 2;
               break;
            case CREATE_WORLD_FOG:
               id = 4;
               break;
            default:
               id = 0;
         }

         bitmask |= id;
      }

      return (byte)bitmask;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public void setUUID(UUID uuid) {
      this.uuid = uuid;
   }

   public WrapperPlayServerBossBar.Action getAction() {
      return this.action;
   }

   public void setAction(WrapperPlayServerBossBar.Action action) {
      this.action = action;
   }

   public Component getTitle() {
      return this.title;
   }

   public void setTitle(Component title) {
      this.title = title;
   }

   public float getHealth() {
      return this.health;
   }

   public void setHealth(float health) {
      this.health = health;
   }

   public BossBar.Color getColor() {
      return this.color;
   }

   public void setColor(BossBar.Color color) {
      this.color = color;
   }

   public BossBar.Overlay getOverlay() {
      return this.overlay;
   }

   public void setOverlay(BossBar.Overlay overlay) {
      this.overlay = overlay;
   }

   public EnumSet<BossBar.Flag> getFlags() {
      return this.flags;
   }

   public void setFlags(EnumSet<BossBar.Flag> flags) {
      this.flags = flags;
   }

   public static enum Action {
      ADD,
      REMOVE,
      UPDATE_HEALTH,
      UPDATE_TITLE,
      UPDATE_STYLE,
      UPDATE_FLAGS;
   }
}
