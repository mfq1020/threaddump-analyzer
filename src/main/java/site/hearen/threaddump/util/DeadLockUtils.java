package site.hearen.threaddump.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import site.hearen.threaddump.entity.ThreadDo;
import site.hearen.threaddump.entity.ThreadDumpDo;
import site.hearen.threaddump.enums.StateEnum;

public final class DeadLockUtils {
    private DeadLockUtils() {
    }

    public static List<List<String>> getDeadLockLoops(ThreadDumpDo threadDumpDo) {
        List<Deque<String>> collectingList = new ArrayList<>();
        Map<String, List<ThreadDo>> lockHelderBlockedListMap = getLockStateHoldingListMap(
                threadDumpDo.getThreadDoList(), StateEnum.BLOCKED);
        Map<String, Boolean> tried = new HashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        for (Map.Entry<String, List<ThreadDo>> entry : lockHelderBlockedListMap.entrySet()) {
            lockHelderBlockedListMap.keySet().stream().forEach(lock -> {
                tried.put(lock, false);
                visited.put(lock, false);
            });
            collectLoops(collectingList, new ArrayDeque<>(), lockHelderBlockedListMap, entry.getKey(), tried, visited);
        }
        List<List<String>> loopList = new ArrayList<>();
        for (int i = collectingList.size() - 1; i >= 0; i--) {
            List<String> candidate = new ArrayList<>(collectingList.get(i));
            int i1 = i - 1;
            for (; i1 >= 0; i1--) {
                List<String> childSet = new ArrayList<>(collectingList.get(i1));
                if (candidate.containsAll(childSet)) {
                    break;
                }
            }
            if (i1 < 0) {
                loopList.add(candidate);
            }
        }
        return loopList;
    }

    private static boolean collectLoops(List<Deque<String>> loopList, Deque<String> loop,
                                        Map<String, List<ThreadDo>> listMap, String curLock,
                                        Map<String, Boolean> tried, Map<String, Boolean> visited) {

        if (visited.get(curLock) != null && visited.get(curLock)) {
            loopList.add(new ArrayDeque<>(loop));
            return true;
        }
        if (tried.get(curLock) != null && tried.get(curLock) || listMap.get(curLock) == null) {
            return false;
        }
        loop.push(curLock);
        visited.put(curLock, true);
        tried.put(curLock, true);
        for (ThreadDo threadDo : listMap.get(curLock)) {
            for (String lockWaiting : threadDo.getLocksWaiting()) {
                if (collectLoops(loopList, loop, listMap, lockWaiting, tried, visited)) {
                    return true;
                }
            }
        }
        visited.put(curLock, false);
        loop.pop();
        return false;
    }


    /**
     * If getDeadLockLoops already used, please just use the result of it to determine
     * getDeadLockLoops(threadDumpDo).size() > 0
     * whether there is a dead lock directly instead of invoke this method again.
     *
     * @param threadDumpDo
     * @return
     */
    @Deprecated
    public static boolean hasDeadlock(ThreadDumpDo threadDumpDo) {
        if (getDeadLockLoops(threadDumpDo).size() > 0) {
            return true;
        }
        return false;
    }

    public static Map<String, List<ThreadDo>> getLockStateHoldingListMap(List<ThreadDo> threadDoList,
                                                                         StateEnum theState) {
        List<ThreadDo> threadList = threadDoList.stream()
                .filter(threadDo -> threadDo.getStateEnum() == theState || theState == null)
                .collect(Collectors.toList());
        Map<String, List<ThreadDo>> lockHoldingListMap = new HashMap<>();
        threadList.forEach(threadDo -> {
            threadDo.getLocksHeld().stream().forEach(lockHeld -> {
                lockHoldingListMap.putIfAbsent(lockHeld, new ArrayList<>());
                lockHoldingListMap.get(lockHeld).add(threadDo);
            });
        });
        return lockHoldingListMap;
    }

    public static Map<String, List<ThreadDo>> getLockStateWaitingListMap(List<ThreadDo> threadDoList,
                                                                         StateEnum theState) {
        List<ThreadDo> threadList = threadDoList.stream()
                .filter(threadDo -> threadDo.getStateEnum() == theState || theState == null)
                .collect(Collectors.toList());
        Map<String, List<ThreadDo>> lockWaitingListMap = new HashMap<>();
        threadList.forEach(threadDo -> {
            threadDo.getLocksWaiting().stream().forEach(lockWaiting -> {
                lockWaitingListMap.putIfAbsent(lockWaiting, new ArrayList<>());
                lockWaitingListMap.get(lockWaiting).add(threadDo);
            });
        });
        return lockWaitingListMap;
    }

}
