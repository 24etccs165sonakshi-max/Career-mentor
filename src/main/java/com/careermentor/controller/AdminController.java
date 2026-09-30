package com.careermentor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.careermentor.model.Assignment;
import com.careermentor.model.Feedback;
import com.careermentor.model.Mentor;
import com.careermentor.model.Student;
import com.careermentor.repository.AssignmentRepository;
import com.careermentor.repository.FeedbackRepository;
import com.careermentor.repository.MentorRepository;
import com.careermentor.repository.StudentRepository;

@Controller
public class AdminController {

    private final MentorRepository mentorRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final FeedbackRepository feedbackRepository;

    public AdminController(
            MentorRepository mentorRepository,
            StudentRepository studentRepository,
            AssignmentRepository assignmentRepository,
            FeedbackRepository feedbackRepository) {

        this.mentorRepository = mentorRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(
            @RequestParam String name,
            @RequestParam String password) {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        // 1. Mentors
        List<Mentor> mentors = mentorRepository.findAll();
        model.addAttribute("mentors", mentors);

        // 2. Student Progress
        List<Student> students = studentRepository.findAll();
        List<StudentProgress> studentProgressList = new ArrayList<>();

        for (Student student : students) {
            List<Assignment> assignments = assignmentRepository.findByStudentId(student.getId());

            int totalAssignments = (assignments != null) ? assignments.size() : 0;
            int completedAssignments = 0;

            if (assignments != null) {
                for (Assignment assignment : assignments) {
                    if ("COMPLETED".equalsIgnoreCase(assignment.getStatus())) {
                        completedAssignments++;
                    }
                }
            }

            int progress = 0;
            if (totalAssignments > 0) {
                progress = (completedAssignments * 100) / totalAssignments;
            }

            studentProgressList.add(
                    new StudentProgress(
                            student,
                            totalAssignments,
                            completedAssignments,
                            progress
                    )
            );
        }
        model.addAttribute("studentProgressList", studentProgressList);

        // 3. Feedbacks with resolved names
        List<Feedback> rawFeedbacks = feedbackRepository.findAll();
        List<FeedbackView> feedbackList = new ArrayList<>();

        if (rawFeedbacks != null) {
            for (Feedback fb : rawFeedbacks) {
                String studentName = "Unknown Student";
                if (fb.getStudentId() != null) {
                    studentName = studentRepository.findById(fb.getStudentId())
                            .map(Student::getName)
                            .orElse("Student (" + fb.getStudentId() + ")");
                }

                String mentorName = "Unknown Mentor";
                if (fb.getMentorId() != null) {
                    mentorName = mentorRepository.findById(fb.getMentorId())
                            .map(Mentor::getName)
                            .orElse("Mentor (" + fb.getMentorId() + ")");
                }

                feedbackList.add(new FeedbackView(
                        studentName,
                        mentorName,
                        fb.getRating(),
                        fb.getComment()
                ));
            }
        }

        model.addAttribute("feedbacks", feedbackList);

        return "admin-dashboard";
    }

    @GetMapping("/admin/add-mentor")
    public String addMentorPage() {
        return "add-mentor";
    }

    @PostMapping("/admin/add-mentor")
    public String addMentor(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String expertise,
            @RequestParam int experience) {

        Mentor mentor = new Mentor();
        mentor.setName(name);
        mentor.setEmail(email);
        mentor.setExpertise(expertise);
        mentor.setExperience(experience);

        mentorRepository.save(mentor);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/mentors/delete/{id}")
    public String deleteMentor(@PathVariable String id) {
        mentorRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // Helper DTOs
    public static class StudentProgress {
        private Student student;
        private int totalAssignments;
        private int completedAssignments;
        private int progress;

        public StudentProgress(Student student, int totalAssignments, int completedAssignments, int progress) {
            this.student = student;
            this.totalAssignments = totalAssignments;
            this.completedAssignments = completedAssignments;
            this.progress = progress;
        }

        public Student getStudent() { return student; }
        public int getTotalAssignments() { return totalAssignments; }
        public int getCompletedAssignments() { return completedAssignments; }
        public int getProgress() { return progress; }
    }

    public static class FeedbackView {
        private String studentName;
        private String mentorName;
        private int rating;
        private String comment;

        public FeedbackView(String studentName, String mentorName, int rating, String comment) {
            this.studentName = studentName;
            this.mentorName = mentorName;
            this.rating = rating;
            this.comment = comment;
        }

        public String getStudentName() { return studentName; }
        public String getMentorName() { return mentorName; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
    }
}