package com.earth2me.essentials;

import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.commands.WarpNotFoundException;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.utils.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.ess3.api.IWarps;
import net.ess3.api.InvalidNameException;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;

public class Warps implements IConf, IWarps {
   private final Map<Warps.StringIgnoreCase, EssentialsConfiguration> warpPoints = new HashMap<>();
   private final File warpsFolder;

   public Warps(final File dataFolder) {
      this.warpsFolder = new File(dataFolder, "warps");
      if (!this.warpsFolder.exists()) {
         this.warpsFolder.mkdirs();
      }

      this.reloadConfig();
   }

   @Override
   public boolean isEmpty() {
      return this.warpPoints.isEmpty();
   }

   @Override
   public boolean isWarp(String name) {
      return this.warpPoints.containsKey(new Warps.StringIgnoreCase(name));
   }

   @Override
   public Collection<String> getList() {
      List<String> keys = new ArrayList<>();

      for (Warps.StringIgnoreCase stringIgnoreCase : this.warpPoints.keySet()) {
         keys.add(stringIgnoreCase.getString());
      }

      keys.sort(String.CASE_INSENSITIVE_ORDER);
      return keys;
   }

   @Override
   public Location getWarp(final String warp) throws WarpNotFoundException {
      EssentialsConfiguration conf = this.warpPoints.get(new Warps.StringIgnoreCase(warp));
      if (conf == null) {
         throw new WarpNotFoundException();
      } else {
         Location loc = conf.getLocation(null).location();
         if (loc == null) {
            throw new WarpNotFoundException();
         } else {
            return loc;
         }
      }
   }

   @Override
   public void setWarp(final String name, final Location loc) throws Exception {
      this.setWarp(null, name, loc);
   }

   @Override
   public void setWarp(final IUser user, final String name, final Location loc) throws Exception {
      String filename = StringUtil.sanitizeFileName(name);
      EssentialsConfiguration conf = this.warpPoints.get(new Warps.StringIgnoreCase(name));
      if (conf == null) {
         File confFile = new File(this.warpsFolder, filename + ".yml");
         if (confFile.exists()) {
            throw new TranslatableException("similarWarpExist");
         }

         conf = new EssentialsConfiguration(confFile);
         conf.load();
         this.warpPoints.put(new Warps.StringIgnoreCase(name), conf);
      }

      conf.setProperty(null, loc);
      conf.setProperty("name", name);
      if (user != null) {
         conf.setProperty("lastowner", user.getBase().getUniqueId().toString());
      }

      conf.save();
   }

   @Override
   public UUID getLastOwner(final String warp) throws WarpNotFoundException {
      EssentialsConfiguration conf = this.warpPoints.get(new Warps.StringIgnoreCase(warp));
      if (conf == null) {
         throw new WarpNotFoundException();
      } else {
         UUID uuid = null;

         try {
            uuid = UUID.fromString(conf.getString("lastowner", null));
         } catch (Exception var5) {
         }

         return uuid;
      }
   }

   @Override
   public void removeWarp(final String name) throws Exception {
      EssentialsConfiguration conf = this.warpPoints.get(new Warps.StringIgnoreCase(name));
      if (conf == null) {
         throw new TranslatableException("warpNotExist");
      } else if (!conf.getFile().delete()) {
         throw new TranslatableException("warpDeleteError");
      } else {
         this.warpPoints.remove(new Warps.StringIgnoreCase(name));
      }
   }

   @Override
   public final void reloadConfig() {
      this.warpPoints.clear();
      File[] listOfFiles = this.warpsFolder.listFiles();
      if (listOfFiles != null) {
         for (File listOfFile : listOfFiles) {
            String filename = listOfFile.getName();
            if (listOfFile.isFile() && filename.endsWith(".yml")) {
               try {
                  EssentialsConfiguration conf = new EssentialsConfiguration(listOfFile);
                  conf.load();
                  String name = conf.getString("name", null);
                  if (name != null && conf.hasProperty("world")) {
                     this.warpPoints.put(new Warps.StringIgnoreCase(name), conf);
                  }
               } catch (Exception var9) {
                  Essentials.getWrappedLogger()
                     .log(Level.WARNING, AdventureUtil.getAdventureFacet().miniToLegacy(I18n.tlLiteral("loadWarpError", filename)), (Throwable)var9);
               }
            }
         }
      }
   }

   @Deprecated
   @Override
   public File getWarpFile(final String name) throws InvalidNameException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public int getCount() {
      return this.getList().size();
   }

   private static class StringIgnoreCase {
      private final String string;

      StringIgnoreCase(final String string) {
         this.string = string;
      }

      @Override
      public int hashCode() {
         return this.getString().toLowerCase(Locale.ENGLISH).hashCode();
      }

      @Override
      public boolean equals(final Object o) {
         return o instanceof Warps.StringIgnoreCase ? this.getString().equalsIgnoreCase(((Warps.StringIgnoreCase)o).getString()) : false;
      }

      public String getString() {
         return this.string;
      }
   }
}
