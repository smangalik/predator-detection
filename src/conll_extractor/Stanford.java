package conll_extractor;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Span;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
//import edu.stonybrook.nlp.concepts.Span;
//import edu.stonybrook.nlp.util.Pair;
//import edu.stonybrook.nlp.util.StringUtils;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Ritwik Banerjee
 */
public class Stanford {

    public final StanfordCoreNLP pipeline;
    public final PipelineExtent  extent;
    public final boolean         canSplitSentences;
    public final boolean         canTokenize;


    public Stanford(PipelineExtent extent) {
        Properties properties = new Properties();
        properties.put("annotators", extent.getProperties());
        this.extent = extent;
        this.pipeline = new StanfordCoreNLP(properties);
        canSplitSentences = extent.getProperties().contains("ssplit");
        canTokenize = extent.getProperties().contains("tokenize");
    }

    public static void main(String[] args) {
        String text = "This, with 1,2-trichloroethane, is a sentence.";
        Stanford pipeline = new Stanford(PipelineExtent.POS_TAG);
        Map<Span, Pair<String,String>> spanToWordPOSPairMap = pipeline.getPOSTagSpans(text);

        for (Map.Entry<Span, Pair<String,String>> entry : spanToWordPOSPairMap.entrySet()) {
            System.out.println(entry.getKey().toString() + " --> " + entry.getValue().toString());
        }
    }

    public List<String> tokenize(String text) {
        if (!extent.getProperties().contains("tokenize"))
            throw throwExceptionWith(PipelineExtent.TOKENIZE.name());
        List<String> tokens = new ArrayList<>();
        Annotation annotation = new Annotation(text);
        this.pipeline.annotate(annotation);
        annotation.get(CoreAnnotations.TokensAnnotation.class).forEach(
            coremap -> tokens.addAll(
                coremap.get(CoreAnnotations.TokensAnnotation.class)
                       .stream()
                       .map(corelabel -> corelabel.get(CoreAnnotations.TextAnnotation.class))
                       .collect(Collectors.toList())
            )
        );
        return tokens;
    }

    private UnsupportedOperationException throwExceptionWith(String desiredOperation) {
        String msg = String.format("Stanford CoreNLP Pipeline instantiated with %s. " +
                                   "Operation %s is not supported.",
                                   extent.name(), desiredOperation);
        return new UnsupportedOperationException(msg);
    }

    public List<String> getTokenizedSentences(String text) {
        if (!extent.getProperties().contains("ssplit"))
            throw throwExceptionWith(PipelineExtent.SENTENCE_SPLIT.name());
        List<String> sentences = new ArrayList<>();
        Annotation annotation = new Annotation(text);
        this.pipeline.annotate(annotation);
        annotation.get(CoreAnnotations.SentencesAnnotation.class).forEach(
            coremap -> sentences.add(coremap.get(CoreAnnotations.TokensAnnotation.class)
                                            .stream()
                                            .map(corelabel -> corelabel.get(CoreAnnotations.TextAnnotation.class))
                                            .map(StringUtils.DEFAULT_TOSTRING)
                                            .collect(Collectors.joining(" "))
            )
        );
        return sentences;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<Entry<String, Span>> getSentencesWithSpans(String text) {
        if (!canSplitSentences) throw throwExceptionWith(PipelineExtent.SENTENCE_SPLIT.name());

        Annotation annotation = new Annotation(text);
        this.pipeline.annotate(annotation);
        List<CoreMap> coremaps = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        return coremaps.stream().map(Stanford::getSentenceWithSpan).collect(Collectors.toList());
    }

    private static Entry<String, Span> getSentenceWithSpan(CoreMap coreMap) {
        String sentenceString = coreMap.get(CoreAnnotations.TextAnnotation.class);
        int    sentenceBegin  = coreMap.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
        int    sentenceEnd    = coreMap.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

        return new SimpleImmutableEntry<>(sentenceString, new Span(sentenceBegin, sentenceEnd));
    }

    public List<Span> getSentenceSpans(String text) {
        if (!canSplitSentences) throw throwExceptionWith(PipelineExtent.SENTENCE_SPLIT.name());

        Annotation annotation = new Annotation(text);
        this.pipeline.annotate(annotation);
        List<CoreMap> coremaps = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        return coremaps.stream().map(Stanford::getSentenceSpan).collect(Collectors.toList());
    }

    private static Span getSentenceSpan(CoreMap coreMap) {
        int    sentenceBegin  = coreMap.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
        int    sentenceEnd    = coreMap.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
        return new Span(sentenceBegin, sentenceEnd);
    }

    public TreeMap<Span, Pair<String,String>> getPOSTagSpans(String text) {
        if (!extent.getProperties().contains("pos"))
            throw throwExceptionWith(PipelineExtent.POS_TAG.name());

        Annotation annotation = new Annotation(text);
        this.pipeline.annotate(annotation);

        TreeMap<Span, Pair<String,String>> spanToWordPOSPairMap = new TreeMap<>((s, t) -> s.start() - t.start());
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word  = token.get(CoreAnnotations.TextAnnotation.class);
                String pos   = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                int    start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int    end   = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                spanToWordPOSPairMap.put(new Span(start, end), new Pair<>(word, pos));
            }
        }

