package com.kos.service;

import com.kos.dto.AuthUser;
import com.kos.dto.Restaurent;
import com.kos.dto.UpdateContactRequest;
import com.kos.dto.UpdateRestaurantRequest;
import com.kos.repository.RestaurentRepository;
import com.kos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private UserRepository users;
    private RestaurentRepository restaurants;
    private OtpService otp;
    private ProfileService svc;

    private AuthUser owner;

    @BeforeEach
    void setup() {
        users       = mock(UserRepository.class);
        restaurants = mock(RestaurentRepository.class);
        otp         = mock(OtpService.class);
        svc         = new ProfileService(users, restaurants, otp);

        owner = new AuthUser();
        owner.setStaffId(1);
        owner.setUsername("owner1");
        owner.setName("Asha Rao");
        owner.setMobile("+919876543210");
        owner.setEmail("asha@example.com");
        owner.setRestaurantId("7");

        when(users.findByUsername("owner1")).thenReturn(Optional.of(owner));
    }

    @Test
    void updateContact_throws403WhenOldNotVerified() {
        UpdateContactRequest req = new UpdateContactRequest();
        req.setField("mobile"); req.setNewValue("+919999999999");

        when(otp.wasRecentlyVerified("owner1", "+919876543210")).thenReturn(false);
        when(otp.wasRecentlyVerified("owner1", "asha@example.com")).thenReturn(false);
        when(otp.wasRecentlyVerified("owner1", "+919999999999")).thenReturn(true);

        assertThrows(ProfileService.VerificationRequiredException.class,
            () -> svc.updateContact("owner1", req));
        verify(users, never()).save(any());
    }

    @Test
    void updateContact_throws403WhenNewNotVerified() {
        UpdateContactRequest req = new UpdateContactRequest();
        req.setField("mobile"); req.setNewValue("+919999999999");

        when(otp.wasRecentlyVerified("owner1", "+919876543210")).thenReturn(true);
        when(otp.wasRecentlyVerified("owner1", "+919999999999")).thenReturn(false);

        assertThrows(ProfileService.VerificationRequiredException.class,
            () -> svc.updateContact("owner1", req));
        verify(users, never()).save(any());
    }

    @Test
    void updateContact_acceptsAlternateChannelForOldGate() {
        UpdateContactRequest req = new UpdateContactRequest();
        req.setField("mobile"); req.setNewValue("+919999999999");

        when(otp.wasRecentlyVerified("owner1", "+919876543210")).thenReturn(false);
        when(otp.wasRecentlyVerified("owner1", "asha@example.com")).thenReturn(true);
        when(otp.wasRecentlyVerified("owner1", "+919999999999")).thenReturn(true);
        when(users.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));

        svc.updateContact("owner1", req);

        ArgumentCaptor<AuthUser> cap = ArgumentCaptor.forClass(AuthUser.class);
        verify(users).save(cap.capture());
        assertEquals("+919999999999", cap.getValue().getMobile());
    }

    @Test
    void updateContact_writesNewMobileOnHappyPath() {
        UpdateContactRequest req = new UpdateContactRequest();
        req.setField("mobile"); req.setNewValue("+919999999999");

        when(otp.wasRecentlyVerified("owner1", "+919876543210")).thenReturn(true);
        when(otp.wasRecentlyVerified("owner1", "+919999999999")).thenReturn(true);
        when(users.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));

        svc.updateContact("owner1", req);

        ArgumentCaptor<AuthUser> cap = ArgumentCaptor.forClass(AuthUser.class);
        verify(users).save(cap.capture());
        assertEquals("+919999999999", cap.getValue().getMobile());
        verify(otp).clearVerification("owner1", "+919876543210");
        verify(otp).clearVerification("owner1", "asha@example.com");
        verify(otp).clearVerification("owner1", "+919999999999");
    }

    @Test
    void updateRestaurant_writesGstinOnlyWhenCurrentlyBlank() {
        Restaurent r = new Restaurent();
        r.setRestaurentId(7);
        r.setRestaurentName("Spice");
        r.setGstin(null);
        r.setFssai("12345678901234"); // already set
        when(restaurants.findById(7)).thenReturn(Optional.of(r));
        when(restaurants.save(any(Restaurent.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateRestaurantRequest req = new UpdateRestaurantRequest();
        req.setAddress("New Address");
        req.setGstin("22AAAAA0000A1Z5");
        req.setFssai("99999999999999"); // should be IGNORED

        svc.updateRestaurant("owner1", req);

        ArgumentCaptor<Restaurent> cap = ArgumentCaptor.forClass(Restaurent.class);
        verify(restaurants).save(cap.capture());
        assertEquals("New Address", cap.getValue().getAddress());
        assertEquals("22AAAAA0000A1Z5", cap.getValue().getGstin());
        assertEquals("12345678901234", cap.getValue().getFssai());
    }
}
