package site.hearen.thread.dump.analyzer.util;


import static site.hearen.thread.dump.analyzer.util.DeadLockUtils.getDeadLockLoops;
import static site.hearen.thread.dump.analyzer.util.DeadLockUtils.getLockStateHoldingListMap;
import static site.hearen.thread.dump.analyzer.util.DeadLockUtils.getLockStateWaitingListMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import site.hearen.thread.dump.analyzer.entity.ThreadDo;
import site.hearen.thread.dump.analyzer.entity.ThreadDumpDo;
import site.hearen.thread.dump.analyzer.vo.ThreadDumpVo;
import site.hearen.thread.dump.analyzer.enums.StateEnum;

@Slf4j
public final class DumpUtils {
    private DumpUtils() {
    }

    public static ThreadDumpDo sequentialParseThreadDump(List<String> lines) {
        return parseThreadDump(lines, false);
    }

    public static ThreadDumpDo parallelParseThreadDump(List<String> lines) {
        return parseThreadDump(lines, true);
    }

    public static String getCheckSum(File file) {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                return org.apache.commons.codec.digest.DigestUtils.sha256Hex(fileInputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Get Check Sum failed with file {}", file.getName());
        }
        return null;
    }

    private static ThreadDumpDo parseThreadDump(List<String> lines, boolean isParallel) {
        try {
            ThreadDumpDo threadDumpDo = ThreadParser.parse(lines, isParallel);
            threadDumpDo.getThreadDoList().forEach(threadDo -> threadDo.setDumpDo(threadDumpDo));
            ThreadDumpVo threadDumpVo = prepareDumpVo(threadDumpDo);
            Gson gson = new Gson();
            String dumpVoStr = gson.toJson(threadDumpVo);
            threadDumpDo.setThreadDumpVoJson(dumpVoStr);
            return threadDumpDo;
        } catch (Exception e) {
        }
        return null;
    }

    public static ThreadDumpVo prepareDumpVo(ThreadDumpDo threadDumpDo) {
        ThreadDumpVo threadDumpVo = ThreadDumpVo.builder().build();
        Map<StateEnum, List<ThreadDo>> stateEnumListMap = ThreadUtils.getStateGroup(threadDumpDo);
        threadDumpVo.setTotalCount(threadDumpDo.getThreadDoList().size());
        threadDumpVo.setBlockedCount(stateEnumListMap.getOrDefault(StateEnum.BLOCKED, new ArrayList<>()).size());
        threadDumpVo.setRunnableCount(stateEnumListMap.getOrDefault(StateEnum.RUNNABLE, new ArrayList<>()).size());
        int waitingCount = stateEnumListMap.getOrDefault(StateEnum.WAITING, new ArrayList<>()).size();
        waitingCount += stateEnumListMap.getOrDefault(StateEnum.TIMED_WAITING, new ArrayList<>()).size();
        threadDumpVo.setWaitingCount(waitingCount);
        threadDumpVo.setRunningCount(stateEnumListMap.getOrDefault(StateEnum.RUNNING, new ArrayList<>()).size());
        threadDumpVo.setDaemonCount(ThreadUtils.getDaemonGroup(threadDumpDo).get(true).size());
        threadDumpVo.setPoolGroup(ThreadUtils.convertListMapToSizeSortedMap(ThreadUtils.getPoolGroup(threadDumpDo)));
        threadDumpVo.setStackTraceGroup(ThreadUtils.convertListMapToSizeSortedMap(ThreadUtils.getCallStackGroup(threadDumpDo)));
        threadDumpVo.setStackTraceWithStateSizeGoup(ThreadUtils.getCallWithStateSizeGroup(threadDumpDo));
        threadDumpVo.setMostUsedMethodGroup(ThreadUtils.convertListMapToSizeSortedMap(ThreadUtils.getMostUsedMethodGroup(threadDumpDo)));
        threadDumpVo.setCpuConsumingGroup(ThreadUtils.convertListMapToSizeSortedMap(ThreadUtils.getCpuConsumingThreads(threadDumpDo)));
        threadDumpVo.setBlockingGroup(ThreadUtils.convertListToSize(ThreadUtils.getBlockingThreads(threadDumpDo)));
        updateDeadLockInfo(threadDumpDo, threadDumpVo);
        threadDumpVo.setGcThreadCount(ThreadUtils.getGcGroup(threadDumpDo).size());
        threadDumpVo.setFinalizerThreadCount(ThreadUtils.getFinalizerGroup(threadDumpDo).size());
        threadDumpVo.setCallStackFlatTree(getCallStackLevelMethodCountingGroup(threadDumpDo, false));
        threadDumpVo.setCallStackTree(buildUpCallStackTree(threadDumpDo, false));
        log.debug("The Vo for Home Page: \n{}", threadDumpVo);
        return threadDumpVo;
    }

