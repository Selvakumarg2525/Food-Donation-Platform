package com.Donation.FoodDonation.model;

import jakarta.persistence.*;

@Entity
public class DonationRequest {
    public DonationRequest() {
    }

    @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;



        @ManyToOne
        @JoinColumn(name = "food_details_id")
        private FoodDetails foodDetails;

        @ManyToOne
        @JoinColumn(name = "donator_id")
        private Donator donator;



        @ManyToOne
        @JoinColumn(name = "recipient_id" , referencedColumnName = "recipientId")
        private Recipient recipient;

    public Donator getDonator() {
        return donator;
    }

    public void setDonator(Donator donator) {
        this.donator = donator;
    }

    @Column(name = "status")
        @Enumerated(EnumType.STRING)
        private DonationStatus status = DonationStatus.pending;  // "pending", "accepted", etc.

        @Column(name = "feedback", length = 500)
        private String feedback;

        @Column(name = "reason" , length = 500)
        private String reason;



    //    getter and setters


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Getter and setter for feedback
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }


    // Getters and setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FoodDetails getFoodDetails() {
        return foodDetails;
    }

    public void setFoodDetails(FoodDetails foodDetails) {
        this.foodDetails = foodDetails;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }
}


