package grainalcohol.dtt.diary;

public enum DiaryParagraph {
    OPENING("opening"),
    HAS_CURED("has_cured"),
    HAS_WORSENED("has_worsened"),
    BODY_NORMAL("body_normal"),
    BODY_ESSENTIAL("body_essential"),
    BODY_MAJOR_IMPACT("body_major_impact"),
    CLOSING("closing"),
    NO_DIARY("no_diary"),
    MANIC_INSERTION("manic_insertion")
    ;

    private final String name;

    DiaryParagraph(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
