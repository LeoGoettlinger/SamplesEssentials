package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin.item;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.instrument.Instrument;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.instrument.Instruments;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MaybeMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;

public class ItemInstrument {
   private MaybeMappedEntity<Instrument> instrument;

   public ItemInstrument(Instrument instrument) {
      this(new MaybeMappedEntity<>(instrument));
   }

   @ApiStatus.Obsolete
   public ItemInstrument(MaybeMappedEntity<Instrument> instrument) {
      this.instrument = instrument;
   }

   public static ItemInstrument read(PacketWrapper<?> wrapper) {
      return !wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1) && !wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)
         ? new ItemInstrument(MaybeMappedEntity.read(wrapper, Instruments.getRegistry(), Instrument::read))
         : new ItemInstrument(Instrument.read(wrapper));
   }

   public static void write(PacketWrapper<?> wrapper, ItemInstrument instrument) {
      if (!wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1) && !wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
         MaybeMappedEntity.write(wrapper, instrument.instrument, Instrument::write);
      } else {
         Instrument.write(wrapper, instrument.instrument.getValueOrThrow());
      }
   }

   public MaybeMappedEntity<Instrument> getInstrument() {
      return this.instrument;
   }

   public void setInstrument(MaybeMappedEntity<Instrument> instrument) {
      this.instrument = instrument;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ItemInstrument)) {
         return false;
      } else {
         ItemInstrument that = (ItemInstrument)obj;
         return this.instrument.equals(that.instrument);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.instrument);
   }
}
