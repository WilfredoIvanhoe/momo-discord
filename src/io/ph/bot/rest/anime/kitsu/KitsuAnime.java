
package io.ph.bot.rest.anime.kitsu;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KitsuAnime {

    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("links")
    @Expose
    private Links________ links;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Links________ getLinks() {
        return links;
    }

    public void setLinks(Links________ links) {
        this.links = links;
    }

}
