package com.asifapps.oxforddigitalattendance.Interfaces;

import com.asifapps.oxforddigitalattendance.Database.Entities.Student;

public interface IStudentClickEvent {
    void onClickDelStudent(Student std);
    void onClickEditStudent(Student std);
}
