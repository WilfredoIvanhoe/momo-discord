
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Relationships {

    @SerializedName("genres")
    @Expose
    private Genres genres;
    @SerializedName("castings")
    @Expose
    private Castings castings;
    @SerializedName("installments")
    @Expose
    private Installments installments;
    @SerializedName("mappings")
    @Expose
    private Mappings mappings;
    @SerializedName("reviews")
    @Expose
    private Reviews reviews;
    @SerializedName("episodes")
    @Expose
    private Episodes episodes;
    @SerializedName("streamingLinks")
    @Expose
    private StreamingLinks streamingLinks;

    public Genres getGenres() {
        return genres;
    }

    public void setGenres(Genres genres) {
        this.genres = genres;
    }

    public Castings getCastings() {
        return castings;
    }

    public void setCastings(Castings castings) {
        this.castings = castings;
    }

    public Installments getInstallments() {
        return installments;
    }

    public void setInstallments(Installments installments) {
        this.installments = installments;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    public Reviews getReviews() {
        return reviews;
    }

    public void setReviews(Reviews reviews) {
        this.reviews = reviews;
    }

    public Episodes getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Episodes episodes) {
        this.episodes = episodes;
    }

    public StreamingLinks getStreamingLinks() {
        return streamingLinks;
    }

    public void setStreamingLinks(StreamingLinks streamingLinks) {
        this.streamingLinks = streamingLinks;
    }

}
