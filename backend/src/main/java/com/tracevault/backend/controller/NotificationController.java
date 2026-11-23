package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.tracevault.backend.entity.Notification;
import com.tracevault.backend.enms.NotificationCategory;
import com.tracevault.backend.enms.NotificationTargetType;
import com.tracevault.backend.service.NotificationService;

@RestController 
@RequestMapping ("/api/notifications")
public class NotificationController {

    public final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @PostMapping ("/user/{userId}")
    public Notification createNotification(@PathVariable UUID userId,
                                           @RequestParam NotificationCategory category,
                                           @RequestParam String title,
                                           @RequestParam String body,
                                           @RequestParam (required = false) String actionLabel,
                                           @RequestParam (required = false) NotificationTargetType targetType,
                                           @RequestParam (required = false) UUID targetId){

                    return notificationService.createNotification(userId, category, title, body, actionLabel, targetType, targetId);
                                           }
    @GetMapping ("/user/{user_id}")
    public List<Notification> getUserNotifications(@PathVariable("user_id") UUID userId){
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping ("/user/{user_id}/unread")
    public List<Notification> getUnreadNotification(@PathVariable("user_id") UUID userId){
        return notificationService.getUnreadNotifications(userId);
    }

    @GetMapping ("/user/{user_id}/unread/count")
    public long countUnreadNotifications(@PathVariable("user_id") UUID userId){
        return notificationService.countUnreadNotifications(userId);
    }

    @PutMapping ("/{notificationId}/user/{userId}/read")
    public Notification markAsRead(@PathVariable UUID notificationId, @PathVariable UUID userId){
        return notificationService.markAsRead(notificationId, userId);
    }

    @PutMapping ("/user/{userId}/read-all")
    public String markAllAsRead(@PathVariable UUID userId){
        notificationService.markAllAsRead(userId);

        return "All notification marked as read.";
    }

    @DeleteMapping ("/{notificationId}/user/{userId}")
    public String deleteNotification(@PathVariable UUID notificationId, @PathVariable UUID userId){
        notificationService.deleteNotification(notificationId, userId);

        return "Notification deleted successfully.";
    }
    
}
