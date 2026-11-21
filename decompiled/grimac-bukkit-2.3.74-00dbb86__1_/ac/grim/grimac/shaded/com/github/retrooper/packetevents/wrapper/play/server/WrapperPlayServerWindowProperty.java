package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerWindowProperty extends PacketWrapper<WrapperPlayServerWindowProperty> {
   private int windowId;
   private int id;
   private int value;

   public WrapperPlayServerWindowProperty(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerWindowProperty(byte windowId, int id, int value) {
      this((int)windowId, id, value);
   }

   public WrapperPlayServerWindowProperty(int windowId, int id, int value) {
      super(PacketType.Play.Server.WINDOW_PROPERTY);
      this.windowId = windowId;
      this.id = id;
      this.value = value;
   }

   @Override
   public void read() {
      this.windowId = this.readContainerId();
      this.id = this.readShort();
      this.value = this.readShort();
   }

   @Override
   public void write() {
      this.writeContainerId(this.windowId);
      this.writeShort(this.id);
      this.writeShort(this.value);
   }

   public void copy(WrapperPlayServerWindowProperty wrapper) {
      this.windowId = wrapper.windowId;
      this.id = wrapper.id;
      this.value = wrapper.value;
   }

   public int getContainerId() {
      return this.windowId;
   }

   public void setContainerId(int windowId) {
      this.windowId = windowId;
   }

   @Deprecated
   public byte getWindowIdB() {
      return (byte)this.getContainerId();
   }

   @Deprecated
   public void setWindowId(byte windowId) {
      this.setContainerId(windowId);
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getValue() {
      return this.value;
   }

   public void setValue(int value) {
      this.value = value;
   }
}
