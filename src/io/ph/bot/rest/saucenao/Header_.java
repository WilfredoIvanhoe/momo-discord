
package io.ph.bot.rest.saucenao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Header_ {

    @SerializedName("similarity")
    @Expose
    private String similarity;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("index_id")
    @Expose
    private Integer indexId;
    @SerializedName("index_name")
    @Expose
    private String indexName;

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Integer getIndexId() {
        return indexId;
    }

    public void setIndexId(Integer indexId) {
        this.indexId = indexId;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

}
