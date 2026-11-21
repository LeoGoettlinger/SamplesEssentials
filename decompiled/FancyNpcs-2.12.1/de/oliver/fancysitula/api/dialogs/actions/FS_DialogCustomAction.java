package de.oliver.fancysitula.api.dialogs.actions;

import java.util.Map;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Nullable;

public class FS_DialogCustomAction implements FS_DialogActionButtonAction {
   private String id;
   private Map<String, String> additions;

   public FS_DialogCustomAction(String id, Map<String, String> additions) {
      this.id = id;
      this.additions = additions;
   }

   @Subst("")
   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   @Nullable
   public Map<String, String> getAdditions() {
      return this.additions;
   }

   public void setAdditions(@Nullable Map<String, String> additions) {
      this.additions = additions;
   }
}
