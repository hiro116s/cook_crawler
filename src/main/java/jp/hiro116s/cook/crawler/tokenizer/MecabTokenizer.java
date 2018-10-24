package jp.hiro116s.cook.crawler.tokenizer;

import com.google.common.collect.ImmutableList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class MecabTokenizer {
    private final Analyzer analyzer;

    public MecabTokenizer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public List<Result> tokenize(final Reader reader) {
        try (final TokenStream tokenStream = analyzer.tokenStream("", reader)) {
            final BaseFormAttribute baseAttr = tokenStream.addAttribute(BaseFormAttribute.class);
            final CharTermAttribute charAttr = tokenStream.addAttribute(CharTermAttribute.class);
            final PartOfSpeechAttribute posAttr = tokenStream.addAttribute(PartOfSpeechAttribute.class);
            final ReadingAttribute readAttr = tokenStream.addAttribute(ReadingAttribute.class);

            tokenStream.reset();
            ImmutableList.Builder<Result> builder = ImmutableList.builder();
            while (tokenStream.incrementToken()) {
                String text = charAttr.toString();                // 単語
                String baseForm = baseAttr.getBaseForm();       // 原型
                String reading = readAttr.getReading();         // 読み
                String partOfSpeech = posAttr.getPartOfSpeech();    // 品詞

                builder.add(new Result(text, baseForm, reading, partOfSpeech));
            }
            return builder.build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
