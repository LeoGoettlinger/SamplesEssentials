package com.sk89q.worldedit.internal;

import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.Logger;

public class SchematicsEventListener {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   @Subscribe
   public void onConfigLoad(ConfigurationLoadEvent event) {
      Path config = event.getConfiguration().getWorkingDirectory().toPath();

      try {
         Files.createDirectories(config.resolve(event.getConfiguration().saveDir));
      } catch (FileAlreadyExistsException var4) {
         LOGGER.debug("Schematic directory exists as file. Possible symlink.", var4);
      } catch (IOException var5) {
         LOGGER.warn("Failed to create schematics directory", var5);
      }
   }
}
