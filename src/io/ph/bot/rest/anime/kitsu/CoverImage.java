
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CoverImage {

    @SerializedName("small")
    @Expose
    private String small;
    @SerializedName("large")
    @Expose
    private String large;
    @SerializedName("original")
    @Expose
    private String original;

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

}
