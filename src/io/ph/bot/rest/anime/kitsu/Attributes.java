
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attributes {

    @SerializedName("slug")
    @Expose
    private String slug;
    @SerializedName("synopsis")
    @Expose
    private String synopsis;
    @SerializedName("coverImageTopOffset")
    @Expose
    private Integer coverImageTopOffset;
    @SerializedName("titles")
    @Expose
    private Titles titles;
    @SerializedName("canonicalTitle")
    @Expose
    private String canonicalTitle;
    @SerializedName("abbreviatedTitles")
    @Expose
    private Object abbreviatedTitles;
    @SerializedName("averageRating")
    @Expose
    private Double averageRating;
    @SerializedName("ratingFrequencies")
    @Expose
    private RatingFrequencies ratingFrequencies;
    @SerializedName("startDate")
    @Expose
    private String startDate;
    @SerializedName("endDate")
    @Expose
    private String endDate;
    @SerializedName("posterImage")
    @Expose
    private PosterImage posterImage;
    @SerializedName("coverImage")
    @Expose
    private CoverImage coverImage;
    @SerializedName("episodeCount")
    @Expose
    private Integer episodeCount;
    @SerializedName("episodeLength")
    @Expose
    private Integer episodeLength;
    @SerializedName("showType")
    @Expose
    private String showType;
    @SerializedName("youtubeVideoId")
    @Expose
    private String youtubeVideoId;
    @SerializedName("ageRating")
    @Expose
    private String ageRating;
    @SerializedName("ageRatingGuide")
    @Expose
    private String ageRatingGuide;
    @SerializedName("nsfw")
    @Expose
    private Boolean nsfw;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Integer getCoverImageTopOffset() {
        return coverImageTopOffset;
    }

    public void setCoverImageTopOffset(Integer coverImageTopOffset) {
        this.coverImageTopOffset = coverImageTopOffset;
    }

    public Titles getTitles() {
        return titles;
    }

    public void setTitles(Titles titles) {
        this.titles = titles;
    }

    public String getCanonicalTitle() {
        return canonicalTitle;
    }

    public void setCanonicalTitle(String canonicalTitle) {
        this.canonicalTitle = canonicalTitle;
    }

    public Object getAbbreviatedTitles() {
        return abbreviatedTitles;
    }

    public void setAbbreviatedTitles(Object abbreviatedTitles) {
        this.abbreviatedTitles = abbreviatedTitles;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public RatingFrequencies getRatingFrequencies() {
        return ratingFrequencies;
    }

    public void setRatingFrequencies(RatingFrequencies ratingFrequencies) {
        this.ratingFrequencies = ratingFrequencies;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public PosterImage getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(PosterImage posterImage) {
        this.posterImage = posterImage;
    }

    public CoverImage getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(CoverImage coverImage) {
        this.coverImage = coverImage;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public Integer getEpisodeLength() {
        return episodeLength;
    }

    public void setEpisodeLength(Integer episodeLength) {
        this.episodeLength = episodeLength;
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getAgeRatingGuide() {
        return ageRatingGuide;
    }

    public void setAgeRatingGuide(String ageRatingGuide) {
        this.ageRatingGuide = ageRatingGuide;
    }

    public Boolean getNsfw() {
        return nsfw;
    }

    public void setNsfw(Boolean nsfw) {
        this.nsfw = nsfw;
    }

}
