package com.Donation.FoodDonation.repository;

import com.Donation.FoodDonation.model.DonationRequest;
import com.Donation.FoodDonation.model.DonationStatus;
import com.Donation.FoodDonation.model.FoodDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonationRequestRepository extends JpaRepository<DonationRequest , Long> {
    // Find requests by the food details (for checking request status)
    List<DonationRequest> findByFoodDetails(FoodDetails foodDetails);

    @Modifying
    @Query("UPDATE DonationRequest dr SET dr.status = 'rejected' WHERE dr.foodDetails.id = :foodId AND dr.status = 'pending' AND dr.recipient.recipientId != :acceptedRecipientId")
    void cancelOtherRequests(@Param("foodId") Long foodId, @Param("acceptedRecipientId") Long acceptedRecipientId);


    // Find requests by recipient ID
    List<DonationRequest> findByRecipient_RecipientId(Long recipientId);

    // Find requests by foodDetailsId and recipientId
    List<DonationRequest> findByFoodDetailsIdAndRecipient_RecipientId(Long foodDetailsId, Long recipientId);

    // Find all requests by status (for donators to check pending/accepted requests)
    List<DonationRequest> findByStatus(Enum  status);




//    @Query("SELECT dr FROM DonationRequest dr WHERE dr.donator.id = :donatorId AND dr.status = 'pending'")commented
    List<DonationRequest> findByFoodDetails_Donator_DonatorIdAndStatus(Long donatorId, DonationStatus status);

    @Query("SELECT dr FROM DonationRequest dr WHERE dr.recipient.recipientId = :recipientId AND dr.status = 'accepted'")
    List<DonationRequest> findSuccessfulDonationsByRecipientId(@Param("recipientId") Long recipientId);

    @Query("SELECT dr FROM DonationRequest dr WHERE dr.foodDetails.donator.donatorId = :donatorId AND dr.status = :status")
    List<DonationRequest> findByDonatorAndStatus(@Param("donatorId") Long donatorId, @Param("status") DonationStatus status);



}
