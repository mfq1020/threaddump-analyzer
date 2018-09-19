package site.hearen.thread.dump.analyzer.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import site.hearen.thread.dump.analyzer.entity.ThreadDo;
import site.hearen.thread.dump.analyzer.enums.LineTypeEnum;
import site.hearen.thread.dump.analyzer.enums.StateEnum;
import site.hearen.thread.dump.analyzer.util.version.LineTypeAbstract;
import site.hearen.thread.dump.analyzer.util.version.LineTypeHotSpot;

@Slf4j
public final class LineParser {
    public static final String DAEMON = "daemon";
    public static final int HEX_UNIT = 16;
    private static final Pattern TITLE_WITHOUT_PRIO_PATTERN = Pattern
            .compile(".*os_prio=(\\d+)\\s+tid=(\\w+)\\s+nid=(\\w+)\\s+.*");
    private static final Pattern TITLE_PATTERN = Pattern
            .compile(".*prio=(\\d+)\\s+os_prio=(\\d+)\\s+tid=(\\w+)\\s+nid=(\\w+)\\s+.*");
    private static LineTypeAbstract lineTypeAbstract = new LineTypeHotSpot();

    private LineParser() {
    }

    public static void setLineTypeAbstract(LineTypeAbstract theLineType) {
        lineTypeAbstract = theLineType;
    }

    public static void parseTitle(ThreadDo threadDo, String line) {
        String threadName = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
        threadDo.setName(threadName);
        if (line.substring(line.lastIndexOf("\"")).contains(DAEMON)) {
            threadDo.setDaemon(true);
        }
        threadDo.setStateEnum(getTitleState(line));
        if (threadName.contains(ThreadUtils.GC_LABEL)) {
            threadDo.setBelongsToGc(true);
        }
        if (threadName.contains(ThreadUtils.FINALIZER_LABEL)) {
            threadDo.setBelongsToFinalizer(true);
        }
        int index = 1;
        Matcher matcher = TITLE_PATTERN.matcher(line);
        if (matcher.find()) {
            threadDo.setPriority(Integer.parseInt(matcher.group(index++)));
            threadDo.setOsPriority(Integer.parseInt(matcher.group(index++)));
            threadDo.setThreadId(Long.parseLong(removeHexPrefix(matcher.group(index++)), HEX_UNIT));
            threadDo.setNativeThreadId(Long.parseLong(removeHexPrefix(matcher.group(index++)), HEX_UNIT));
        } else {
            matcher = TITLE_WITHOUT_PRIO_PATTERN.matcher(line);
            if (matcher.find()) {
                threadDo.setOsPriority(Integer.parseInt(matcher.group(index++)));
                threadDo.setThreadId(Long.parseLong(removeHexPrefix(matcher.group(index++)), HEX_UNIT));
                threadDo.setNativeThreadId(Long.parseLong(removeHexPrefix(matcher.group(index++)), HEX_UNIT));
            }
        }
    }

    private static StateEnum getTitleState(String line) {
        return getStateEnum(line);
    }

    private static StateEnum getStateEnum(String line) {
        StateEnum theState = StateEnum.CREATED;
        for (StateEnum stateEnum : StateEnum.values()) {
            if (line.contains(stateEnum.toKey()) || line.contains(stateEnum.toKey().toLowerCase())) {
                theState = stateEnum;
            }
        }
        return theState;
    }

    public static void parseState(ThreadDo threadDo, String line) {
        threadDo.setStateEnum(getStateEnum(line));
    }

    public static void parseWaitingLock(ThreadDo threadDo, String line) {
        threadDo.getLocksWaiting().add(getHexStrFromAngleBracket(line));
    }

    public static void parseLockHeld(ThreadDo threadDo, String line) {
        threadDo.getLocksHeld().add(getHexStrFromAngleBracket(line));
    }

    public static void parseCallStack(ThreadDo threadDo, String line) {
        String mark = lineTypeAbstract.getKey(LineTypeEnum.STACK_TRACE);
        String callStack = line.substring(line.indexOf(mark) + mark.length());
        threadDo.getCallStack().add(callStack);
    }

    private static String removeHexPrefix(String hexStr) {
        return hexStr.substring(hexStr.indexOf("x") + 1);
    }

    private static String getHexStrFromAngleBracket(String line) {
//        return Long.valueOf(removeHexPrefix(
//                line.substring(line.indexOf("<") + 1, line.lastIndexOf(">"))), HEX_UNIT).toString();
        String ret = line;
        try {
            ret = removeHexPrefix(
                    line.substring(line.indexOf("<") + 1, line.lastIndexOf(">")));
        } catch (Exception e) {
            log.info("The error line: {}", line);
            e.printStackTrace();
        }
        return ret;
    }
}
