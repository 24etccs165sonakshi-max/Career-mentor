package com.careermentor.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.careermentor.dto.Login;
import com.careermentor.dto.RegisterRequest;
import com.careermentor.model.Student;
import com.careermentor.repository.StudentRepository;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest request) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required";
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required";
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "Passwords do not match";
        }

        if (studentRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }

        Student student = new Student();

        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setRole(request.getRole() == null || request.getRole().isEmpty() ? "Student" : request.getRole());
        studentRepository.save(student);
        return "Registration successful";
    }

    public String login(Login login) {

        if (login.getEmail() == null || login.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        if (login.getPassword() == null || login.getPassword().trim().isEmpty()) {
            return "Password is required";
        }

        Optional<Student> student = studentRepository.findByEmail(login.getEmail());

        if (student.isEmpty()) {
            return "User not found";
        }

        if (!passwordEncoder.matches(login.getPassword(), student.get().getPassword())) {
            return "Invalid password";
        }

        return "Login successful";
    }
}
