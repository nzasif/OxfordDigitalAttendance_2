package com.asifapps.oxforddigitalattendance.Database.Daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.asifapps.oxforddigitalattendance.Database.Entities.Admin;

@Dao
public interface LoginDao {
    @Query("Select * from admin where Password = :password and UserName = :userName")
    public Admin getAdmin(String userName, String password);

    @Query("Select * from admin")
    public Admin getAdmin();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void updateAdmin(Admin admin);
}
