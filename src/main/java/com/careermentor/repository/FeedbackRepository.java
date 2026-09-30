package com.careermentor.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.careermentor.model.Feedback;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {

    List<Feedback> findByMentorId(String mentorId);

    List<Feedback> findByStudentId(String studentId);

    Feedback findByStudentIdAndMentorId(String studentId, String mentorId);
}