package com.careermentor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.careermentor.model.Mentor;
import com.careermentor.repository.MentorRepository;

@Service
public class MentorService {

    private final MentorRepository mentorRepository;

    public MentorService(MentorRepository mentorRepository) {
        this.mentorRepository = mentorRepository;
    }

    public Mentor addMentor(Mentor mentor) {
        return mentorRepository.save(mentor);
    }

    public List<Mentor> getAllMentors() {
        return mentorRepository.findAll();
    }

    public void deleteMentor(String id) {
        mentorRepository.deleteById(id);
    }
}