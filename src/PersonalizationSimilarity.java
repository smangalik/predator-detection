package wordnet;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PersonalizationSimilarity {
    
    /** Beginning Directory */
    private static final File dirtyDir = new File("/home/sid/Documents/CSE487/GeneralData");
    
    /** Ending Directory */
    private static final File csvDir = new File("/home/sid/Documents/CSE487/Similarity_Person/");
    
    /** Lexical Database */
    private static ILexicalDatabase db = new NictWordNet();
    
    /** Word Number Counter */
    private static int wordNumber = 0;
    
    public static void main (String args[]) throws
            FileNotFoundException, IOException, SAXException,
            ParserConfigurationException, TransformerException{
        
        // Properties of the pipeline
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        
        // StanfordCoreNLP
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        // All files to clean
        File[] dirtyFiles = null;
        
        // All captured words from the chats
        ArrayList<String> wordList = new ArrayList<>();
        ArrayList<String> wordListVictim = new ArrayList<>();
        ArrayList<String> wordListPredator = new ArrayList<>();
        
        //Select your dirty files
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(dirtyDir);
        fileChooser.setMultiSelectionEnabled(true);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            dirtyFiles = fileChooser.getSelectedFiles();
        }
        
        // for each dirty file make a clean with CoNLL Form and Parse Tree
        for(File dirtyFile : dirtyFiles){
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(dirtyFile);
            docBuilder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {}
                @Override
                public void fatalError(SAXParseException exception) throws SAXException {}
                @Override
                public void warning(SAXParseException exception) throws SAXException {}
            });
            
            // File writer used to make our csv
            PrintWriter victimWriter;
            PrintWriter predatorWriter;
            String csvNameVictim = csvDir.toString() + "/" +
                    dirtyFile.getName().replace(".xml","Victim") + ".csv";
            String csvNamePredator = csvDir.toString() + "/" +
                    dirtyFile.getName().replace(".xml","Predator") + ".csv";
            System.out.println(csvNameVictim);
            victimWriter = new PrintWriter(csvNameVictim, "UTF-8");
            predatorWriter = new PrintWriter(csvNamePredator, "UTF-8");
            
            String predatorName = dirtyFile.getName().replace(".xml","");
            predatorName = predatorName.toLowerCase();
            System.out.println(predatorName);
            
            // Get all of the <POST> posts
            NodeList bodies = doc.getElementsByTagName("BODY");
            
            for (int i = 0; i < bodies.getLength(); i++){
                
                Node body = bodies.item(i);
                Node date = body.getPreviousSibling().getPreviousSibling();
                Node username = date.getPreviousSibling().getPreviousSibling();
                
                // String in the <BODY> and <username>
                String bodyString = body.getTextContent();
                String nameString = username.getTextContent();
                
                //Clean up text lingo
                if(bodyString.matches(".*(emoti).*") || bodyString.matches(".*disconnected"))
                    continue;
                
                if(bodyString.length() < 1){
                    continue;
                }
                
                // Clean up the chat message
                bodyString = cleanString(bodyString);
                String firstChar = bodyString.substring(0,1).toUpperCase();
                bodyString = firstChar + bodyString.substring(1);
                
                // Get a preview of the cleaned chats
                //System.out.println(bodyString);
                
                // Grab only Nouns and Verbs
                Annotation document = new Annotation(bodyString);
                pipeline.annotate(document);
                List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
                for(CoreMap sentence: sentences) { // traversing the words in the current sentence
                    
                    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                        // this is the text of the token
                        String word = token.get(TextAnnotation.class);
                        // this is the PPOS tag of the token
                        String pos = token.get(PartOfSpeechAnnotation.class);
                        // this is the lemma of the token
                        String lemma = token.lemma();
                        
                        if(pos.charAt(0) == 'N' || pos.charAt(0) == 'V')
                            if(nameString.equals(predatorName)){
                                //System.out.println("Predator: " + lemma);
                                wordListPredator.add(lemma);
                            }
                            else{
                                //System.out.println("Victim: " + lemma);
                                wordListVictim.add(lemma);
                            }
                    }
                    
                }
            }
            
            /** Victim's chatlog */
            //csv header
            victimWriter.println("number,person,word,label");
            for(String word : wordListVictim){
                // Compares all words with the depersonalizing words
                RelatednessCalculator rc = new WuPalmer(db);
                String personResult = personSimilarity(rc, word);
                System.out.println(personResult);
                victimWriter.println(personResult);
            }
            
            wordNumber = 0;
            
            /** Predator's chatlog */
            //csv header
            predatorWriter.println("number,person,word,label");
            for(String word : wordListPredator){
                // Compares all words with the word "person"
                RelatednessCalculator rc = new WuPalmer(db);
                String personResult = personSimilarity(rc, word);
                System.out.println(personResult);
                predatorWriter.println(personResult);
            }
            
            // end of file run
            wordNumber = 0;
            victimWriter.close();
            predatorWriter.close();
            
        }
    }
       
    /**
     * Uses RelatedCalculator of your choice to compare with DEPERSONALIZING WORDS
     *
     * @param word1 word that will be compared with "person"
     * @return formatted string with the concept similarity inside of it
     */
    private static String personSimilarity(RelatednessCalculator rc, String word1){
        ILexicalDatabase db = new NictWordNet();
        WS4JConfiguration.getInstance().setMFS(true);
        List<POS[]> posPairs = rc.getPOSPairs();
        double maxScore = -1D;
        
        for(POS[] posPair: posPairs) {
            List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
            // set synset2 manually
            List<Concept> rough_synsets2;
            rough_synsets2 = (List<Concept>)db.getAllConcepts("boy", posPair[1].toString());
            ArrayList<Concept> synsets2 = new ArrayList<>();
            if (posPair[1] == POS.n){ // get all noun meanings
                synsets2.removeAll(synsets2);
                synsets2.addAll(rough_synsets2);
            }
            else if (posPair[1] == POS.v){ // get all verb meanings
                synsets2.removeAll(synsets2);
            }
            for(Concept synset1: synsets1) {
                for (Concept synset2: synsets2) {
                    Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
                    double score = relatedness.getScore();
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }
        }
        
        for(POS[] posPair: posPairs) {
            List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
            // set synset2 manually
            List<Concept> rough_synsets2;
            rough_synsets2 = (List<Concept>)db.getAllConcepts("girl", posPair[1].toString());
            ArrayList<Concept> synsets2 = new ArrayList<>();
            if (posPair[1] == POS.n){ // get all noun meanings
                synsets2.removeAll(synsets2);
                synsets2.addAll(rough_synsets2);
            }
            else if (posPair[1] == POS.v){ // get all verb meanings
                synsets2.removeAll(synsets2);
            }
            for(Concept synset1: synsets1) {
                for (Concept synset2: synsets2) {
                    Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
                    double score = relatedness.getScore();
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }
        }
        
        for(POS[] posPair: posPairs) {
            List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
            // set synset2 manually
            List<Concept> rough_synsets2;
            rough_synsets2 = (List<Concept>)db.getAllConcepts("child", posPair[1].toString());
            ArrayList<Concept> synsets2 = new ArrayList<>();
            if (posPair[1] == POS.n){ // get all noun meanings
                synsets2.removeAll(synsets2);
                synsets2.addAll(rough_synsets2);
            }
            else if (posPair[1] == POS.v){ // get all verb meanings
                synsets2.removeAll(synsets2);
            }
            for(Concept synset1: synsets1) {
                for (Concept synset2: synsets2) {
                    Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
                    double score = relatedness.getScore();
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }
        }
        
        if (maxScore == -1D) {
            maxScore = 0.0;
        }
        
        if (maxScore > 1) {
            maxScore = 1.0;
        }
        
        //round off max score
        double max = Math.round(maxScore * 10000);
        max = max / 10000;

        wordNumber++;
        return wordNumber + ", " + max + ", " +  word1 + ", 1";
    }
    
    /**
     * Does a very naive and quick cleaning of chat log lingo
     * @param input string that will be cleaned
     * @return the cleaned string
     */
    public static String cleanString(String input){
        HashMap<String, String> dirtyWords = new HashMap<>();
        // contractions
        dirtyWords.put("cant", "cannot");
        dirtyWords.put("didnt", "did not");
        dirtyWords.put("wouldnt", "would not");
        dirtyWords.put("ill", "I will");
        // typos
        dirtyWords.put("cauz", "because");
        dirtyWords.put("cuz", "because");
        dirtyWords.put("hav", "have");
        dirtyWords.put("kno", "know");
        dirtyWords.put("gues", "guess");
        dirtyWords.put("suposed", "supposed");
        // lingo
        dirtyWords.put("2", "to");
        dirtyWords.put("preggerz", "pregnant");
        dirtyWords.put("bf", "boyfriend");
        dirtyWords.put("gf", "girlfriend");
        dirtyWords.put("b4", "before");
        dirtyWords.put("h8", "hate");
        dirtyWords.put("im", "I am");
        dirtyWords.put("i", "I");
        dirtyWords.put("r", "are");
        dirtyWords.put("ru", "are you");
        dirtyWords.put("u", "you");
        dirtyWords.put("U", "You");
        dirtyWords.put("uc", "you see");
        dirtyWords.put("ur", "your");
        dirtyWords.put("tho", "though");
        dirtyWords.put("ya", "yes");
        dirtyWords.put("Ya", "yes");
        dirtyWords.put("pics", "pictures");
        dirtyWords.put("pix", "pictures");
        dirtyWords.put("prof", "profile");
        dirtyWords.put("brb", "be right back");
        dirtyWords.put("Brb", "be right back");
        dirtyWords.put("mite", "might");
        
        for(HashMap.Entry<String, String> entry : dirtyWords.entrySet()){
            String dirty = entry.getKey();
            String clean = entry.getValue();
            input = input.replaceAll("\\b" + dirty + "\\b", clean);
        }
        
        return input;
    }
}
