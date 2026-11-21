package ch.njol.util.coll;

import java.util.Map;
import java.util.Set;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public interface BidiMap<T1, T2> extends Map<T1, T2> {
   BidiMap<T2, T1> getReverseView();

   T1 getKey(T2 var1);

   T2 getValue(T1 var1);

   Set<T2> valueSet();
}
