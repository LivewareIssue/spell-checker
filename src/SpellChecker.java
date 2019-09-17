import model.RadixTree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.*;

/**
 * A class used to spell-check text against a dictionary.
 */
public final class SpellChecker {

    public static void main(String...args){

        if (args.length == 1){

            // If only the document-to-check's path was specified, use the standard dictionary.
            SpellChecker spellcheck = standard();
            spellcheck.checkDocument(args[0]);

        } else if (args.length == 2) {

            SpellChecker spellcheck = custom(args[1]);
            spellcheck.checkDocument(args[0]);

        } else {

            // Incorrect number of arguments / invalid arguments supplied.
            System.out.println(usage);
            System.exit(2);
        }
    }

    private final RadixTree
            dictionary;

    private final static String
            fileNotFoundError       = "Error: The specified file could not be found",
            standardDictionaryPath  =  "/usr/share/dict/words",
            usage                   = "Usage: java SpellChecker [document [dictionary]]";

    /**
     * Creates a SpellChecker instance from a given dictionary.
     *
     * @param dictionary The dictionary to check files against.
     */
    private SpellChecker(RadixTree dictionary){
        this.dictionary  = dictionary;
    }

    /**
     * Gets a SpellChecker instance based on the standard dictionary.
     *
     * @return A SpellChecker instance based on the standard dictionary.
     */
    private static SpellChecker standard(){
        return custom(standardDictionaryPath);
    }

    /**
     * Gets a SpellChecker instance based on the specified dictioanry file.
     *
     * @param path The path of the dictionary file to use.
     * @return A SpellChecker instance based on the specified dictioanry file.
     */
    private static SpellChecker custom(String path) {

        RadixTree dictionary = new RadixTree.Node();

        // Instead of finally cause to close the resource, Java 7 introduced syntax to automatically close
        // instances of steams/files that implements the AutoCloseable interface.
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {

            // While the file is not empty, insert each line (assumed to be a word) into a dictioanry.
            String word;
            while ((word = bufferedReader.readLine()) != null) {
                dictionary.insert(word.toLowerCase());
            }

        } catch (FileNotFoundException e) {
            System.out.println(fileNotFoundError);
            System.exit(2);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }

        return new SpellChecker(dictionary);
    }

    /**
     * Checks a list of strings for spelling mistakes.
     * @param text a list of strings to check for spelling mistakes.
     */
    private void check(ArrayList<String> text){

        // true is the file contains no errors.
        boolean correct = true;
        for (int lineNumber = 0; lineNumber < text.size(); lineNumber++){

            // split the line into words at punctuation/word boundaries.
            // trim whitespace of each word and convert it to lowercase.
            // filter out the words.
            ArrayList<String> words = Stream
                    .of(text.get(lineNumber).split("[\\p{Punct}\\s+]"))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> s.matches("[A-Za-z']+"))
                    .collect(Collectors.toCollection(ArrayList::new));

            // If the line was empty, continue.
            if (words.isEmpty()) continue;

            // filter the words over whether they are contained within the dictionary.
            ArrayList<String> incorrect = words
                    .stream()
                    .filter(w -> !dictionary.contains(w))
                    .collect(Collectors.toCollection(ArrayList::new));

            // Do not print the line if there were no mistakes.
            if (incorrect.isEmpty()) continue;

            // At least one mistake was found in the document.
            correct = false;

            // Print out the line number and line containing mistakes,
            System.out.println(String.format("%d %s\n", lineNumber, text.get(lineNumber)));

            // Print out each mis-spelt word, seperated by whitespace.
            System.out.println(String.join(" ", incorrect) + "\n");
        }

        // No spelling mistakes were made.
        if (correct) System.exit(0);

        // At least one spelling mistake was made.
        System.exit(1);
    }

    /**
     * Checks a file for spelling mistakes.
     *
     * @param path The path of the file to check.
     */
    private void checkDocument(String path){
        ArrayList<String> text = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine();

            while (line != null){
                text.add(line);
                line = br.readLine();
            }

        } catch (FileNotFoundException f) {
            System.out.println(fileNotFoundError);
            System.exit(2);

        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        check(text);
    }
}
