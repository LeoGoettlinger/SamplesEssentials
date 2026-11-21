package dev.cwhead.GravesX.module;

import dev.cwhead.GravesX.module.command.GravesXModuleCommand;
import java.util.Collection;
import java.util.List;

public interface GravesXModuleController {
   boolean isEnabled();

   boolean isEnabled(String var1);

   void disableModule();

   boolean disableModule(String var1);

   void enableModule();

   boolean enableModule(String var1);

   GravesXModuleDescriptor getModule(String var1);

   GravesXModuleDescriptor getThisModule();

   boolean isEnabled(GravesXModule var1);

   boolean isEnabled(Class<? extends GravesXModule> var1);

   boolean disableModule(GravesXModule var1);

   boolean disableModule(Class<? extends GravesXModule> var1);

   boolean enableModule(GravesXModule var1);

   boolean enableModule(Class<? extends GravesXModule> var1);

   GravesXModuleDescriptor getModule(GravesXModule var1);

   GravesXModuleDescriptor getModule(Class<? extends GravesXModule> var1);

   Collection<GravesXModuleDescriptor> listModules();

   void registerCommand(String var1, GravesXModuleCommand var2);

   void registerCommand(String var1, Class<? extends GravesXModuleCommand> var2);

   default String getName(String moduleKey) {
      GravesXModuleDescriptor d = this.getModule(moduleKey);
      return d != null ? d.getName() : null;
   }

   default String getVersion(String moduleKey) {
      GravesXModuleDescriptor d = this.getModule(moduleKey);
      return d != null ? d.getVersion() : null;
   }

   default String getWebsite(String moduleKey) {
      GravesXModuleDescriptor d = this.getModule(moduleKey);
      return d != null ? d.getWebsite() : null;
   }

   default List<String> getAuthors(String moduleKey) {
      GravesXModuleDescriptor d = this.getModule(moduleKey);
      return d != null ? d.getAuthors() : List.of();
   }

   default boolean supportsFolia(String moduleKey) {
      GravesXModuleDescriptor d = this.getModule(moduleKey);
      return d != null && d.supportsFolia();
   }

   default boolean supportsFolia() {
      GravesXModuleDescriptor d = this.getThisModule();
      return d != null && d.supportsFolia();
   }

   default GravesXModuleController.LoadPhase getLoadPhase(String moduleKey) {
      GravesXModuleDescriptor d = this.getModule(moduleKey);
      GravesXModuleController.LoadPhase phase = d != null ? d.getLoadPhase() : null;
      return phase != null ? phase : GravesXModuleController.LoadPhase.STARTUP;
   }

   default GravesXModuleController.LoadPhase getLoadPhase() {
      GravesXModuleDescriptor d = this.getThisModule();
      GravesXModuleController.LoadPhase phase = d != null ? d.getLoadPhase() : null;
      return phase != null ? phase : GravesXModuleController.LoadPhase.STARTUP;
   }

   public static enum LoadPhase {
      STARTUP,
      POSTWORLD,
      COMPLETED;
   }
}
