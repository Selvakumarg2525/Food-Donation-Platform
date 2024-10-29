package com.Donation.FoodDonation.repository;


import com.Donation.FoodDonation.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findFirstByEmailAndPassword(String email, String password);

}


