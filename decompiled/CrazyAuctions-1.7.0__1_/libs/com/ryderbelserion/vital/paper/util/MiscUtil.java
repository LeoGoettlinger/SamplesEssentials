package libs.com.ryderbelserion.vital.paper.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import libs.com.ryderbelserion.vital.common.util.AdvUtil;
import libs.com.ryderbelserion.vital.paper.api.bStats;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MiscUtil {
   public MiscUtil() {
      throw new AssertionError();
   }

   public static void updateTitle(@NotNull Player player, @NotNull String title) {
      ServerPlayer entityPlayer = (ServerPlayer)((CraftHumanEntity)player).getHandle();
      int containerId = entityPlayer.containerMenu.containerId;
      MenuType<?> windowType = CraftContainer.getNotchInventoryType(player.getOpenInventory().getTopInventory());
      entityPlayer.connection
         .send(
            new ClientboundOpenScreenPacket(
               containerId, windowType, CraftChatMessage.fromJSON((String)JSONComponentSerializer.json().serialize(AdvUtil.parse(title)))
            )
         );
      player.updateInventory();
   }

   public static bStats.JsonObjectBuilder.JsonObject getChartData(Callable<Map<String, Integer>> callable) throws Exception {
      bStats.JsonObjectBuilder valuesBuilder = new bStats.JsonObjectBuilder();
      Map<String, Integer> map = callable.call();
      if (map != null && !map.isEmpty()) {
         boolean allSkipped = true;

         for (Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() != 0) {
               allSkipped = false;
               valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
         }

         return allSkipped ? null : new bStats.JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
      } else {
         return null;
      }
   }
}
