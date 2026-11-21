package vrx.economy.vRXClans;

import eu.decentsoftware.holograms.api.DHAPI;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class HologramManager {
   private final VRXClans plugin;
   private final File file;
   private YamlConfiguration config;

   public HologramManager(VRXClans plugin) {
      this.plugin = plugin;
      this.file = new File(plugin.getDataFolder(), "holograms.yml");
      this.config = YamlConfiguration.loadConfiguration(this.file);
   }

   public void setHologram(String type, Location loc) {
      String id = "vrxclans_" + type;
      if (DHAPI.getHologram(id) != null) {
         DHAPI.removeHologram(id);
      }

      List<String> lines = Arrays.asList("§b§lLade Rangliste...", "§7Bitte warten");
      DHAPI.createHologram(id, loc, lines);
      this.config.set("holograms." + type + ".world", loc.getWorld().getName());
      this.config.set("holograms." + type + ".x", loc.getX());
      this.config.set("holograms." + type + ".y", loc.getY());
      this.config.set("holograms." + type + ".z", loc.getZ());
      this.save();
      this.updateHolograms();
   }

   public void updateHolograms() {
      this.updateLeaderboard("kills", "Top 5 Clan Kills");
      this.updateLeaderboard("level", "Top 5 Clan Level");
      this.updateLeaderboard("bank", "Top 5 Clan Reichtum");
   }

   private void updateLeaderboard(String type, String title) {
      String id = "vrxclans_" + type;
      if (DHAPI.getHologram(id) == null) {
         if (!this.config.contains("holograms." + type)) {
            return;
         }

         World world = Bukkit.getWorld(this.config.getString("holograms." + type + ".world", ""));
         if (world == null) {
            this.plugin.getLogger().warning("Hologram " + type + " could not be loaded: world does not exist.");
            return;
         }

         Location loc = new Location(
            world,
            this.config.getDouble("holograms." + type + ".x"),
            this.config.getDouble("holograms." + type + ".y"),
            this.config.getDouble("holograms." + type + ".z")
         );
         DHAPI.createHologram(id, loc, Collections.singletonList("§7Lade..."));
      }

      List<Clan> sortedClans = new ArrayList<>(this.plugin.getClanManager().getClans().values());
      if (type.equals("kills")) {
         sortedClans.sort((a, b) -> Integer.compare(b.getKills(), a.getKills()));
      } else if (type.equals("level")) {
         sortedClans.sort((a, b) -> Integer.compare(b.getLevel(), a.getLevel()));
      } else {
         sortedClans.sort((a, b) -> Double.compare(b.getBankBalance(), a.getBankBalance()));
      }

      List<String> lines = new ArrayList<>();
      lines.add("§6§l\ud83c\udfc6 " + title + " \ud83c\udfc6");
      lines.add("§8§m-----------------------");

      for (int i = 0; i < Math.min(5, sortedClans.size()); i++) {
         Clan c = sortedClans.get(i);
         String val = type.equals("kills") ? c.getKills() + " Kills" : (type.equals("level") ? "Level " + c.getLevel() : c.getBankBalance() + "$");
         lines.add("§e#" + (i + 1) + " §b" + c.getName() + " §8» §f" + val);
      }

      if (sortedClans.isEmpty()) {
         lines.add("§cNo Clans gefunden");
      }

      lines.add("§8§m-----------------------");
      lines.add("§7Letztes Update: §f" + new SimpleDateFormat("HH:mm").format(new Date()));
      DHAPI.setHologramLines(DHAPI.getHologram(id), lines);
   }

   private void save() {
      try {
         this.config.save(this.file);
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }
}
