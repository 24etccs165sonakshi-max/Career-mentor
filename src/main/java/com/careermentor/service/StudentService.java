package com.careermentor.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.careermentor.model.Student;
import com.careermentor.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Student updateInterests(String studentId, List<String> interests) {

        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null) {
            return null;
        }

        student.setInterests(interests);

        return studentRepository.save(student);
    }

    public Student getStudentById(String studentId) {

        return studentRepository.findById(studentId).orElse(null);
    }

    public List<Student> getAllStudents() {

        return studentRepository.findAll();
    }

    public boolean changePassword(String email, String newPassword) {

        Optional<Student> studentOptional =
                studentRepository.findByEmail(email);

        if (studentOptional.isEmpty()) {
            return false;
        }

        Student student = studentOptional.get();

        student.setPassword(passwordEncoder.encode(newPassword));

        studentRepository.save(student);

        return true;
    }
}