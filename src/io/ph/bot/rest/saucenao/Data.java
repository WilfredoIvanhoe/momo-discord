
package io.ph.bot.rest.saucenao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("pixiv_id")
    @Expose
    private Integer pixivId;
    @SerializedName("member_name")
    @Expose
    private String memberName;
    @SerializedName("member_id")
    @Expose
    private Integer memberId;
    @SerializedName("danbooru_id")
    @Expose
    private Integer danbooruId;
    @SerializedName("gelbooru_id")
    @Expose
    private Integer gelbooruId;
    @SerializedName("sankaku_id")
    @Expose
    private Integer sankakuId;
    @SerializedName("anime-pictures_id")
    @Expose
    private Integer animePicturesId;
    @SerializedName("creator")
    @Expose
    private String creator;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("drawr_id")
    @Expose
    private Integer drawrId;
    @SerializedName("shutterstock_id")
    @Expose
    private Integer shutterstockId;
    @SerializedName("anidb_aid")
    @Expose
    private Integer anidbAid;
    @SerializedName("part")
    @Expose
    private String part;
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPixivId() {
        return pixivId;
    }

    public void setPixivId(Integer pixivId) {
        this.pixivId = pixivId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getDanbooruId() {
        return danbooruId;
    }

    public void setDanbooruId(Integer danbooruId) {
        this.danbooruId = danbooruId;
    }

    public Integer getGelbooruId() {
        return gelbooruId;
    }

    public void setGelbooruId(Integer gelbooruId) {
        this.gelbooruId = gelbooruId;
    }

    public Integer getSankakuId() {
        return sankakuId;
    }

    public void setSankakuId(Integer sankakuId) {
        this.sankakuId = sankakuId;
    }

    public Integer getAnimePicturesId() {
        return animePicturesId;
    }

    public void setAnimePicturesId(Integer animePicturesId) {
        this.animePicturesId = animePicturesId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getDrawrId() {
        return drawrId;
    }

    public void setDrawrId(Integer drawrId) {
        this.drawrId = drawrId;
    }

	public Integer getShutterstockId() {
		return shutterstockId;
	}

	public void setShutterstockId(Integer shutterstockId) {
		this.shutterstockId = shutterstockId;
	}

	public Integer getAnidbAid() {
		return anidbAid;
	}

	public void setAnidbAid(Integer anidbAid) {
		this.anidbAid = anidbAid;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}

}
