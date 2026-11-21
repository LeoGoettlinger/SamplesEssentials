package net.luckperms.api.metastacking;

import java.util.List;
import net.luckperms.api.query.OptionKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface MetaStackDefinition {
   OptionKey<MetaStackDefinition> PREFIX_STACK_KEY = OptionKey.of("prefixstack", MetaStackDefinition.class);
   OptionKey<MetaStackDefinition> SUFFIX_STACK_KEY = OptionKey.of("suffixstack", MetaStackDefinition.class);

   @NonNull
   @Unmodifiable
   List<MetaStackElement> getElements();

   @NonNull
   DuplicateRemovalFunction getDuplicateRemovalFunction();

   @NonNull
   String getStartSpacer();

   @NonNull
   String getMiddleSpacer();

   @NonNull
   String getEndSpacer();
}
