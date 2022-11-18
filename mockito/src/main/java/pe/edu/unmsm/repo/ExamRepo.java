package pe.edu.unmsm.repo;

import pe.edu.unmsm.model.Exam;

import java.util.List;

public interface ExamRepo {
    Exam save(Exam exam);

    List<Exam> findAll();
}
