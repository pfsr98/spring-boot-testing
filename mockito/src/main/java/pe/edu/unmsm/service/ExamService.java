package pe.edu.unmsm.service;

import pe.edu.unmsm.model.Exam;

public interface ExamService {
    Exam findExamByName(String name);

    Exam findExamByNameWithQuestions(String name);

    Exam save(Exam exam);
}
