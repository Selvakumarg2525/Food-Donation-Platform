package com.Donation.FoodDonation.controller;

import com.Donation.FoodDonation.model.*;
import com.Donation.FoodDonation.repository.DonationRequestRepository;
import com.Donation.FoodDonation.repository.DonatorRepository;
import com.Donation.FoodDonation.repository.FoodDetailsRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class DonatorController {

    @Autowired
    private DonatorRepository donatorRepository;

    @Autowired
    private FoodDetailsRepository foodDetailsRepository;

    @Autowired
    private DonationRequestRepository donationRequestRepository;

    @GetMapping("/donator/register")
    public String showDonatorRegistrationForm(Model model) {
        model.addAttribute("donator", new Donator());
        return "donator-register";
    }

    @PostMapping("/donator/register")
    public String registerDonator(Donator donator) {
        donatorRepository.save(donator);
        return "redirect:/donator/login";
    }

    @GetMapping("/donator/login")
    public String showDonatorLoginForm(Model model) {
        return "donator-login";
    }


    @GetMapping("/donator/issue-food")
    public String showIssueFoodForm(Model model) {
        model.addAttribute("foodDetails", new FoodDetails());
        return "issue-food";
    }

    @PostMapping("/donator/issue-food")
    public String issueFood(@ModelAttribute FoodDetails foodDetails, HttpSession session) {
        Long donatorId = (Long) session.getAttribute("donatorId");
        if (donatorId != null) {
            Donator donator = donatorRepository.findById(donatorId).orElse(null);

            if (donator != null) {
                foodDetails.setDonator(donator);
                foodDetailsRepository.save(foodDetails);
                return "redirect:/donator/dashboard";
            } else {
                return "redirect:/donator/login";
            }
        } else {
            return "redirect:/donator/login";
        }
    }


    @GetMapping("/donator/view-requests")
    public String showDonationRequests(Model model, HttpSession session) {
        Long donatorId = (Long) session.getAttribute("donatorId");

        if (donatorId == null) {
            return "redirect:/donator/login";  // Redirect if not logged in
        }

        // Fetch all the food items issued by this donator
        List<FoodDetails> donatorFoodItems = foodDetailsRepository.findByDonator_DonatorId(donatorId);

        // Fetch only pending donation requests for the donator's food items
        List<Map<String, Object>> pendingRequests = new ArrayList<>();
        for (FoodDetails food : donatorFoodItems) {
            List<DonationRequest> foodRequests = donationRequestRepository.findByFoodDetails(food);

            // Filter out the requests that are "PENDING"
            for (DonationRequest request : foodRequests) {
                if ("pending".equals(request.getStatus().toString())) {  // Enum or String
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("request", request);  // Use "request" instead of "requests"

                    // Fetch the donator for this specific request
                    Recipient recipient = request.getRecipient(); // Assuming FoodDetails has a Donator reference
                    requestData.put("recipients", recipient); // Use the donator of the specific food item

                    requestData.put("foodDetails", food);
                    pendingRequests.add(requestData);
                }
            }
        }

        // Add filtered pending requests to the model
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("showRequests", true);

        return "view-requests";
    }






    @GetMapping("/donator/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return "redirect:/donator/login"; // Redirect to login page
    }

    @GetMapping("/donator/view-successful-donations")
    public String viewSuccessfulDonations(HttpSession session, Model model) {
        // Retrieve donator's ID from the session
        Long donatorId = (Long) session.getAttribute("donatorId");

        // Check if donator is logged in by verifying the donatorId
        if (donatorId == null) {
            // If donator is not in session, redirect to login page or handle it appropriately
            return "redirect:/donator/login";
        }

        // Fetch the list of successful donations for this donator (status = 'ACCEPTED')
        List<DonationRequest> successfulDonations = donationRequestRepository.findByDonatorAndStatus(donatorId, DonationStatus.accepted);

        // Add the list of successful donations to the model to pass to the view
        model.addAttribute("successfulDonations", successfulDonations);

        // Return the view name (Thymeleaf template) to be rendered
        return "view-donator-successfull-donations";
    }

    @GetMapping("/donator/edit-profile")
    public String showEditProfileForm(HttpSession session, Model model) {
        Long donatorId = (Long) session.getAttribute("donatorId");
        if (donatorId == null) {
            return "redirect:/donator/login";
        }

        // Fetch the donator's profile
        Donator donator = donatorRepository.findById(donatorId).orElse(null);
        model.addAttribute("donator", donator);
        return "donator-edit-profile";  // This will render the donator-edit-profile.html page
    }

    @PostMapping("/donator/save-profile")
    public String saveDonatorProfile(@ModelAttribute Donator donator, HttpSession session) {
        Long donatorId = (Long) session.getAttribute("donatorId");
        if (donatorId != null) {
            Donator existingDonator = donatorRepository.findById(donatorId).orElse(null);

            if (existingDonator != null) {
                if (donator.getPassword() == null || donator.getPassword().isEmpty()) {
                    donator.setPassword(existingDonator.getPassword());
                }
            }
            donator.setDonatorId(donatorId); // Ensure we are updating the correct donator
            donatorRepository.save(donator); // Save the updated profile
            return "redirect:/donator/dashboard";
        }
        return "redirect:/donator/login";
    }

    @PostMapping("/donator/login")
    public String handleDonatorLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Donator donator = donatorRepository.findByEmailAndPassword(email, password);
        if (donator != null) {
            session.setAttribute("donatorId", donator.getDonatorId());
            session.setAttribute("donatorName", donator.getName());
            session.setAttribute("donatorPlace", donator.getPlace());
            return "redirect:/donator/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "donator-login";
        }
    }

    @GetMapping("/donator/dashboard")
    public String showDonatorDashboard(Model model, HttpSession session) {
        Long donatorId = (Long) session.getAttribute("donatorId");
        String donatorName = (String) session.getAttribute("donatorName");
        if (donatorId == null) {
            return "redirect:/donator/login";
        }

        model.addAttribute("showRequests", false);
        model.addAttribute("donatorName",donatorName);
        return "donator-dashboard";
    }

    @GetMapping("/donator/home")
    public String donatorHomeProfile(@ModelAttribute Donator donator, HttpSession session) {
        Long donatorId = (Long) session.getAttribute("donatorId");
        if (donatorId != null) {
            return "donator-dashboard";
        }

        return "redirect:/donator/login";
    }
    @GetMapping("/donator/viewProfile")
    public String showDonatorProfile(Model model , HttpSession session){
        Long donatorId = (Long) session.getAttribute("donatorId");
        if(donatorId != null){
             Donator donator = donatorRepository.findById(donatorId).orElse(null);
             if(donator!=null){
                  model.addAttribute("donator",donator);
                  return  "donator-profile";
             }
        }
        return "donator-login";
    }

}
