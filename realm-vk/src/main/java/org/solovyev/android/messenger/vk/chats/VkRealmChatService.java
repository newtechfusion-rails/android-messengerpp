package org.solovyev.android.messenger.vk.chats;

import android.content.Context;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.vk.messages.VkMessagesSendHttpTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:30 PM
 */
public class VkRealmChatService implements RealmChatService {

    @NotNull
    private static final String TAG = VkRealmChatService.class.getSimpleName();

    @NotNull
    private final Realm realm;

    public VkRealmChatService(@NotNull Realm realm) {
        this.realm = realm;
    }

    /*@NotNull
    @Override
    public List<Chat> getUserChats(@NotNull Integer userId) {
        try {
            final List<Chat> result = new ArrayList<Chat>();
            for (VkMessagesGetDialogsHttpTransaction vkMessagesGetDialogsHttpTransaction : VkMessagesGetDialogsHttpTransaction.newInstances(100)) {
                result.addAll(HttpTransactions.execute(vkMessagesGetDialogsHttpTransaction));
            }
            return result;
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }*/

    @NotNull
    @Override
    public List<ChatMessage> getChatMessages(@NotNull String realmUserId, @NotNull Context context) {
        try {
            return HttpTransactions.execute(new VkMessagesGetHttpTransaction(realm, getUser(realmUserId), context));
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    @NotNull
    @Override
    public List<ChatMessage> getNewerChatMessagesForChat(@NotNull String realmChatId, @NotNull String realmUserId, @NotNull Context context) {
        return getChatMessagesForChat(realmChatId, realmUserId, context, new VkHttpTransactionForMessagesForChatProvider() {
            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@NotNull User user, @NotNull String secondUserId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(realm, secondUserId, user, context));
            }

            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@NotNull User user, @NotNull String chatId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(realm, chatId, user, context));
            }
        });
    }

    private List<ChatMessage> getChatMessagesForChat(@NotNull String realmChatId, @NotNull String realmUserId, @NotNull Context context, @NotNull VkHttpTransactionForMessagesForChatProvider p) {
        final Chat chat = getChatService().getChatById(realm.newRealmEntity(realmChatId));

        if (chat != null) {
            try {
                if (chat.isPrivate()) {
                    final int index = realmChatId.indexOf("_");
                    if (index >= 0) {

                        final String secondUserId = realmChatId.substring(index + 1, realmChatId.length());
                        final List<ChatMessage> result = new ArrayList<ChatMessage>(100);
                        for (List<ChatMessage> messages : HttpTransactions.execute(p.getForPrivateChat(getUser(realmUserId), secondUserId, context))) {
                            result.addAll(messages);
                        }
                        return result;

                    } else {
                        Log.e(TAG, "Chat is private but don't have '_', chat id: " + realmChatId);
                        return Collections.emptyList();
                    }

                } else {
                    final List<ChatMessage> result = new ArrayList<ChatMessage>(100);
                    for (List<ChatMessage> messages : HttpTransactions.execute(p.getForChat(getUser(realmUserId), realmChatId, context))) {
                        result.addAll(messages);
                    }
                    return result;
                }
            } catch (IOException e) {
                throw new HttpRuntimeIoException(e);
            }
        } else {
            Log.e(TAG, "Chat is not found for chat id: " + realmChatId);
            return Collections.emptyList();
        }
    }

    @NotNull
    @Override
    public List<ChatMessage> getOlderChatMessagesForChat(@NotNull String realmChatId, @NotNull String realmUserId, @NotNull final Integer offset, @NotNull Context context) {
        return getChatMessagesForChat(realmChatId, realmUserId, context, new VkHttpTransactionForMessagesForChatProvider() {
            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@NotNull User user, @NotNull String secondUserId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(realm, secondUserId, user, offset, context));
            }

            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@NotNull User user, @NotNull String chatId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(realm, chatId, user, offset, context));
            }
        });
    }

    private static interface VkHttpTransactionForMessagesForChatProvider {
        @NotNull
        List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@NotNull User user, @NotNull String secondUserId, @NotNull Context context);

        @NotNull
        List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@NotNull User user, @NotNull String chatId, @NotNull Context context);

    }

    @NotNull
    private User getUser(@NotNull String realmUserId) {
        return getUserService().getUserById(realm.newRealmEntity(realmUserId));
    }

    @NotNull
    private UserService getUserService() {
        return AbstractMessengerApplication.getServiceLocator().getUserService();
    }

    @NotNull
    private ChatService getChatService() {
        return AbstractMessengerApplication.getServiceLocator().getChatService();
    }


    @NotNull
    @Override
    public List<ApiChat> getUserChats(@NotNull String realmUserId, @NotNull Context context) {
        try {
            final User user = AbstractMessengerApplication.getServiceLocator().getUserService().getUserById(realm.newRealmEntity(realmUserId));
            return HttpTransactions.execute(VkMessagesGetDialogsHttpTransaction.newInstance(realm, user, context));
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    @NotNull
    @Override
    public String sendChatMessage(@NotNull Chat chat, @NotNull ChatMessage chatMessage, @NotNull Context context) {
        try {
            return HttpTransactions.execute(new VkMessagesSendHttpTransaction(realm, chatMessage, chat));
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }
}