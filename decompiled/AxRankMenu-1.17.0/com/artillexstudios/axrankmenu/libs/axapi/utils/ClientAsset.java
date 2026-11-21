package com.artillexstudios.axrankmenu.libs.axapi.utils;

import net.kyori.adventure.key.Key;

public interface ClientAsset {
   public record ResourceTexture(Key id, Key texturePath) implements ClientAsset.Texture {
      public ResourceTexture(Key id) {
         this(id, id != null ? Key.key(id.namespace(), "textures/" + id.key() + ".png") : null);
      }
   }

   public interface Texture {
      Key texturePath();
   }
}
