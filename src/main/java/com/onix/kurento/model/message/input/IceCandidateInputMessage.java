package com.onix.kurento.model.message.input;

import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class IceCandidateInputMessage {

    String sdp;
    String sdpMid;
    int sdpMLineIndex;

}
