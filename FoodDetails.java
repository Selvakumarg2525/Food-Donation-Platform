package com.Donation.FoodDonation.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class FoodDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient // Not persisted in the database, just for display purposes
    private DonationStatus requestStatus;

    // Getter and Setter for requestStatus
    public DonationStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(DonationStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donator_id")
    private Donator donator;

    @Column(name = "description")
    private String description;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "food_made_date")
    private String foodMadeDate;

    private Date parseFoodMadeDate;

    @Column(name = "food_made_time")
    private String timeOfFoodMade;

    @Column( name = "quantity")
    private Integer quantity;

    @OneToMany(mappedBy = "foodDetails")
    private List<DonationRequest> requests;

    // getters and setters
    public List<DonationRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<DonationRequest> requests) {
        this.requests = requests;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodMadeDate() {
        return foodMadeDate;
    }

    public void setFoodMadeDate(String foodMadeDate) {
        this.foodMadeDate = foodMadeDate;
    }

    public String getTimeOfFoodMade() {
        return timeOfFoodMade;
    }

    public void setTimeOfFoodMade(String timeOfFoodMade) {
        this.timeOfFoodMade = timeOfFoodMade;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Donator getDonator() {
        return donator;
    }

    public void setDonator(Donator donator) {
        this.donator = donator;
    }

    // Derived getters for donatorName and donatorPlace

    public String getDonatorName() {
        return donator != null ? donator.getName() : null;
    }

    public String getDonatorPlace() {
        return donator != null ? donator.getPlace() : null;
    }



    public Date getParseFoodMadeDate() {
        return parseFoodMadeDate;
    }

    public void setParseFoodMadeDate(Date parseFoodMadeDate) {
        this.parseFoodMadeDate = parseFoodMadeDate;
    }


}
