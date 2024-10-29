package com.Donation.FoodDonation.controller;

import com.Donation.FoodDonation.model.*;
import com.Donation.FoodDonation.repository.DonationRequestRepository;
import com.Donation.FoodDonation.repository.DonatorRepository;
import com.Donation.FoodDonation.repository.FoodDetailsRepository;
import com.Donation.FoodDonation.repository.RecipientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RecipientController {

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private FoodDetailsRepository foodDetailsRepository;

    @Autowired
    private DonationRequestRepository donationRequestRepository;

    @Autowired
    private DonatorRepository donatorRepository;

    @GetMapping("/recipient/register")
    public String showRecipientRegistrationForm(Model model) {
        model.addAttribute("recipient", new Recipient());
        return "recipient-register";
    }

    @PostMapping("/recipient/register")
    public String registerRecipient(Recipient recipient) {
        recipientRepository.save(recipient);
        return "redirect:/recipient/login";
    }

    @GetMapping("/recipient/login")
    public String showRecipientLoginForm() {
        return "recipient-login";
    }
    @GetMapping("/recipient/logout")
    public String showRecipientLogoutForm() {
        return "recipient-login";
    }

    @PostMapping("/recipient/login")
    public String handleRecipientLogin(@RequestParam String email, @RequestParam String password,HttpSession session, Model model) {
        Recipient recipient = recipientRepository.findByEmailAndPassword(email, password);
        if (recipient != null) {
            // Pass the recipientId when logging in
            session.setAttribute("recipientId",recipient.getRecipientId());
            session.setAttribute("recipientName",recipient.getName());
            return "redirect:/recipient/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "recipient-login";
        }


    }

    @GetMapping("/recipient/dashboard")
    public String showRecipientDashboard(HttpSession session, Model model) {
        // Get the recipientId from the session
        Long recipientId = (Long) session.getAttribute("recipientId");
        String recipientName = (String) session.getAttribute("recipientName");
        // If the recipientId is not in the session, redirect to the login page
        if (recipientId == null) {
            return "redirect:/recipient/login"; // Or handle it appropriately
        }

        // Pass the recipientId to the dashboard and available-donations link
        model.addAttribute("recipientId", recipientId);
        model.addAttribute("recipientName",recipientName);

        return "recipient-dashboard";
    }


    @GetMapping("/recipient/available-donations")
    public String showAvailableDonations(Model model, @RequestParam Long recipientId) {
        // Retrieve the recipient based on recipientId
        Recipient recipient = recipientRepository.findById(recipientId).orElse(null);

        if (recipient != null) {
            // Get the recipient's district
            String recipientDistrict = recipient.getDistrict();

            // Fetch all food donations in the recipient's district
            List<FoodDetails> availableDonations = foodDetailsRepository.findAll()
                    .stream()
                    .filter(food -> food.getDonator().getDistrict().equals(recipientDistrict))
                    .collect(Collectors.toList());

            // Attach requests and their statuses to each FoodDetails
            availableDonations.forEach(food -> {
                // Fetch all requests for the given food item
                List<DonationRequest> requests = donationRequestRepository.findByFoodDetails(food);

                // Check if the recipient has already requested this food
                DonationRequest recipientRequest = requests.stream()
                        .filter(req -> req.getRecipient().getRecipientId().equals(recipientId))
                        .findFirst()
                        .orElse(null);

                // Set request status for the current food item
                if (recipientRequest != null) {
                    food.setRequestStatus(recipientRequest.getStatus()); // Assuming you have a field in FoodDetails
                } else {
                    food.setRequestStatus(DonationStatus.NONE); // Assuming NONE means no request made
                }
            });

            // Add the available donations and recipientId to the model
            model.addAttribute("availableDonations", availableDonations);
            model.addAttribute("recipientId", recipientId);
        } else {
            model.addAttribute("error", "Recipient not found.");
            return "error"; // or redirect to an appropriate error page
        }

        return "available-donations"; // Return the Thymeleaf template for available donations
    }

    @PostMapping("/recipient/request-food/{foodDetailsId}")
    public String requestFood(@RequestParam Long foodDetailsId, HttpSession session) {
        Long recipientId = (Long) session.getAttribute("recipientId");
        if (recipientId != null) {
            Recipient recipient = recipientRepository.findById(recipientId).orElse(null);
            FoodDetails foodDetails = foodDetailsRepository.findById(foodDetailsId).orElse(null);
        if(recipient != null && foodDetails != null) {
            DonationRequest request = new DonationRequest();
            request.setRecipient(recipient);
            request.setFoodDetails(foodDetails);
            request.setStatus(DonationStatus.pending);

            donationRequestRepository.save(request);
            return "redirect:/recipient/available-donations";
        }
        }
        return "redirect:/recipient/login";
    }
    @GetMapping("/recipient/view-donator")
    public String viewDonator(@RequestParam Long requestId, Model model) {
        DonationRequest request = donationRequestRepository.findById(requestId).orElse(null);
        if (request != null && DonationStatus.accepted.equals(request.getStatus())) {
            Donator donator = request.getFoodDetails().getDonator();
            model.addAttribute("donator", donator);
            return "view-donator"; // Create a Thymeleaf template for this
        } else {
            model.addAttribute("error", "Request not found or not accepted yet.");
            return "error"; // Show an error page or return to previous page
        }
    }

    @GetMapping("/recipient/successful-donations")
    public String viewSuccessfulDonations(@RequestParam("recipientId") Long recipientId, Model model) {
        // Fetch successful donations from the repository
        List<DonationRequest> successfulDonations = donationRequestRepository.findSuccessfulDonationsByRecipientId(recipientId);

        // Add the list of successful donations to the model
        model.addAttribute("successfulDonations", successfulDonations);

        // Return the view name for successful donations
        return "view-recipient-successfull-donations";
    }
    @PostMapping("/recipient/submit-feedback")
    public String submitFeedback(@RequestParam("requestId") Long requestId, @RequestParam("feedback") String feedback, Model model) {
        DonationRequest request = donationRequestRepository.findById(requestId).orElse(null);

        if (request != null && request.getStatus() == DonationStatus.accepted) {
            // Assume you have a feedback field in DonationRequest model (you may need to add it)
            request.setFeedback(feedback);
            donationRequestRepository.save(request);

            model.addAttribute("message", "Feedback submitted successfully!");
        } else {
            model.addAttribute("error", "Cannot submit feedback at this time.");
        }

        return "redirect:/recipient/successful-donations?recipientId=" + request.getRecipient().getRecipientId();
    }

    // In RecipientController
    @PostMapping("/recipient/submit-reason")
    public String submitReason(
            @RequestParam("foodDetailsId") Long foodDetailsId,
            @RequestParam("recipientId") Long recipientId,
            @RequestParam("reason") String reason,
            Model model) {

        // Retrieve the list of donation requests for the specific foodDetailsId and recipientId
        List<DonationRequest> requests = donationRequestRepository
                .findByFoodDetailsIdAndRecipient_RecipientId(foodDetailsId, recipientId);

        // Find the FoodDetails and Recipient from their respective repositories
        FoodDetails foodDetails = foodDetailsRepository.findById(foodDetailsId).orElse(null);
        Recipient recipient = recipientRepository.findById(recipientId).orElse(null);

        if (foodDetails != null && recipient != null) {
            DonationRequest donationRequest;

            if (!requests.isEmpty()) {
                // If a request exists, update the existing request
                donationRequest = requests.get(0);
            } else {
                // If no request exists, create a new DonationRequest
                donationRequest = new DonationRequest();
            }

            // Set the necessary fields for the donation request
            donationRequest.setFoodDetails(foodDetails);
            donationRequest.setRecipient(recipient);
            donationRequest.setReason(reason);
            donationRequest.setStatus(DonationStatus.pending); // set status to pending by default

            // Save the request back to the repository
            donationRequestRepository.save(donationRequest);

            // Add a success message to the model
            model.addAttribute("message", "Request submitted successfully with reason.");
        } else {
            // Add an error message if the food details or recipient are not found
            model.addAttribute("error", "Unable to submit request. Food details or recipient not found.");
        }

        // Redirect back to the available donations page
        return "redirect:/recipient/available-donations?recipientId=" + recipientId;
    }







    @GetMapping("recipient/edit-profile")
    public String showEditProfileForm(HttpSession session, Model model) {
        Long recipientId = (Long) session.getAttribute("recipientId");
        if (recipientId == null) {
            return "redirect:/recipient/login";
        }

        // Fetch the donator's profile
        Recipient recipient = recipientRepository.findById(recipientId).orElse(null);
        model.addAttribute("recipient", recipient);
        return "recipient-edit-profile";  // This will render the recipient-edit-profile.html page
    }

    @PostMapping("/recipient/save-profile")
    public String saveRecipientProfile(@ModelAttribute Recipient recipient, HttpSession session) {
        Long recipientId = (Long) session.getAttribute("recipientId");
        if (recipientId != null) {
            Recipient existingRecipient = recipientRepository.findById(recipientId).orElse(null);

            if (existingRecipient != null) {
                if (recipient.getPassword() == null || recipient.getPassword().isEmpty()) {
                    recipient.setPassword(existingRecipient.getPassword());
                }
            }
            recipient.setRecipientId(recipientId); // Ensure we are updating the correct donator
            recipientRepository.save(recipient); // Save the updated profile
            return "redirect:/recipient/dashboard";
        }
        return "redirect:/recipient/login";
    }
    @GetMapping("/recipient/viewProfile")
    public String showRecipientProfile(Model model , HttpSession session){
        Long recipientId = (Long) session.getAttribute("recipientId");
        if(recipientId != null){
            Recipient recipient = recipientRepository.findById(recipientId).orElse(null);
            if(recipient!=null){
                model.addAttribute("recipient",recipient);
                return  "recipient-profile";
            }
        }
        return "recipient-login";
    }

    @GetMapping("/recipient/home")
    public String donatorHomeProfile(@ModelAttribute Recipient recipient, HttpSession session) {
        Long recipientId = (Long) session.getAttribute("recipientId");
        if (recipientId != null) {
            return "recipient-dashboard";
        }

        return "redirect:/recipient/login";
    }





}
