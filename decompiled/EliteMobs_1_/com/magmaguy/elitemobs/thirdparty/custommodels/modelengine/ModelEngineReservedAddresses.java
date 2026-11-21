package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import com.magmaguy.elitemobs.MetadataHandler;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModelEngineReservedAddresses {
   public static void reserve() {
      if (ModelEngineChecker.modelEngineIsInstalled()) {
         Path modelEngineBlueprintsPath = Paths.get(
            MetadataHandler.PLUGIN.getDataFolder().getParentFile().getAbsolutePath() + File.separatorChar + "ModelEngine" + File.separatorChar + "blueprints"
         );
         makeDirectory(modelEngineBlueprintsPath, "em_1_primis");
         makeDirectory(modelEngineBlueprintsPath, "em_2_goblins");
      }
   }

   private static void makeDirectory(Path originalPath, String directory) {
      Path primisPath = Paths.get(originalPath.toString() + File.separatorChar + directory);
      if (!primisPath.toFile().exists()) {
         primisPath.toFile().mkdir();
      }
   }
}
