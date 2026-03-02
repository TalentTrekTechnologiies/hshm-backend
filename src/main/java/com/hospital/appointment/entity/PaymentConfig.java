package com.hospital.appointment.entity;

import com.hospital.appointment.enums.RefundPolicyType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_config")
public class PaymentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal consultationFee;

    @Column(nullable = false)
    private BigDecimal cancellationFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundPolicyType refundPolicyType;

    @Column(nullable = false)
    private boolean paymentEnabled;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {   // 🔴 ADD THIS
        this.id = id;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public RefundPolicyType getRefundPolicyType() {
        return refundPolicyType;
    }

    public void setRefundPolicyType(RefundPolicyType refundPolicyType) {
        this.refundPolicyType = refundPolicyType;
    }

    public boolean isPaymentEnabled() {
        return paymentEnabled;
    }

    public void setPaymentEnabled(boolean paymentEnabled) {
        this.paymentEnabled = paymentEnabled;
    }
}