package site.hearen.thread.dump.analyzer.util;

import static site.hearen.thread.dump.analyzer.util.LineParser.parseCallStack;
import static site.hearen.thread.dump.analyzer.util.LineParser.parseLockHeld;
import static site.hearen.thread.dump.analyzer.util.LineParser.parseState;
import static site.hearen.thread.dump.analyzer.util.LineParser.parseTitle;
import static site.hearen.thread.dump.analyzer.util.LineParser.parseWaitingLock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import site.hearen.thread.dump.analyzer.entity.ThreadDo;
import site.hearen.thread.dump.analyzer.entity.ThreadDumpDo;
import site.hearen.thread.dump.analyzer.enums.DumpVersionEnum;
import site.hearen.thread.dump.analyzer.enums.LineTypeEnum;
import site.hearen.thread.dump.analyzer.util.version.LineTypeAbstract;
import site.hearen.thread.dump.analyzer.util.version.LineTypeHotSpot;
import site.hearen.thread.dump.analyzer.util.version.LineTypeOpenJdk;

@Component
@Slf4j
public final class ThreadParser {
    private static final Pattern FIRST_LINE_PATTERN = Pattern.compile("^\\d+:$");
    private static LineTypeAbstract lineTypeAbstract = new LineTypeHotSpot();

    private ThreadParser() {
    }

    public static ThreadDumpDo parse(List<String> lines, boolean isParallel) {
        ThreadDumpDo threadDumpDo = ThreadDumpDo.builder()
                .parsedTime(new Date()).build();
        try {
            parseHeader(threadDumpDo, lines);
        } catch (Exception e){

        }
        lines = removeInvalidFooter(lines);
        if (isParallel) {
            threadDumpDo.setThreadDoList(parallelLinesParse(lines));
        } else {
            threadDumpDo.setThreadDoList(parseLines(lines));
        }
        List<ThreadDo> threadDoList = threadDumpDo.getThreadDoList();
        cleanInvalid(threadDoList);
//        log.info("Parsed Thread Entities: \n{}", toJsonString(threadDoList));
        return threadDumpDo;
    }

    private static List<ThreadDo> parallelLinesParse(List<String> lines) {
        List<ThreadDo> completeList = new ArrayList<>();
        List<List<ThreadDo>> blockList = splitUpLinesByCoreCount(lines).parallelStream()
                .map(linesBlock -> parseLines(linesBlock)).collect(Collectors.toList());
        blockList.forEach(list -> completeList.addAll(list));
        return completeList;
    }

    private static List<ThreadDo> parseLines(List<String> lines) {
        List<ThreadDo> threadDoList = new ArrayList<>();
        lines.stream()
                .forEach(line -> parseNewLine(threadDoList, line));
        threadDoList.stream().forEach(threadDo -> threadDo.setCallStack(getCallStack(threadDo)));
        return threadDoList;
    }

    private static List<String> getCallStack(ThreadDo threadDo) {
        List<String> callStacks = new ArrayList<>();
        threadDo.getDetails().stream().forEach(line -> {
            if (LineTypeChecker.getLineType(line, lineTypeAbstract) == LineTypeEnum.STACK_TRACE) {
                String mark = lineTypeAbstract.getKey(LineTypeEnum.STACK_TRACE);
                callStacks.add(line.substring(line.indexOf(mark) + mark.length()));
            }
        });
        return callStacks;
    }

    private static List<List<String>> splitUpLinesByCoreCount(List<String> lines) {
        int coreCount = Runtime.getRuntime().availableProcessors();
        return splitUpLinesByCount(lines, coreCount);
    }

    private static List<List<String>> splitUpLinesByCount(List<String> lines, int blockCount) {
        int blockSize = lines.size() / blockCount;
        List<List<String>> blockList = new ArrayList<>();
        int begin = 0;
        int end = blockSize;
        for (int i = 0; i < blockCount; ++i) {
            while (end < lines.size() && LineTypeChecker.getLineType(lines.get(end), lineTypeAbstract) != LineTypeEnum.TITLE) {
                end++;
            }
            blockList.add(lines.subList(begin, end));
            begin = end;
            end = blockSize + end;
            if (end > lines.size()) {
                break;
            }
        }
        return blockList;
    }

