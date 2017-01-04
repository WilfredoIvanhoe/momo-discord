
package io.ph.bot.rest.saucenao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Header {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("account_type")
    @Expose
    private String accountType;
    @SerializedName("short_limit")
    @Expose
    private String shortLimit;
    @SerializedName("long_limit")
    @Expose
    private String longLimit;
    @SerializedName("long_remaining")
    @Expose
    private Integer longRemaining;
    @SerializedName("short_remaining")
    @Expose
    private Integer shortRemaining;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("results_requested")
    @Expose
    private String resultsRequested;
    @SerializedName("search_depth")
    @Expose
    private String searchDepth;
    @SerializedName("minimum_similarity")
    @Expose
    private Double minimumSimilarity;
    @SerializedName("query_image_display")
    @Expose
    private String queryImageDisplay;
    @SerializedName("query_image")
    @Expose
    private String queryImage;
    @SerializedName("results_returned")
    @Expose
    private String resultsReturned;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getShortLimit() {
        return shortLimit;
    }

    public void setShortLimit(String shortLimit) {
        this.shortLimit = shortLimit;
    }

    public String getLongLimit() {
        return longLimit;
    }

    public void setLongLimit(String longLimit) {
        this.longLimit = longLimit;
    }

    public Integer getLongRemaining() {
        return longRemaining;
    }

    public void setLongRemaining(Integer longRemaining) {
        this.longRemaining = longRemaining;
    }

    public Integer getShortRemaining() {
        return shortRemaining;
    }

    public void setShortRemaining(Integer shortRemaining) {
        this.shortRemaining = shortRemaining;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getResultsRequested() {
        return resultsRequested;
    }

    public void setResultsRequested(String resultsRequested) {
        this.resultsRequested = resultsRequested;
    }

    public String getSearchDepth() {
        return searchDepth;
    }

    public void setSearchDepth(String searchDepth) {
        this.searchDepth = searchDepth;
    }

    public Double getMinimumSimilarity() {
        return minimumSimilarity;
    }

    public void setMinimumSimilarity(Double minimumSimilarity) {
        this.minimumSimilarity = minimumSimilarity;
    }

    public String getQueryImageDisplay() {
        return queryImageDisplay;
    }

    public void setQueryImageDisplay(String queryImageDisplay) {
        this.queryImageDisplay = queryImageDisplay;
    }

    public String getQueryImage() {
        return queryImage;
    }

    public void setQueryImage(String queryImage) {
        this.queryImage = queryImage;
    }

    public String getResultsReturned() {
        return resultsReturned;
    }

    public void setResultsReturned(String resultsReturned) {
        this.resultsReturned = resultsReturned;
    }

}
