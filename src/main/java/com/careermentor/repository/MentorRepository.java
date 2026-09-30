package com.careermentor.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.careermentor.model.Mentor;
public interface MentorRepository extends MongoRepository<Mentor, String>{
    
}
