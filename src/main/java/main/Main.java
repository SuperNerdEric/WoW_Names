package main;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
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

    private static ArrayList<String> getWords(int offset) throws IOException {
        ArrayList<String> words = new ArrayList<String>();

        offset = offset * 25;
        String OFFSET_DICTIONARY_API_URL = DICTIONARY_API_URL + PART_OF_SPEECH + "&offset=" + offset + "&limit=25";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(OFFSET_DICTIONARY_API_URL);
        CloseableHttpResponse response1 = httpclient.execute(httpGet);
        HttpEntity httpEntity = response1.getEntity();
        String responseString = EntityUtils.toString(httpEntity, "UTF-8");
        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("results");
        for (int i = 0; i < jsonArray.size(); i++) {
            String word = jsonArray.get(i).getAsJsonObject().get("headword").getAsString();
            words.add(word);
        }
        return words;
    }

    private static ArrayList<String> getPotentialAvailableNames(ArrayList<String> words) throws IOException {
        ArrayList<String> potentialAvailableNames = new ArrayList<String>();
        for (int i = 0; i < words.size(); i++) {
            if(isAlpha(words.get(i)) && words.get(i).length()<10) {
                String ARMORY_URL = WOW_API_URL + WOW_SERVER + "/" + words.get(i);
                UrlValidator urlValidator = new UrlValidator();
                if (urlValidator.isValid(ARMORY_URL)) {
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpGet httpGet = new HttpGet(ARMORY_URL);
                    CloseableHttpResponse response1 = httpclient.execute(httpGet);
                    if (response1.getStatusLine().getStatusCode() == 404) {
                        if(!potentialNames.contains(words.get(i))){
                            potentialNames.add(words.get(i));
                            potentialAvailableNames.add(words.get(i));
                            System.out.println(words.get(i));
                        }

                    }
                }
            }
        }

        return potentialAvailableNames;
    }

    public static boolean isAlpha(String name) {
        return name.matches("[a-zA-Z]+");
    }
}
