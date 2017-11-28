package main;

import com.google.gson.Gson;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashSet;

public class Main {
    private static String DICTIONARY_API_URL = "http://api.pearson.com/v2/dictionaries/entries?part_of_speech=";
    private static String PART_OF_SPEECH = "noun";
    private static String WOW_API_URL = "https://worldofwarcraft.com/en-us/character/";
    private static String WOW_SERVER = "illidan";
    private static HashSet<String> potentialNames = new HashSet<String>();

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 2000; i++) {
            getPotentialAvailableNames(getWords(i));
        }

    }
    private static Words getWords(int offset) throws IOException {

        offset = offset * 25;
        String OFFSET_DICTIONARY_API_URL = DICTIONARY_API_URL + PART_OF_SPEECH + "&offset=" + offset + "&limit=25";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(OFFSET_DICTIONARY_API_URL);
        CloseableHttpResponse response1 = httpclient.execute(httpGet);
        HttpEntity httpEntity = response1.getEntity();
        String responseString = EntityUtils.toString(httpEntity, "UTF-8");

        Gson gson = new Gson();
        Words words = gson.fromJson(responseString, Words.class);

        return words;
    }

    private static void getPotentialAvailableNames(Words words) throws IOException {
        for (Words.Result result : words.getResults()) {
            String word = result.getHeadword();
            if (!potentialNames.contains(word) && isAlpha(word) && word.length() < 10) {
                String armoryUrl = WOW_API_URL + WOW_SERVER + "/" + word;
                UrlValidator urlValidator = new UrlValidator();
                if (urlValidator.isValid(armoryUrl)) {
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpGet httpGet = new HttpGet(armoryUrl);
                    CloseableHttpResponse armoryResponse = httpclient.execute(httpGet);

                    //404 Response means the name is potentially available
                    if (armoryResponse.getStatusLine().getStatusCode() == 404) {
                        potentialNames.add(word);
                        System.out.println(word);
                    }
                }
            }
        }
    }

    public static boolean isAlpha(String name) {
        return name.matches("[a-zA-Z]+");
    }
}
