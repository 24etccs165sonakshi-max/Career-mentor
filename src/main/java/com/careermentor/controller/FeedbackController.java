package com.careermentor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.careermentor.model.Feedback;
import com.careermentor.model.Mentor;
import com.careermentor.model.Student;
import com.careermentor.repository.FeedbackRepository;
import com.careermentor.repository.MentorRepository;
import com.careermentor.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;

    public FeedbackController(
            FeedbackRepository feedbackRepository,
            StudentRepository studentRepository,
            MentorRepository mentorRepository) {
        this.feedbackRepository = feedbackRepository;
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
    }

    // ==========================================
    // 1. STUDENT SUBMITS FEEDBACK
    // ==========================================
    @PostMapping("/student/feedback")
    public String submitFeedback(
            @RequestParam("mentorId") String mentorId,
            @RequestParam("comment") String comment,
            @RequestParam(value = "rating", defaultValue = "5") int rating,
            HttpSession session) {

        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/login";
        }

        // Check if student already gave feedback to this mentor or create new
        Feedback feedback = feedbackRepository.findByStudentIdAndMentorId(student.getId(), mentorId);
        if (feedback == null) {
            feedback = new Feedback();
            feedback.setStudentId(student.getId());
            feedback.setMentorId(mentorId);
        }

        feedback.setComment(comment != null ? comment.trim() : "");
        feedback.setRating(rating);
        feedbackRepository.save(feedback);

        return "redirect:/student-dashboard?feedbackSubmitted=true";
    }

    // ==========================================
    // 2. ADMIN VIEW FEEDBACK & RATINGS PAGE
    // ==========================================
    @GetMapping("/admin/feedback")
    public String viewAdminFeedback(HttpSession session, Model model) {

        // 1. Fetch all mentors
        List<Mentor> mentors = mentorRepository.findAll();
        model.addAttribute("mentors", mentors);

        // 2. Map feedbacks and calculate ratings per mentor
        Map<String, List<Feedback>> feedbackMap = new HashMap<>();
        Map<String, RatingSummary> ratingMap = new HashMap<>();

        for (Mentor mentor : mentors) {
            List<Feedback> mentorFeedbacks = feedbackRepository.findByMentorId(mentor.getId());
            if (mentorFeedbacks == null) {
                mentorFeedbacks = new ArrayList<>();
            }

            feedbackMap.put(mentor.getId(), mentorFeedbacks);

            if (!mentorFeedbacks.isEmpty()) {
                double totalRating = 0.0;
                for (Feedback fb : mentorFeedbacks) {
                    totalRating += fb.getRating();
                }
                double average = totalRating / mentorFeedbacks.size();
                ratingMap.put(mentor.getId(), new RatingSummary(average, mentorFeedbacks.size()));
            } else {
                ratingMap.put(mentor.getId(), null);
            }
        }

        model.addAttribute("feedbackMap", feedbackMap);
        model.addAttribute("ratingMap", ratingMap);

        return "admin-feedback";
    }

    // ==========================================
    // RATING SUMMARY DTO
    // ==========================================
    public static class RatingSummary {
        private final double average;
        private final int count;

        public RatingSummary(double average, int count) {
            this.average = average;
            this.count = count;
        }

        public double getAverage() {
            return average;
        }

        public int getCount() {
            return count;
        }
    }
}