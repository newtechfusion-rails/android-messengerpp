/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.menu.AMenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.Accounts;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.users.Users.getUserIdFromArguments;
import static org.solovyev.common.text.Strings.isEmpty;

public class ContactFragment extends BaseUserFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "contact-info";

	private ActivityMenu<Menu, MenuItem> menu;

	private boolean updateUiOnResume = false;

	public ContactFragment() {
		super(R.layout.mpp_fragment_contact);
	}

	@Nonnull
	public static MultiPaneFragmentDef newViewContactFragmentDef(@Nonnull Context context, @Nonnull Account account, @Nonnull Entity contact, boolean addToBackStack) {
		final Bundle arguments = Users.newUserArguments(account, contact);
		return newViewContactFragmentDef(context, arguments, addToBackStack);
	}

	@Nonnull
	public static MultiPaneFragmentDef newViewContactFragmentDef(@Nonnull Context context, @Nonnull Bundle arguments, boolean addToBackStack) {
		final String accountId = Accounts.getAccountIdFromArguments(arguments);
		final String contactId = getUserIdFromArguments(arguments);

		if (isEmpty(accountId)) {
			throw new IllegalArgumentException("Account id must be provided in arguments");
		}

		if (isEmpty(contactId)) {
			throw new IllegalArgumentException("Contact id must be provided in arguments");
		}

		assert contactId != null;
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, ContactFragment.class, context, arguments, new ContactFragmentReuseCondition(contactId));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		updateUi(root);
	}

	private void updateUi(@Nonnull View root) {
		final User contact = getUser();
		final Context context = getThemeContext();

		final ViewGroup propertiesViewGroup = (ViewGroup) root.findViewById(R.id.mpp_contact_properties_viewgroup);
		propertiesViewGroup.removeAllViews();

		final Multimap<String, String> properties = ArrayListMultimap.create();
		for (AProperty property : getAccountService().getUserProperties(contact, context)) {
			properties.put(property.getName(), property.getValue());
		}

		final ViewFromLayoutBuilder<View> propertyDividerBuilder = ViewFromLayoutBuilder.newInstance(R.layout.mpp_property_divider);

		final ViewFromLayoutBuilder<View> propertyViewBuilder = ViewFromLayoutBuilder.newInstance(R.layout.mpp_property);
		final View headerView = propertyViewBuilder.build(context);

		final ImageView contactIcon = (ImageView) headerView.findViewById(R.id.mpp_property_icon);
		contactIcon.setVisibility(VISIBLE);
		getUserService().getIconsService().setUserPhoto(contact, contactIcon);

		final TextView contactName = (TextView) headerView.findViewById(R.id.mpp_property_label);
		contactName.setText(contact.getDisplayName());

		final TextView accountName = (TextView) headerView.findViewById(R.id.mpp_property_value);
		accountName.setText(Accounts.getAccountName(getAccount()));

		propertiesViewGroup.addView(headerView);

		for (Map.Entry<String, Collection<String>> entry : properties.asMap().entrySet()) {
			for (String propertyValue : entry.getValue()) {
				// add divider first
				propertiesViewGroup.addView(propertyDividerBuilder.build(context));

				final View propertyView = propertyViewBuilder.build(context);

				final TextView propertyLabel = (TextView) propertyView.findViewById(R.id.mpp_property_label);
				propertyLabel.setText(entry.getKey());

				final TextView propertyValueTextView = (TextView) propertyView.findViewById(R.id.mpp_property_value);
				propertyValueTextView.setText(propertyValue);

				propertiesViewGroup.addView(propertyView);
			}
		}

		root.findViewById(R.id.mpp_save_button).setVisibility(GONE);
		root.findViewById(R.id.mpp_remove_button).setVisibility(GONE);

		getMultiPaneManager().showTitle(getSherlockActivity(), this, contact.getDisplayName());
	}

	@Override
	protected void onUserChanged(@Nonnull User user) {
		super.onUserChanged(user);

		updateUiOnResume = true;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (updateUiOnResume) {
			updateUi(getView());
			updateUiOnResume = false;
		}
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getUser().getDisplayName();
	}

		/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

		final EditContactMenuItem editContactMenuItem = new EditContactMenuItem();
		menuItems.add(editContactMenuItem);

		final OpenChatMenuItem openChatMenuItem = new OpenChatMenuItem();
		menuItems.add(openChatMenuItem);

		this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_contact, menuItems, SherlockMenuHelper.getInstance(), new JPredicate<AMenuItem<MenuItem>>() {
			@Override
			public boolean apply(@Nullable AMenuItem<MenuItem> menuItem) {
				if (menuItem == editContactMenuItem) {
					return !getAccount().getRealm().canEditUsers();
				} else if (menuItem == openChatMenuItem) {
					return !(getSherlockActivity() instanceof ContactsActivity);
				}
				return false;
			}
		});
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	private class EditContactMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_edit_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			if (getAccount().getRealm().canEditUsers()) {
				getEventManager().fire(new ContactUiEvent.Edit(getUser(), getAccount()));
			}
		}
	}

	private class OpenChatMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_chat_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			getEventManager().fire(new ContactUiEvent.OpenChat(getUser(), getAccount()));
		}
	}
}
