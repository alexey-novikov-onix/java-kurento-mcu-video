package com.onix.kurento.service;

import com.onix.kurento.model.User;
import com.onix.kurento.model.message.output.*;
import lombok.RequiredArgsConstructor;
import org.kurento.client.IceCandidate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebRtcService {

    private final UserService userService;
    private final RoomUserService roomUserService;
    private final KurentoRoomService kurentoRoomService;
    private final StompMessagingService stompMessagingService;

    public void userJoin(final int userId, final int roomId) {
        final Optional<User> userOptional = this.userService.findById(userId);

        if (userOptional.isPresent()) {
            final User user = userOptional.get();

            final List<User> roomUsers = this.roomUserService.findUsersByRoomId(roomId);

            final Optional<User> roomUserOptional = this.roomUserService.findUserById(userId);

            if (roomUserOptional.isPresent()) {
                this.kurentoRoomService.removeUserObjects(userId);
                this.kurentoRoomService.removeMixerObjects(userId);
            } else {
                this.roomUserService.add(user, roomId);
            }

            this.kurentoRoomService.initializeEndpoints(
                    roomId,
                    userId,
                    userEvent -> {
                        final IceCandidate iceCandidate = userEvent.getCandidate();
                        this.stompMessagingService.sendToUser(userId, new UserIceCandidateOutputMessage(
                                iceCandidate.getCandidate(),
                                iceCandidate.getSdpMid(),
                                iceCandidate.getSdpMLineIndex()
                        ));
                    },
                    mixerEvent -> {
                        final IceCandidate iceCandidate = mixerEvent.getCandidate();
                        this.stompMessagingService.sendToUser(userId, new MixerIceCandidateOutputMessage(
                                iceCandidate.getCandidate(),
                                iceCandidate.getSdpMid(),
                                iceCandidate.getSdpMLineIndex()
                        ));
                    }
            );

            roomUsers.forEach(roomUser -> this.stompMessagingService.sendToUser(
                    roomUser.getId(),
                    new RoomUserAddedOutputMessage(user)
            ));

            roomUsers.add(user);

            this.stompMessagingService.sendToUser(userId, new RoomUsersOutputMessage(roomUsers));
        }
    }

    public void userOffer(final int userId, final String sdpOffer) {
        final Optional<User> roomUserOptional = this.roomUserService.findUserById(userId);

        if (roomUserOptional.isPresent()) {
            final String sdpAnswer = this.kurentoRoomService.processUserOffer(userId, sdpOffer);

            this.stompMessagingService.sendToUser(userId, new UserAnswerOutputMessage(sdpAnswer));
        }
    }

    public void mixerOffer(final int userId, final String sdpOffer) {
        final Optional<User> roomUserOptional = this.roomUserService.findUserById(userId);

        if (roomUserOptional.isPresent()) {
            final String sdpAnswer = this.kurentoRoomService.processMixerOffer(userId, sdpOffer);

            this.stompMessagingService.sendToUser(userId, new MixerAnswerOutputMessage(sdpAnswer));
        }
    }

    public void userLeave(final int userId) {
        final Optional<User> roomUserOptional = this.roomUserService.findUserById(userId);

        if (roomUserOptional.isPresent()) {
            final User user = roomUserOptional.get();

            final int roomId = this.roomUserService.findRoomIdByUserId(userId);

            final List<User> roomUsers = this.roomUserService.findUsersByRoomId(roomId);
            roomUsers.stream()
                    .filter(u -> u.getId() != userId)
                    .forEach(roomUser -> this.stompMessagingService.sendToUser(
                        roomUser.getId(),
                        new RoomUserLeftOutputMessage(user)
                ));

            if (roomUsers.isEmpty()) {
                this.kurentoRoomService.removeRoomObjects(roomId);
            }

            this.roomUserService.delete(userId);

            this.kurentoRoomService.removeUserObjects(userId);
            this.kurentoRoomService.removeMixerObjects(userId);
        }
    }

    public void userIceCandidate(
            final int userId,
            final String sdp,
            final String sdpMid,
            final int sdpMLineIndex
    ) {
        this.kurentoRoomService.addIceCandidateToUserEndpoint(userId, sdp, sdpMid, sdpMLineIndex);
    }

    public void mixerIceCandidate(
            final int userId,
            final String sdp,
            final String sdpMid,
            final int sdpMLineIndex
    ) {
       this.kurentoRoomService.addIceCandidateToMixerEndpoint(userId, sdp, sdpMid, sdpMLineIndex);
    }

}
