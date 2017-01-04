
package io.ph.bot.rest.saucenao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("header")
    @Expose
    private Header_ header;
    @SerializedName("data")
    @Expose
    private Data data;

    public Header_ getHeader() {
        return header;
    }

    public void setHeader(Header_ header) {
        this.header = header;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
