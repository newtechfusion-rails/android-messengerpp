package org.solovyev.android.messenger.chats;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import static java.lang.Math.min;
import static java.util.Collections.sort;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:49 PM
 */
public final class Chats {

	@Nonnull
	public static final String CHATS_FRAGMENT_TAG = "chats";
	static final int MAX_RECENT_CHATS = 20;

	private Chats() {
		throw new AssertionError();
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable Message lastMessage, @Nonnull User user, int unreadMessagesCount) {
		String result = getDisplayName(chat, lastMessage, user);
		if (unreadMessagesCount > 0) {
			result += " (" + unreadMessagesCount + ")";
		}
		return result;
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable Message message, @Nonnull User user) {
		final String title = message != null ? message.getTitle() : null;
		if (Strings.isEmpty(title) || title.equals(" ... ")) {

			if (chat.isPrivate()) {
				return Users.getDisplayNameFor(chat.getSecondUser());
			} else {
				return "";
			}
		} else {
			return title;
		}
	}

	@Nonnull
	public static Chat newPrivateChat(@Nonnull Entity chat) {
		return ChatImpl.newPrivate(chat);
	}

	@Nonnull
	public static ApiChat newPrivateApiChat(@Nonnull Entity chat,
											@Nonnull Collection<User> participants,
											@Nonnull Collection<Message> messages) {
		final ApiChatImpl result = ApiChatImpl.newInstance(chat, messages.size(), true);
		for (User participant : participants) {
			result.addParticipant(participant);
		}
		for (Message message : messages) {
			result.addMessage(message);
		}
		return result;
	}

	@Nonnull
	public static ApiChat newEmptyApiChat(@Nonnull Chat chat, @Nonnull List<User> participants) {
		return ApiChatImpl.newInstance(chat, Collections.<Message>emptyList(), participants);
	}

	@Nonnull
	static List<UiChat> getLastChatsByDate(@Nonnull List<UiChat> result, int count) {
		sort(result, new LastMessageDateChatComparator());

		return result.subList(0, min(result.size(), count));
	}
}
