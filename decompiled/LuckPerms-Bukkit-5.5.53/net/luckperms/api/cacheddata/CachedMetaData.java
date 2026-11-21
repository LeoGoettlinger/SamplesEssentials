package net.luckperms.api.cacheddata;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Function;
import net.luckperms.api.metastacking.MetaStackDefinition;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.node.types.WeightNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface CachedMetaData extends CachedData {
   @NonNull
   Result<String, MetaNode> queryMetaValue(@NonNull String var1);

   @Nullable
   default String getMetaValue(@NonNull String key) {
      return this.queryMetaValue(key).result();
   }

   @NonNull
   default <T> Optional<T> getMetaValue(@NonNull String key, @NonNull Function<String, ? extends T> valueTransformer) {
      return Optional.ofNullable(this.getMetaValue(key)).map(value -> {
         try {
            return valueTransformer.apply(value);
         } catch (IllegalArgumentException var3) {
            return null;
         }
      });
   }

   @NonNull
   Result<String, PrefixNode> queryPrefix();

   @Nullable
   default String getPrefix() {
      return this.queryPrefix().result();
   }

   @NonNull
   Result<String, SuffixNode> querySuffix();

   @Nullable
   default String getSuffix() {
      return this.querySuffix().result();
   }

   @NonNull
   Result<Integer, WeightNode> queryWeight();

   default int getWeight() {
      return this.queryWeight().result();
   }

   @NonNull
   @Unmodifiable
   Map<String, List<String>> getMeta();

   @NonNull
   @Unmodifiable
   SortedMap<Integer, String> getPrefixes();

   @NonNull
   @Unmodifiable
   SortedMap<Integer, String> getSuffixes();

   @Nullable
   String getPrimaryGroup();

   @NonNull
   MetaStackDefinition getPrefixStackDefinition();

   @NonNull
   MetaStackDefinition getSuffixStackDefinition();
}
