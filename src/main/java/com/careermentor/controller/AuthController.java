package com.careermentor.controller;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.careermentor.model.Student;
import com.careermentor.repository.StudentRepository;
import com.careermentor.service.StudentService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;

    public AuthController(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder,
            StudentService studentService) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("student", new Student());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @ModelAttribute("student") Student student,
            Model model) {

        if (student.getName() == null || student.getName().trim().isEmpty()) {
            model.addAttribute("error", "Name is required.");
            return "register";
        }

        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email is required.");
            return "register";
        }

        if (student.getPassword() == null || student.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Password is required.");
            return "register";
        }

        if (studentRepository.existsByEmail(student.getEmail().trim())) {
            model.addAttribute("error", "Email is already registered! Please login.");
            return "register";
        }

        student.setEmail(student.getEmail().trim());
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        student.setRole("STUDENT");

        studentRepository.save(student);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student != null) {
            if ("ADMIN".equalsIgnoreCase(student.getRole())) {
                return "redirect:/admin/dashboard";
            }
            if (student.getInterests() != null && !student.getInterests().isEmpty()) {
                return "redirect:/student-dashboard";
            }
            return "redirect:/interests";
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session) {

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return "redirect:/login?error=true";
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(email.trim());
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            if (student.getPassword() != null && passwordEncoder.matches(password, student.getPassword())) {
                session.setAttribute("loggedInStudent", student);

                // Check Admin role
                if ("ADMIN".equalsIgnoreCase(student.getRole())) {
                    return "redirect:/admin/dashboard";
                }

                // If student already selected interests, go to dashboard directly
                if (student.getInterests() != null && !student.getInterests().isEmpty()) {
                    return "redirect:/student-dashboard";
                }

                // If first time / no interests, go to interests selection
                return "redirect:/interests";
            }
        }

        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            return "redirect:/forgot-password?error=true";
        }

        boolean changed = studentService.changePassword(email, password);

        if (!changed) {
            return "redirect:/forgot-password?error=true";
        }

        return "redirect:/login?success=true";
    }
}