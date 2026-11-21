package com.artillexstudios.axafkzone.libs.axapi.nms.wrapper;

import com.artillexstudios.axafkzone.libs.axapi.AxPlugin;
import com.artillexstudios.axafkzone.libs.axapi.items.HashGenerator;
import com.artillexstudios.axafkzone.libs.axapi.utils.PlayerTextures;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public interface ServerPlayerWrapper extends Wrapper<Player> {
   String PACKET_HANDLER = "packet_handler";
   String AXAPI_HANDLER = "axapi_handler_" + ((AxPlugin)AxPlugin.getPlugin(AxPlugin.class)).getName().toLowerCase(Locale.ENGLISH);

   static ServerPlayerWrapper wrap(Object player) {
      return WrapperRegistry.SERVER_PLAYER.map(player);
   }

   void inject();

   void uninject();

   void sendPacket(Object packet);

   void message(Component message);

   double getBase(Attribute attribute);

   PlayerTextures textures();

   double getX();

   double getZ();

   HashGenerator hashGenerator();
}
