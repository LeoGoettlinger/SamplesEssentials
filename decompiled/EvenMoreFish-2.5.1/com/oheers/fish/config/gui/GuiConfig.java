package com.oheers.fish.config.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.messages.EMFSingleMessage;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

public abstract class GuiConfig extends ConfigBase {
   public GuiConfig(@NonNull String name) {
      super("gui/" + name, "gui/" + name, EvenMoreFish.getInstance(), true);
   }

   @NonNull
   public EMFSingleMessage getTitle() {
      return EMFSingleMessage.fromString(this.getConfig().getString("title", "EvenMoreFish GUI"));
   }

   @NonNull
   public String[] getLayout() {
      return this.getConfig().getStringList("layout").stream().filter(Objects::nonNull).limit(6L).toArray(String[]::new);
   }

   public abstract boolean isPaginated();
}
