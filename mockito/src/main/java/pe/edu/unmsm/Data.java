package pe.edu.unmsm;

import pe.edu.unmsm.model.Exam;

import java.util.Arrays;
import java.util.List;

public class Data {
    public static final List<Exam> EXAMS = Arrays.asList(
            new Exam(5L, "Matemáticas"),
            new Exam(6L, "Lenguaje"),
            new Exam(7L, "Historia")
    );
    public static final List<Exam> EXAMS_WITH_NEGATIVE_ID = Arrays.asList(
            new Exam(-5L, "Matemáticas"),
            new Exam(-6L, "Lenguaje"),
            new Exam(-7L, "Historia")
    );
    public static final List<Exam> EXAMS_WITH_NULL_ID = Arrays.asList(
            new Exam(null, "Matemáticas"),
            new Exam(null, "Lenguaje"),
            new Exam(null, "Historia")
    );
    public static final List<String> QUESTIONS = Arrays.asList(
            "Aritmética",
            "Integrales",
            "Derivadas",
            "Trigonometría",
            "Geometría"
    );
    public static final Exam EXAM = new Exam(null, "Física");
}
