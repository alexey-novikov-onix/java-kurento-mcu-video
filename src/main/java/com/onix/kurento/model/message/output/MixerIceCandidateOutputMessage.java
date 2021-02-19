package com.onix.kurento.model.message.output;

import com.onix.kurento.model.message.OutputMessage;
import lombok.Getter;
import lombok.Value;

import static com.onix.kurento.enums.OutputMessageType.WEBRTC_MIXER_ICE_CANDIDATE;

@Getter
public final class MixerIceCandidateOutputMessage extends OutputMessage<MixerIceCandidateOutputMessage.AddIceCandidate> {

    public MixerIceCandidateOutputMessage(
            final String sdp,
            final String sdpMid,
            final int sdpMLineIndex
    ) {
        super(WEBRTC_MIXER_ICE_CANDIDATE, new AddIceCandidate(sdp, sdpMid, sdpMLineIndex));
    }

    @Value
    static class AddIceCandidate {

        String sdp;
        String sdpMid;
        int sdpMLineIndex;

    }

}
