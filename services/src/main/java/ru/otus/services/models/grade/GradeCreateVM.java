package ru.otus.services.models.grade;

import ru.otus.repository.entities.GradeEnum;

public class GradeCreateVM {
    private GradeEnum gradeType;

    public GradeCreateVM() {
    }

    public GradeCreateVM(GradeEnum gradeType) {
        this.gradeType = gradeType;
    }

    public GradeEnum getGradeType() {
        return gradeType;
    }

    public void setGradeType(GradeEnum gradeType) {
        this.gradeType = gradeType;
    }
}
