
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mappings {

    @SerializedName("links")
    @Expose
    private Links____ links;

    public Links____ getLinks() {
        return links;
    }

    public void setLinks(Links____ links) {
        this.links = links;
    }

}
