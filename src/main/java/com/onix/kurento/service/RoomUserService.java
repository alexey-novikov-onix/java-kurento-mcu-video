package com.onix.kurento.service;

import com.onix.kurento.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class RoomUserService {

    private final ConcurrentMap<Integer, User> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> roomUsers = new ConcurrentHashMap<>();

    public void add(final User user, final int roomId) {
        this.users.put(user.getId(), user);
        this.roomUsers.put(user.getId(), roomId);
    }

    public Optional<User> findUserById(final int id) {
        final User user = this.users.get(id);

        return Objects.isNull(user) ? Optional.empty() : Optional.of(user);
    }

    public void delete(final int userId) {
        this.users.remove(userId);
        this.roomUsers.remove(userId);
    }

    public List<User> findUsersByRoomId(final int roomId) {
        return this.roomUsers.entrySet().stream()
                .filter(entry -> entry.getValue().equals(roomId))
                .map(entry -> this.users.get(entry.getKey()))
                .collect(Collectors.toList());
    }

    public int findRoomIdByUserId(final int userId) {
        return this.roomUsers.get(userId);
    }

}
