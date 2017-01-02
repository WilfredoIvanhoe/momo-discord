
package io.ph.bot.rest.pokemon.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionDetail_ {

    @SerializedName("max_chance")
    @Expose
    private Integer maxChance;
    @SerializedName("encounter_details")
    @Expose
    private List<EncounterDetail> encounterDetails = null;
    @SerializedName("version")
    @Expose
    private Version__ version;

    public Integer getMaxChance() {
        return maxChance;
    }

    public void setMaxChance(Integer maxChance) {
        this.maxChance = maxChance;
    }

    public List<EncounterDetail> getEncounterDetails() {
        return encounterDetails;
    }

    public void setEncounterDetails(List<EncounterDetail> encounterDetails) {
        this.encounterDetails = encounterDetails;
    }

    public Version__ getVersion() {
        return version;
    }

    public void setVersion(Version__ version) {
        this.version = version;
    }

}
