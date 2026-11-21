package github.nighter.smartspawner.spawner.data.storage;

import github.nighter.smartspawner.spawner.properties.ItemSignature;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public final class SpawnerInventoryCodec {
   private static final byte FORMAT_VERSION = 2;
   private static final byte FORMAT_VERSION_V1 = 1;

   private SpawnerInventoryCodec() {
   }

   public static byte[] encode(Map<ItemSignature, Long> items) throws IOException {
      if (items != null && !items.isEmpty()) {
         LinkedHashMap<ItemStack, LinkedHashMap<Integer, Long>> groups = new LinkedHashMap<>(Math.max(16, items.size() * 2));

         for (Entry<ItemSignature, Long> entry : items.entrySet()) {
            ItemSignature signature = entry.getKey();
            Long amount = entry.getValue();
            if (amount != null && amount > 0L) {
               ItemStack template = signature.getTemplate();
               if (template != null && template.getType() != Material.AIR) {
                  int damage = signature.getDamage();
                  if (damage != 0 && (signature.hasItemMeta() ? template.getItemMeta() : null) instanceof Damageable damageable) {
                     damageable.setDamage(0);
                     template.setItemMeta(damageable);
                  }

                  groups.computeIfAbsent(template, k -> new LinkedHashMap<>()).merge(damage, amount, Long::sum);
               }
            }
         }

         if (groups.isEmpty()) {
            return null;
         } else {
            List<ItemStack> baseTemplates = new ArrayList<>(groups.keySet());
            byte[] itemPayload = ItemStack.serializeItemsAsBytes(baseTemplates);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(itemPayload.length + groups.size() * 16 + 16);

            try (DataOutputStream out = new DataOutputStream(buffer)) {
               out.writeByte(2);
               out.writeInt(groups.size());

               for (LinkedHashMap<Integer, Long> variants : groups.values()) {
                  out.writeInt(variants.size());

                  for (Entry<Integer, Long> variant : variants.entrySet()) {
                     out.writeInt(variant.getKey());
                     out.writeLong(variant.getValue());
                  }
               }

               out.writeInt(itemPayload.length);
               out.write(itemPayload);
            }

            return buffer.toByteArray();
         }
      } else {
         return null;
      }
   }

   public static Map<ItemStack, Long> decode(byte[] blob) throws IOException {
      if (blob != null && blob.length != 0) {
         Map var3;
         try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(blob))) {
            byte version = in.readByte();

            var3 = switch (version) {
               case 1 -> decodeV1(in);
               case 2 -> decodeV2(in);
               default -> throw new IOException("Unsupported spawner inventory format version: " + version);
            };
         }

         return var3;
      } else {
         return Map.of();
      }
   }

   private static Map<ItemStack, Long> decodeV1(DataInputStream in) throws IOException {
      int count = in.readInt();
      if (count < 0) {
         throw new IOException("Negative spawner inventory entry count: " + count);
      } else if (count == 0) {
         return Map.of();
      } else {
         long[] amounts = new long[count];

         for (int i = 0; i < count; i++) {
            amounts[i] = in.readLong();
         }

         byte[] itemPayload = readPayload(in);
         ItemStack[] templates = ItemStack.deserializeItemsFromBytes(itemPayload);
         if (templates.length != count) {
            throw new IOException("Spawner inventory entry count mismatch: " + count + " amounts but " + templates.length + " items");
         } else {
            Map<ItemStack, Long> result = new LinkedHashMap<>(Math.max(16, count * 2));

            for (int i = 0; i < count; i++) {
               ItemStack template = templates[i];
               if (template != null && template.getType() != Material.AIR && amounts[i] > 0L) {
                  result.merge(template, amounts[i], Long::sum);
               }
            }

            return result;
         }
      }
   }

   private static Map<ItemStack, Long> decodeV2(DataInputStream in) throws IOException {
      int groupCount = in.readInt();
      if (groupCount < 0) {
         throw new IOException("Negative spawner inventory group count: " + groupCount);
      } else if (groupCount == 0) {
         return Map.of();
      } else {
         int[] variantCounts = new int[groupCount];
         int[][] damages = new int[groupCount][];
         long[][] amounts = new long[groupCount][];

         for (int g = 0; g < groupCount; g++) {
            int variants = in.readInt();
            if (variants < 0) {
               throw new IOException("Negative spawner inventory variant count: " + variants);
            }

            int[] groupDamages = new int[variants];
            long[] groupAmounts = new long[variants];

            for (int i = 0; i < variants; i++) {
               groupDamages[i] = in.readInt();
               groupAmounts[i] = in.readLong();
            }

            variantCounts[g] = variants;
            damages[g] = groupDamages;
            amounts[g] = groupAmounts;
         }

         byte[] itemPayload = readPayload(in);
         ItemStack[] bases = ItemStack.deserializeItemsFromBytes(itemPayload);
         if (bases.length != groupCount) {
            throw new IOException("Spawner inventory group count mismatch: " + groupCount + " groups but " + bases.length + " base items");
         } else {
            Map<ItemStack, Long> result = new LinkedHashMap<>(Math.max(16, groupCount * 2));

            for (int g = 0; g < groupCount; g++) {
               ItemStack base = bases[g];
               if (base != null && base.getType() != Material.AIR) {
                  for (int i = 0; i < variantCounts[g]; i++) {
                     long amount = amounts[g][i];
                     if (amount > 0L) {
                        int damage = damages[g][i];
                        ItemStack variant = base.clone();
                        if (damage != 0 && variant.getItemMeta() instanceof Damageable damageable) {
                           damageable.setDamage(damage);
                           variant.setItemMeta(damageable);
                        }

                        result.merge(variant, amount, Long::sum);
                     }
                  }
               }
            }

            return result;
         }
      }
   }

   private static byte[] readPayload(DataInputStream in) throws IOException {
      int payloadLength = in.readInt();
      if (payloadLength < 0) {
         throw new IOException("Negative spawner inventory payload length: " + payloadLength);
      } else {
         byte[] itemPayload = in.readNBytes(payloadLength);
         if (itemPayload.length != payloadLength) {
            throw new IOException("Truncated spawner inventory payload: expected " + payloadLength + " bytes, got " + itemPayload.length);
         } else {
            return itemPayload;
         }
      }
   }

   public static long totalItems(Map<ItemSignature, Long> items) {
      if (items != null && !items.isEmpty()) {
         long total = 0L;

         for (Long amount : items.values()) {
            if (amount != null && amount > 0L) {
               long sum = total + amount;
               if (sum < total) {
                  return Long.MAX_VALUE;
               }

               total = sum;
            }
         }

         return total;
      } else {
         return 0L;
      }
   }
}
