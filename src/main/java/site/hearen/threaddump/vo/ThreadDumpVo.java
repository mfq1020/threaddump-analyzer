package site.hearen.threaddump.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.hearen.threaddump.util.DeadLockCounter;
import site.hearen.threaddump.util.TreeNode;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadDumpVo {
    int totalCount;
    int blockedCount;
    int runnableCount;
    int waitingCount;
    int runningCount;
    int daemonCount;
    // ToDo: can be further detailed;
    Map<String, Integer> poolGroup;
    Map<String, Integer> stackTraceGroup;
    Map<String, Map<String, Integer>> stackTraceWithStateSizeGoup;
    Map<String, Integer> mostUsedMethodGroup;
    Map<String, Integer> cpuConsumingGroup;
    Map<String, Integer> blockingGroup;
    List<Map<String, Integer>> callStackFlatTree;
    TreeNode callStackTree;
    boolean hasDeadlock;
    List<List<String>> deadLockSimpleList;
    List<List<DeadLockCounter>> deadLockComplexList;
    int gcThreadCount;
    int finalizerThreadCount;
}

