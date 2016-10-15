package conll_extractor;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.XMLOutputter;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CoNLL_Extractor {
    
    public static void main(String args[]) throws ClassNotFoundException, IOException {
        
        // Properties of the pipeline
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        
        // StanfordCoreNLP
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        /** the chat lines in a selected file as Strings   */
        ArrayList<String> posts = FileCleaner.getPosts();
        
        /** the n-grams in the post   */
        ArrayList<String> engrams = new ArrayList<>();
        
        /**
         * the "n" of the n-gram
         */
        int n = 3;
        
        // extract n-grams from posts
        for (String post : posts) {
            ArrayList<String> words = (ArrayList<String>) StringUtils.getNgramsString(post, 1, 1);
            ArrayList<String> ngrams = (ArrayList<String>) StringUtils.getNgrams(words, n, n);
            for (String gram : ngrams) {
                engrams.add(gram);
                //System.out.println(gram);
            }
        }
        
        // extract tokens, POS, and lemmas
        for (String post : posts) {
            Annotation document = new Annotation(post);
            pipeline.annotate(document);
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            for(CoreMap sentence: sentences) {
                // traversing the words in the current sentence
                // a CoreLabel is a CoreMap with additional token-specific methods
                int num = 0;
                
                System.out.println("Mine");
                
                for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                    //this is the number of the token
                    num++;
                    // this is the text of the token
                    String word = token.get(TextAnnotation.class);
                    // this is the POS tag of the token
                    String pos = token.get(PartOfSpeechAnnotation.class);
                    pos = pos.substring(0, 2);
                    // this is the PPOS tag of the token
                    String ppos = token.get(PartOfSpeechAnnotation.class);
                    // this is the lemma label of the token
                    String lemma = token.lemma();
                    
                    System.out.println(num + "\t" + 
                            word + "\t" + pos + "\t" + 
                            ppos + "\t" + lemma);
                }
                // this is an outputter for annotations
                System.out.println("Theirs");
                System.out.println(new CoNLLOutputter().print(document));
                //System.out.print(new XMLOutputter().print(document));
                
                // this is the parse tree of the current sentence
                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);                
                System.out.println(tree.toString() + "\n"); // newline
            }
        }
    }
    
    
}
