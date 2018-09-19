package site.hearen.thread.dump.analyzer.util;

import site.hearen.thread.dump.analyzer.enums.LineTypeEnum;
import site.hearen.thread.dump.analyzer.util.version.LineTypeAbstract;

public final class LineTypeChecker {

    private LineTypeChecker() {
    }
    public static LineTypeEnum getLineType(String line, LineTypeAbstract lineTypeAbstract) {
        LineTypeEnum typeEnum = LineTypeEnum.UNKNOWN;
        for (LineTypeEnum lineTypeEnum : LineTypeEnum.values()) {
            if (line.contains(lineTypeAbstract.getKey(lineTypeEnum))) {
                typeEnum = lineTypeEnum;
            }
        }
        if (typeEnum == LineTypeEnum.WAITING_LOCK || typeEnum == LineTypeEnum.LOCKING) {
            boolean isValidLockLine = line.contains("<") && line.contains(">");
            if (!isValidLockLine) {
                typeEnum = LineTypeEnum.UNKNOWN;
            }
        }
        return typeEnum;
    }
}
