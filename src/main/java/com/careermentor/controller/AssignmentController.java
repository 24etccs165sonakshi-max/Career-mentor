package com.careermentor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.careermentor.model.Assignment;
import com.careermentor.repository.StudentRepository;
import com.careermentor.service.AssignmentService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mentor/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final StudentRepository studentRepository;

    public AssignmentController(AssignmentService assignmentService, StudentRepository studentRepository) {
        this.assignmentService = assignmentService;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public String listMentorAssignments(HttpSession session, Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("newAssignment", new Assignment());
        return "mentor-assignments";
    }

    // @PostMapping("/add")
    // public String createAssignment(@ModelAttribute("newAssignment") Assignment assignment) {
    //     assignmentService.addAssignment(assignment);
    //     return "redirect:/mentor/assignments";
    // }

    // @PostMapping("/delete/{id}")
    // public String deleteAssignment(@PathVariable("id") String id) {
    //     assignmentService.deleteAssignment(id);
    //     return "redirect:/mentor/assignments";
    // }
}