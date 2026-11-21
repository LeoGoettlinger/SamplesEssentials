package net.luckperms.api.query.meta;

import java.util.List;
import net.luckperms.api.cacheddata.Result;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.query.OptionKey;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MetaValueSelector {
   OptionKey<MetaValueSelector> KEY = OptionKey.of("metavalueselector", MetaValueSelector.class);

   @NonNull
   Result<String, MetaNode> selectValue(@NonNull String var1, @NonNull List<? extends Result<String, MetaNode>> var2);
}
