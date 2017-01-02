
package io.ph.bot.rest.osu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OsuUser {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("count300")
    @Expose
    private String count300;
    @SerializedName("count100")
    @Expose
    private String count100;
    @SerializedName("count50")
    @Expose
    private String count50;
    @SerializedName("playcount")
    @Expose
    private String playcount;
    @SerializedName("ranked_score")
    @Expose
    private String rankedScore;
    @SerializedName("total_score")
    @Expose
    private String totalScore;
    @SerializedName("pp_rank")
    @Expose
    private String ppRank;
    @SerializedName("level")
    @Expose
    private String level;
    @SerializedName("pp_raw")
    @Expose
    private String ppRaw;
    @SerializedName("accuracy")
    @Expose
    private String accuracy;
    @SerializedName("count_rank_ss")
    @Expose
    private String countRankSs;
    @SerializedName("count_rank_s")
    @Expose
    private String countRankS;
    @SerializedName("count_rank_a")
    @Expose
    private String countRankA;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("pp_country_rank")
    @Expose
    private String ppCountryRank;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCount300() {
        return count300;
    }

    public void setCount300(String count300) {
        this.count300 = count300;
    }

    public String getCount100() {
        return count100;
    }

    public void setCount100(String count100) {
        this.count100 = count100;
    }

    public String getCount50() {
        return count50;
    }

    public void setCount50(String count50) {
        this.count50 = count50;
    }

    public String getPlaycount() {
        return playcount;
    }

    public void setPlaycount(String playcount) {
        this.playcount = playcount;
    }

    public String getRankedScore() {
        return rankedScore;
    }

    public void setRankedScore(String rankedScore) {
        this.rankedScore = rankedScore;
    }

    public String getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore;
    }

    public String getPpRank() {
        return ppRank;
    }

    public void setPpRank(String ppRank) {
        this.ppRank = ppRank;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPpRaw() {
        return ppRaw;
    }

    public void setPpRaw(String ppRaw) {
        this.ppRaw = ppRaw;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getCountRankSs() {
        return countRankSs;
    }

    public void setCountRankSs(String countRankSs) {
        this.countRankSs = countRankSs;
    }

    public String getCountRankS() {
        return countRankS;
    }

    public void setCountRankS(String countRankS) {
        this.countRankS = countRankS;
    }

    public String getCountRankA() {
        return countRankA;
    }

    public void setCountRankA(String countRankA) {
        this.countRankA = countRankA;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPpCountryRank() {
        return ppCountryRank;
    }

    public void setPpCountryRank(String ppCountryRank) {
        this.ppCountryRank = ppCountryRank;
    }
}
