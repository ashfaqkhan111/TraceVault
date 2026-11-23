package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.Notification;
import com.tracevault.backend.entity.User;
import com.tracevault.backend.repository.NotificationRepository;
import com.tracevault.backend.repository.UserRepository;
import com.tracevault.backend.enms.NotificationCategory;
import com.tracevault.backend.enms.NotificationTargetType;

@Service 
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    public NotificationService(UserRepository userRepository, NotificationRepository notificationRepository){
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(UUID userId, NotificationCategory notificationCategory,
                                            String title, String body, String actionLabel,
                                            NotificationTargetType targetType, UUID targetId ){
                        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found."));

                        Notification notification  = new Notification();

                        notification.setUser(user);
                        notification.setCategory(notificationCategory);
                        notification.setTitle(title);
                        notification.setBody(body);
                        notification.setActionLabel(actionLabel);
                        notification.setTargetType(targetType);
                        notification.setTargetId(targetId);

                        return notificationRepository.save(notification);
                                            }

    public  List<Notification> getUserNotifications(UUID userId){
        return notificationRepository.findByUser_UserId(userId);
    }

    public List<Notification> getUnreadNotifications(UUID userId){
        return notificationRepository.findByUser_UserIdAndIsReadFalse(userId);
    }

    public long countUnreadNotifications(UUID userId){
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    @Transactional 
    public Notification markAsRead(UUID notificationId, UUID userId){
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                                    () -> new RuntimeException("Notification not found"));

        if(!notification.getUser().getUserId().equals(userId)){
            throw new RuntimeException("You are not authorize to update this notification.");
        }

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    @Transactional 
    public void markAllAsRead(UUID userId){
        List<Notification> notifications = notificationRepository.findByUser_UserIdAndIsReadFalse(userId);

        for(Notification notification : notifications){
                notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(UUID notificationId, UUID userId){
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
            () -> new RuntimeException("notifcation not found."));
        
        if(!notification.getUser().getUserId().equals(userId)){
            throw new RuntimeException("you are not authorize to delete this notification.");

        }

        notificationRepository.delete(notification);
    }
}
