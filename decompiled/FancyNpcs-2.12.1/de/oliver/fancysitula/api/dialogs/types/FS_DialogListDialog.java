package de.oliver.fancysitula.api.dialogs.types;

import de.oliver.fancysitula.api.dialogs.FS_CommonDialogData;
import de.oliver.fancysitula.api.dialogs.FS_Dialog;
import de.oliver.fancysitula.api.dialogs.actions.FS_DialogActionButton;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class FS_DialogListDialog implements FS_Dialog {
   private FS_CommonDialogData dialogData;
   private List<FS_Dialog> dialogs;
   @Nullable
   private FS_DialogActionButton exitButton;
   private int columns;
   private int buttonWidth;

   public FS_DialogListDialog(FS_CommonDialogData dialogData, List<FS_Dialog> dialogs, @Nullable FS_DialogActionButton exitButton, int columns, int buttonWidth) {
      this.dialogData = dialogData;
      this.dialogs = dialogs;
      this.exitButton = exitButton;
      this.columns = columns;
      this.buttonWidth = buttonWidth;
   }

   public FS_CommonDialogData getDialogData() {
      return this.dialogData;
   }

   public void setDialogData(FS_CommonDialogData dialogData) {
      this.dialogData = dialogData;
   }

   public List<FS_Dialog> getDialogs() {
      return this.dialogs;
   }

   public void setDialogs(List<FS_Dialog> dialogs) {
      this.dialogs = dialogs;
   }

   @Nullable
   public FS_DialogActionButton getExitButton() {
      return this.exitButton;
   }

   public void setExitButton(@Nullable FS_DialogActionButton exitButton) {
      this.exitButton = exitButton;
   }

   public int getColumns() {
      return this.columns;
   }

   public void setColumns(int columns) {
      this.columns = columns;
   }

   public int getButtonWidth() {
      return this.buttonWidth;
   }

   public void setButtonWidth(int buttonWidth) {
      this.buttonWidth = buttonWidth;
   }
}
