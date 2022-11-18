package pe.edu.unmsm.repo;

import pe.edu.unmsm.Data;

import java.util.List;

public class QuestionRepoImpl implements QuestionRepo{
    @Override
    public List<String> findQuestionsByExamId(Long examId) {
        System.out.println("QuestionRepoImpl.findQuestionsByExamId");
        return Data.QUESTIONS;
    }

    @Override
    public void saveAll(List<String> questions) {
        System.out.println("QuestionRepoImpl.saveAll");
    }
}
