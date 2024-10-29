package com.Donation.FoodDonation.repository;

import com.Donation.FoodDonation.model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    Recipient findByEmailAndPassword(String email, String password);
}
