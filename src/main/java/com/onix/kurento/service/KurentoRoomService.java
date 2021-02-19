package com.onix.kurento.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.*;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KurentoRoomService {

    private final KurentoClient kurentoClient;

    private final ConcurrentMap<Integer, MediaPipeline> roomMediaPipelines = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Composite> roomComposites = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, HubPort> roomHubPorts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, WebRtcEndpoint> userEndpoints = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, WebRtcEndpoint> mixerEndpoints = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, HubPort> mixerHubPorts = new ConcurrentHashMap<>();

    public void initializeEndpoints(
            final int roomId,
            final int userId,
            final EventListener<IceCandidateFoundEvent> listener1,
            final EventListener<IceCandidateFoundEvent> listener2
    ) {
        final MediaPipeline mediaPipeline = this.getMediaPipeline(roomId);

        log.info("Create [OUTGOING_ENDPOINT] for identifier [{}]", userId);
        final WebRtcEndpoint outgoingEndpoint = new WebRtcEndpoint.Builder(mediaPipeline).build();
        outgoingEndpoint.addIceCandidateFoundListener(listener1);
        this.userEndpoints.put(userId, outgoingEndpoint);

        log.info("Create [MIXER_ENDPOINT] for identifier [{}]", userId);
        final WebRtcEndpoint mixerEndpoint = new WebRtcEndpoint.Builder(mediaPipeline).build();
        mixerEndpoint.addIceCandidateFoundListener(listener2);
        this.mixerEndpoints.put(userId, mixerEndpoint);

        log.info("Create [MIXER_HUB_PORT] for identifier [{}]", userId);
        final HubPort mixerHubPort = new HubPort.Builder(this.getComposite(roomId)).build();
        this.mixerHubPorts.put(userId, mixerHubPort);

        this.getHubPort(roomId).connect(mixerEndpoint, MediaType.VIDEO);
        outgoingEndpoint.connect(mixerHubPort, MediaType.VIDEO);
    }

    public String processUserOffer(final int userId, final String sdpOffer) {
        final WebRtcEndpoint userEndpoint = this.getUserEndpoint(userId);
        final String sdpAnswer = userEndpoint.processOffer(sdpOffer);

        userEndpoint.gatherCandidates();

        return sdpAnswer;
    }

    public String processMixerOffer(final int userId, final String sdpOffer) {
        final WebRtcEndpoint userEndpoint = this.getMixerEndpoint(userId);
        final String sdpAnswer = userEndpoint.processOffer(sdpOffer);

        userEndpoint.gatherCandidates();

        return sdpAnswer;
    }

    public WebRtcEndpoint getUserEndpoint(final int userId) {
        return this.userEndpoints.get(userId);
    }

    public WebRtcEndpoint getMixerEndpoint(final int userId) {
        return this.mixerEndpoints.get(userId);
    }

    public void removeUserObjects(final int userId) {
        if (this.userEndpoints.containsKey(userId)) {
            log.info("Release [OUTGOING_ENDPOINT] for identifier [{}]", userId);
            this.userEndpoints.remove(userId).release();
        }
    }

    public void removeMixerObjects(final int userId) {
        if (this.mixerEndpoints.containsKey(userId)) {
            log.info("Release [MIXER_ENDPOINT] for identifier [{}]", userId);
            this.mixerEndpoints.remove(userId).release();
        }

        if (this.mixerHubPorts.containsKey(userId)) {
            log.info("Release [MIXER_HUB_PORT] for identifier [{}]", userId);
            this.mixerHubPorts.remove(userId).release();
        }
    }

    public void addIceCandidateToUserEndpoint(
            final int userId,
            final String sdp,
            final String sdpMid,
            final int sdpMLineIndex
    ) {
        if (this.userEndpoints.containsKey(userId)) {
            this.userEndpoints.get(userId)
                    .addIceCandidate(new IceCandidate(sdp, sdpMid, sdpMLineIndex));
        }
    }

    public void addIceCandidateToMixerEndpoint(
            final int userId,
            final String sdp,
            final String sdpMid,
            final int sdpMLineIndex
    ) {
        if (this.mixerEndpoints.containsKey(userId)) {
            this.mixerEndpoints.get(userId).addIceCandidate(new IceCandidate(sdp, sdpMid, sdpMLineIndex));
        }
    }

    public void removeRoomObjects(final int roodId) {
        if (this.roomHubPorts.containsKey(roodId)) {
            log.info("Release [ROOM_HUB_PORT] for identifier [{}]", roodId);
            this.roomHubPorts.remove(roodId).release();
        }
        if (this.roomComposites.containsKey(roodId)) {
            log.info("Release [ROOM_COMPOSITES] for identifier [{}]", roodId);
            this.roomComposites.remove(roodId).release();
        }
        if (this.roomMediaPipelines.containsKey(roodId)) {
            log.info("Release [ROOM_PIPELINE] for identifier [{}]", roodId);
            this.roomMediaPipelines.remove(roodId).release();
        }
    }

    private MediaPipeline getMediaPipeline(final int roomId) {
        if (!this.roomMediaPipelines.containsKey(roomId)) {
            log.info("Create [ROOM_PIPELINE] for identifier [{}]", roomId);
            this.roomMediaPipelines.put(roomId, this.kurentoClient.createMediaPipeline());
        }

        return this.roomMediaPipelines.get(roomId);
    }

    private Composite getComposite(final int roomId) {
        if (!this.roomComposites.containsKey(roomId)) {
            log.info("Create [ROOM_COMPOSITE] for identifier [{}]", roomId);
            this.roomComposites.put(roomId, new Composite.Builder(this.getMediaPipeline(roomId)).build());
        }

        return this.roomComposites.get(roomId);
    }

    private HubPort getHubPort(final int roomId) {
        if (!this.roomHubPorts.containsKey(roomId)) {
            log.info("Create [ROOM_HUB_PORT] for identifier [{}]", roomId);
            this.roomHubPorts.put(roomId, new HubPort.Builder(this.getComposite(roomId)).build());
        }

        return this.roomHubPorts.get(roomId);
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Start release all media objects.");

        this.userEndpoints.forEach((userId, endpoint) -> {
            log.info("Release [OUTGOING_ENDPOINT] for identifier [{}]", userId);
            endpoint.release();
        });

        this.mixerEndpoints.forEach((userId, endpoint) -> {
            log.info("Release [MIXER_ENDPOINT] for identifier [{}]", userId);
            endpoint.release();
        });

        this.mixerHubPorts.forEach((userId, hubPort) -> {
            log.info("Release [MIXER_HUB_PORT] for identifier [{}]", userId);
            hubPort.release();
        });

        this.roomHubPorts.forEach((userId, hubPort) -> {
            log.info("Release [ROOM_HUB_PORT] for identifier [{}]", userId);
            hubPort.release();
        });

        this.roomComposites.forEach((userId, composite) -> {
            log.info("Release [ROOM_COMPOSITE] for identifier [{}]", userId);
            composite.release();
        });

        this.roomMediaPipelines.forEach((roomId, pipeline) -> {
            log.info("Release [ROOM_PIPELINE] for identifier [{}]", roomId);
            pipeline.release();
        });
    }

}
