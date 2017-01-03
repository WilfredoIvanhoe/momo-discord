
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Reviews {

    @SerializedName("links")
    @Expose
    private Links_____ links;

    public Links_____ getLinks() {
        return links;
    }

    public void setLinks(Links_____ links) {
        this.links = links;
    }

}
