package com.careermentor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.careermentor.model.Assignment;
import com.careermentor.model.Mentor;
import com.careermentor.model.Student;
import com.careermentor.repository.MentorRepository;
import com.careermentor.repository.StudentRepository;
import com.careermentor.service.AssignmentService;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final AssignmentService assignmentService;

    public StudentController(StudentRepository studentRepository, 
                             MentorRepository mentorRepository, 
                             AssignmentService assignmentService) {
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
        this.assignmentService = assignmentService;
    }

    // ==========================================
    // 1. INTERESTS (ALLOWS UPDATES)
    // ==========================================
    @GetMapping("/interests")
    public String showInterestsPage(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/login";
        }

        model.addAttribute("student", student);
        return "interests";
    }

    @PostMapping("/interests")
    public String saveInterests(@RequestParam(value = "interests", required = false) List<String> interests, 
                                HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/login";
        }

        student.setInterests(interests != null ? interests : new ArrayList<>());
        Student updated = studentRepository.save(student);
        session.setAttribute("loggedInStudent", updated);
        return "redirect:/student/mentors";
    }

    // ==========================================
    // 2. MENTORS VIEW
    // ==========================================
    @RequestMapping(value = "/student/mentors", method = {RequestMethod.GET, RequestMethod.POST})
    public String showMentorsPage(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/login";
        }

        List<Mentor> allMentors = mentorRepository.findAll();
        List<Mentor> recommended = new ArrayList<>();
        List<String> interests = student.getInterests();

        if (interests != null && !interests.isEmpty()) {
            for (Mentor mentor : allMentors) {
                if (mentor.getExpertise() != null) {
                    for (String interest : interests) {
                        if (mentor.getExpertise().equalsIgnoreCase(interest.trim())) {
                            if (!recommended.contains(mentor)) {
                                recommended.add(mentor);
                            }
                            break;
                        }
                    }
                }
            }
        }

        model.addAttribute("mentors", recommended.isEmpty() ? allMentors : recommended);
        model.addAttribute("student", student);
        return "mentors";
    }

    // ==========================================
    // 3. MENTOR ASSIGNMENT
    // ==========================================
    @RequestMapping(value = "/student/mentors/assign/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String assignMentorByPath(@PathVariable("id") String mentorId, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/login";
        }

        student.setMentorId(mentorId);
        Student updated = studentRepository.save(student);
        session.setAttribute("loggedInStudent", updated);

        seedDefaultAssignmentsForStudent(updated.getId(), mentorId, updated.getInterests());

        return "redirect:/student-dashboard";
    }

    @RequestMapping(value = "/choose-mentor", method = {RequestMethod.GET, RequestMethod.POST})
    public String chooseMentor(@RequestParam("mentorId") String mentorId, HttpSession session) {
        return assignMentorByPath(mentorId, session);
    }

    // ==========================================
    // 4. DASHBOARD
    // ==========================================
    @RequestMapping(value = "/student-dashboard", method = {RequestMethod.GET, RequestMethod.POST})
    public String showDashboard(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/login";
        }

        model.addAttribute("student", student);

        if (student.getMentorId() != null && !student.getMentorId().isEmpty()) {
            Optional<Mentor> mentorOpt = mentorRepository.findById(student.getMentorId());
            mentorOpt.ifPresent(mentor -> model.addAttribute("mentor", mentor));
        }

        List<Assignment> assignments = assignmentService.getStudentAssignments(student.getId());
        if ((assignments == null || assignments.isEmpty()) && student.getMentorId() != null) {
            seedDefaultAssignmentsForStudent(student.getId(), student.getMentorId(), student.getInterests());
            assignments = assignmentService.getStudentAssignments(student.getId());
        }
        model.addAttribute("assignments", assignments != null ? assignments : new ArrayList<>());

        List<String> roadmap = new ArrayList<>();
        List<String> interests = student.getInterests();

        if (interests != null && !interests.isEmpty()) {
            String domain = interests.get(0);
            roadmap.add("Understand " + domain + " Fundamentals & Basics");
            roadmap.add("Master Core Concepts & Standard Frameworks");
            roadmap.add("Complete Practical Mentor Assignments");
            roadmap.add("Build a Domain-Specific Mini Project");
            roadmap.add("Develop Full-Stack Major Capstone Project");
            roadmap.add("Mock Interviews & Portfolio Review");
            roadmap.add("Achieve Your Target Role in " + domain);
        } else {
            roadmap.add("Complete Your Profile Information");
            roadmap.add("Select Your Tech Interests");
            roadmap.add("Choose a Dedicated Mentor");
            roadmap.add("Complete Practical Tasks");
            roadmap.add("Build Real-World Projects");
            roadmap.add("Prepare for Industry Interviews");
            roadmap.add("Achieve Your Career Goal");
        }

        model.addAttribute("roadmap", roadmap);
        return "student-dashboard";
    }

    // ==========================================
    // 5. ASSIGNMENTS
    // ==========================================
    @RequestMapping(value = "/student/assignments/{id}/complete", method = {RequestMethod.GET, RequestMethod.POST})
    public String completeAssignment(@PathVariable("id") String id, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/login";
        }

        assignmentService.completeAssignment(id);
        return "redirect:/student-dashboard";
    }

    // ==========================================
    // 6. SEED ASSIGNMENTS HELPER
    // ==========================================
    private void seedDefaultAssignmentsForStudent(String studentId, String mentorId, List<String> interests) {
        String domain = (interests != null && !interests.isEmpty()) ? interests.get(0) : "Software Engineering";

        Assignment a1 = new Assignment(
            "Foundations of " + domain,
            "Review key principles, syntax, and tooling required for " + domain + ".",
            "25 Aug 2026",
            mentorId,
            studentId
        );

        Assignment a2 = new Assignment(
            "Practical Coding Problem Set",
            "Implement 5 core algorithms and data structure routines related to " + domain + ".",
            "02 Sep 2026",
            mentorId,
            studentId
        );

        Assignment a3 = new Assignment(
            "Mini Project Prototype",
            "Build and push a functioning starter prototype repository to GitHub.",
            "10 Sep 2026",
            mentorId,
            studentId
        );

        assignmentService.addAssignment(a1);
        assignmentService.addAssignment(a2);
        assignmentService.addAssignment(a3);
    }
}