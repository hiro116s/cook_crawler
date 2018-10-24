package jp.hiro116s.cook.crawler.tokenizer;

import java.util.Objects;

public class Result {
    private final String text, baseForm, reading, partOfSpeech;

    public Result(String text, String baseForm, String reading, String partOfSpeech) {
        this.text = text;
        this.baseForm = baseForm;
        this.reading = reading;
        this.partOfSpeech = partOfSpeech;
    }

    public String getText() {
        return text;
    }

    public String getBaseForm() {
        return baseForm;
    }

    public String getReading() {
        return reading;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Objects.equals(reading, result.reading);
    }

    @Override
    public int hashCode() {

        return Objects.hash(reading);
    }

    @Override
    public String toString() {
        return "Result{" +
                "text='" + text + '\'' +
                ", baseForm='" + baseForm + '\'' +
                ", reading='" + reading + '\'' +
                ", partOfSpeech='" + partOfSpeech + '\'' +
                '}';
    }
}