        return spanToWordPOSPairMap;
    }

    public List<String> getTokens(String text) {
        if (!canTokenize) throw throwExceptionWith(PipelineExtent.TOKENIZE.name());

        Annotation annotation = new Annotation(text);
        this.pipeline.annotate(annotation);
        List<CoreLabel> corelabels = annotation.get(CoreAnnotations.TokensAnnotation.class);

        return corelabels.stream()
                         .map(corelabel -> corelabel.get(CoreAnnotations.TextAnnotation.class))
                         .collect(Collectors.toList());
    }

    public List<Pair<String,String>> tag(String text) {
        if (!extent.getProperties().contains("pos"))
            throw throwExceptionWith(PipelineExtent.POS_TAG.name());
        List<Pair<String,String>> tagged_text = new ArrayList<>();
        Annotation annotation = new Annotation(text);
        this.pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                tagged_text.add(new Pair<>(word, pos));
            }
        }

        return tagged_text;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String lemmatize(String word) {
        if (!extent.getProperties().contains("lemma"))
            throw throwExceptionWith(PipelineExtent.LEMMATIZE.name());
        String lemma = word;
        try {
            Annotation annotation = new Annotation(word);
            this.pipeline.annotate(annotation);
            CoreMap coreMap = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
            CoreLabel label = coreMap.get(CoreAnnotations.TokensAnnotation.class).get(0);
            lemma = label.get(CoreAnnotations.LemmaAnnotation.class);
        } catch (Exception ignore) { /* sometimes, a blank token gets through! */ }
        return lemma;
    }

    /**
     * @param sentence The sentence to be chunked
     * @return A list of (phrase, POS) pairs.
     */
    public List<Entry<String, String>> chunk(String sentence) {
        if (!extent.getProperties().contains("parse"))
            throw throwExceptionWith(PipelineExtent.PARSE.name());
        Annotation annotation = new Annotation(sentence);
        pipeline.annotate(annotation);
        CoreMap coreMap = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        Tree tree = coreMap.get(TreeCoreAnnotations.TreeAnnotation.class);
        List<Entry<String, String>> chunks = new ArrayList<>();
        extractChunks(tree, chunks);
        return chunks;
    }

    private void extractChunks(Tree tree, List<Entry<String, String>> chunks) {
        Arrays.stream(tree.children()).forEach(child -> {
            String tag = child.value();
            if (child.isPhrasal() && isPreTerminal(child)) {
                String phrase = child.getLeaves().stream()
                                     .flatMap(node -> node.yieldWords().stream())
                                     .map(Word::word)
                                     .collect(Collectors.joining(" "));
                chunks.add(new SimpleImmutableEntry<>(phrase, tag));
            } else {
                extractChunks(child, chunks);
            }
        });
    }

    private boolean isPreTerminal(Tree tree) {
        return Arrays.stream(tree.children()).filter(Tree::isPhrasal).count() == 0;
    }

    public enum PipelineExtent {

        TOKENIZE("tokenize"),
        SENTENCE_SPLIT("tokenize, ssplit"),
        POS_TAG("tokenize, ssplit, pos"),
        LEMMATIZE("tokenize, ssplit, pos, lemma"),
        PARSE("tokenize, ssplit, pos, lemma, parse");

        private String properties;

        private PipelineExtent(String properties) {
            this.properties = properties;
        }

        public String getProperties() {
            return this.properties;
        }
    }
}

