package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Operation {
   Set<String> warnedDeprecatedClasses = new HashSet<>();

   Operation resume(RunContext run) throws WorldEditException;

   void cancel();

   @Deprecated
   default void addStatusMessages(List<String> messages) {
   }

   default Iterable<Component> getStatusMessages() {
      List<String> oldMessages = new ArrayList<>();
      this.addStatusMessages(oldMessages);
      if (oldMessages.size() > 0) {
         String className = this.getClass().getName();
         if (!warnedDeprecatedClasses.contains(className)) {
            WorldEdit.logger.warn("An operation is using the old status message API. This will be removed in WorldEdit 8. Class: " + className);
            warnedDeprecatedClasses.add(className);
         }
      }

      return oldMessages.stream().map(TextComponent::of).collect(Collectors.toList());
   }
}
