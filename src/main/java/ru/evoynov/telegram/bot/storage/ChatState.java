package ru.evoynov.telegram.bot.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evoynov.telegram.bot.flow.command.selector.SelectorState;
import ru.evoynov.telegram.bot.flow.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class ChatState {

    @Getter
    private long chatId;

    @Getter
    private String phone;

    @Getter
    private String fullName;

    @Getter
    private String pendingFullName;

    @Getter
    private CarType carType;

    @Getter
    @JsonIgnore
    private SelectorState selectorState;

    @Getter
    private Stage currentStage;

    @Getter
    private int menuMessageId = -1;

    @Getter
    private boolean approved = false;

    @JsonIgnore
    private final List<Update> updates = new ArrayList<>();

    @Setter
    @JsonIgnore
    private ChatStateMap storage;

    public ChatState(long chatId, ChatStateMap storage) {
        this.chatId = chatId;
        this.setStorage(storage);
    }

    @JsonIgnore
    public String getChatIdStr() {
        return String.valueOf(getChatId());
    }

    public void setCurrentStage(Stage stage) {
        currentStage = stage;
        updates.clear();
        getStorage().ifPresent(ChatStateMap::store);
    }

    public void addUpdate(Update update) {
        updates.add(update);
    }

    @JsonIgnore
    public int getLastReceivedMessageId() {
        if (updates.isEmpty()) {
            return -1;
        }
        Update last = updates.get(updates.size() - 1);
        if (last.hasMessage()) {
            return last.getMessage().getMessageId();
        }
        if (last.hasCallbackQuery()) {
            return last.getCallbackQuery().getMessage().getMessageId();
        }
        return -1;
    }

    @JsonIgnore
    public String getLastCallbackId() {
        if (updates.isEmpty()) {
            return null;
        }
        Update last = updates.get(updates.size() - 1);
        if (last.hasCallbackQuery()) {
            return last.getCallbackQuery().getId();
        }
        return null;
    }

    public boolean canRedrawMenu() {
        return getMenuMessageId() != -1;
    }

    public void resetMenuMessageId() {
        setMenuMessageId(-1);
    }

    public void setMenuMessageId(int menuMessageId) {
        this.menuMessageId = menuMessageId;
        getStorage().ifPresent(ChatStateMap::store);
    }

    public void resetUserProfile() {
        setPhone(null);
        setFullName(null);
        setPendingFullName(null);
    }

    public void resetCarTypeSelection() {
        setCarType(null);
    }

    public void setPhone(String phone) {
        this.phone = phone;
        getStorage().ifPresent(ChatStateMap::store);
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        getStorage().ifPresent(ChatStateMap::store);
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
        getStorage().ifPresent(ChatStateMap::store);
    }

    public void setSelectorState(SelectorState selectorState) {
        this.selectorState = selectorState;
        getStorage().ifPresent(ChatStateMap::store);
    }

    public Optional<ChatStateMap> getStorage() {
        return Optional.ofNullable(storage);
    }

    public void setPendingFullName(String pendingFullName) {
        this.pendingFullName = pendingFullName;
        getStorage().ifPresent(ChatStateMap::store);
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
        getStorage().ifPresent(ChatStateMap::store);
    }
}
