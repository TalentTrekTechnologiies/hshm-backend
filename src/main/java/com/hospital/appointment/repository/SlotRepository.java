package com.hospital.appointment.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.enums.SlotStatus;

import jakarta.transaction.Transactional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    // ✅ Slot listing (patient side)
    List<Slot> findByDoctorIdAndSlotDateOrderByStartTimeAsc(Long doctorId, LocalDate slotDate);

    List<Slot> findByDoctorIdAndSlotDateAndStatusOrderByStartTimeAsc(
            Long doctorId,
            LocalDate slotDate,
            SlotStatus status
    );

    // ✅ Prevent duplicate slot generation
    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);

    // =========================================================
    // ATOMIC LOCKING (Race-condition safe)
    // =========================================================
    @Transactional
    @Modifying
    @Query("""
        update Slot s
        set s.status = com.hospital.appointment.enums.SlotStatus.LOCKED,
            s.lockedBy = (select p from Patient p where p.id = :patientId),
            s.lockUntil = :lockUntil
        where s.id = :slotId
          and s.status = com.hospital.appointment.enums.SlotStatus.AVAILABLE
    """)
    int lockSlot(@Param("slotId") Long slotId,
                 @Param("patientId") Long patientId,
                 @Param("lockUntil") LocalDateTime lockUntil);

    // =========================================================
    // RELEASE EXPIRED LOCKS (Scheduler)
    // =========================================================
    @Transactional
    @Modifying
    @Query("""
        update Slot s
        set s.status = com.hospital.appointment.enums.SlotStatus.AVAILABLE,
            s.lockedBy = null,
            s.lockUntil = null
        where s.status = com.hospital.appointment.enums.SlotStatus.LOCKED
          and s.lockUntil < :now
    """)
    int releaseExpiredLocks(@Param("now") LocalDateTime now);
}

