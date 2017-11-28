package main;

import java.util.ArrayList;

public class Words {
    private ArrayList<Result> results;

    public ArrayList<Result> getResults() {
        return results;
    }

    public void setResults(ArrayList<Result> results) {
        this.results = results;
    }

    public class Result {
        public String headword;

        public String getHeadword() {
            return headword;
        }

        public void setHeadword(String headword) {
            this.headword = headword;
        }
    }
}
