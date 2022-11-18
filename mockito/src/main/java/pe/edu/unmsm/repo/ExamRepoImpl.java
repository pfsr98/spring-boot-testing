package pe.edu.unmsm.repo;

import pe.edu.unmsm.Data;
import pe.edu.unmsm.model.Exam;

import java.util.List;

public class ExamRepoImpl implements ExamRepo{
    @Override
    public Exam save(Exam exam) {
        System.out.println("ExamRepoImpl.save");
        return Data.EXAM;
    }

    @Override
    public List<Exam> findAll() {
        System.out.println("ExamRepoImpl.findAll");
        return Data.EXAMS;
    }
}
