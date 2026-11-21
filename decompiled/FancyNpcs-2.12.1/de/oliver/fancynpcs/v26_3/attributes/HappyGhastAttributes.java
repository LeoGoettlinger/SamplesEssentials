package de.oliver.fancynpcs.v26_3.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v26_3.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.entity.EntityType;

public class HappyGhastAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(
         new NpcAttribute(
            "harness",
            List.of(
               "white",
               "orange",
               "magenta",
               "light_blue",
               "yellow",
               "lime",
               "pink",
               "gray",
               "light_gray",
               "cyan",
               "purple",
               "blue",
               "brown",
               "green",
               "red",
               "black"
            ),
            List.of(EntityType.HAPPY_GHAST),
            HappyGhastAttributes::setHarness
         )
      );
      return attributes;
   }

   private static void setHarness(Npc npc, String value) {
      HappyGhast ghast = ReflectionHelper.getEntity(npc);
      String var4 = value.toLowerCase();

      ItemStack harnessItem = switch (var4) {
         case "white" -> ((Item)Items.HARNESS.white()).getDefaultInstance();
         case "orange" -> ((Item)Items.HARNESS.orange()).getDefaultInstance();
         case "magenta" -> ((Item)Items.HARNESS.magenta()).getDefaultInstance();
         case "light_blue" -> ((Item)Items.HARNESS.lightBlue()).getDefaultInstance();
         case "yellow" -> ((Item)Items.HARNESS.yellow()).getDefaultInstance();
         case "lime" -> ((Item)Items.HARNESS.lime()).getDefaultInstance();
         case "pink" -> ((Item)Items.HARNESS.pink()).getDefaultInstance();
         case "gray" -> ((Item)Items.HARNESS.gray()).getDefaultInstance();
         case "light_gray" -> ((Item)Items.HARNESS.lightGray()).getDefaultInstance();
         case "cyan" -> ((Item)Items.HARNESS.cyan()).getDefaultInstance();
         case "purple" -> ((Item)Items.HARNESS.purple()).getDefaultInstance();
         case "blue" -> ((Item)Items.HARNESS.blue()).getDefaultInstance();
         case "brown" -> ((Item)Items.HARNESS.brown()).getDefaultInstance();
         case "green" -> ((Item)Items.HARNESS.green()).getDefaultInstance();
         case "red" -> ((Item)Items.HARNESS.red()).getDefaultInstance();
         case "black" -> ((Item)Items.HARNESS.black()).getDefaultInstance();
         default -> Items.AIR.getDefaultInstance();
      };
      if (!harnessItem.isEmpty()) {
         ghast.setItemSlot(EquipmentSlot.BODY, harnessItem);
      }
   }
}
