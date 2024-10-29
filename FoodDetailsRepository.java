package com.Donation.FoodDonation.repository;

import com.Donation.FoodDonation.model.FoodDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodDetailsRepository extends JpaRepository<FoodDetails,Long> {
    List<FoodDetails> findByDonator_DonatorId(Long donatorId);
}
