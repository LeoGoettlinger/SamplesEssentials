package com.github.retrooper.packetevents.protocol.dialog;

import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MultiActionDialog extends AbstractMappedEntity implements Dialog {
   private final CommonDialogData common;
   private final List<ActionButton> actions;
   @Nullable
   private final ActionButton exitAction;
   private final int columns;

   public MultiActionDialog(CommonDialogData common, List<ActionButton> actions, @Nullable ActionButton exitAction, int columns) {
      this(null, common, actions, exitAction, columns);
   }

   @Internal
   public MultiActionDialog(
      @Nullable TypesBuilderData data, CommonDialogData common, List<ActionButton> actions, @Nullable ActionButton exitAction, int columns
   ) {
      super(data);
      this.common = common;
      this.actions = actions;
      this.exitAction = exitAction;
      this.columns = columns;
   }

   public static MultiActionDialog decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      CommonDialogData common = CommonDialogData.decode(compound, wrapper);
      List<ActionButton> actions = compound.getListOrThrow("actions", ActionButton::decode, wrapper);
      ActionButton action = compound.getOrNull("exit_action", ActionButton::decode, wrapper);
      int columns = compound.getNumberTagValueOrDefault("columns", 2).intValue();
      return new MultiActionDialog(null, common, actions, action, columns);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, MultiActionDialog dialog) {
      CommonDialogData.encode(compound, wrapper, dialog.common);
      compound.setList("actions", dialog.actions, ActionButton::encode, wrapper);
      if (dialog.exitAction != null) {
         compound.set("exit_action", dialog.exitAction, ActionButton::encode, wrapper);
      }

      if (dialog.columns != 2) {
         compound.setTag("columns", new NBTInt(dialog.columns));
      }
   }

   public Dialog copy(@Nullable TypesBuilderData newData) {
      return new MultiActionDialog(newData, this.common, this.actions, this.exitAction, this.columns);
   }

   public CommonDialogData getCommon() {
      return this.common;
   }

   public List<ActionButton> getActions() {
      return this.actions;
   }

   @Nullable
   public ActionButton getExitAction() {
      return this.exitAction;
   }

   public int getColumns() {
      return this.columns;
   }

   @Override
   public DialogType<?> getType() {
      return DialogTypes.MULTI_ACTION;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof MultiActionDialog)) {
         return false;
      } else {
         MultiActionDialog that = (MultiActionDialog)obj;
         if (this.columns != that.columns) {
            return false;
         } else if (!this.common.equals(that.common)) {
            return false;
         } else {
            return !this.actions.equals(that.actions) ? false : Objects.equals(this.exitAction, that.exitAction);
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.common, this.actions, this.exitAction, this.columns);
   }
}
