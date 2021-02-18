package com.asifapps.oxforddigitalattendance.Database.Entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Admin {
    @PrimaryKey
    public int AdminId;
    public String UserName;
    public String Password;

    public int passwordUsage;
}
