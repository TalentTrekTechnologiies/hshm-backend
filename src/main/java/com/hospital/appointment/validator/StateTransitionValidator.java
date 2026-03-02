package com.hospital.appointment.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.PaymentStatus;
import com.hospital.appointment.exception.BadRequestException;

@Component
public class StateTransitionValidator {

    // ================= APPOINTMENT =================

    public void validateAppointmentTransition(AppointmentStatus current,
                                              AppointmentStatus next) {

        if (current == next) return;

        switch (current) {

            case PENDING_PAYMENT:
                allow(next,
                        AppointmentStatus.PAYMENT_PROCESSING,
                        AppointmentStatus.EXPIRED);
                break;

            case PAYMENT_PROCESSING:
                allow(next,
                        AppointmentStatus.CONFIRMED,
                        AppointmentStatus.FAILED,
                        AppointmentStatus.EXPIRED);
                break;

            case FAILED:
                // 🔥 Allow retry after failed payment
                allow(next,
                        AppointmentStatus.PAYMENT_PROCESSING);
                break;

            case CONFIRMED:
                allow(next,
                        AppointmentStatus.CANCELLATION_REQUESTED);
                break;

            case CANCELLATION_REQUESTED:
                allow(next,
                        AppointmentStatus.CANCELLED);
                break;

            case CANCELLED:
                allow(next,
                        AppointmentStatus.REFUND_PENDING);
                break;

            case REFUND_PENDING:
                allow(next,
                        AppointmentStatus.REFUND_COMPLETED);
                break;

            default:
                throw new BadRequestException(
                        "Invalid appointment state transition: " + current + " → " + next);
        }
    }

    // ================= PAYMENT =================

    public void validatePaymentTransition(PaymentStatus current,
                                          PaymentStatus next) {

        if (current == next) return;

        switch (current) {

            case INITIATED:
                allow(next,
                        PaymentStatus.SUCCESS,
                        PaymentStatus.FAILED,
                        PaymentStatus.EXPIRED);
                break;

            case PROCESSING:
                allow(next,
                        PaymentStatus.SUCCESS,
                        PaymentStatus.FAILED,
                        PaymentStatus.EXPIRED);
                break;

            default:
                throw new BadRequestException(
                        "Invalid payment state transition: " + current + " → " + next);
        }
    }

    // ================= HELPER =================

    private void allow(Enum<?> next, Enum<?>... allowed) {
        if (!Set.of(allowed).contains(next)) {
            throw new BadRequestException("Illegal state transition to " + next);
        }
    }
}