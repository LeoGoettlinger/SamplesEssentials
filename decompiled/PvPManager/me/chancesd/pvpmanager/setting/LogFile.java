package me.chancesd.pvpmanager.setting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import me.chancesd.pvpmanager.player.BasePlayer;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class LogFile {
   private final File file;
   private final SimpleDateFormat format = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ");

   public LogFile(File file) {
      this.file = file;

      try {
         if (!file.exists() && file.createNewFile()) {
            this.write("This file logs every player that disconnected during combat");
            this.write("You can disable the logging in the config file\n");
         }
      } catch (IOException var3) {
         Log.severe(var3.getMessage(), var3);
      }
   }

   private void write(final String line) {
      ScheduleUtils.runAsync(new BukkitRunnable() {
         public void run() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(LogFile.this.file, true))) {
               bw.write(line);
               bw.newLine();
            } catch (IOException var6) {
               Log.severe(var6.getMessage(), var6);
            }
         }
      });
   }

   public final void log(String line) {
      this.write(this.format.format(new Date()) + line);
   }

   public final void logCombatLog(CombatPlayer player) {
      Location loc = player.getPlayer().getLocation();
      String locationInfo = "";
      if (loc != null) {
         World world = loc.getWorld();
         if (world != null) {
            locationInfo = String.format(" | World:%s, X:%.2f, Y:%.2f, Z:%.2f", world.getName(), loc.getX(), loc.getY(), loc.getZ());
         } else {
            locationInfo = String.format(" | World:Unknown, X:%.2f, Y:%.2f, Z:%.2f", loc.getX(), loc.getY(), loc.getZ());
         }
      } else {
         locationInfo = " | Location:Unknown";
      }

      String data = player.getName()
         + " tried to escape combat! ("
         + String.format("%.3f", player.getTagTimeLeft() / 1000.0)
         + "s left) | In combat with: "
         + player.getEnemies().stream().map(BasePlayer::getName).toList()
         + locationInfo;
      this.write(this.format.format(new Date()) + data);
   }
}
