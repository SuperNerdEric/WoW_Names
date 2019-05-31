package main;

import com.google.common.collect.Streams;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

public class Main {
    private static final String DICTIONARY_PATH = "src/main/resources/dict";
    private static final String WOW_API_URL = "https://worldofwarcraft.com/en-us/character/";
    public static final String SERVER_PROMPT = "Server name (e.g. bleeding-hollow): ";
    public static final String PART_OF_SPEECH_PROMPT = "Part of speech (noun, verb, adjective, adverb): ";

    public static void main(String[] args) throws IOException {
        getNames();
    }

    private static void getNames() throws IOException {
        File file = new File(DICTIONARY_PATH);
        IDictionary dict = new Dictionary(file);
        dict.open();


        Scanner scan = new Scanner(System.in);
        System.out.print(SERVER_PROMPT);
        String serverName = scan.next();
        System.out.print(PART_OF_SPEECH_PROMPT);
        String partOfSpeechString = scan.next().toUpperCase();

        if (validPartOfSpeech(partOfSpeechString)) {
            POS partOfSpeech = POS.valueOf(partOfSpeechString);
            Iterator<IIndexWord> wordIterator = dict.getIndexWordIterator(partOfSpeech);

            Streams.stream(wordIterator).forEach(word -> {
                if (nameIsPotentiallyAvailable(serverName, word.getLemma())) {
                    System.out.println(word.getLemma());
                }
            });
        } else {
            System.out.println("Invalid part of speech: " + partOfSpeechString);
        }
    }

    /**
     * Check if a name is potentially available for given server. A name is considered potentially if it consists of
     * only letters, is between 2 and 12 characters long, and returns a 404 from the WoW API.
     *
     * @param serverName
     * @param name
     * @return
     * @throws IOException
     */
    private static boolean nameIsPotentiallyAvailable(String serverName, String name) {
        String armoryUrl = WOW_API_URL + serverName + "/" + name;
        UrlValidator urlValidator = new UrlValidator();
        if (StringUtils.isAlpha(name) && name.length() > 1 && name.length() < 13 && urlValidator.isValid(armoryUrl)) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(armoryUrl);
            CloseableHttpResponse armoryResponse = null;
            try {
                armoryResponse = httpclient.execute(httpGet);
            } catch (IOException e) {
                System.out.println("Failed to call WoW API");
                e.printStackTrace();
                return false;
            }

            //404 Response means the name is potentially available
            if (armoryResponse.getStatusLine().getStatusCode() == 404) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate part of speech
     * @param partOfSpeechString
     * @return
     */
    private static boolean validPartOfSpeech(String partOfSpeechString) {
        try {
            POS.valueOf(partOfSpeechString);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
