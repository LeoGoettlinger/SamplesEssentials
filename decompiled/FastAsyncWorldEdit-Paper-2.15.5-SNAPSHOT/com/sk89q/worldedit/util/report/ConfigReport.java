package com.sk89q.worldedit.util.report;

import com.sk89q.worldedit.WorldEdit;

public class ConfigReport extends DataReport {
   public ConfigReport() {
      super("WorldEdit Configuration");
      this.append("Configuration", new HierarchyObjectReport("Configuration", WorldEdit.getInstance().getConfiguration()));
   }
}
