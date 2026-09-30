package com.careermentor.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.careermentor.model.Assignment;

public interface AssignmentRepository extends MongoRepository<Assignment, String> {
    List<Assignment> findByStudentId(String studentId);
    List<Assignment> findByMentorId(String mentorId);
}