package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.clickevent;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.dialog.Dialog;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTEnd;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure.AdventureSupportUtil;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure.NbtTagHolder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Locale;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ClickEvent {
   static ClickEvent decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      String actionName = compound.getStringTagValueOrThrow("action");
      ClickEventAction<?> action = ClickEventActions.getRegistry().getByNameOrThrow(actionName);
      return action.decode(compound, wrapper);
   }

   static void encode(NBTCompound compound, PacketWrapper<?> wrapper, ClickEvent clickEvent) {
      compound.set("action", clickEvent.getAction().getName(), ResourceLocation::encode, wrapper);
      ((ClickEventAction<ClickEvent>)clickEvent.getAction()).encode(compound, wrapper, clickEvent);
   }

   ClickEventAction<?> getAction();

   static ClickEvent fromAdventure(ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent clickEvent) {
      String var1 = clickEvent.action().name().toLowerCase(Locale.ROOT);
      switch (var1) {
         case "open_url":
            return new OpenUrlClickEvent(AdventureSupportUtil.getStringValue(clickEvent));
         case "open_file":
            return new OpenFileClickEvent(AdventureSupportUtil.getStringValue(clickEvent));
         case "run_command":
            return new RunCommandClickEvent(AdventureSupportUtil.getStringValue(clickEvent));
         case "suggest_command":
            return new SuggestCommandClickEvent(AdventureSupportUtil.getStringValue(clickEvent));
         case "change_page":
            return new ChangePageClickEvent(AdventureSupportUtil.getStringValue(clickEvent));
         case "copy_to_clipboard":
            return new CopyToClipboardClickEvent(AdventureSupportUtil.getStringValue(clickEvent));
         case "show_dialog":
            return new ShowDialogClickEvent((Dialog)((ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent.Payload.Dialog)clickEvent.payload()).dialog());
         case "custom":
            ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent.Payload.Custom payload = (ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent.Payload.Custom)clickEvent.payload();
            NbtTagHolder nbtTag = (NbtTagHolder)payload.nbt();
            return new CustomClickEvent(new ResourceLocation(payload.key()), nbtTag != null && !(nbtTag.getTag() instanceof NBTEnd) ? nbtTag.getTag() : null);
         default:
            throw new UnsupportedOperationException("Unsupported clickevent: " + clickEvent);
      }
   }

   ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent asAdventure();
}
