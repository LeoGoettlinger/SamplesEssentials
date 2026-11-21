package com.sk89q.worldedit.util.report;

public class HierarchyObjectReport extends ShallowObjectReport {
   public HierarchyObjectReport(String title, Object object) {
      super(title, object);
      Class<?> type = object.getClass();

      while ((type = type.getSuperclass()) != null) {
         this.scanClass(object, type);
      }
   }
}
