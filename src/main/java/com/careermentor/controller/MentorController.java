package com.careermentor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.careermentor.model.Mentor;
import com.careermentor.repository.MentorRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class MentorController {

    private final MentorRepository mentorRepository;

    public MentorController(MentorRepository mentorRepository) {
        this.mentorRepository = mentorRepository;
    }

    @GetMapping("/admin-mentors")
    public String showAdminMentors(HttpSession session, Model model) {
        model.addAttribute("mentors", mentorRepository.findAll());
        model.addAttribute("newMentor", new Mentor());
        return "admin-mentors";
    }

    @PostMapping("/admin/mentors/add")
    public String addMentor(@ModelAttribute("newMentor") Mentor mentor) {
        mentorRepository.save(mentor);
        return "redirect:/admin-mentors";
    }
}