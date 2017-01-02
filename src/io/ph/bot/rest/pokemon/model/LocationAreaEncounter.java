
package io.ph.bot.rest.pokemon.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationAreaEncounter {

    @SerializedName("location_area")
    @Expose
    private LocationArea locationArea;
    @SerializedName("version_details")
    @Expose
    private List<VersionDetail_> versionDetails = null;

    public LocationArea getLocationArea() {
        return locationArea;
    }

    public void setLocationArea(LocationArea locationArea) {
        this.locationArea = locationArea;
    }

    public List<VersionDetail_> getVersionDetails() {
        return versionDetails;
    }

    public void setVersionDetails(List<VersionDetail_> versionDetails) {
        this.versionDetails = versionDetails;
    }

}
