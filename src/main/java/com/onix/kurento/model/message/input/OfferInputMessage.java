package com.onix.kurento.model.message.input;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "sdp")
public class OfferInputMessage {

    String sdp;

}
