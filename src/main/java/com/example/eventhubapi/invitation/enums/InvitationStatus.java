package com.example.eventhubapi.invitation.enums;

public enum InvitationStatus {
    SENT,
    ACCEPTED,
    DECLINED,
    REVOKED, // For when the sender cancels the invitation
    EXPIRED
}