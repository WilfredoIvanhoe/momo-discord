
package io.ph.bot.rest.anime.kitsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RatingFrequencies {

    @SerializedName("0.0")
    @Expose
    private String zeroPointZero;
    @SerializedName("0.5")
    @Expose
    private String zeroPointFive;
    @SerializedName("1.0")
    @Expose
    private String onePointZero;
    @SerializedName("1.5")
    @Expose
    private String onePointFive;
    @SerializedName("2.0")
    @Expose
    private String twoPointZero;
    @SerializedName("2.5")
    @Expose
    private String twoPointFive;
    @SerializedName("3.0")
    @Expose
    private String threePointZero;
    @SerializedName("3.5")
    @Expose
    private String threePointFive;
    @SerializedName("4.0")
    @Expose
    private String fourPointZero;
    @SerializedName("4.5")
    @Expose
    private String fourPointFive;
    @SerializedName("5.0")
    @Expose
    private String fivePointZero;
    @SerializedName("nil")
    @Expose
    private String nil;

    public String getZeroPointZero() {
        return zeroPointZero;
    }

    public void setZeroPointZero(String zeroPointZero) {
        this.zeroPointZero = zeroPointZero;
    }

    public String getZeroPointFive() {
        return zeroPointFive;
    }

    public void setZeroPointFive(String zeroPointFive) {
        this.zeroPointFive = zeroPointFive;
    }

    public String getOnePointZero() {
        return onePointZero;
    }

    public void setOnePointZero(String onePointZero) {
        this.onePointZero = onePointZero;
    }

    public String getOnePointFive() {
        return onePointFive;
    }

    public void setOnePointFive(String onePointFive) {
        this.onePointFive = onePointFive;
    }

    public String getTwoPointZero() {
        return twoPointZero;
    }

    public void setTwoPointZero(String twoPointZero) {
        this.twoPointZero = twoPointZero;
    }

    public String getTwoPointFive() {
        return twoPointFive;
    }

    public void setTwoPointFive(String twoPointFive) {
        this.twoPointFive = twoPointFive;
    }

    public String getThreePointZero() {
        return threePointZero;
    }

    public void setThreePointZero(String threePointZero) {
        this.threePointZero = threePointZero;
    }

    public String getThreePointFive() {
        return threePointFive;
    }

    public void setThreePointFive(String threePointFive) {
        this.threePointFive = threePointFive;
    }

    public String getFourPointZero() {
        return fourPointZero;
    }

    public void setFourPointZero(String fourPointZero) {
        this.fourPointZero = fourPointZero;
    }

    public String getFourPointFive() {
        return fourPointFive;
    }

    public void setFourPointFive(String fourPointFive) {
        this.fourPointFive = fourPointFive;
    }

    public String getFivePointZero() {
        return fivePointZero;
    }

    public void setFivePointZero(String fivePointZero) {
        this.fivePointZero = fivePointZero;
    }

    public String getNil() {
        return nil;
    }

    public void setNil(String nil) {
        this.nil = nil;
    }

}
