package com.Donation.FoodDonation.repository;

import com.Donation.FoodDonation.model.Donator;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DonatorRepository extends JpaRepository<Donator, Long> {
    Donator findByEmailAndPassword(String email, String password);
}
