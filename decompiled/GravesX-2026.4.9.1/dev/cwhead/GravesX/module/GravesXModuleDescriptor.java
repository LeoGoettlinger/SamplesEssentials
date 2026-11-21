package dev.cwhead.GravesX.module;

import dev.cwhead.GravesX.module.util.ModuleInfo;
import java.util.List;

public interface GravesXModuleDescriptor {
   String getName();

   String getVersion();

   String getDescription();

   String getWebsite();

   List<String> getAuthors();

   String getMainClass();

   List<String> getModuleDepends();

   List<String> getModuleSoftDepends();

   List<String> getModuleLoadBefore();

   List<String> getPluginDepends();

   List<String> getPluginSoftDepends();

   GravesXModuleController.LoadPhase getLoadPhase();

   List<ModuleInfo.LibraryDef> getLibraries();

   boolean isEnabled();

   boolean supportsFolia();
}
