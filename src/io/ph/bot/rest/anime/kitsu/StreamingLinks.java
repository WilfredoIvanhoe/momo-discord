
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StreamingLinks {

    @SerializedName("links")
    @Expose
    private Links_______ links;

    public Links_______ getLinks() {
        return links;
    }

    public void setLinks(Links_______ links) {
        this.links = links;
    }

}
