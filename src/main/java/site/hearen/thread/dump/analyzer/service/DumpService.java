package site.hearen.thread.dump.analyzer.service;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import site.hearen.thread.dump.analyzer.entity.ThreadDo;
import site.hearen.thread.dump.analyzer.entity.ThreadDumpDo;
import site.hearen.thread.dump.analyzer.util.DeadLockUtils;
import site.hearen.thread.dump.analyzer.util.DumpUtils;
import site.hearen.thread.dump.analyzer.util.ThreadUtils;
import site.hearen.thread.dump.analyzer.vo.ThreadDumpVo;
import site.hearen.thread.dump.analyzer.dao.ThreadDumpDoRepository;
import site.hearen.thread.dump.analyzer.util.ThreadParser;

@Service
@Slf4j
public class DumpService {
    @Autowired
    private ThreadDumpDoRepository dumpDoRepository;

    public ThreadDumpVo parse(String fileName, boolean isPara) throws IOException, ParseException {
        ClassLoader classLoader = getClass().getClassLoader();
        String folderName = "thread_dumps/";
        File file = new File(classLoader.getResource(folderName.concat(fileName)).getFile());
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                log.debug("thread dump original line: {}", line);
                lines.add(line);
            }
        }
        ThreadDumpDo threadDumpDo;
        if (isPara) {
            threadDumpDo = DumpUtils.sequentialParseThreadDump(lines);
        } else {
            threadDumpDo = DumpUtils.parallelParseThreadDump(lines);
        }
        threadDumpDo.setCheckSum(DumpUtils.getCheckSum(file));
        threadDumpDo.setFileName(fileName);
        threadDumpDo.setCreatedBy("");
//        dumpDoRepository.save(threadDumpDo);
        return threadDumpDo.getVo();
    }

    public List<String> loadFile() throws IOException, ParseException {
        ClassLoader classLoader = getClass().getClassLoader();
        String folderName = "thread_dumps/";
        File file = new File(classLoader.getResource(folderName.concat("dead_lock.txt")).getFile());
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static void test(Supplier<ThreadDumpDo> supplier, int times, String message) {
        List<Long> timeCosts = new ArrayList<>();
        Long start;
        int sum = 0;
        for (int i = 0; i < times; ++i) {
            start = System.nanoTime();
                supplier.get();
//                out.println(supplier.get());
            timeCosts.add((System.nanoTime() - start) / 1_000_000);
        }
//        timeCosts.forEach(timeCost -> out.println(String.format("TimeCost: %s", timeCost)));
        out.println(String.format("%s - sum: %d - time cost summary (ms): %s", message, sum,
                timeCosts.stream().collect(Collectors.summarizingLong(Long::longValue))));
    }

    public void testParser () throws Exception {
        List<String> lines = loadFile();
        test(() -> ThreadParser.parse(lines, false), 200, "seq");
        test(() -> ThreadParser.parse(lines, true), 200, "para");
    }

    public ThreadDumpVo getOldVo(Long dumpId) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        return threadDumpDo.getVo();
    }


    public List<List<String>> getSimpleDeadLockLoops(Long dumpId) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        return DeadLockUtils.getDeadLockLoops(threadDumpDo);
    }

    public List<ThreadDo> getStackTrace(Long dumpId, String stackTrace) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        Map<String, List<ThreadDo>> threadDoMap = ThreadUtils.getCallStackGroup(threadDumpDo);
        return threadDoMap.get(stackTrace);
    }

    public List<ThreadDo> getStackWithState(Long dumpId, String stackTrace, String state) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        Map<String, Map<String, List<ThreadDo>>> threadDoWithStateMap = ThreadUtils
                .getCallStackWithStateGroup(threadDumpDo);
        return threadDoWithStateMap.get(stackTrace).get(state);
    }

    public List<ThreadDo> getMostUsedMethod(Long dumpId, String mostUsedMethod) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        return ThreadUtils.getMostUsedMethodGroup(threadDumpDo).get(mostUsedMethod);
    }

    public Map<String, List<ThreadDo>> getCpuConsuming(Long dumpId) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        return ThreadUtils.getCpuConsumingThreads(threadDumpDo);
    }

    public Map<String, List<ThreadDo>> getBlocking(Long dumpId) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        return ThreadUtils.getBlockingThreads(threadDumpDo);
    }

    public List<ThreadDo> getGcThreads(Long dumpId) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        return ThreadUtils.getGcGroup(threadDumpDo);
    }

    public List<ThreadDo> getFinalizerThreads(Long dumpId) {
        ThreadDumpDo threadDumpDo = dumpDoRepository.getOne(dumpId);
        return ThreadUtils.getFinalizerGroup(threadDumpDo);
    }
}
