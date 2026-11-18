package com.goevent.service;

import com.goevent.dto.UserDTO;
import com.goevent.repository.AuthenticationRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleAdminAuthenticationTest {
    @Test void invalidTokenCannotReachDatabaseOrCreateAnAdminSession() throws Exception {
        var repository=mock(AuthenticationRepo.class);
        var service=new AuthenticationService(repository,new BCryptPasswordEncoder(4));
        ReflectionTestUtils.setField(service,"googleClientId","fixture.apps.googleusercontent.com");
        var verifier=mock(GoogleIdTokenVerifier.class);
        try(var builders=mockConstruction(GoogleIdTokenVerifier.Builder.class,(builder,context)->{
            when(builder.setAudience(anyCollection())).thenReturn(builder);when(builder.build()).thenReturn(verifier);
        })) {
            var request=new MockHttpServletRequest();
            var result=JsonParser.parseString(service.handleGoogleAuthentication(Map.of("idToken","invalid-fixture"),request)).getAsJsonObject();
            assertFalse(result.get("status").getAsBoolean());assertNull(request.getSession(false));verifyNoInteractions(repository);
        }
    }
    @Test void verifiedGoogleOrganizerRegistrationIsActiveWithoutAdminReview() throws Exception {
        var users=mock(AuthenticationRepo.class);
        var service=new AuthenticationService(users,new BCryptPasswordEncoder(4));
        ReflectionTestUtils.setField(service,"googleClientId","fixture.apps.googleusercontent.com");
        var verifier=mock(GoogleIdTokenVerifier.class);var token=mock(GoogleIdToken.class);
        var payload=new GoogleIdToken.Payload();payload.setSubject("organizer-subject");
        payload.setEmail("organizer@example.invalid");payload.setEmailVerified(true);
        when(token.getPayload()).thenReturn(payload);when(verifier.verify("signed-fixture")).thenReturn(token);
        var user=new UserDTO();user.setId(7);user.setRoleId(2);user.setUserStatusId(1);user.setEmail("organizer@example.invalid");
        when(users.registerGoogleUser(anyString(),anyString(),anyString(),anyInt(),anyInt(),anyString(),any(),anyInt())).thenReturn(1);
        when(users.getUserByEmail("organizer@example.invalid")).thenReturn(user);
        try(var builders=mockConstruction(GoogleIdTokenVerifier.Builder.class,(builder,context)->{
            when(builder.setAudience(anyCollection())).thenReturn(builder);when(builder.build()).thenReturn(verifier);
        })) {
            var request=new MockHttpServletRequest();
            var result=JsonParser.parseString(service.handleGoogleAuthentication(Map.of("idToken","signed-fixture","roleId",2,"orgId",1,"position","Chair"),request)).getAsJsonObject();
            assertTrue(result.get("status").getAsBoolean());assertEquals("REGISTERED",result.get("action").getAsString());
            assertEquals(7,request.getSession().getAttribute("userId"));
            verify(users).registerOrganizerData(7,1,"Chair");
        }
    }

}
