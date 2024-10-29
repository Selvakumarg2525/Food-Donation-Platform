package com.Donation.FoodDonation.controller;

import com.Donation.FoodDonation.model.*;
import com.Donation.FoodDonation.repository.AdminRepository;
import com.Donation.FoodDonation.repository.DonationRequestRepository;
import com.Donation.FoodDonation.repository.DonatorRepository;
import com.Donation.FoodDonation.repository.RecipientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class AdminController {
        @Autowired
        private AdminRepository adminRepository;
        @Autowired
        private DonationRequestRepository donationRequestRepository;
        @Autowired
        private RecipientRepository recipientRepository;
        @Autowired
        private DonatorRepository donatorRepository;


    @GetMapping("/")
    public String showHomePage() {
        return "dashboard";
    }

        @GetMapping("/admin/login")
        public String showAdminLoginPage() {
            return "admin-login"; // This renders admin-login.html
        }
    @GetMapping("/admin/logout")
    public String showAdminLogoutPage() {
        return "dashboard"; // This renders admin-login.html
    }

    @PostMapping("/admin/login")
    public String handleAdminLogin(@RequestParam String email, @RequestParam String password, HttpSession session,Model model)
    {
        Admin admin = adminRepository.findFirstByEmailAndPassword(email, password);
        if (admin != null) {
            // Successful login
            session.setAttribute("adminId",admin.getAdminId());
            return "redirect:/admin/dashboard";
        } else {
            // Login failed
            model.addAttribute("error", "Invalid email or password");
            return "admin-login";
        }
    }
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard() {
        return "admin-dashboard";
    }
    @GetMapping("/admin/admin-profile")
    public String showAdminProfile(Model model , HttpSession session){
        Long adminId = (Long) session.getAttribute("adminId");
        if(adminId != null){
            Admin admin = adminRepository.findById(adminId).orElse(null);
            if(admin!=null){
                model.addAttribute("admin",admin);
                return  "admin-profile";
            }
        }
        return "dashboard";
    }
    @GetMapping("/admin/update-profile")
    public String adminUpdateProfile(Model model, HttpSession session){
        Long adminId = (Long) session.getAttribute("adminId");
        if(adminId == null){
            return "redirect:/admin/login";
        }
        Admin admin = adminRepository.findById(adminId).orElse(null);
        model.addAttribute("admin", admin);
        return "admin-update-profile";
    }
    @PostMapping("/admin/save-profile")
    public String saveDonatorProfile(@ModelAttribute Admin admin, HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId != null) {
            Admin existingAdmin = adminRepository.findById(adminId).orElse(null);

            if (existingAdmin != null) {
                if (admin.getPassword() == null || admin.getPassword().isEmpty()) {
                    admin.setPassword(existingAdmin.getPassword());
                }
            }
            admin.setAdminId(adminId); // Ensure we are updating the correct donator
            adminRepository.save(admin); // Save the updated profile
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/login";
    }
    @GetMapping("/admin/successful-donations")
    public String viewSuccessfulDonations(Model model) {
        // Fetch all successful donations (status = 'ACCEPTED')
        List<DonationRequest> successfulDonations = donationRequestRepository.findByStatus(DonationStatus.accepted);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        for(DonationRequest donationRequest : successfulDonations){
            try{
                Date foodMadeDate = format.parse(donationRequest.getFoodDetails().getFoodMadeDate());

                donationRequest.getFoodDetails().setParseFoodMadeDate(foodMadeDate);
                         // Set parsed date in a new field
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        // Add the list of successful donations to the model to pass to the view
        model.addAttribute("successfulDonations", successfulDonations);

        return "admin-view-successful-donations"; // Return the view name
    }
    @GetMapping("/admin/view-recipient")
    public String viewRecipient(Model model){
        List<Recipient> recipients = recipientRepository.findAll();
        model.addAttribute("recipients",recipients);
        model.addAttribute("totalRecipients",recipients.size());
        return "admin-view-recipients";
    }
    @PostMapping("/admin/delete-recipient/{recipientId}")
    public String deleteRecipient(@PathVariable Long  recipientId , Model model){
        try {
            recipientRepository.deleteById(recipientId);
            model.addAttribute("successMessage", "Recipient successfully deleted");
        }
        catch(EmptyResultDataAccessException e){
            model.addAttribute("errorMessage","Recipient Deleted failed");
        }
        return "redirect:/admin/admin-view-recipients";

    }

    @GetMapping("/admin/view-donator")
    public String viewDonator(Model model){
        List<Donator> donators = donatorRepository.findAll();
        model.addAttribute("donators",donators);
        model.addAttribute("totalRecipients",donators.size());
        return "admin-view-donators";
    }
    @PostMapping("/admin/delete-donator/{donatorId}")
    public String deleteDonator(@PathVariable Long  donatorId , Model model){
        try {
            donatorRepository.deleteById(donatorId);
            model.addAttribute("successMessage", "Recipient successfully deleted");
        }
        catch(EmptyResultDataAccessException e){
            model.addAttribute("errorMessage","Donator Deleted failed");
        }
        return "redirect:/admin/admin-view-donators";

    }

}

