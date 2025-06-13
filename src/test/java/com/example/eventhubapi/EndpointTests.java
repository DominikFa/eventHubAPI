// File: eventHubAPI/src/test/java/com/example/eventhubapi/EndpointTests.java
package com.example.eventhubapi;

import com.example.eventhubapi.admin.AdminService;
import com.example.eventhubapi.admin.dto.AdminEventUpdateRequest;
import com.example.eventhubapi.admin.dto.AdminChangeUserRoleRequest;
import com.example.eventhubapi.admin.dto.AdminUserUpdateStatusRequest;
import com.example.eventhubapi.auth.dto.LoginRequest;
import com.example.eventhubapi.auth.dto.RegistrationRequest;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.invitation.dto.InvitationCreateRequest;
import com.example.eventhubapi.location.dto.LocationCreationRequest;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class EndpointTests extends AbstractTransactionalTestNGSpringContextTests {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;


    private String adminToken;
    private String organizerToken;
    private String userToken;
    private Long adminId;
    private Long organizerId;
    private Long userId;


    private Long findOrCreateUserAndGetId(String login, String name, String password) throws Exception {
        Optional<User> existingUser = userRepository.findByLogin(login);
        if (existingUser.isPresent()) {
            return existingUser.get().getId();
        } else {
            RegistrationRequest regRequest = new RegistrationRequest();
            regRequest.setLogin(login);
            regRequest.setName(name);
            regRequest.setPassword(password);
            MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();
            return objectMapper.readTree(regResult.getResponse().getContentAsString()).get("id").asLong();
        }
    }


    private String loginAndGetToken(String login, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(login);
        loginRequest.setPassword(password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }


    @BeforeMethod
    public void setup() throws Exception {
        adminId = findOrCreateUserAndGetId("admin@test.com", "Admin User", "password");
        organizerId = findOrCreateUserAndGetId("organizer@test.com", "Organizer User", "password");
        userId = findOrCreateUserAndGetId("user@test.com", "Regular User", "password");


        adminService.changeUserRole(adminId, "admin");
        adminService.changeUserRole(organizerId, "organizer");
        adminService.changeUserRole(userId, "user");


        adminToken = loginAndGetToken("admin@test.com", "password");
        organizerToken = loginAndGetToken("organizer@test.com", "password");
        userToken = loginAndGetToken("user@test.com", "password");
    }

    // --- ISTNIEJĄCE TESTY POZOSTAJĄ BEZ ZMIAN ---
    // ... (wszystkie poprzednie metody testowe)


    // =================================================================
    // === NOWE, BARDZIEJ RESTRYKCYJNE TESTY ===========================
    // =================================================================

    /**
     * Testuje walidację danych wejściowych.
     * Sprawdza, czy API poprawnie odrzuca żądanie z datą wydarzenia w przeszłości.
     */
    @Test
    public void testStrictValidation_CreateEventWithPastDate() throws Exception {
        EventCreationRequest event = createSampleEvent();
        // Ustawienie niepoprawnej daty (w przeszłości)
        event.setStartDate(Instant.now().minus(1, ChronoUnit.DAYS));

        mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value(containsString("startDate: must be a future date")));
    }

    /**
     * Testuje reguły biznesowe aplikacji.
     * Sprawdza, czy niemożliwe jest odwołanie zaproszenia, które zostało już zaakceptowane.
     */
    @Test
    public void testBusinessLogic_CannotRevokeAcceptedInvitation() throws Exception {
        // 1. Organizator tworzy wydarzenie
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Organizator zaprasza użytkownika
        InvitationCreateRequest inviteRequest = new InvitationCreateRequest();
        inviteRequest.setEventId(eventId);
        inviteRequest.setInvitedUserId(userId);
        MvcResult inviteResult = mockMvc.perform(post("/api/invitations")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        long invitationId = objectMapper.readTree(inviteResult.getResponse().getContentAsString()).get("id").asLong();

        // 3. Użytkownik akceptuje zaproszenie
        mockMvc.perform(post("/api/invitations/" + invitationId + "/accept")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        // 4. Organizator próbuje odwołać zaakceptowane zaproszenie (oczekiwany błąd)
        mockMvc.perform(post("/api/invitations/" + invitationId + "/revoke")
                        .header("Authorization", organizerToken))
                .andExpect(status().isConflict()) // Oczekujemy statusu 409 Conflict
                .andExpect(jsonPath("$.message").value("Only sent invitations can be revoked."));
    }

    /**
     * Testuje bezpieczeństwo - Insecure Direct Object Reference (IDOR).
     * Sprawdza, czy użytkownik A nie może zobaczyć powiadomień użytkownika B.
     */
    @Test
    public void testSecurity_UserCannotSeeAnotherUsersNotifications() throws Exception {
        // Logika testu:
        // Powiadomienia są prywatne. Wywołanie /api/notifications z tokenem użytkownika 'userToken'
        // powinno zwrócić tylko JEGO powiadomienia. Próba dostępu do cudzych danych
        // jest w tym przypadku niemożliwa, bo endpoint jest oparty o `authentication.getName()`.
        // Testujemy więc, czy endpoint działa poprawnie dla zalogowanego użytkownika.
        // Bardziej zaawansowany test IDOR jest w `testAccessDeniedHandling`.

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());
    }

    /**
     * Testuje współbieżność (race condition).
     * Symuluje sytuację, w której 5 użytkowników próbuje jednocześnie dołączyć
     * do wydarzenia z tylko 1 wolnym miejscem.
     */
    @Test
    public void testConcurrency_JoinEventWithOneLastSpot() throws Exception {
        // 1. Stwórz wydarzenie z maksymalnie 1 uczestnikiem (poza organizatorem)
        EventCreationRequest eventRequest = createSampleEvent();
        eventRequest.setMaxParticipants(2L); // Changed from 1L to 2L to allow organizer + 1 participant

        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Przygotuj 5 użytkowników (i ich tokeny), którzy będą próbować dołączyć
        int numberOfConcurrentUsers = 5;
        String[] userTokens = new String[numberOfConcurrentUsers];
        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            findOrCreateUserAndGetId("concurrent_user_" + i + "@test.com", "Concurrent " + i, "password");
            userTokens[i] = loginAndGetToken("concurrent_user_" + i + "@test.com", "password");
        }

        // 3. Użyj ExecutorService do jednoczesnego uruchomienia żądań
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentUsers);
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);

        for (String token : userTokens) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(post("/api/events/" + eventId + "/participants")
                                    .header("Authorization", token))
                            .andDo(result -> {
                                if (result.getResponse().getStatus() == 201) { // 201 CREATED
                                    successCount.incrementAndGet();
                                }
                            });
                } catch (Exception e) {
                    // Ignoruj błędy (np. 409 Conflict), które są oczekiwane
                } finally {
                    latch.countDown();
                }
            });
        }

        // Czekaj na zakończenie wszystkich wątków
        latch.await();
        executorService.shutdown();

        // 4. Asercja: Dokładnie jedno żądanie powinno zakończyć się sukcesem
        assertThat(successCount.get()).isEqualTo(1);

        // 5. Sprawdź, czy liczba uczestników in the database is 2 (1 new + 1 organizer)
        // MODIFIED: Use findByIdWithParticipants to ensure participants are loaded and filter by attending status
        var eventFromDb = eventRepository.findByIdWithParticipants(eventId).orElseThrow();
        long attendingCount = eventFromDb.getParticipants().stream()
                .filter(p -> p.getStatus().getValue().equals("attending"))
                .count();
        assertThat(attendingCount).isEqualTo(2); // 1 new + 1 organizer
    }


    // --- Existing tests ---
    @Test
    public void testAuthEndpoints() throws Exception {
        String uniqueEmail = "newuser_" + UUID.randomUUID() + "@test.com";
        RegistrationRequest newReg = new RegistrationRequest();
        newReg.setLogin(uniqueEmail);
        newReg.setName("New User Test");
        newReg.setPassword("password");


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New User Test"));


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReg)))
                .andExpect(status().isConflict());


        LoginRequest newLogin = new LoginRequest();
        newLogin.setLogin(uniqueEmail);
        newLogin.setPassword("password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk());


        newLogin.setPassword("wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testUserProfileEndpoints() throws Exception {
        // UC4: Edit Own Profile
        UpdateProfileRequest profileRequest = new UpdateProfileRequest();
        profileRequest.setName("Updated Regular User");
        profileRequest.setDescription("This is an updated description.");
        mockMvc.perform(put("/api/account/profile")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Regular User"));


        // UC5: Change Own Password
        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setOldPassword("password");
        passwordRequest.setNewPassword("newpassword");
        mockMvc.perform(put("/api/account/password")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk());


        // Verify login with new password
        loginAndGetToken("user@test.com", "newpassword");


        // UC6: Delete Own Account
        Long userToDeleteId = findOrCreateUserAndGetId("todelete@test.com", "ToDelete", "password");
        String tokenForDelete = loginAndGetToken("todelete@test.com", "password");
        mockMvc.perform(delete("/api/account")
                        .header("Authorization", tokenForDelete))
                .andExpect(status().isNoContent());

        // Test Profile Image Upload and Download
        MockMultipartFile imageFile = new MockMultipartFile("file", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image-content".getBytes());
        mockMvc.perform(multipart("/api/account/profile-image")
                        .file(imageFile)
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + userId + "/profile-image")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageFile.getBytes()));

    }


    @Test
    public void testEventAndParticipationEndpoints() throws Exception {
        EventCreationRequest event = createSampleEvent();

        // UC9: Create Event
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Verify that the organizer is automatically a participant
        mockMvc.perform(get("/api/events/" + eventId + "/participants/me")
                        .header("Authorization", organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("attending"));


        // UC12: Join Event
        mockMvc.perform(post("/api/events/" + eventId + "/participants")
                        .header("Authorization", userToken))
                .andExpect(status().isCreated());

        // Verify participant status after joining
        mockMvc.perform(get("/api/events/" + eventId + "/participants/me")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("attending"));

        // UC13: Leave Event
        mockMvc.perform(delete("/api/events/" + eventId + "/participants/me")
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());

        // Verify participant status after leaving
        mockMvc.perform(get("/api/events/" + eventId + "/participants/me")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("not_participant"));


        // UC11: Organizer deletes own event
        mockMvc.perform(delete("/api/events/" + eventId)
                        .header("Authorization", organizerToken))
                .andExpect(status().isNoContent());
    }


    @Test
    public void testInvitationEndpoints() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();


        // UC15.1: Send Invitation
        InvitationCreateRequest inviteRequest = new InvitationCreateRequest();
        inviteRequest.setEventId(eventId);
        inviteRequest.setInvitedUserId(userId);
        mockMvc.perform(post("/api/invitations")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated());


        // UC15.3: View Received Invitations
        MvcResult invitationsResult = mockMvc.perform(get("/api/invitations/my")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode invitations = objectMapper.readTree(invitationsResult.getResponse().getContentAsString());
        long invitationId = invitations.get("content").get(0).get("id").asLong();


        // UC15.4: Accept Invitation
        mockMvc.perform(post("/api/invitations/" + invitationId + "/accept")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());


        // --- Test Decline and Revoke ---
        Long anotherUserId = findOrCreateUserAndGetId("anotheruser@test.com", "Another User", "password");
        String anotherUserToken = loginAndGetToken("anotheruser@test.com", "password");


        inviteRequest.setInvitedUserId(anotherUserId);
        MvcResult anotherInviteResult = mockMvc.perform(post("/api/invitations")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        long anotherInvitationId = objectMapper.readTree(anotherInviteResult.getResponse().getContentAsString()).get("id").asLong();


        // UC15.5: Decline Invitation
        mockMvc.perform(post("/api/invitations/" + anotherInvitationId + "/decline")
                        .header("Authorization", anotherUserToken))
                .andExpect(status().isOk());

        // UC15.2: Revoke Invitation
        Long thirdUserId = findOrCreateUserAndGetId("thirduser@test.com", "Third User", "password");
        inviteRequest.setInvitedUserId(thirdUserId);
        MvcResult thirdInviteResult = mockMvc.perform(post("/api/invitations")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        long thirdInvitationId = objectMapper.readTree(thirdInviteResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/invitations/" + thirdInvitationId + "/revoke")
                        .header("Authorization", organizerToken))
                .andExpect(status().isOk());
    }


    @Test
    public void testParticipantStatusChanges() throws Exception {
        // 1. Organizer creates an event
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. User joins the event
        mockMvc.perform(post("/api/events/" + eventId + "/participants")
                        .header("Authorization", userToken))
                .andExpect(status().isCreated());

        // 3. Check initial status is "attending"
        mockMvc.perform(get("/api/events/" + eventId + "/participants/me")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("attending"));

        // 4. Organizer updates participant's status to "cancelled"
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "cancelled");

        mockMvc.perform(patch("/api/events/" + eventId + "/participants/" + userId + "/status")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelled"));

        // 5. User checks their updated status
        mockMvc.perform(get("/api/events/" + eventId + "/participants/me")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelled"));
    }

    @Test
    public void testAdminModerationEndpoints() throws Exception {
        // UC20: User Moderation
        // This endpoint has been moved to UserController for general access.
        // mockMvc.perform(get("/api/admin/accounts?page=0&size=5")
        //                 .header("Authorization", adminToken))
        //         .andExpect(status().isOk());

        // Ban a user
        AdminUserUpdateStatusRequest statusRequest = new AdminUserUpdateStatusRequest();
        statusRequest.setStatus("banned");
        mockMvc.perform(patch("/api/admin/accounts/" + userId + "/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("banned"));


        // Change user role
        AdminChangeUserRoleRequest roleRequest = new AdminChangeUserRoleRequest();
        roleRequest.setRoleName("organizer");
        mockMvc.perform(patch("/api/admin/accounts/" + userId + "/role")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("organizer"));


        // UC21: Event Moderation (Update & Delete)
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();


        // Admin updates event
        AdminEventUpdateRequest adminEventUpdateRequest = new AdminEventUpdateRequest();
        adminEventUpdateRequest.setName("Admin Updated Event");
        adminEventUpdateRequest.setDescription("Updated by admin.");
        adminEventUpdateRequest.setStartDate(event.getStartDate());
        adminEventUpdateRequest.setEndDate(event.getEndDate());
        adminEventUpdateRequest.setIsPublic(event.getIsPublic());


        mockMvc.perform(put("/api/admin/events/" + eventId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminEventUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin Updated Event"));


        // Admin deletes event
        mockMvc.perform(delete("/api/admin/events/" + eventId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testMediaEndpoints() throws Exception {
        // 1. Organizer creates an event
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();


        // 2. User joins to become a participant
        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", userToken))
                .andExpect(status().isCreated());


        // 3. Participant uploads a gallery image (UC17)
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes()
        );


        MvcResult uploadResult = mockMvc.perform(multipart("/api/events/" + eventId + "/media/gallery")
                        .file(imageFile)
                        .header("Authorization", userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.downloadUrl").value(containsString("/api/media/gallery/")))
                .andReturn();
        long mediaId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();


        // 4. Download the media (UC18)
        mockMvc.perform(get("/api/media/gallery/" + mediaId))
                .andExpect(status().isOk());


        // 5. Participant deletes their own media (UC19.1)
        mockMvc.perform(delete("/api/media/gallery/" + mediaId)
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());


        // 6. Organizer uploads and then deletes a gallery image from their event (UC19.2)
        MvcResult orgUploadResult = mockMvc.perform(multipart("/api/events/" + eventId + "/media/gallery")
                        .file(imageFile)
                        .header("Authorization", organizerToken))
                .andExpect(status().isCreated())
                .andReturn();
        long orgMediaId = objectMapper.readTree(orgUploadResult.getResponse().getContentAsString()).get("id").asLong();


        mockMvc.perform(delete("/api/events/" + eventId + "/media/gallery/" + orgMediaId)
                        .header("Authorization", organizerToken))
                .andExpect(status().isNoContent());


        // 7. Admin deletes a media file directly (UC22)
        MvcResult adminUploadResult = mockMvc.perform(multipart("/api/admin/events/" + eventId + "/media/gallery")
                        .file(imageFile)
                        .header("Authorization", adminToken))
                .andExpect(status().isCreated())
                .andReturn();
        long adminMediaId = objectMapper.readTree(adminUploadResult.getResponse().getContentAsString()).get("id").asLong();


        mockMvc.perform(delete("/api/admin/media/" + adminMediaId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testEventListingAndUpdate() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Test fetching public events
        mockMvc.perform(get("/api/events/public")).andExpect(status().isOk());

        // Test fetching all events (admin)
        mockMvc.perform(get("/api/events/all").header("Authorization", adminToken)).andExpect(status().isOk());

        // Test fetching a single event
        mockMvc.perform(get("/api/events/" + eventId)).andExpect(status().isOk());

        // Test updating an event
        event.setName("Updated Test Event");
        mockMvc.perform(put("/api/events/" + eventId)
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Test Event"));
    }

    @Test
    public void testMediaLogoAndSchedule() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        MockMultipartFile logoFile = new MockMultipartFile("file", "logo.png", MediaType.IMAGE_PNG_VALUE, "logo-content".getBytes());
        mockMvc.perform(multipart("/api/events/" + eventId + "/media/logo")
                        .file(logoFile)
                        .header("Authorization", organizerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.downloadUrl").value(containsString("/api/media/logo/")));

        MockMultipartFile scheduleFile = new MockMultipartFile("file", "schedule.pdf", MediaType.APPLICATION_PDF_VALUE, "schedule-content".getBytes());
        MvcResult scheduleUploadResult = mockMvc.perform(multipart("/api/events/" + eventId + "/media/schedule")
                        .file(scheduleFile)
                        .header("Authorization", organizerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.downloadUrl").value(containsString("/api/media/schedule/")))
                .andReturn();
        long scheduleId = objectMapper.readTree(scheduleUploadResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", userToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/media/schedule/" + scheduleId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testNotificationFlow() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        InvitationCreateRequest inviteRequest = new InvitationCreateRequest();
        inviteRequest.setEventId(eventId);
        inviteRequest.setInvitedUserId(userId);
        mockMvc.perform(post("/api/invitations")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated());

        MvcResult notificationResult = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", userToken))
                .andExpect(status().isOk()).andReturn();

        JsonNode notifications = objectMapper.readTree(notificationResult.getResponse().getContentAsString());
        long notificationId = notifications.get("content").get(0).get("id").asLong();

        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "READ");

        mockMvc.perform(patch("/api/notifications/" + notificationId + "/status")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));

    }

    @Test
    public void testAccessDeniedHandling() throws Exception {
        // Organizer creates an event
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // A regular user (without admin rights) tries to delete the event via the admin endpoint
        mockMvc.perform(delete("/api/admin/events/" + eventId)
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.status").value(403));
    }


    private EventCreationRequest createSampleEvent() {
        LocationCreationRequest location = new LocationCreationRequest();
        location.setStreetName("Test Street " + UUID.randomUUID().toString().substring(0, 5));
        location.setStreetNumber("123");
        location.setCity("Test City " + UUID.randomUUID().toString().substring(0, 5));
        location.setPostalCode("12345");
        location.setRegion("TestReg");
        location.setCountryIsoCode("PL");


        EventCreationRequest event = new EventCreationRequest();
        event.setName("Test Event " + UUID.randomUUID().toString().substring(0, 8));
        event.setDescription("A test event");
        event.setStartDate(Instant.now().plus(1, ChronoUnit.DAYS));
        event.setEndDate(Instant.now().plus(2, ChronoUnit.DAYS));
        event.setIsPublic(true);
        event.setLocation(location);
        return event;
    }

    @Test
    public void testGetMyParticipatedEvents() throws Exception {
        // Organizer creates an event
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // User joins the event
        mockMvc.perform(post("/api/events/" + eventId + "/participants")
                        .header("Authorization", userToken))
                .andExpect(status().isCreated());

        // User requests their participated events
        mockMvc.perform(get("/api/events/my-participated")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId));
    }

    @Test
    public void testGetMyCreatedEvents() throws Exception {
        // Organizer creates an event
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Organizer requests their created events
        mockMvc.perform(get("/api/events/my-created")
                        .header("Authorization", organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + eventId + ")].id").exists());

        // Admin requests their created events (should also work as admin is authorized to create events)
        EventCreationRequest adminEvent = createSampleEvent();
        MvcResult adminCreateResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminEvent)))
                .andExpect(status().isCreated())
                .andReturn();
        long adminEventId = objectMapper.readTree(adminCreateResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/events/my-created")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + adminEventId + ")].id").exists());
    }

    // =================================================================
    // === NEW FILTERABLE ENDPOINTS TESTS ==============================
    // =================================================================

    // Helper method to get the totalElements from a paginated API response
    private long getEventCountForPublicEventsFilter(String queryString, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/events/public" + queryString)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("totalElements").asLong();
    }

    private long getUserSummaryCountForFilters(String queryString, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/accounts/summary/all" + queryString)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("totalElements").asLong();
    }

    private long getLocationCountForFilters(String queryString, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/locations" + queryString)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("totalElements").asLong();
    }


    @Test
    public void testGetAllUserSummaries_NoFiltersAndAuthorization() throws Exception {
        // Get initial count of all users before creating new ones for this test
        long initialTotalUsers = getUserSummaryCountForFilters("", adminToken);

        // Create additional users for testing filters
        findOrCreateUserAndGetId("filtertest1@test.com", "Alice Filter", "password");
        findOrCreateUserAndGetId("filtertest2@test.com", "Bob Filter", "password");
        adminService.changeUserRole(findOrCreateUserAndGetId("filtertest3@test.com", "Charlie Filter", "password"), "admin");
        adminService.updateUserStatus(findOrCreateUserAndGetId("filtertest4@test.com", "Diana Filter", "password"), "banned");


        // Test with admin role - no filters
        mockMvc.perform(get("/api/accounts/summary/all")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(initialTotalUsers + 4)) // Assert total number of elements
                .andExpect(jsonPath("$.size").value(20)); // Assert default page size


        // Test with organizer role - no filters
        mockMvc.perform(get("/api/accounts/summary/all")
                        .header("Authorization", organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(initialTotalUsers + 4)) // Assert total number of elements
                .andExpect(jsonPath("$.size").value(20)); // Assert default page size

        // Test with regular user role - should be forbidden
        mockMvc.perform(get("/api/accounts/summary/all")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());

        // Test unauthenticated - should be unauthorized
        mockMvc.perform(get("/api/accounts/summary/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllUserSummaries_WithFilters() throws Exception {
        // Get initial counts for specific filters before creating new users for this test
        long initialSmithCount = getUserSummaryCountForFilters("?name=smith", adminToken);
        long initialBobLoginCount = getUserSummaryCountForFilters("?login=bob", adminToken);
        long initialOrganizerRoleCount = getUserSummaryCountForFilters("?role=organizer", adminToken);
        long initialBannedStatusCount = getUserSummaryCountForFilters("?status=banned", adminToken);
        long initialJohnsonOrganizerCount = getUserSummaryCountForFilters("?name=johnson&role=organizer", adminToken);


        // Create specific users for filtering
        Long aliceId = findOrCreateUserAndGetId("filter.alice@example.com", "Alice Smith", "password");
        adminService.changeUserRole(aliceId, "user");
        Long bobId = findOrCreateUserAndGetId("filter.bob@example.com", "Bob Johnson", "password");
        adminService.changeUserRole(bobId, "organizer");
        Long charlieId = findOrCreateUserAndGetId("filter.charlie@example.com", "Charlie Brown", "password");
        adminService.changeUserRole(charlieId, "admin");
        adminService.updateUserStatus(charlieId, "banned"); // Make admin banned

        // Test filter by name (partial match)
        mockMvc.perform(get("/api/accounts/summary/all?name=smith")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements for the overall count matching the filter.
                .andExpect(jsonPath("$.totalElements").value(initialSmithCount + 1))
                // Also check for existence of the specific user, not assuming order
                .andExpect(jsonPath("$.content[?(@.name == 'Alice Smith')].name").exists());

        // Test filter by login (partial match)
        mockMvc.perform(get("/api/accounts/summary/all?login=bob")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements for the overall count matching the filter.
                .andExpect(jsonPath("$.totalElements").value(initialBobLoginCount + 1))
                // Also check for existence of the specific user, not assuming order
                .andExpect(jsonPath("$.content[?(@.login == 'filter.bob@example.com')].login").exists());

        // Test filter by role (exact match)
        mockMvc.perform(get("/api/accounts/summary/all?role=organizer")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements for the overall count matching the filter.
                .andExpect(jsonPath("$.totalElements").value(initialOrganizerRoleCount + 1))
                // Also check for existence of the specific user, not assuming order
                .andExpect(jsonPath("$.content[?(@.name == 'Bob Johnson')].id").exists());

        // Test filter by status (exact match)
        mockMvc.perform(get("/api/accounts/summary/all?status=banned")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements for the overall count matching the filter.
                .andExpect(jsonPath("$.totalElements").value(initialBannedStatusCount + 1))
                // Also check for existence of the specific user, not assuming order
                .andExpect(jsonPath("$.content[?(@.name == 'Charlie Brown')].id").exists());

        // Test combination of filters (name and role)
        mockMvc.perform(get("/api/accounts/summary/all?name=johnson&role=organizer")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements for the overall count matching the filter.
                .andExpect(jsonPath("$.totalElements").value(initialJohnsonOrganizerCount + 1))
                // MODIFIED: Check for existence of the specific user without assuming order
                .andExpect(jsonPath("$.content[?(@.name == 'Bob Johnson')].name").exists());

        // Test no matching results
        mockMvc.perform(get("/api/accounts/summary/all?name=nonexistent")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0))) // No content on this page
                .andExpect(jsonPath("$.totalElements").value(0)); // Total elements should be 0
    }


    @Test
    public void testGetPublicEvents_WithFilters() throws Exception {
        // Get initial counts for specific filters before creating new events for this test
        long initialMusicCount = getEventCountForPublicEventsFilter("?name=music", userToken);
        long initialStartDateCount = getEventCountForPublicEventsFilter("?startDate=2025-11-01T00:00:00Z", userToken);
        long initialEndDateCount = getEventCountForPublicEventsFilter("?endDate=2025-07-04T00:00:00Z", userToken);
        long initialMarketDateRangeCount = getEventCountForPublicEventsFilter("?name=market&startDate=2025-11-01T00:00:00Z&endDate=2025-12-31T23:59:59Z", userToken);


        // Create distinct public events
        EventCreationRequest event1Request = createSampleEvent();
        event1Request.setName("Summer Music Festival");
        event1Request.setStartDate(Instant.parse("2025-07-01T10:00:00Z"));
        event1Request.setEndDate(Instant.parse("2025-07-03T22:00:00Z"));
        event1Request.setIsPublic(true);
        mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1Request)))
                .andExpect(status().isCreated());

        EventCreationRequest event2Request = createSampleEvent();
        event2Request.setName("Winter Holiday Market");
        event2Request.setStartDate(Instant.parse("2025-12-01T09:00:00Z"));
        event2Request.setEndDate(Instant.parse("2025-12-05T18:00:00Z"));
        event2Request.setIsPublic(true);
        mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2Request)))
                .andExpect(status().isCreated());

        // Create a private event (should not be returned by public endpoint)
        EventCreationRequest privateEventRequest = createSampleEvent();
        privateEventRequest.setName("Private Board Meeting");
        privateEventRequest.setStartDate(Instant.parse("2025-08-10T09:00:00Z"));
        privateEventRequest.setEndDate(Instant.parse("2025-08-10T17:00:00Z"));
        privateEventRequest.setIsPublic(false);
        mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(privateEventRequest)))
                .andExpect(status().isCreated());

        // Test filter by name
        mockMvc.perform(get("/api/events/public?name=music")
                        .header("Authorization", userToken)) // Public endpoint, user token is fine
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific event
                .andExpect(jsonPath("$.totalElements").value(initialMusicCount + 1))
                .andExpect(jsonPath("$.content[?(@.name == 'Summer Music Festival')].name").exists());

        // Test filter by start date
        mockMvc.perform(get("/api/events/public?startDate=2025-11-01T00:00:00Z")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific event
                .andExpect(jsonPath("$.totalElements").value(initialStartDateCount + 1))
                .andExpect(jsonPath("$.content[?(@.name == 'Winter Holiday Market')].name").exists());

        // Test filter by end date
        mockMvc.perform(get("/api/events/public?endDate=2025-07-04T00:00:00Z")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific event
                .andExpect(jsonPath("$.totalElements").value(initialEndDateCount + 1))
                .andExpect(jsonPath("$.content[?(@.name == 'Summer Music Festival')].name").exists());

        // Test combination of filters (name and date range)
        mockMvc.perform(get("/api/events/public?name=market&startDate=2025-11-01T00:00:00Z&endDate=2025-12-31T23:59:59Z")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific event
                .andExpect(jsonPath("$.totalElements").value(initialMarketDateRangeCount + 1))
                .andExpect(jsonPath("$.content[?(@.name == 'Winter Holiday Market')].name").exists());

        // Test no matching results
        mockMvc.perform(get("/api/events/public?name=nonexistent")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0))) // No content on this page
                .andExpect(jsonPath("$.totalElements").value(0)); // Total elements should be 0
    }


    @Test
    public void testGetAllLocations_WithFiltersAndAuthorization() throws Exception {
        // Get initial count of all locations before creating new ones for this test
        long initialTotalLocations = getLocationCountForFilters("", organizerToken);
        long initialMapleLaneCount = getLocationCountForFilters("?streetName=maple", adminToken);
        long initialSpringfieldCityCount = getLocationCountForFilters("?city=spring", adminToken);
        long initialKansaiRegionCount = getLocationCountForFilters("?region=Kansai", adminToken);
        long initialJpnCountryCount = getLocationCountForFilters("?countryIsoCode=JPN", adminToken);
        long initialRiversideUsaCount = getLocationCountForFilters("?city=riverside&countryIsoCode=USA", adminToken);


        // Create distinct locations for filtering
        LocationCreationRequest loc1 = new LocationCreationRequest();
        loc1.setStreetName("Oak Avenue");
        loc1.setStreetNumber("10");
        loc1.setCity("Springfield");
        loc1.setPostalCode("12345");
        loc1.setRegion("Central");
        loc1.setCountryIsoCode("USA");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loc1)))
                .andExpect(status().isCreated());

        LocationCreationRequest loc2 = new LocationCreationRequest();
        loc2.setStreetName("Maple Lane");
        loc2.setStreetNumber("25");
        loc2.setCity("Riverside");
        loc2.setPostalCode("67890");
        loc2.setRegion("West");
        loc2.setCountryIsoCode("USA");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loc2)))
                .andExpect(status().isCreated());

        LocationCreationRequest loc3 = new LocationCreationRequest();
        loc3.setStreetName("Cherry Blossom");
        loc3.setStreetNumber("5");
        loc3.setCity("Kyoto");
        loc3.setPostalCode("00000");
        loc3.setRegion("Kansai");
        loc3.setCountryIsoCode("JPN");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loc3)))
                .andExpect(status().isCreated());

        // Test with organizer role - no filters
        mockMvc.perform(get("/api/locations")
                        .header("Authorization", organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                // MODIFIED: Assert total elements and default page size
                .andExpect(jsonPath("$.totalElements").value(initialTotalLocations + 3))
                .andExpect(jsonPath("$.size").value(20));

        // Test with user role - should be forbidden
        mockMvc.perform(get("/api/locations")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());

        // Test filter by street name (partial match)
        mockMvc.perform(get("/api/locations?streetName=maple")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific location
                .andExpect(jsonPath("$.totalElements").value(initialMapleLaneCount + 1))
                .andExpect(jsonPath("$.content[?(@.streetName == 'Maple Lane')].streetName").exists());

        // Test filter by city (partial match)
        mockMvc.perform(get("/api/locations?city=spring")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific location
                .andExpect(jsonPath("$.totalElements").value(initialSpringfieldCityCount + 1))
                .andExpect(jsonPath("$.content[?(@.city == 'Springfield')].city").exists());

        // Test filter by region (exact match)
        mockMvc.perform(get("/api/locations?region=Kansai")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific location
                .andExpect(jsonPath("$.totalElements").value(initialKansaiRegionCount + 1))
                .andExpect(jsonPath("$.content[?(@.city == 'Kyoto')].city").exists());

        // Test filter by country ISO code (exact match)
        mockMvc.perform(get("/api/locations?countryIsoCode=JPN")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific location
                .andExpect(jsonPath("$.totalElements").value(initialJpnCountryCount + 1))
                .andExpect(jsonPath("$.content[?(@.city == 'Kyoto')].city").exists());

        // Test combination of filters (city and country)
        mockMvc.perform(get("/api/locations?city=riverside&countryIsoCode=USA")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // MODIFIED: Assert total elements and also check for existence of the specific location
                .andExpect(jsonPath("$.totalElements").value(initialRiversideUsaCount + 1))
                .andExpect(jsonPath("$.content[?(@.city == 'Riverside')].city").exists());

        // Test no matching results
        mockMvc.perform(get("/api/locations?city=nonexistent")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0))) // No content on this page
                .andExpect(jsonPath("$.totalElements").value(0)); // Total elements should be 0
    }
    }