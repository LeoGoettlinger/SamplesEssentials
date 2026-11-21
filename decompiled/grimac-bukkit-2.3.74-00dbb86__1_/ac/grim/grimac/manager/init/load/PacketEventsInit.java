package ac.grim.grimac.manager.init.load;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.PEVersion;
import ac.grim.grimac.utils.anticheat.LogUtil;
import java.util.concurrent.Executors;

public class PacketEventsInit implements LoadableInitable {
   private final PacketEventsAPI<?> packetEventsAPI;
   PEVersion MINIMUM_REQUIRED_PE_VERSION = new PEVersion(2, 8, 0, true);

   public PacketEventsInit(PacketEventsAPI<?> packetEventsAPI) {
      this.packetEventsAPI = packetEventsAPI;
   }

   @Override
   public void load() {
      LogUtil.info("Loading PacketEvents...");
      PacketEvents.setAPI(this.packetEventsAPI);
      if (!this.checkPacketEventsVersion()) {
         LogUtil.error(
            "\n******************************************************\nGrimAC requires PacketEvents >= "
               + this.MINIMUM_REQUIRED_PE_VERSION
               + (this.MINIMUM_REQUIRED_PE_VERSION.snapshot() ? "-SNAPSHOT" : "")
               + "\nCurrent version: "
               + PacketEvents.getAPI().getVersion()
               + "\nPlease update PacketEvents to a compatible version.\n*****************************************************"
         );
      }

      PacketEvents.getAPI()
         .getSettings()
         .fullStackTrace(true)
         .kickOnPacketException(true)
         .preViaInjection(true)
         .checkForUpdates(false)
         .reEncodeByDefault(false)
         .debug(false);
      PacketEvents.getAPI().load();
      Executors.defaultThreadFactory().newThread(() -> {
         StateTypes.AIR.getName();
         ItemTypes.AIR.getName();
         EntityTypes.PLAYER.getParent();
         EntityDataTypes.BOOLEAN.getName();
         ChatTypes.CHAT.getName();
         EnchantmentTypes.ALL_DAMAGE_PROTECTION.getName();
         ParticleTypes.DUST.getName();
         WrappedBlockState.getByString(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), "", false);
      }).start();
   }

   private boolean checkPacketEventsVersion() {
      PEVersion current = PacketEvents.getAPI().getVersion();
      PEVersion required = this.MINIMUM_REQUIRED_PE_VERSION;
      if (current.isNewerThan(required)) {
         return true;
      } else {
         return current.major() == required.major()
               && current.minor() == required.minor()
               && current.patch() == required.patch()
               && current.snapshot() == required.snapshot()
            ? true
            : required.snapshot() && current.major() == required.major() && current.minor() == required.minor() && current.patch() == required.patch();
      }
   }
}
