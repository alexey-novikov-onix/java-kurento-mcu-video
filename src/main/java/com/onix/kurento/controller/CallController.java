package com.onix.kurento.controller;

import com.onix.kurento.model.UserPrincipal;
import com.onix.kurento.model.message.input.IceCandidateInputMessage;
import com.onix.kurento.model.message.input.JoinInputMessage;
import com.onix.kurento.model.message.input.OfferInputMessage;
import com.onix.kurento.service.WebRtcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
@MessageMapping("/webrtc")
public final class CallController {

    private final WebRtcService webRtcService;

    @MessageMapping("/user/join")
    void userJoin(final @Payload JoinInputMessage message, final UserPrincipal principal) {
        log.info("INCOMING JOIN {}, user {}", message, principal.getId());
        this.webRtcService.userJoin(principal.getId(), message.getRoomId());
    }

    @MessageMapping("/user/offer")
    void userOffer(final @Payload OfferInputMessage message, final UserPrincipal principal) {
        log.info("INCOMING USER OFFER {}, user {}", message, principal.getId());
        this.webRtcService.userOffer(principal.getId(), message.getSdp());
    }

    @MessageMapping("/mixer/offer")
    void mixerOffer(final @Payload OfferInputMessage message, final UserPrincipal principal) {
        log.info("INCOMING MIXER OFFER {}, user {}", message, principal.getId());
        this.webRtcService.mixerOffer(principal.getId(), message.getSdp());
    }

    @MessageMapping("/user/leave")
    void userLeave(final UserPrincipal principal) {
        log.info("INCOMING LEAVE, user {}", principal.getId());
        this.webRtcService.userLeave(principal.getId());
    }

    @MessageMapping("/user/ice-candidate")
    void userIceCandidate(
            final @Payload IceCandidateInputMessage message,
            final UserPrincipal principal
    ) {
        log.info("INCOMING ICE CANDIDATE {}, user {}", message, principal.getId());
        this.webRtcService.userIceCandidate(
                principal.getId(),
                message.getSdp(),
                message.getSdpMid(),
                message.getSdpMLineIndex()
        );
    }

    @MessageMapping("/mixer/ice-candidate")
    void mixerIceCandidate(
            final @Payload IceCandidateInputMessage message,
            final UserPrincipal principal
    ) {
        log.info("INCOMING ICE CANDIDATE {}, user {}", message, principal.getId());
        this.webRtcService.mixerIceCandidate(
                principal.getId(),
                message.getSdp(),
                message.getSdpMid(),
                message.getSdpMLineIndex()
        );
    }

}
