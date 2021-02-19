package com.onix.kurento.model.message.output;

import com.onix.kurento.model.message.OutputMessage;
import lombok.Getter;
import lombok.Value;

import static com.onix.kurento.enums.OutputMessageType.WEBRTC_USER_ICE_CANDIDATE;

@Getter
public final class UserIceCandidateOutputMessage extends OutputMessage<UserIceCandidateOutputMessage.AddIceCandidate> {

    public UserIceCandidateOutputMessage(
            final String sdp,
            final String sdpMid,
            final int sdpMLineIndex
    ) {
        super(WEBRTC_USER_ICE_CANDIDATE, new AddIceCandidate(sdp, sdpMid, sdpMLineIndex));
    }

    @Value
    static class AddIceCandidate {

        String sdp;
        String sdpMid;
        int sdpMLineIndex;

    }

}
