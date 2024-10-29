package com.Donation.FoodDonation.controller;

import com.Donation.FoodDonation.model.DonationRequest;
import com.Donation.FoodDonation.model.DonationStatus;
import com.Donation.FoodDonation.model.FoodDetails;
import com.Donation.FoodDonation.model.Recipient;
import com.Donation.FoodDonation.repository.DonationRequestRepository;
import com.Donation.FoodDonation.repository.FoodDetailsRepository;
import com.Donation.FoodDonation.repository.RecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DonationRequestController {

    @Autowired
    private DonationRequestRepository donationRequestRepository;

    @Autowired
    private FoodDetailsRepository foodDetailsRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    // Handle request submission
    @PostMapping("/recipient/request-food")
    public String requestFood(@RequestParam Long foodDetailsId, @RequestParam Long recipientId, Model model) {
        FoodDetails foodDetails = foodDetailsRepository.findById(foodDetailsId).orElse(null);
        Recipient recipient = recipientRepository.findById(recipientId).orElse(null);

        if (foodDetails != null && recipient != null) {
            // Check if there is already a request for this food item by this recipient
            List<DonationRequest> existingRequest = donationRequestRepository.findByFoodDetailsIdAndRecipient_RecipientId(foodDetailsId, recipientId);
            if (existingRequest.isEmpty()) {  // <-- Correct check here
                DonationRequest newRequest = new DonationRequest();
                newRequest.setFoodDetails(foodDetails);
                newRequest.setRecipient(recipient);
                newRequest.setStatus(DonationStatus.pending); // or another initial status
                donationRequestRepository.save(newRequest);
            } else {
                model.addAttribute("error", "Request already exists.");
                return "error";
            }
        } else {
            model.addAttribute("error", "Food item or recipient not found.");
            return "error";
        }

        return "redirect:/recipient/available-donations?recipientId=" + recipientId;
    }


    @GetMapping("/recipient/view-requests")
    public String viewRequests(@RequestParam Long recipientId, @RequestParam Long foodId, Model model) {
        // Find all requests made by the recipient for the specific food item
        List<DonationRequest> requests = donationRequestRepository.findByFoodDetailsIdAndRecipient_RecipientId(foodId, recipientId);

        if (requests != null && !requests.isEmpty()) {
            model.addAttribute("requests", requests);  // Show all the requests
        } else {
            model.addAttribute("error", "No request found for this food item.");
            return "error";
        }

        model.addAttribute("recipientId", recipientId);
        return "view-requests"; // Ensure this is the correct Thymeleaf template
    }


    // Handle accepting a request
    @PostMapping("/donator/accept-request")
    public String acceptRequest(@RequestParam Long requestId, RedirectAttributes redirectAttributes) {
        DonationRequest request = donationRequestRepository.findById(requestId).orElse(null);

        if (request != null) {
            request.setStatus(DonationStatus.accepted);
            donationRequestRepository.save(request);
            redirectAttributes.addFlashAttribute("success", "Request accepted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Request not found.");
        }

        return "redirect:/donator/view-requests";
    }


    // Handle rejecting a request
    @PostMapping("/donator/reject-request")
    public String rejectRequest(@RequestParam Long requestId, Model model) {
        DonationRequest request = donationRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            model.addAttribute("error", "Request not found.");
            return "donator-dashboard"; // Stay on the same page
        }        if (request != null) {
            request.setStatus(DonationStatus.rejected);
            donationRequestRepository.save(request);
        } else {
            model.addAttribute("error", "Request not found.");
            return "error";
        }
        return "redirect:/donator/dashboard"; // Redirect to appropriate page
    }

//    @Transactional
//    @PostMapping("/donator/accept-request/{requestId}")
//    public String acceptRequest(@PathVariable Long requestId, RedirectAttributes redirectAttributes) {
//        DonationRequest acceptedRequest = donationRequestRepository.findById(requestId).orElse(null);
//
//        if (acceptedRequest != null) {
//            // Accept the selected request
//            acceptedRequest.setStatus(DonationStatus.accepted);
//            donationRequestRepository.save(acceptedRequest);
//
//            // Reject all other requests for the same food item
//            List<DonationRequest> otherRequests = donationRequestRepository.findByFoodDetails(acceptedRequest.getFoodDetails());
//            for (DonationRequest request : otherRequests) {
//
//                if (!request.getId().equals(acceptedRequest.getId())) {
//                    request.setStatus(DonationStatus.rejected);
//                    donationRequestRepository.save(request);
//                }
//            }
//
//            redirectAttributes.addFlashAttribute("success", "Request accepted successfully. All other requests for the same food item have been rejected.");
//        } else {
//            redirectAttributes.addFlashAttribute("error", "Request not found.");
//        }
//
//        return "redirect:/donator/view-requests";
//    }
}
