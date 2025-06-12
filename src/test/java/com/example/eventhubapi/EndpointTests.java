package com.example.eventhubapi;

import com.example.eventhubapi.admin.AdminService;
import com.example.eventhubapi.admin.dto.AdminEventUpdateRequest;
import com.example.eventhubapi.admin.dto.AdminChangeUserRoleRequest;
import com.example.eventhubapi.admin.dto.AdminUserUpdateStatusRequest;
import com.example.eventhubapi.auth.dto.LoginRequest;
import com.example.eventhubapi.auth.dto.RegistrationRequest;
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


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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


    @Test
    public void testAuthEndpoints() throws Exception {
        String uniqueEmail = "newuser_" + UUID.randomUUID() + "@test.com";
        RegistrationRequest newReg = new RegistrationRequest();
        newReg.setLogin(uniqueEmail);
        newReg.setName("New User");
        newReg.setPassword("password");


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReg)))
                .andExpect(status().isCreated());


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


        // UC12: Join Event
        mockMvc.perform(post("/api/events/" + eventId + "/participants")
                        .header("Authorization", userToken))
                .andExpect(status().isCreated());


        // UC13: Leave Event
        mockMvc.perform(delete("/api/events/" + eventId + "/participants/me")
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());


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
                .andExpect(status().isCreated());

        MockMultipartFile scheduleFile = new MockMultipartFile("file", "schedule.pdf", MediaType.APPLICATION_PDF_VALUE, "schedule-content".getBytes());
        MvcResult scheduleUploadResult = mockMvc.perform(multipart("/api/events/" + eventId + "/media/schedule")
                        .file(scheduleFile)
                        .header("Authorization", organizerToken))
                .andExpect(status().isCreated()).andReturn();
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