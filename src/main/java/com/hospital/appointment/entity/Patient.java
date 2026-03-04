package com.hospital.appointment.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(
    name = "patients",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_patient_phone", columnNames = "phone"),
        @UniqueConstraint(name = "uk_patient_email", columnNames = "email")
    }
)
@JsonIgnoreProperties({"familyMembers"})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_code", nullable = false, unique = true, length = 30)
    private String patientCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 5)
    private String bloodGroup;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(name = "emergency_contact", length = 15)
    private String emergencyContact;

    @Column(name = "password_hash", length = 200)
    @JsonIgnore
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "otp_verified", nullable = false)
    private boolean otpVerified = false;

    @Column(nullable = false)
    private boolean active = true;

    // ================= FAMILY RELATION =================

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Patient parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Patient> familyMembers;

    public Patient() {
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() { return id; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isOtpVerified() { return otpVerified; }
    public void setOtpVerified(boolean otpVerified) { this.otpVerified = otpVerified; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Patient getParent() { return parent; }
    public void setParent(Patient parent) { this.parent = parent; }

    public List<Patient> getFamilyMembers() { return familyMembers; }
    public void setFamilyMembers(List<Patient> familyMembers) { this.familyMembers = familyMembers; }
}