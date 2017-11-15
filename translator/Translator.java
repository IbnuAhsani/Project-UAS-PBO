import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Simple program to translate a text passage from
 * one language to another on a word-by-word basis.
 * 
 * @author David Matuszek 
 * @version 1.0
 */
public class Translator extends Frame {
    Panel textPanel = new Panel();
    Panel buttonPanel = new Panel();
    Button loadDictionaryButton = new Button("Load dictionary");
    Button translateButton = new Button("Translate");
    TextArea sourceText = new TextArea();
    TextArea targetText = new TextArea();
    
    Hashtable dictionary = new Hashtable(100);
    Pattern letter = Pattern.compile("[-'a-zA-Zäöü]");
    String punctuation = "(\\p{Punct})";
    String word = "\\b([-'a-zA-Zäöü]+)\\b";
    String allTheRest = "(.*)";
    String whitespace = "(\\s++)";
    Pattern dictionaryEntry = Pattern.compile("\\s*" + word + "\\s*" + "=" +
                                              "\\s*" + allTheRest);
    Pattern something = Pattern.compile(word + "|" + punctuation +
                                        "|" + whitespace);
    Pattern blankLine = Pattern.compile("^\\s*$");
    Pattern terminator = Pattern.compile("[\\.\\?\\!]");
    
    /**
     * Creates a Translator object and tells it to create a GUI.
     */
    public static void main(String args[]) {
        Translator translator = new Translator();
        translator.createGui();
    }

    /**
     * Creates and displays the GUI for the Translator program;
     * all the actual work is done by the <code>loadDictionary</code>
     * and <code>translate</code> methods, which are invoked by
     * button presses in the GUI.
     */
    void createGui() {
        // Set various GUI parameters
        setTitle("Translation program by David Matuszek");
        setSize(800,500);
        setBackground(new Color(255, 255, 225));
        setLayout(new BorderLayout());

        // Lay out the GUI
        add(textPanel, BorderLayout.CENTER);
            textPanel.setLayout(new GridLayout(1, 2));
            textPanel.add(sourceText);
            textPanel.add(targetText);
        add(buttonPanel, BorderLayout.SOUTH);
            buttonPanel.add(loadDictionaryButton);
            buttonPanel.add(translateButton);
            translateButton.setEnabled(false);
 
        // Add action listeners
        loadDictionaryButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadDictionary();
                    translateButton.setEnabled(true);
                }
            });
        translateButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    translate();
                }
            });
            
        // Make the new Frame visible
        setVisible(true);
        
        // Implement the window's Close icon
        Close c = new Close();
        addWindowListener(c);
    }
    
    /**
     * Loads in a dictionary. Source language input is
     * lowercased, but target language input is left as is.
     */
    void loadDictionary() {
        LineReader r = new LineReader("Read what dictionary?");
        if (r == null) return;
        String line = r.readLine();
        while (line != null) {
            Matcher m = dictionaryEntry.matcher(line);
            if (m.lookingAt()) {
                String source = m.group(1).toLowerCase();
                String target = m.group(2).trim();
                dictionary.put(source, target);
                sourceText.append(source + " = " + target + "\n");
            }
            line = r.readLine();
        }
        r.close();
    }
    
    /**
     * Reads in and translates a text passage according to a
     * previously loaded dictionary.
     */
    void translate() {
        // Get file to translate; blank out text areas
        LineReader r = new LineReader("Read what text passage?");
        if (r == null) return;
        sourceText.setText("");
        targetText.setText("");
        
        String translation;
        boolean capitalNeeded = true;
        
        String line = r.readLine();
        while (line != null) {
            // Find the next "thing" from the input passage
            Matcher m = something.matcher(line);
            String thingFound;
            while (m.lookingAt()) {
                thingFound = m.group(0);
                if (m.group(1) != null) {
                    // Found a word -- translate it
                    String lowerCaseWord = m.group(1).toLowerCase();
                    translation = (String)dictionary.get(lowerCaseWord);
                    if (translation != null) {
                        appendText(thingFound,
                                   capitalize(translation, capitalNeeded));
                    } else {
                        appendText(thingFound, thingFound);
                    }
                    capitalNeeded = false;
                    
                } else if (m.group(2) != null) {
                    // Found punctuation -- copy and check if end of sentence
                    appendText(m.group(), m.group());
                    if (terminator.matcher(thingFound).matches()) {
                        capitalNeeded = true;
                    }
                    
                } else {
                    // Found whitespace -- just copy it
                    appendText(m.group(), m.group());
                }
                line = line.substring(thingFound.length());
                m.reset(line);
            } // Finish processing this line and read in another
            line = r.readLine();
            appendText("\n", "\n");
            if (line != null && blankLine.matcher(line).matches()) {
                capitalNeeded = true;
            }
        }
        // Finished processing all lines
        r.close();
    }
    
    /**
     * Returns a String similar to the input String, but with the
     * first character capitalized, if the second parameter is
     * <code>true</code>; otherwise it returns the String unchanged.
     * 
     * @param input The String to be (possibly) capitalized.
     * @param doIt Tells whether to capitalize the input String.
     * @return If <code>doIt</code> is true, creates and returns
     *         a modified String, else returns the original String.
     */
    String capitalize(String input, boolean doIt) {
        if (doIt) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }
        else return input;
    }
    
    /**
     * Convenience routine to append the first argument to the
     * <code>sourceText</code> TextArea and the second argument
     * to the <code>targetText</code> TextArea.
     * 
     * @param source The String to append to the
     *        <code>sourceText</code> TextArea
     * @param target The String to append to the
     *        <code>targetText</code> TextArea
     */
    void appendText(String source, String target) {
        sourceText.append(source);
        targetText.append(target);
    }
    
    /**
     * An inner class to quit the program when the window's
     * close box is clicked. Incorrectly considered to be an
     * error by BlueJ.
     */
    class Close extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            dispose();
            System.exit(0);  // Treated as an error by BlueJ
        }
    }
}
