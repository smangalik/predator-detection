package conll_extractor;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.StringUtils;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class CoNLL_Extractor {

    public static void main(String args[]) throws ClassNotFoundException, IOException {

        /** the chat lines in a selected file as Strings */
        ArrayList<String> posts = FileCleaner.cleanFile();

        /** the resulting n-grams as Strings */
        ArrayList<String> engrams = new ArrayList<>();

        /** the resulting tokens as Strings */
        ArrayList<String> tokens = new ArrayList<>();

        /** the tagged Strings */
        ArrayList<String> taggedStrings = new ArrayList<>();

        /** the "n" of the n-gram */
        int n = 3;

        // extract n-grams from posts
        for(String post : posts){
                ArrayList<String> words = (ArrayList<String>) StringUtils.getNgramsString(post, 1, 1);
                ArrayList<String> ngrams = (ArrayList<String>) StringUtils.getNgrams(words, n, n);
                for (String gram : ngrams){
                        engrams.add(gram);
                        //System.out.println(gram);
                }
        }

        // tokenize string in the file
        for (String post : posts){
            PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(post), 
                new CoreLabelTokenFactory(), "");
            while (ptbt.hasNext()) {
                CoreLabel label = ptbt.next();
                tokens.add(label.toString());
                //System.out.println(label);
            }
        }     


        //extract n-grams from posts
        MaxentTagger tagger = new MaxentTagger("taggers/left3words-wsj-0-18.tagger");
            for(String post : posts){
                taggedStrings.add(tagger.tagString(post));
            }				
            for(String tagged : taggedStrings){
                System.out.println(tagged);
            }
    }
	
}