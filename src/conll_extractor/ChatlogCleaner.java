package conll_extractor;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author sid
 */

public class ChatlogCleaner {
    
    public static void main (String args[]) throws FileNotFoundException, IOException, SAXException{
        
        // All files to clean
        File[] dirtyFiles = null;
        
        // Beginning Directory
        File dirtyDir = new File("/home/sid/Documents/CSE487/GeneralData");
        
        /** Ending Directory */
        File cleanDir = new File("/home/sid/Documents/CSE487/ParsedData");
        
        // Properties of the pipeline
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        
        // StanfordCoreNLP Pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        //Select your dirty files
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(dirtyDir);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            dirtyFiles = fileChooser.getSelectedFiles();
        }
        
        // for each dirty file make a clean with CoNLL Form and Parse Tree
        for(File dirtyFile : dirtyFiles){
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(dirtyFile);
            
            String conll; // The ConLL String
            String tree;  // The Parse Tree String
            
            // Getting the CoNLL and Parse Trees
            Annotation document = new Annotation(post);
            pipeline.annotate(document);
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            for(CoreMap sentence: sentences) { // gives all sentences
                
                // CoNLL Form as a String
                conll = new CoNLLOutputter().print(document);
                
                // CoNLL Form as a String
                tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class).toString();
                
            }
            
        }
        
    }
    
}
