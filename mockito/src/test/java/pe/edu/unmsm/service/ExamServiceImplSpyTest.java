package pe.edu.unmsm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.unmsm.model.Exam;
import pe.edu.unmsm.repo.ExamRepoImpl;
import pe.edu.unmsm.repo.QuestionRepoImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExamServiceImplSpyTest {
    @Spy
    ExamRepoImpl examRepo;

    @Spy
    QuestionRepoImpl questionRepo;

    @InjectMocks
    ExamServiceImpl examService;

    @Test
    void testSpy() {
        List<String> questions = List.of("Aritmética");

//        when(questionRepo1.findQuestionsByExamId(anyLong())).thenReturn(questions);
        doReturn(questions).when(questionRepo).findQuestionsByExamId(anyLong());

        Exam exam = examService.findExamByNameWithQuestions("Matemáticas");

        assertEquals(5, exam.getId());
        assertEquals("Matemáticas", exam.getName());
        assertEquals(1, exam.getQuestions().size());
        assertTrue(exam.getQuestions().contains("Aritmética"));

        verify(examRepo).findAll();
        verify(questionRepo).findQuestionsByExamId(anyLong());
    }
}
