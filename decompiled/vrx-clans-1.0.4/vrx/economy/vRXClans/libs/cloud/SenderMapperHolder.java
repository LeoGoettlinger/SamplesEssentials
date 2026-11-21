package vrx.economy.vRXClans.libs.cloud;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface SenderMapperHolder<B, M> {
   @NonNull
   SenderMapper<B, M> senderMapper();
}
