package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class NameTagMessageSupport {
   private NameTagMessageSupport() {
   }

   public static void nameTagProcessor(final EliteEntity eliteEntity, final Entity entity, List<String> list) {
      if (entity != null && eliteEntity.isValid() && list != null && !list.isEmpty()) {
         String tempName = list.get(ThreadLocalRandom.current().nextInt(list.size()));
         entity.setCustomName(ChatColorConverter.convert(tempName));
         (new BukkitRunnable() {
            public void run() {
               if (entity.isValid()) {
                  entity.setCustomName(eliteEntity.getName());
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 80L);
      }
   }
}
