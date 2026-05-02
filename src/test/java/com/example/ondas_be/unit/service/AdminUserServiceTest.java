package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminUserFilterRequest;
import com.example.ondas_be.application.dto.request.BanUserRequest;
import com.example.ondas_be.application.dto.response.AdminUserResponse;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.service.impl.AdminUserService;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.AdminUserRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepoPort userRepoPort;

    @Mock
    private AdminUserRepoPort adminUserRepoPort;

    @InjectMocks
    private AdminUserService adminUserService;

    private UUID userId;
    private User activeUser;
    private User bannedUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        activeUser = new User(
                userId,
                "user@example.com",
                "hashed-password",
                "Test User",
                "http://avatar.url",
                true,
                null,
                null,
                LocalDateTime.now().minusDays(1),
                Role.USER,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );

        bannedUser = new User(
                userId,
                "user@example.com",
                "hashed-password",
                "Test User",
                "http://avatar.url",
                false,
                "Violated terms",
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(1),
                Role.USER,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );
    }

    // ── getUsers ────────────────────────────────────────────────────────────

    @Test
    void getUsers_ShouldReturnPageResult_WhenNoFilter() {
        AdminUserFilterRequest filter = new AdminUserFilterRequest();
        var page = new PageImpl<>(List.of(activeUser), PageRequest.of(0, 20), 1);

        when(adminUserRepoPort.findAllWithFilters(isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        PageResultDto<AdminUserResponse> result = adminUserService.getUsers(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getItems().size());
        assertEquals("user@example.com", result.getItems().get(0).getEmail());
        assertTrue(result.getItems().get(0).isActive());
    }

    @Test
    void getUsers_ShouldPassKeywordTrimmed_WhenKeywordProvided() {
        AdminUserFilterRequest filter = new AdminUserFilterRequest();
        filter.setKeyword("  test  ");
        var page = new PageImpl<>(List.of(activeUser), PageRequest.of(0, 20), 1);

        when(adminUserRepoPort.findAllWithFilters(eq("test"), isNull(), isNull(), any()))
                .thenReturn(page);

        PageResultDto<AdminUserResponse> result = adminUserService.getUsers(filter);

        assertEquals(1, result.getItems().size());
        verify(adminUserRepoPort).findAllWithFilters(eq("test"), isNull(), isNull(), any());
    }

    @Test
    void getUsers_ShouldFilterByActiveAndRole() {
        AdminUserFilterRequest filter = new AdminUserFilterRequest();
        filter.setRole(Role.USER);
        filter.setActive(true);
        var page = new PageImpl<>(List.of(activeUser), PageRequest.of(0, 20), 1);

        when(adminUserRepoPort.findAllWithFilters(isNull(), eq("USER"), eq(true), any()))
                .thenReturn(page);

        PageResultDto<AdminUserResponse> result = adminUserService.getUsers(filter);

        assertEquals(1, result.getItems().size());
        verify(adminUserRepoPort).findAllWithFilters(isNull(), eq("USER"), eq(true), any());
    }

    @Test
    void getUsers_ShouldReturnEmptyPage_WhenNoUsersMatch() {
        AdminUserFilterRequest filter = new AdminUserFilterRequest();
        filter.setKeyword("nonexistent");
        var page = new PageImpl<User>(List.of(), PageRequest.of(0, 20), 0);

        when(adminUserRepoPort.findAllWithFilters(eq("nonexistent"), isNull(), isNull(), any()))
                .thenReturn(page);

        PageResultDto<AdminUserResponse> result = adminUserService.getUsers(filter);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getItems().isEmpty());
    }

    // ── getUserById ─────────────────────────────────────────────────────────

    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepoPort.findById(userId)).thenReturn(Optional.of(activeUser));

        AdminUserResponse response = adminUserService.getUserById(userId);

        assertEquals(userId, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertTrue(response.isActive());
        assertNull(response.getBanReason());
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(userRepoPort.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> adminUserService.getUserById(unknownId));
    }

    // ── banUser ─────────────────────────────────────────────────────────────

    @Test
    void banUser_ShouldSetActiveToFalseAndSetBanReason() {
        BanUserRequest request = new BanUserRequest();
        request.setBanReason("Spam activity");

        when(userRepoPort.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepoPort.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            // trả về chính user đã save (có ban info)
            return new User(
                    saved.getId(), saved.getEmail(), saved.getPasswordHash(),
                    saved.getDisplayName(), saved.getAvatarUrl(),
                    saved.isActive(), saved.getBanReason(), saved.getBannedAt(),
                    saved.getLastLoginAt(), saved.getRole(),
                    saved.getCreatedAt(), saved.getUpdatedAt()
            );
        });

        AdminUserResponse response = adminUserService.banUser(userId, request);

        assertFalse(response.isActive());
        assertEquals("Spam activity", response.getBanReason());
        assertNotNull(response.getBannedAt());

        // Xác nhận user được lưu với active = false
        verify(userRepoPort).save(argThat(u -> !u.isActive()
                && "Spam activity".equals(u.getBanReason())
                && u.getBannedAt() != null));
    }

    @Test
    void banUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        UUID unknownId = UUID.randomUUID();
        BanUserRequest request = new BanUserRequest();
        request.setBanReason("Reason");

        when(userRepoPort.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> adminUserService.banUser(unknownId, request));
        verify(userRepoPort, never()).save(any());
    }

    // ── unbanUser ────────────────────────────────────────────────────────────

    @Test
    void unbanUser_ShouldClearBanInfoAndSetActiveToTrue() {
        when(userRepoPort.findById(userId)).thenReturn(Optional.of(bannedUser));
        when(userRepoPort.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            return new User(
                    saved.getId(), saved.getEmail(), saved.getPasswordHash(),
                    saved.getDisplayName(), saved.getAvatarUrl(),
                    saved.isActive(), saved.getBanReason(), saved.getBannedAt(),
                    saved.getLastLoginAt(), saved.getRole(),
                    saved.getCreatedAt(), saved.getUpdatedAt()
            );
        });

        AdminUserResponse response = adminUserService.unbanUser(userId);

        assertTrue(response.isActive());
        assertNull(response.getBanReason());
        assertNull(response.getBannedAt());

        verify(userRepoPort).save(argThat(u -> u.isActive()
                && u.getBanReason() == null
                && u.getBannedAt() == null));
    }

    @Test
    void unbanUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(userRepoPort.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> adminUserService.unbanUser(unknownId));
        verify(userRepoPort, never()).save(any());
    }
}
