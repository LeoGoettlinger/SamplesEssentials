package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.dialog;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTString;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.dialog.DialogLike;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Dialog extends MappedEntity, DeepComparableEntity, CopyableEntity<Dialog>, DialogLike {
   static Dialog read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntityOrDirect(Dialogs.getRegistry(), Dialog::readDirect);
   }

   static void write(PacketWrapper<?> wrapper, Dialog dialog) {
      wrapper.writeMappedEntityOrDirect(dialog, Dialog::writeDirect);
   }

   static Dialog readDirect(PacketWrapper<?> wrapper) {
      return decodeDirect(wrapper.readNBTRaw(), wrapper, null);
   }

   static void writeDirect(PacketWrapper<?> wrapper, Dialog dialog) {
      wrapper.writeNBTRaw(encodeDirect(dialog, wrapper));
   }

   static Dialog decode(NBT nbt, PacketWrapper<?> wrapper) {
      return nbt instanceof NBTString
         ? wrapper.replaceRegistry(Dialogs.getRegistry()).getByNameOrThrow(((NBTString)nbt).getValue())
         : decodeDirect(nbt, wrapper, null);
   }

   static NBT encode(PacketWrapper<?> wrapper, Dialog dialog) {
      return (NBT)(dialog.isRegistered() ? new NBTString(dialog.getName().toString()) : encodeDirect(dialog, wrapper));
   }

   @ApiStatus.Internal
   static Dialog decodeDirect(NBT nbt, PacketWrapper<?> wrapper, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      String dialogTypeName = compound.getStringTagValueOrThrow("type");
      DialogType<?> dialogType = DialogTypes.getRegistry().getByNameOrThrow(dialogTypeName);
      return dialogType.decode(compound, wrapper).copy(data);
   }

   @ApiStatus.Internal
   static NBT encodeDirect(Dialog dialog, PacketWrapper<?> wrapper) {
      NBTCompound compound = new NBTCompound();
      compound.setTag("type", new NBTString(dialog.getType().getName().toString()));
      ((DialogType<Dialog>)dialog.getType()).encode(compound, wrapper, dialog);
      return compound;
   }

   DialogType<?> getType();
}
