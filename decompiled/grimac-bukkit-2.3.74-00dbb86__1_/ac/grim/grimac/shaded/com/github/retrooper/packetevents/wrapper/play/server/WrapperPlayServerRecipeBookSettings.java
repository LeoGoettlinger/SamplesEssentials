package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.recipe.RecipeBookSettings;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerRecipeBookSettings extends PacketWrapper<WrapperPlayServerRecipeBookSettings> {
   private RecipeBookSettings settings;

   public WrapperPlayServerRecipeBookSettings(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerRecipeBookSettings(RecipeBookSettings settings) {
      super(PacketType.Play.Server.RECIPE_BOOK_SETTINGS);
      this.settings = settings;
   }

   @Override
   public void read() {
      this.settings = RecipeBookSettings.read(this);
   }

   @Override
   public void write() {
      RecipeBookSettings.write(this, this.settings);
   }

   public void copy(WrapperPlayServerRecipeBookSettings wrapper) {
      this.settings = wrapper.settings;
   }

   public RecipeBookSettings getSettings() {
      return this.settings;
   }

   public void setSettings(RecipeBookSettings settings) {
      this.settings = settings;
   }
}
