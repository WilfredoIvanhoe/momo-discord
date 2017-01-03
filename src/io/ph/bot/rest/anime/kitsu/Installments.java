
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Installments {

    @SerializedName("links")
    @Expose
    private Links___ links;

    public Links___ getLinks() {
        return links;
    }

    public void setLinks(Links___ links) {
        this.links = links;
    }

}
