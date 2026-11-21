package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.clickevent;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.dialog.Dialog;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.dialog.Dialogs;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntityRef;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ShowDialogClickEvent implements ClickEvent {
   private final MappedEntityRef<Dialog> dialog;

   public ShowDialogClickEvent(Dialog dialog) {
      this(new MappedEntityRef.Static<>(dialog));
   }

   public ShowDialogClickEvent(MappedEntityRef<Dialog> dialog) {
      this.dialog = dialog;
   }

   public static ShowDialogClickEvent decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      MappedEntityRef<Dialog> dialog = MappedEntityRef.decode(compound.getTagOrThrow("dialog"), Dialogs.getRegistry(), Dialog::decode, wrapper);
      return new ShowDialogClickEvent(dialog);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, ShowDialogClickEvent clickEvent) {
      compound.setTag("dialog", MappedEntityRef.encode(wrapper, Dialog::encode, clickEvent.dialog));
   }

   @Override
   public ClickEventAction<?> getAction() {
      return ClickEventActions.SHOW_DIALOG;
   }

   @Override
   public ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent asAdventure() {
      return ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent.showDialog(this.dialog.get());
   }

   public MappedEntityRef<Dialog> getDialogRef() {
      return this.dialog;
   }

   public Dialog getDialog() {
      return this.dialog.get();
   }
}
