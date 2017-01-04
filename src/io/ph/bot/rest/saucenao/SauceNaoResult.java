
package io.ph.bot.rest.saucenao;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SauceNaoResult {

    @SerializedName("header")
    @Expose
    private Header header;
    @SerializedName("results")
    @Expose
    private List<Result> results = null;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

}
