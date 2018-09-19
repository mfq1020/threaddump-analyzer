package site.hearen.thread.dump.analyzer.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeadLockCounter {
    String lockAddress;
    Integer holderCount;
    Integer waiterCount;
}
