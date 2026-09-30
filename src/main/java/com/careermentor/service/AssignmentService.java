package com.careermentor.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.careermentor.model.Assignment;
import com.careermentor.repository.AssignmentRepository;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public Assignment addAssignment(Assignment assignment) {
        assignment.setStatus("PENDING");
        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getStudentAssignments(String studentId) {
        return assignmentRepository.findByStudentId(studentId);
    }

    public List<Assignment> getMentorAssignments(String mentorId) {
        return assignmentRepository.findByMentorId(mentorId);
    }

    public Assignment completeAssignment(String id){
        Assignment assignment = assignmentRepository.findById(id).orElse(null);
        if (assignment != null) {
            assignment.setStatus("COMPLETED");
            assignmentRepository.save(assignment);
        }
        return assignment;
    }

    public void deleteAssignment(String id)
    {
        assignmentRepository.deleteById(id);
    }
}