    private static void updateDeadLockInfo(ThreadDumpDo threadDumpDo, ThreadDumpVo threadDumpVo) {
        List<List<String>> deadLockLoops = getDeadLockLoops(threadDumpDo);
        threadDumpVo.setDeadLockSimpleList(deadLockLoops);
        threadDumpVo.setHasDeadlock(deadLockLoops.size() > 0);
        threadDumpVo.setDeadLockComplexList(getDeadLockComplexList(deadLockLoops, threadDumpDo.getThreadDoList()));
    }

    private static List<List<DeadLockCounter>> getDeadLockComplexList(List<List<String>> deadLockLoops,
                                                                      List<ThreadDo> threadDoList) {
        Map<String, List<ThreadDo>> holderListMap = getLockStateHoldingListMap(threadDoList, null);
        Map<String, List<ThreadDo>> waiterListMap = getLockStateWaitingListMap(threadDoList, null);
        List<List<DeadLockCounter>> deadLockComplexList = new ArrayList<>();
        deadLockLoops.stream().forEach(loop -> {
            List<DeadLockCounter> lockCounters = new ArrayList<>();
            loop.stream().forEach(lock -> {
                lockCounters.add(DeadLockCounter.builder()
                        .lockAddress(lock)
                        .holderCount(holderListMap.get(lock).size())
                        .waiterCount(waiterListMap.get(lock).size()).build());
            });
            deadLockComplexList.add(lockCounters);
        });
        return deadLockComplexList;
    }

    private static TreeNode buildUpCallStackTree(ThreadDumpDo threadDumpDo, boolean isReversed) {
        List<ThreadDo> threadDoList = threadDumpDo.getThreadDoList().stream()
                .filter(threadDo -> threadDo.getCallStack().size() > 0).collect(Collectors.toList());
        return TreeNode.builder().callTrace("ROOT").count(threadDoList.size())
                .children(getIthLevelTreeNode(threadDoList, 0)).build();
    }

    private static List<TreeNode> getIthLevelTreeNode(List<ThreadDo> threadDos, int level) {
        List<TreeNode> children = new LinkedList<>();
        Map<String, List<ThreadDo>> listMap = getLevelGroup(threadDos, level);
        for (Map.Entry<String, List<ThreadDo>> entry : listMap.entrySet()) {
            TreeNode treeNode = TreeNode.builder().callTrace(entry.getKey()).count(entry.getValue().size()).build();
            treeNode.setChildren(getIthLevelTreeNode(entry.getValue(), level + 1));
            children.add(treeNode);
        }
        return children;
    }


    private static List<Map<String, Integer>> getCallStackLevelMethodCountingGroup(ThreadDumpDo threadDumpDo,
                                                                                   boolean isReversed) {
        List<Map<String, Integer>> countingGroup = new ArrayList<>();
        if (isReversed) {
            threadDumpDo.getThreadDoList().forEach(threadDo -> threadDo.getCallStack().sort(Comparator.reverseOrder()));
        }
        for (int index = 0; true; ++index) {
            if (!levelMethodCountingHelper(countingGroup, index++, threadDumpDo.getThreadDoList())) {
                break;
            }
        }
        if (isReversed) {
            threadDumpDo.getThreadDoList().forEach(threadDo -> threadDo.getCallStack().sort(Comparator.reverseOrder()));
        }
        return countingGroup;
    }

    private static boolean levelMethodCountingHelper(List<Map<String, Integer>> countingGroup, int index,
                                                     List<ThreadDo> threadDos) {
        Map<String, Integer> sizeMap = ThreadUtils.convertListMapToSizeSortedMap(getCompleteLevelGroup(threadDos, index));
        if (CollectionUtils.isNotEmpty(sizeMap.entrySet()) && sizeMap.entrySet().size() > 0) {
            countingGroup.add(sizeMap);
            return true;
        }
        return false;
    }

    private static Map<String, List<ThreadDo>> getCompleteLevelGroup(List<ThreadDo> threadDos, int index) {
        return threadDos.stream()
                .filter(threadDo -> threadDo.getCallStack().size() > index)
                .collect(Collectors.groupingBy(threadDo ->
                        StringUtils.join(threadDo.getCallStack().subList(0, index + 1), ThreadUtils.CALL_STACK_SEPARATOR)));

    }

    private static Map<String, List<ThreadDo>> getLevelGroup(List<ThreadDo> threadDos, int index) {
        return threadDos.stream()
                .filter(threadDo -> threadDo.getCallStack().size() > index)
                .collect(Collectors.groupingBy(threadDo -> threadDo.getCallStack().get(index)));
    }
}
