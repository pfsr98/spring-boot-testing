package pe.edu.unmsm.service;

import pe.edu.unmsm.model.Exam;
import pe.edu.unmsm.repo.ExamRepo;
import pe.edu.unmsm.repo.QuestionRepo;

import java.util.List;

public class ExamServiceImpl implements ExamService {
    private final ExamRepo examRepo;
    private final QuestionRepo questionRepo;

    public ExamServiceImpl(ExamRepo examRepo, QuestionRepo questionRepo) {
        this.examRepo = examRepo;
        this.questionRepo = questionRepo;
    }

    @Override
    public Exam findExamByName(String name) {
        return examRepo.findAll()
                .stream()
                .filter(exam -> exam.getName().contains(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Exam findExamByNameWithQuestions(String name) {
        Exam exam = findExamByName(name);
        if (exam == null) return null;
        List<String> questions = questionRepo.findQuestionsByExamId(exam.getId());
        exam.setQuestions(questions);
        return exam;
    }

    @Override
    public Exam save(Exam exam) {
        if (!exam.getQuestions().isEmpty()) questionRepo.saveAll(exam.getQuestions());
        return examRepo.save(exam);
    }
}
