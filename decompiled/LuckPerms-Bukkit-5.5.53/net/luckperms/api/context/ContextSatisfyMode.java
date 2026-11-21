package net.luckperms.api.context;

import net.luckperms.api.query.OptionKey;

public enum ContextSatisfyMode {
   AT_LEAST_ONE_VALUE_PER_KEY,
   ALL_VALUES_PER_KEY;

   public static final OptionKey<ContextSatisfyMode> KEY = OptionKey.of("contextsatisfymode", ContextSatisfyMode.class);
}
