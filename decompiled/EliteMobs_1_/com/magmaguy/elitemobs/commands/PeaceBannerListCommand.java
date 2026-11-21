package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerManager;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PeaceBannerListCommand extends AdvancedCommand {
   public PeaceBannerListCommand() {
      super(List.of("peacebanner"));
      this.addLiteral("list");
      this.setUsage("/em peacebanner list");
      this.setPermission("elitemobs.peacebanner.admin");
      this.setDescription("Lists all placed Peace Banners with their coordinates.");
   }

   @Override
   public void execute(CommandData commandData) {
      Map<String, PeaceBannerManager.BannerData> bannerData = PeaceBannerManager.getAllBannerData();
      if (bannerData.isEmpty()) {
         commandData.getCommandSender().sendMessage("No Peace Banners are currently placed.");
      } else {
         commandData.getCommandSender().sendMessage("Peace Banners (" + bannerData.size() + " total):");

         for (Entry<String, PeaceBannerManager.BannerData> entry : bannerData.entrySet()) {
            PeaceBannerManager.BannerData data = entry.getValue();
            commandData.getCommandSender()
               .sendMessage("  - " + data.worldName() + " @ " + data.x() + ", " + data.y() + ", " + data.z() + " (radius: " + data.chunkRadius() + " chunks)");
         }
      }
   }
}
