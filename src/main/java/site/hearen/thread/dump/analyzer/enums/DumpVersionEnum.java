package site.hearen.thread.dump.analyzer.enums;

public enum DumpVersionEnum {
    HOTSPOT("Java HotSpot(TM)"),
    OPEN_JDK("Full thread dump OpenJDK");

    private String keyword;

    DumpVersionEnum(String key) {
        this.keyword = key;
    }

    public String toKey() {
        return keyword;
    }
}
