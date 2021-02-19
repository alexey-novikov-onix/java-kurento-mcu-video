package com.onix.kurento.model.message.output;

import com.onix.kurento.model.message.OutputMessage;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import static com.onix.kurento.enums.OutputMessageType.WEBRTC_MIXER_ANSWER;

@Getter
public final class MixerAnswerOutputMessage extends OutputMessage<MixerAnswerOutputMessage.AnswerCandidate> {

    public MixerAnswerOutputMessage(final String sdp) {
        super(WEBRTC_MIXER_ANSWER, new AnswerCandidate(sdp));
    }

    @Value
    @ToString(exclude = "sdp")
    static class AnswerCandidate {

        String sdp;

    }

}
