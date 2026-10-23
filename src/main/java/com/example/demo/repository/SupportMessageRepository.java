package com.example.demo.repository;

import com.example.demo.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByEmployeeIdAndOtherPartyUsernameOrderBySentDateAsc(Long employeeId, String otherPartyUsername);

    @Query("SELECT DISTINCT s.employeeId, s.otherPartyUsername FROM SupportMessage s WHERE s.otherPartyUsername = :staffUsername")
    List<Object[]> findThreadsForStaff(String staffUsername);

    @Query("SELECT DISTINCT s.otherPartyUsername FROM SupportMessage s WHERE s.employeeId = :employeeId")
    List<String> findStaffContactsForEmployee(Long employeeId);

    @Query("SELECT s FROM SupportMessage s WHERE " +
            "(s.senderUsername = :userA AND s.receiverUsername = :userB) OR " +
            "(s.senderUsername = :userB AND s.receiverUsername = :userA) " +
            "ORDER BY s.sentDate ASC")
    List<SupportMessage> findConversationBetween(@Param("userA") String userA, @Param("userB") String userB);

    @Query("SELECT DISTINCT CASE WHEN s.senderUsername = :username THEN s.receiverUsername ELSE s.senderUsername END " +
            "FROM SupportMessage s WHERE s.senderUsername = :username OR s.receiverUsername = :username")
    List<String> findDistinctContactsForUser(@Param("username") String username);

    long countByReceiverUsernameAndIsReadFalse(String receiverUsername);

    long countBySenderUsernameAndReceiverUsernameAndIsReadFalse(String senderUsername, String receiverUsername);

    List<SupportMessage> findBySenderUsernameAndReceiverUsernameAndIsReadFalse(String senderUsername, String receiverUsername);
}