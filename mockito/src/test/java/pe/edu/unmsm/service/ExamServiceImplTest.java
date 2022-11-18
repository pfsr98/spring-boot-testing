package pe.edu.unmsm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import pe.edu.unmsm.Data;
import pe.edu.unmsm.model.Exam;
import pe.edu.unmsm.repo.ExamRepo;
import pe.edu.unmsm.repo.ExamRepoImpl;
import pe.edu.unmsm.repo.QuestionRepo;
import pe.edu.unmsm.repo.QuestionRepoImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceImplTest {
    @Mock
    ExamRepoImpl examRepo;

    @Mock
    QuestionRepoImpl questionRepo;

    @InjectMocks
    ExamServiceImpl examService;

    @Captor
    ArgumentCaptor<Long> captor;

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findExamByName() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);

        Exam exam = examService.findExamByName("Matemáticas");

        assertNotNull(exam);
        assertEquals(5L, exam.getId());
        assertEquals("Matemáticas", exam.getName());
    }

    @Test
    void findExamByNameEmptyList() {
        List<Exam> examList = Collections.emptyList();

        when(examRepo.findAll()).thenReturn(examList);

        Exam exam = examService.findExamByName("Matemáticas");

        assertNull(exam);
    }

    @Test
    void testExamQuestions() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);
        when(questionRepo.findQuestionsByExamId(anyLong())).thenReturn(Data.QUESTIONS);

        Exam exam = examService.findExamByNameWithQuestions("Matemáticas");

        assertEquals(5, exam.getQuestions().size());
        assertTrue(exam.getQuestions().contains("Integrales"));
    }

    @Test
    void testExamQuestionsVerify() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);
        when(questionRepo.findQuestionsByExamId(anyLong())).thenReturn(Data.QUESTIONS);

        Exam exam = examService.findExamByNameWithQuestions("Matemáticas");

        assertEquals(5, exam.getQuestions().size());
        assertTrue(exam.getQuestions().contains("Integrales"));

        verify(examRepo).findAll();
        verify(questionRepo).findQuestionsByExamId(anyLong());
    }

    @Test
    void testExamNotFoundVerify() {
        //given
        when(examRepo.findAll()).thenReturn(Data.EXAMS);

        //when
        Exam exam = examService.findExamByNameWithQuestions("Matemáticas2");

        //then
        assertNull(exam);

        verify(examRepo).findAll();
    }

    @Test
    void testSaveExam() {
        //given
        Exam newExam = Data.EXAM;
        newExam.setQuestions(Data.QUESTIONS);

        when(examRepo.save(any(Exam.class))).then(new Answer<Exam>() {
            long sequence = 8L;

            @Override
            public Exam answer(InvocationOnMock invocationOnMock) {
                Exam exam = invocationOnMock.getArgument(0);
                exam.setId(sequence++);
                return exam;
            }
        });

        //when
        Exam exam = examService.save(newExam);

        //then
        assertNotNull(exam.getId());
        assertEquals(8L, exam.getId());
        assertEquals("Física", exam.getName());

        verify(examRepo).save(any(Exam.class));
        verify(questionRepo).saveAll(anyList());
    }

    @Test
    void testExceptionHandling() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS_WITH_NULL_ID);
        when(questionRepo.findQuestionsByExamId(isNull())).thenThrow(IllegalArgumentException.class);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> examService.findExamByNameWithQuestions("Matemáticas"));
        assertEquals(IllegalArgumentException.class, exception.getClass());

        verify(examRepo).findAll();
        verify(questionRepo).findQuestionsByExamId(isNull());
    }

    @Test
    void testArgumentMatchers() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);
        when(questionRepo.findQuestionsByExamId(anyLong())).thenReturn(Data.QUESTIONS);

        examService.findExamByNameWithQuestions("Matemáticas");

        verify(examRepo).findAll();
//        verify(questionRepo).findQuestionsByExamId(argThat(arg -> arg.equals(5L)));
        verify(questionRepo).findQuestionsByExamId(argThat(arg -> arg != null && arg >= 5L));
    }

    @Test
    void testArgumentMatchers2() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);
        when(questionRepo.findQuestionsByExamId(anyLong())).thenReturn(Data.QUESTIONS);

        examService.findExamByNameWithQuestions("Matemáticas");

        verify(examRepo).findAll();
        verify(questionRepo).findQuestionsByExamId(argThat(new MyArgMatchers()));
    }

    public static class MyArgMatchers implements ArgumentMatcher<Long> {
        private Long argument;

        @Override
        public boolean matches(Long aLong) {
            argument = aLong;
            return aLong != null && aLong > 0;
        }

        @Override
        public String toString() {
            return "Mensaje personalizado de error que se imprime cuando la prueba falla: " + argument + " debe ser un entero positivo";
        }
    }

    @Test
    void testArgumentCaptor() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);

        examService.findExamByNameWithQuestions("Matemáticas");

        verify(questionRepo).findQuestionsByExamId(captor.capture());

        assertEquals(5L, captor.getValue());
    }

    @Test
    void testDoThrow() {
        Exam exam = Data.EXAM;
        exam.setQuestions(Data.QUESTIONS);

        doThrow(IllegalArgumentException.class).when(questionRepo).saveAll(anyList());

        assertThrows(IllegalArgumentException.class, () -> examService.save(exam));
    }

    @Test
    void testDoAnswer() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);
