package net.dehasher.hlib;

import net.dehasher.hlib.config.Info;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public record Console(CommandSender commandSender) implements ConsoleCommandSender {
    @Override
    public void sendMessage(@NonNull String message) {
        if (commandSender != null) commandSender.sendMessage(message);
    }

    @Override
    public void sendMessage(String... messages) {
        if (commandSender != null) commandSender.sendMessage(messages);
    }

    @Override
    public void sendMessage(UUID sender, @NonNull String message) {
        if (commandSender != null) commandSender.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID sender, String... messages) {
        if (commandSender != null) commandSender.sendMessage(messages);
    }

    @Override
    public @NonNull Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public @NonNull String getName() {
        return commandSender instanceof Player ? commandSender.getName() : Info.consoleName;
    }

    @Override
    public @NonNull Spigot spigot() {
        return Bukkit.getConsoleSender().spigot();
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(@NonNull String input) {}

    @Override
    public boolean beginConversation(@NonNull Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(@NonNull Conversation conversation) {}

    @Override
    public void abandonConversation(@NonNull Conversation conversation, @NonNull ConversationAbandonedEvent details) {}

    @Override
    public void sendRawMessage(@NonNull String message) {}

    @Override
    public void sendRawMessage(UUID sender, @NonNull String message) {}

    @Override
    public boolean isPermissionSet(@NonNull String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(@NonNull Permission perm) {
        return true;
    }

    @Override
    public boolean hasPermission(@NonNull String name) {
        return true;
    }

    @Override
    public boolean hasPermission(@NonNull Permission perm) {
        return true;
    }

    @Override
    public @NonNull PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value) {
        return new PermissionAttachment(plugin, commandSender);
    }

    @Override
    public @NonNull PermissionAttachment addAttachment(@NonNull Plugin plugin) {
        return new PermissionAttachment(plugin, commandSender);
    }

    @Override
    public PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(@NonNull Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(@NonNull PermissionAttachment attachment) {}

    @Override
    public void recalculatePermissions() {}

    @Override
    public @NonNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {}
}