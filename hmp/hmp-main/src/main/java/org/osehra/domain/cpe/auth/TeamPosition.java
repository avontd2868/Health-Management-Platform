package org.osehra.cpe.auth;

import java.io.Serializable;

import org.osehra.cpe.datetime.IntervalOfTime;
import org.osehra.cpe.datetime.PointInTime;

public class TeamPosition implements Serializable{

    private String position;
    private String teamName;
    private String teamPhone;
    private VistaUserClass role; // TODO: this is available from VistA, but not yet set anywhere

    private PointInTime effectiveDate;
    private PointInTime inactiveDate;

    public TeamPosition(String position, String teamName, String teamPhone, PointInTime effectiveDate, PointInTime inactiveDate) {
        this.position = position;
        this.teamName = teamName;
        this.teamPhone = teamPhone;
        this.effectiveDate = effectiveDate;
        this.inactiveDate = inactiveDate;
    }

    public String getPosition() {
        return position;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamPhone() {
        return teamPhone;
    }

    public VistaUserClass getRole() {
        return role;
    }

    public PointInTime getEffectiveDate() {
        return effectiveDate;
    }

    public PointInTime getInactiveDate() {
        return inactiveDate;
    }

    public IntervalOfTime getEffectiveDateRange() {
        if (getEffectiveDate() == null || getInactiveDate() == null) return null;
        return new IntervalOfTime(getEffectiveDate(), getInactiveDate());
    }
}
