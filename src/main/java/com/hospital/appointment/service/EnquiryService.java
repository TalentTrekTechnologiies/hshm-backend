package com.hospital.appointment.service;

import java.util.List;

import com.hospital.appointment.entity.Enquiry;
import com.hospital.appointment.enums.EnquiryStatus;

public interface EnquiryService {

    // Patient: create enquiry
    Enquiry createEnquiry(Enquiry enquiry);

    // Admin: view all enquiries (latest first)
    List<Enquiry> getAllEnquiries();

    // Admin: filter enquiries by status
    List<Enquiry> getEnquiriesByStatus(EnquiryStatus status);

    // Admin: update enquiry status
    Enquiry updateEnquiryStatus(Long enquiryId, EnquiryStatus status);

    // Admin: get single enquiry by id
    Enquiry getEnquiryById(Long enquiryId);
}

