package pl.sgorski.nethelt.webapi.notification.dto.command;

public record NotificationCommand(Long userId, String title, String content) {}
