package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Skull Texture")
@Description({"The skull texture of a player head. This allows you to give a skull a custom texture (e.g. instead of it being a Steve head, it's Notch's head).\nThe texture input is a base64 string containing the texture data to use (https://minecraft-heads.com is one site that provides easy access to base64 texture strings).\nResetting the texture of a skull will make it look like a Steve/Alex head.\n"})
@Example("set the skull texture of {_i} to \"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4NmRmZDc0Y2JhZmJkMWRiZTQ3OWY1ZTAzNzRjMDliZjJlYjRlMzg2NjExZmM0ZmM2OTlmMDJlY2E0ZGQyYyJ9fX0=\"")
@Since({"2.16"})
public class ExprSkullTexture extends SimplePropertyExpression<ItemType, String> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprSkullTexture.class, String.class, "(skull|head) texture", "itemtypes", false)
               .supplier(ExprSkullTexture::new))
            .build()
      );
   }

   @Nullable
   public String convert(ItemType item) {
      if (item.getItemMeta() instanceof SkullMeta meta) {
         PlayerProfile profile = meta.getPlayerProfile();
         return profile == null
            ? null
            : profile.getProperties()
               .stream()
               .filter(property -> property.getName().equals("textures"))
               .findFirst()
               .<String>map(ProfileProperty::getValue)
               .orElse(null);
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET -> (Class[])CollectionUtils.array(String.class);
         case DELETE, RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      PlayerProfile profile = null;
      if (delta != null) {
         profile = Bukkit.createProfile(UUID.randomUUID());
         profile.setProperty(new ProfileProperty("textures", (String)delta[0]));
      }

      for (ItemType item : this.getExpr().getArray(event)) {
         if (item.getItemMeta() instanceof SkullMeta meta) {
            meta.setPlayerProfile(profile);
            item.setItemMeta(meta);
         }
      }
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   protected String getPropertyName() {
      return "skull texture";
   }
}
