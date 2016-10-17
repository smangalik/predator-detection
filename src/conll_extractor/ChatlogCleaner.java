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
import java.util.ArrayList;
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
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author sid
 */

public class ChatlogCleaner {
    
    /** Beginning Directory */
    private static final File dirtyDir = new File("/home/sid/Documents/CSE487/GeneralData");
    
    /** Ending Directory */
    private static final File cleanDir = new File("/home/sid/Documents/CSE487/ParsedData");
    
    /** StanfordCoreNLP Pipeline */
    private static StanfordCoreNLP pipeline;
    
    public static void main (String args[]) throws
            FileNotFoundException, IOException, SAXException, ParserConfigurationException{
        
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        pipeline = new StanfordCoreNLP(props);
        
        // All files to clean
        File[] dirtyFiles = null;
        
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
            
            // Get all of the <BODY> posts
            NodeList bodies = doc.getElementsByTagName("BODY");
            for (int i = 0; i < bodies.getLength(); i++){
                Node body = bodies.item(i);
                
                // String in the <BODY>
                String bodyString = body.getTextContent();
                
                //Clean up text lingo
                if(bodyString.matches(".*(emoti).*") || bodyString.matches(".*disconnected"))
                    continue;
                bodyString = bodyString.replace(" r ", " are ");
                bodyString = bodyString.replace(" u ", " you ");
                
                // Getting the CoNLL and Parse Trees
                Annotation document = new Annotation(bodyString);
                pipeline.annotate(document);
                List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
                for(CoreMap sentence: sentences) { 
                    
                    // CoNLL Form as a String
                    String conll = new CoNLLOutputter().print(document);
                    // Tree as a String
                    String tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class).toString();
                    
                    System.out.println(conll);
                    System.out.println(tree);
                    System.out.println("--------------------------------------------------");
                    
                    // append <SENTENCE> to <BODY>
                    Element sentenceNode = doc.createElement("SENTENCE");
                    sentenceNode.appendChild(doc.createTextNode(bodyString));
                    body.appendChild(sentenceNode);
                    
                    // append <CoNLL> to <SENTENCE>
                    Element conllNode = doc.createElement("CoNLL");
                    conllNode.appendChild(doc.createTextNode(conll));
                    sentenceNode.appendChild(conllNode);
                    
                    // append <TREE> to <SENTENCE>
                    Element treeNode = doc.createElement("TREE");
                    treeNode.appendChild(doc.createTextNode(tree));
                    sentenceNode.appendChild(treeNode);
                    
                }
                
            }
        }
        
    }
    
}