//        when(questionRepo.findQuestionsByExamId(anyLong())).thenReturn(Data.QUESTIONS);
        doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            return id == 5L ? Data.QUESTIONS : null;
        }).when(questionRepo).findQuestionsByExamId(anyLong());

        Exam exam = examService.findExamByNameWithQuestions("Matemáticas");

        assertEquals(5L, exam.getId());
        assertEquals("Matemáticas", exam.getName());
        assertEquals(5, exam.getQuestions().size());
        assertTrue(exam.getQuestions().contains("Geometría"));

        verify(questionRepo).findQuestionsByExamId(anyLong());
    }

    @Test
    void testSaveExamDoAnswer() {
        //given
        Exam newExam = Data.EXAM;
        newExam.setQuestions(Data.QUESTIONS);

        doAnswer(new Answer<Exam>() {
            long sequence = 8L;

            @Override
            public Exam answer(InvocationOnMock invocationOnMock) {
                Exam exam = invocationOnMock.getArgument(0);
                exam.setId(sequence++);
                return exam;
            }
        }).when(examRepo).save(any(Exam.class));

        //when
        Exam exam = examService.save(newExam);

        //then
        assertNotNull(exam.getId());
        assertEquals(8L, exam.getId());
        assertEquals("Física", exam.getName());

        verify(examRepo).save(any(Exam.class));
        verify(questionRepo).saveAll(anyList());
    }

    @Test
    void testDoCallRealMethod() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);
//        when(questionRepo.findQuestionsByExamId(anyLong())).thenReturn(Data.QUESTIONS);
        doCallRealMethod().when(questionRepo).findQuestionsByExamId(anyLong());

        Exam exam = examService.findExamByNameWithQuestions("Matemáticas");

        assertEquals(5L, exam.getId());
        assertEquals("Matemáticas", exam.getName());
    }

    @Test
    void testSpy() {
        ExamRepo examRepo1 = spy(ExamRepoImpl.class);
        QuestionRepo questionRepo1 = spy(QuestionRepoImpl.class);
        ExamService examService1 = new ExamServiceImpl(examRepo1, questionRepo1);

        List<String> questions = List.of("Aritmética");

//        when(questionRepo1.findQuestionsByExamId(anyLong())).thenReturn(questions);
        doReturn(questions).when(questionRepo1).findQuestionsByExamId(anyLong());

        Exam exam = examService1.findExamByNameWithQuestions("Matemáticas");

        assertEquals(5, exam.getId());
        assertEquals("Matemáticas", exam.getName());
        assertEquals(1, exam.getQuestions().size());
        assertTrue(exam.getQuestions().contains("Aritmética"));

        verify(examRepo1).findAll();
        verify(questionRepo1).findQuestionsByExamId(anyLong());
    }

    @Test
    void testOrderOfInvocations() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);

        examService.findExamByNameWithQuestions("Matemáticas");
        examService.findExamByNameWithQuestions("Lenguaje");

        InOrder inOrder = inOrder(questionRepo);
        inOrder.verify(questionRepo).findQuestionsByExamId(5L);
        inOrder.verify(questionRepo).findQuestionsByExamId(6L);
    }

    @Test
    void testOrderOfInvocations2() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);

        examService.findExamByNameWithQuestions("Matemáticas");
        examService.findExamByNameWithQuestions("Lenguaje");

        InOrder inOrder = inOrder(examRepo, questionRepo);
        inOrder.verify(examRepo).findAll();
        inOrder.verify(questionRepo).findQuestionsByExamId(5L);
        inOrder.verify(examRepo).findAll();
        inOrder.verify(questionRepo).findQuestionsByExamId(6L);
    }

    @Test
    void testNumberOfInvocations() {
        when(examRepo.findAll()).thenReturn(Data.EXAMS);

        examService.findExamByNameWithQuestions("Matemáticas");

        verify(questionRepo).findQuestionsByExamId(5L);
        verify(questionRepo, times(1)).findQuestionsByExamId(5L);
        verify(questionRepo, atLeast(1)).findQuestionsByExamId(5L);
        verify(questionRepo, atLeastOnce()).findQuestionsByExamId(5L);
        verify(questionRepo, atMost(1)).findQuestionsByExamId(5L);
        verify(questionRepo, atMostOnce()).findQuestionsByExamId(5L);
    }

    @Test
    void testNumberOfInvocations2() {
        when(examRepo.findAll()).thenReturn(Collections.emptyList());

        examService.findExamByNameWithQuestions("Matemáticas");

        verify(questionRepo, never()).findQuestionsByExamId(5L);
        verifyNoInteractions(questionRepo);
        verify(examRepo, times(1)).findAll();
        verify(examRepo, atLeast(1)).findAll();
        verify(examRepo, atMostOnce()).findAll();
        verify(examRepo, atMost(1)).findAll();
        verify(examRepo, atMostOnce()).findAll();
    }
}