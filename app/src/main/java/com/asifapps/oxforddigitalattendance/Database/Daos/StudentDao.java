package com.asifapps.oxforddigitalattendance.Database.Daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.asifapps.oxforddigitalattendance.Database.Entities.Student;

import java.util.List;

@Dao
public interface StudentDao {
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    public void insertStudent(Student student);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    public void insertStudents(List<Student> students);

    @Update
    public void updateStudent(Student student);

    @Update
    public void updateStudents(List<Student> students);

    @Delete
    public void deleteStudent(Student student);

    @Query("Select StdId From Student")
    public List<Integer> getAllStudentsId();

    @Query("Select * From Student order by Rno ASC")
    public List<Student> getAllStudents();

    @Query("Select * from Student where StdId = :stId")
    public Student getStudent(Integer stId);

    @Query("Select * from Student where Rno = :rno And Class = :cl")
    public Student getStudent(String rno, String cl);

    @Query("Select * from Student where Class = :Class order by Rno ASC")
    public List<Student> getStudents(String Class);

    @Query("Select StdId From Student where Rno = :rno and Class = :Class")
    public int getStdId(String rno, String Class);

}
