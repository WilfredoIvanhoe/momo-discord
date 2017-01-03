
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Episodes {

    @SerializedName("links")
    @Expose
    private Links______ links;

    public Links______ getLinks() {
        return links;
    }

    public void setLinks(Links______ links) {
        this.links = links;
    }

}
