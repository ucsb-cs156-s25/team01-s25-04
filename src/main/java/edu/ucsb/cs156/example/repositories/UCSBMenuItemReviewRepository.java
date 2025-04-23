package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBMenuItemReview;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The UCSBMenuItemReviewRepository is a repository for UCSBMenuItemReview entities.
 */

@Repository
public interface UCSBMenuItemReviewRepository extends CrudRepository<UCSBMenuItemReview, Long> {
}