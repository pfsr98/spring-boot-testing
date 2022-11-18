package pe.edu.unmsm.repo;

import java.util.List;

public interface QuestionRepo {
    List<String> findQuestionsByExamId(Long examId);

    void saveAll(List<String> questions);
}
