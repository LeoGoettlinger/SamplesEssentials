package com.github.retrooper.packetevents.protocol.dialog.body;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlainMessageDialogBody implements DialogBody {
   private final PlainMessage message;

   public PlainMessageDialogBody(PlainMessage message) {
      this.message = message;
   }

   public static PlainMessageDialogBody decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      return new PlainMessageDialogBody(PlainMessage.decode(compound, wrapper));
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, PlainMessageDialogBody body) {
      PlainMessage.encode(compound, wrapper, body.message);
   }

   @Override
   public DialogBodyType<?> getType() {
      return DialogBodyTypes.PLAIN_MESSAGE;
   }

   public PlainMessage getMessage() {
      return this.message;
   }
}
