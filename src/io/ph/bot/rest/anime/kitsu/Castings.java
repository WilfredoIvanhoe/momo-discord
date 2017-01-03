
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Castings {

    @SerializedName("links")
    @Expose
    private Links__ links;

    public Links__ getLinks() {
        return links;
    }

    public void setLinks(Links__ links) {
        this.links = links;
    }

}
