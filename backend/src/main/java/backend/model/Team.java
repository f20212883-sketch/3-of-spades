package backend.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Team {

    private final Set<Player> members;

    public Team() {
        this.members = new HashSet<>();
    }

    public Team(Set<Player> members) {
        this.members = new HashSet<>(members);
    }

    // =====================================
    // TEAM OPERATIONS
    // =====================================

    public void addMember(Player player) {

        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        members.add(player);
    }

    public void removeMember(Player player) {
        members.remove(player);
    }

    public boolean contains(Player player) {
        return members.contains(player);
    }

    public int size() {
        return members.size();
    }

    public void clear() {
        members.clear();
    }

    // =====================================
    // GETTERS
    // =====================================

    public Set<Player> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public void setMembers(Set<Player> newMembers) {

        members.clear();

        if (newMembers != null) {
            members.addAll(newMembers);
        }
    }

    @Override
    public String toString() {
        return "Team{" +
                "members=" + members +
                '}';
    }
}