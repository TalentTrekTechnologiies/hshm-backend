package com.hospital.appointment.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hospital.appointment.repository.SlotRepository;

@Component
public class SlotLockCleanupScheduler {

    private final SlotRepository slotRepository;

    public SlotLockCleanupScheduler(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredLockedSlots() {

        int released = slotRepository.releaseExpiredLocks(LocalDateTime.now());

        if (released > 0) {
            System.out.println("Released expired locked slots: " + released);
        }
    }
}

