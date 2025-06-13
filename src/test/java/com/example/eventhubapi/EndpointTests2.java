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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class EndpointTests2 extends AbstractTransactionalTestNGSpringContextTests {


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

    // =================================================================
    // === PODSTAWOWE TESTY FUNKCJONALNE ===============================
    // =================================================================

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
    public void testAdminModerationEndpoints() throws Exception {
        // UC20: User Moderation
        mockMvc.perform(get("/api/admin/accounts?page=0&size=5")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

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

    // =================================================================
    // === TESTY RYGORYSTYCZNE I SCENARIUSZE BRZEGOWE ===================
    // =================================================================

    @Test
    public void testStrictValidation_CreateEventWithPastDate() throws Exception {
        EventCreationRequest event = createSampleEvent();
        event.setStartDate(Instant.now().minus(1, ChronoUnit.DAYS));

        mockMvc.perform(post("/api/events")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message", containsString("must be a future date")));
    }

    @Test
    public void testBusinessLogic_CannotRevokeAcceptedInvitation() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(event))).andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        InvitationCreateRequest inviteRequest = new InvitationCreateRequest();
        inviteRequest.setEventId(eventId);
        inviteRequest.setInvitedUserId(userId);
        MvcResult inviteResult = mockMvc.perform(post("/api/invitations").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inviteRequest))).andReturn();
        long invitationId = objectMapper.readTree(inviteResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/invitations/" + invitationId + "/accept").header("Authorization", userToken)).andExpect(status().isOk());

        mockMvc.perform(post("/api/invitations/" + invitationId + "/revoke").header("Authorization", organizerToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only sent invitations can be revoked."));
    }

    @Test
    @Transactional(propagation = Propagation.NEVER) // Disables top-level transaction for this test
    public void testConcurrency_JoinEventWithOneLastSpot() throws Exception {
        EventCreationRequest eventRequest = createSampleEvent();
        eventRequest.setMaxParticipants(2L);

        MvcResult createResult = mockMvc.perform(post("/api/events").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eventRequest))).andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        int numberOfConcurrentUsers = 5;
        String[] userTokens = new String[numberOfConcurrentUsers];
        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            findOrCreateUserAndGetId("concurrent_user_" + i + "@test.com", "Concurrent " + i, "password");
            userTokens[i] = loginAndGetToken("concurrent_user_" + i + "@test.com", "password");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentUsers);
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);

        for (String token : userTokens) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", token))
                            .andDo(result -> {
                                if (result.getResponse().getStatus() == 201) {
                                    successCount.incrementAndGet();
                                }
                            });
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);


        var eventFromDb = eventRepository.findByIdWithParticipants(eventId).orElseThrow();
        long attendingCount = eventFromDb.getParticipants().stream()
                .filter(p -> p.getStatus().getValue().equals("attending"))
                .count();
        assertThat(attendingCount).isEqualTo(2);
    }


    @Test
    public void testParticipantCount_IgnoresCancelledAndBanned() throws Exception {
        EventCreationRequest eventRequest = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eventRequest))).andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/events/" + eventId)).andExpect(status().isOk()).andExpect(jsonPath("$.participantsCount").value(1));

        Long userAttendingId = findOrCreateUserAndGetId("attending@test.com", "Attending", "password");
        String userAttendingToken = loginAndGetToken("attending@test.com", "password");
        Long userCancelledId = findOrCreateUserAndGetId("cancelled@test.com", "Cancelled", "password");
        String userCancelledToken = loginAndGetToken("cancelled@test.com", "password");
        Long userBannedId = findOrCreateUserAndGetId("banned@test.com", "Banned", "password");

        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", userAttendingToken)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", userCancelledToken)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", loginAndGetToken("banned@test.com", "password"))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/events/" + eventId)).andExpect(status().isOk()).andExpect(jsonPath("$.participantsCount").value(4));

        Map<String, String> statusUpdateCancelled = Map.of("status", "cancelled");
        mockMvc.perform(patch("/api/events/" + eventId + "/participants/" + userCancelledId + "/status").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(statusUpdateCancelled)));

        Map<String, String> statusUpdateBanned = Map.of("status", "banned");
        mockMvc.perform(patch("/api/events/" + eventId + "/participants/" + userBannedId + "/status").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(statusUpdateBanned)));

        mockMvc.perform(get("/api/events/" + eventId)).andExpect(status().isOk()).andExpect(jsonPath("$.participantsCount").value(2));
    }

    @Test
    public void testSecurity_OrganizerCannotModifyAnotherOrganizersEvent() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult createResult = mockMvc.perform(post("/api/events").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(event))).andReturn();
        long eventId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        Long organizer2Id = findOrCreateUserAndGetId("organizer2@test.com", "Organizer Two", "password");
        adminService.changeUserRole(organizer2Id, "organizer");
        String organizer2Token = loginAndGetToken("organizer2@test.com", "password");

        event.setName("Hacked Event Name");
        mockMvc.perform(put("/api/events/" + eventId).header("Authorization", organizer2Token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSecurity_UserCannotDeleteAnotherUsersMedia() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult r = mockMvc.perform(post("/api/events").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(event))).andReturn();
        long eventId = objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", userToken)).andExpect(status().isCreated());
        Long user2Id = findOrCreateUserAndGetId("user2@test.com", "User Two", "password");
        String user2Token = loginAndGetToken("user2@test.com", "password");
        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", user2Token)).andExpect(status().isCreated());

        MockMultipartFile imageFile = new MockMultipartFile("file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());
        MvcResult uploadResult = mockMvc.perform(multipart("/api/events/" + eventId + "/media/gallery").file(imageFile).header("Authorization", userToken)).andReturn();
        long mediaId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/media/gallery/" + mediaId).header("Authorization", user2Token)).andExpect(status().isForbidden());
    }

    @Test
    public void testSecurity_BannedUserCannotLogin() throws Exception {
        AdminUserUpdateStatusRequest statusRequest = new AdminUserUpdateStatusRequest();
        statusRequest.setStatus("banned");
        mockMvc.perform(patch("/api/admin/accounts/" + userId + "/status").header("Authorization", adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(statusRequest))).andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin("user@test.com");
        loginRequest.setPassword("password");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isUnauthorized());
    }

    @Test
    public void testStateTransition_CannotJoinEventTwice() throws Exception {
        EventCreationRequest event = createSampleEvent();
        MvcResult r = mockMvc.perform(post("/api/events").header("Authorization", organizerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(event))).andReturn();
        long eventId = objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", userToken)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/events/" + eventId + "/participants").header("Authorization", userToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User is already a participant in this event."));
    }

    @Test
    public void testPagination_RequestingOutOfBoundsPageReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/admin/accounts?page=999").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    public void testStateTransition_UpdateNonExistentEventReturnsNotFound() throws Exception {
        AdminEventUpdateRequest updateRequest = new AdminEventUpdateRequest();
        updateRequest.setName("Does not matter");
        updateRequest.setStartDate(Instant.now().plus(1, ChronoUnit.DAYS));
        updateRequest.setEndDate(Instant.now().plus(2, ChronoUnit.DAYS));
        updateRequest.setIsPublic(true);

        long nonExistentEventId = 99999L;
        mockMvc.perform(put("/api/admin/events/" + nonExistentEventId).header("Authorization", adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    private EventCreationRequest createSampleEvent() {
        LocationCreationRequest location = new LocationCreationRequest();
        location.setStreetName("Test Street");
        location.setStreetNumber("123");
        location.setCity("Test City");
        location.setPostalCode("12345");
        location.setRegion("TestReg");
        location.setCountryIsoCode("PL");


        EventCreationRequest event = new EventCreationRequest();
        event.setName("Test Event " + UUID.randomUUID());
        event.setDescription("A test event");
        event.setStartDate(Instant.now().plus(1, ChronoUnit.DAYS));
        event.setEndDate(Instant.now().plus(2, ChronoUnit.DAYS));
        event.setIsPublic(true);
        event.setLocation(location);
        return event;
    }
}