    private static void cleanInvalid(List<ThreadDo> threadDoList) {
        Iterator<ThreadDo> iterator = threadDoList.iterator();
        while (iterator.hasNext()) {
            if (isThreadDoInvalid(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private static boolean isThreadDoInvalid(ThreadDo threadDo) {
        if (threadDo == null) {
            return true;
        }
        if (StringUtils.isEmpty(threadDo.getName()) || StringUtils.isEmpty(threadDo.getName().trim())) {
            return true;
        }
        if (threadDo.getThreadId() == 0 || threadDo.getNativeThreadId() == 0) {
            return true;
        }
        return false;
    }

    private static void parseNewLine(List<ThreadDo> threadDoList, String newLine) {
        ThreadDo theThreadDo = ThreadDo.builder().build();
        LineTypeEnum lineTypeEnum = LineTypeChecker.getLineType(newLine, lineTypeAbstract);
        if (lineTypeEnum != LineTypeEnum.UNKNOWN) {
            if (lineTypeEnum == LineTypeEnum.TITLE) {
                theThreadDo = ThreadDo.builder()
                        .locksHeld(new ArrayList<>())
                        .locksWaiting(new ArrayList<>())
                        .callStack(new ArrayList<>())
                        .details(new ArrayList<>())
                        .build();
                threadDoList.add(theThreadDo);
            }
            ThreadUtils.getLast(threadDoList).getDetails().add(newLine);
        }
        switch (lineTypeEnum) {
            case TITLE:
                parseTitle(theThreadDo, newLine);
                break;
            case STATE:
                theThreadDo = ThreadUtils.getLast(threadDoList);
                parseState(theThreadDo, newLine);
                break;
            case WAITING_LOCK:
                theThreadDo = ThreadUtils.getLast(threadDoList);
                parseWaitingLock(theThreadDo, newLine);
                break;
            case LOCKING:
                theThreadDo = ThreadUtils.getLast(threadDoList);
                parseLockHeld(theThreadDo, newLine);
                break;
            case STACK_TRACE:
                theThreadDo = ThreadUtils.getLast(threadDoList);
                parseCallStack(theThreadDo, newLine);
                break;
            default:
                break;

        }
    }

    private static void parseHeader(ThreadDumpDo threadDumpDo, List<String> lines) throws ParseException {
        if (FIRST_LINE_PATTERN.matcher(lines.get(0)).matches()) {
            lines.remove(0);
        }
        //ToDo: this line might be lost - check it - it's PID - Hearen;
        Date dumpedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lines.get(0));
        lines.remove(0);
        threadDumpDo.setDumpedTime(dumpedTime);
        threadDumpDo.setDetails(lines.get(0));
        DumpVersionEnum theDumpVersion = DumpVersionEnum.HOTSPOT;
        for (DumpVersionEnum versionEnum : DumpVersionEnum.values()) {
            if (threadDumpDo.getDetails().contains(versionEnum.toKey())) {
                theDumpVersion = versionEnum;
            }
        }
        threadDumpDo.setVersion(theDumpVersion);
        rebuildParser(theDumpVersion);
        lines.remove(0);
    }

    private static void rebuildParser(DumpVersionEnum versionEnum) {
        LineTypeAbstract theLineType = new LineTypeHotSpot();
        switch (versionEnum) {
            case HOTSPOT:
                theLineType = new LineTypeHotSpot();
                break;
            case OPEN_JDK:
                theLineType = new LineTypeOpenJdk();
                break;
            default:
                break;
        }
        setLineTypeAbstract(theLineType);
    }

    private static void setLineTypeAbstract(LineTypeAbstract theLineType) {
        lineTypeAbstract = theLineType;
        LineParser.setLineTypeAbstract(theLineType);
    }

    private static List<String> removeInvalidFooter(List<String> lines) {
        int i = lines.size() - 1;
        while (i > -1 && LineTypeChecker.getLineType(lines.get(i), lineTypeAbstract) != LineTypeEnum.THREAD_BLOCK_END) {
            i--;
        }
        return lines.subList(0, i);
    }
}
