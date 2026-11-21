package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin.item;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTString;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

public class DebugStickState {
   private Map<StateType, String> properties;

   public DebugStickState(Map<StateType, String> properties) {
      this.properties = properties;
   }

   public static DebugStickState read(PacketWrapper<?> wrapper) {
      NBTCompound compound = wrapper.readNBT();
      Map<StateType, String> properties = new HashMap<>(compound.size());

      for (Entry<String, NBT> tag : compound.getTags().entrySet()) {
         StateType stateType = StateTypes.getByName(tag.getKey());
         String property = ((NBTString)tag.getValue()).getValue();
         properties.put(stateType, property);
      }

      return new DebugStickState(properties);
   }

   public static void write(PacketWrapper<?> wrapper, DebugStickState state) {
      NBTCompound compound = new NBTCompound();

      for (Entry<StateType, String> property : state.properties.entrySet()) {
         compound.setTag(property.getKey().getName(), new NBTString(property.getValue()));
      }

      wrapper.writeNBT(compound);
   }

   @Nullable
   public String getProperty(StateType stateType) {
      return this.properties.get(stateType);
   }

   public void setProperty(StateType stateType, @Nullable String property) {
      if (property != null) {
         this.properties.put(stateType, property);
      } else {
         this.properties.remove(stateType);
      }
   }

   public Map<StateType, String> getProperties() {
      return this.properties;
   }

   public void setProperties(Map<StateType, String> properties) {
      this.properties = properties;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DebugStickState)) {
         return false;
      } else {
         DebugStickState that = (DebugStickState)o;
         return this.properties.equals(that.properties);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.properties);
   }
}
