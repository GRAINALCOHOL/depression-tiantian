package grainalcohol.dtt.diary.wrapper;

import grainalcohol.dtt.diary.DiaryParagraph;

public class BakedTranslationKey {
    private final String key;
    private final DiaryParagraph paragraph;

    public BakedTranslationKey(String key, DiaryParagraph paragraph) {
        this.key = key;
        this.paragraph = paragraph;
    }
}
