package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperPlayServerGameRuleValues extends PacketWrapper<WrapperPlayServerGameRuleValues> {
   @MonotonicNonNull
   private Map<ResourceLocation, String> values;

   public WrapperPlayServerGameRuleValues(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerGameRuleValues(PacketTypeCommon packetType, Map<ResourceLocation, String> values) {
      super(packetType);
      this.values = values;
   }

   @Override
   public void read() {
      this.values = this.readMap(ResourceLocation::read, PacketWrapper::readString);
   }

   @Override
   public void write() {
      this.writeMap(this.values, ResourceLocation::write, PacketWrapper::writeString);
   }

   public void copy(WrapperPlayServerGameRuleValues wrapper) {
      this.values = wrapper.values;
   }

   public Map<ResourceLocation, String> getValues() {
      return this.values;
   }

   public void setValues(Map<ResourceLocation, String> values) {
      this.values = values;
   }
}
