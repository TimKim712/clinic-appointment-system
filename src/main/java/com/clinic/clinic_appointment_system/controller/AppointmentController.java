package com.clinic.clinic_appointment_system.controller;

import com.clinic.clinic_appointment_system.model.*;
import com.clinic.clinic_appointment_system.repository.*;
import com.clinic.clinic_appointment_system.service.AppointmentService;
import com.clinic.clinic_appointment_system.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the Appointment Scheduler bounded context.
 *
 * This is the single entry point clients use for appointment operations.
 * After a booking is persisted it delegates — via NotificationServiceClient —
 * to the external Notification Service through one coarse-grained HTTP call.
 *
 * Interaction flow:
 *   Client
 *     └─► POST /api/appointments/book          (this controller)
 *               └─► AppointmentService.bookAppointment()   (persist + optimistic lock)
 *               └─► NotificationServiceClient.sendAppointmentConfirmation()
 *                         └─► POST /mock/notify            (MockNotificationController)
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final NotificationService notificationClient;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilitySlotRepository slotRepository;

    public AppointmentController(AppointmentService appointmentService, NotificationService notificationClient, PatientRepository patientRepository, ProviderRepository providerRepository, ServiceRepository serviceRepository, AvailabilitySlotRepository slotRepository) {
        this.appointmentService  = appointmentService;
        this.notificationClient  = notificationClient;
        this.patientRepository   = patientRepository;
        this.providerRepository  = providerRepository;
        this.serviceRepository   = serviceRepository;
        this.slotRepository      = slotRepository;
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /**
     * Coarse-grained booking endpoint.
     *
     * A single POST carries all IDs needed to create and confirm an
     * appointment.  Internally this fans out to:
     *   1. AppointmentService  — transactional booking with optimistic locking
     *   2. NotificationService — one remote call to dispatch a confirmation
     *
     * The client receives a unified response containing the booking outcome
     * and notification status — no follow-up calls required.
     *
     * Example request body:
     * {
     *   "patientId":  1,
     *   "providerId": 2,
     *   "serviceId":  3,
     *   "slotId":     5
     * }
     */
    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @RequestBody Appointment appointment) {

        // ── 1. Persist the appointment ────────────────────────────────────────
        appointmentService.bookAppointment(appointment);

        // ── 2. Gather context for the notification (one DB read per entity) ───
        //    These lookups are intentionally kept inside the scheduler context;
        //    the Notification Service knows nothing about our data model.

        NotificationRequest notifRequest = buildNotificationRequest(appointment);

        // ── 3. Call the external Notification Service (one coarse-grained call)
        NotificationResponse notifResponse =
                notificationClient.sendAppointmentConfirmation(notifRequest);

        // ── 4. Return a unified response to the client ────────────────────────
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Appointment booked successfully");
        body.put("appointmentId", appointment.getId());
        body.put("notificationStatus", notifResponse != null ? notifResponse.getStatus() : "UNKNOWN");
        body.put("notificationMessageId", notifResponse != null ? notifResponse.getMessageId() : null);

        return ResponseEntity.ok(body);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Assembles a NotificationRequest by looking up all relevant entities.
     * This keeps the notification DTO free from JPA/JDBC dependencies and
     * ensures the Notification Service receives human-readable strings rather
     * than raw foreign-key IDs.
     */
    private NotificationRequest buildNotificationRequest(Appointment appointment) {

        // Fetch patient email + name
        List<Patient> patients = patientRepository.findAll();
        Patient patient = patients.stream()
                .filter(p -> p.getId().equals(appointment.getPatientId()))
                .findFirst()
                .orElse(null);

        // Fetch provider name
        List<Provider> providers = providerRepository.findAll();
        Provider provider = providers.stream()
                .filter(p -> p.getId().equals(appointment.getProviderId()))
                .findFirst()
                .orElse(null);

        // Fetch service name
        List<MedicalService> services = serviceRepository.findAll();
        MedicalService service = services.stream()
                .filter(s -> s.getId().equals(appointment.getServiceId()))
                .findFirst()
                .orElse(null);

        // Fetch slot datetime
        AvailabilitySlot slot = slotRepository.findByIdForUpdate(appointment.getSlotId());

        return new NotificationRequest(
                appointment.getId(),
                patient != null ? patient.getEmail() : "unknown@example.com",
                patient != null ? patient.getUsername() : "Patient",
                provider != null ? provider.getUsername() : "Provider",
                service != null ? service.getName() : "Medical Service",
                slot != null ? slot.getStartTime().toString() : "TBD"
        );
    }
}
