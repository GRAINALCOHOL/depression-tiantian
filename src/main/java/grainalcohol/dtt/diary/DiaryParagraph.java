package grainalcohol.dtt.diary;

public enum DiaryParagraph {
    WEATHER("weather"),
    NO_DIARY("no_diary"),
    MANIC_GENERAL("manic_general"),
    OPENING("opening"),
    CLOSING("closing"),
    CURED("cured"),
    WORSENED("worsened"),
    GENERAL("general"),
    TOPIC("topic"),
    ;

    private final String translationKey;

    DiaryParagraph(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
