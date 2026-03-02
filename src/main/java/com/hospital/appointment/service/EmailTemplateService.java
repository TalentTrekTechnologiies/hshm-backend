package com.hospital.appointment.service;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Payment;

@Service
public class EmailTemplateService {

    private static final String HOSPITAL_NAME = "Harsha Multispeciality Hospital";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    // =========================================================
    // 1) OTP EMAIL
    // =========================================================
    public String otpSubject() {
        return HOSPITAL_NAME + " – OTP Verification";
    }

    public String otpBody(String otp) {
        return """
                Dear Patient,

                Thank you for choosing Harsha Multispeciality Hospital.

                Your One-Time Password (OTP) for verification is:

                OTP: %s

                This OTP is valid for 5 minutes.
                Please do not share this OTP with anyone.

                If you did not request this OTP, please ignore this email.

                Warm Regards,
                Harsha Multispeciality Hospital
                """.formatted(otp);
    }

    // =========================================================
    // 2) APPOINTMENT CONFIRMED
    // =========================================================
    public String appointmentConfirmedSubject() {
        return "Appointment Confirmed – " + HOSPITAL_NAME;
    }

    public String appointmentConfirmedBody(Appointment appointment) {

        String patientName = appointment.getPatient().getName();
        String doctorName = appointment.getDoctor().getName();
        String departmentName = appointment.getDoctor().getDepartment() != null
                ? appointment.getDoctor().getDepartment().getName()
                : "N/A";

        String date = appointment.getSlot().getSlotDate().format(DATE_FORMAT);
        String startTime = appointment.getSlot().getStartTime().format(TIME_FORMAT);
        String endTime = appointment.getSlot().getEndTime().format(TIME_FORMAT);

        return """
                Dear %s,

                Your appointment has been successfully confirmed at Harsha Multispeciality Hospital.

                Appointment Details:
                - Doctor: %s
                - Department: %s
                - Date: %s
                - Time: %s - %s
                - Appointment ID: %d

                The doctor will contact you via WhatsApp/Phone at the scheduled time.

                Thank you,
                Harsha Multispeciality Hospital
                """.formatted(patientName, doctorName, departmentName, date, startTime, endTime, appointment.getId());
    }

    // =========================================================
    // 3) APPOINTMENT CANCELLED
    // =========================================================
    public String appointmentCancelledSubject() {
        return "Appointment Cancelled – " + HOSPITAL_NAME;
    }

    public String appointmentCancelledBody(Appointment appointment, String reason) {

        String patientName = appointment.getPatient().getName();
        String doctorName = appointment.getDoctor().getName();

        String date = appointment.getSlot().getSlotDate().format(DATE_FORMAT);
        String startTime = appointment.getSlot().getStartTime().format(TIME_FORMAT);
        String endTime = appointment.getSlot().getEndTime().format(TIME_FORMAT);

        if (reason == null || reason.isBlank()) {
            reason = "User cancelled";
        }

        return """
                Dear %s,

                Your appointment has been cancelled successfully.

                Cancelled Appointment Details:
                - Doctor: %s
                - Date: %s
                - Time: %s - %s
                - Appointment ID: %d

                Reason:
                %s

                If payment was completed, refund will be initiated as per hospital policy.

                Regards,
                Harsha Multispeciality Hospital
                """.formatted(patientName, doctorName, date, startTime, endTime, appointment.getId(), reason);
    }

    // =========================================================
    // 4) APPOINTMENT RESCHEDULED
    // =========================================================
    public String appointmentRescheduledSubject() {
        return "Appointment Rescheduled – " + HOSPITAL_NAME;
    }

    public String appointmentRescheduledBody(Appointment appointment) {

        String patientName = appointment.getPatient().getName();
        String doctorName = appointment.getDoctor().getName();

        String date = appointment.getSlot().getSlotDate().format(DATE_FORMAT);
        String startTime = appointment.getSlot().getStartTime().format(TIME_FORMAT);
        String endTime = appointment.getSlot().getEndTime().format(TIME_FORMAT);

        return """
                Dear %s,

                Your appointment has been rescheduled successfully.

                Updated Appointment Details:
                - Doctor: %s
                - New Date: %s
                - New Time: %s - %s
                - Appointment ID: %d

                Thank you for your cooperation.

                Warm Regards,
                Harsha Multispeciality Hospital
                """.formatted(patientName, doctorName, date, startTime, endTime, appointment.getId());
    }

    // =========================================================
    // 5) REFUND INITIATED
    // =========================================================
    public String refundInitiatedSubject() {
        return "Refund Initiated – " + HOSPITAL_NAME;
    }

    public String refundInitiatedBody(Appointment appointment, Payment payment) {

        String patientName = appointment.getPatient().getName();

        return """
                Dear %s,

                Your refund has been initiated successfully for the cancelled appointment.

                Refund Details:
                - Appointment ID: %d
                - Amount: ₹%s
                - Refund Status: INITIATED

                The refund will reflect in your account within 3–5 working days.

                Regards,
                Harsha Multispeciality Hospital
                """.formatted(patientName, appointment.getId(), payment.getAmount().toPlainString());
    }
}
