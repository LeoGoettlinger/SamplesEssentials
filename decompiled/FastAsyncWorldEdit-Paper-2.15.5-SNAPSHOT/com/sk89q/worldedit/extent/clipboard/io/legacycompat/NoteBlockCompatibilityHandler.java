package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

public class NoteBlockCompatibilityHandler implements NBTCompatibilityHandler {
   private static final IntegerProperty NOTE_PROPERTY;

   @Override
   public BaseBlock updateNbt(BaseBlock block) {
      if (NOTE_PROPERTY != null && block.getBlockType() == BlockTypes.NOTE_BLOCK) {
         LinCompoundTag tag = block.getNbt();
         if (tag == null) {
            return block;
         } else {
            LinByteTag noteTag = tag.findTag("note", LinTagType.byteTag());
            return noteTag == null ? block : block.with(NOTE_PROPERTY, Integer.valueOf(noteTag.valueAsByte())).toBaseBlock();
         }
      } else {
         return block;
      }
   }

   static {
      IntegerProperty temp;
      try {
         temp = (IntegerProperty)BlockTypes.NOTE_BLOCK.<Integer>getProperty("note");
      } catch (IllegalArgumentException | ClassCastException | NullPointerException var2) {
         temp = null;
      }

      NOTE_PROPERTY = temp;
   }
}
