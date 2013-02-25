package org.solovyev.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:43 PM
 */
public interface MessengerCommonActivity {

    void onCreate(@NotNull SherlockFragmentActivity activity, Bundle savedInstanceState);

    void onRestart(@NotNull Activity activity);

    @NotNull
    ImageButton createFooterImageButton(int imageResId, int contentDescriptionResId, @NotNull Activity activity);

    @NotNull
    Button createFooterButton(int captionResId, @NotNull Activity activity);

    @NotNull
    ViewGroup getFooterLeft(@NotNull Activity activity);

    @NotNull
    ViewGroup getFooterCenter(@NotNull Activity activity);

    @NotNull
    ViewGroup getFooterRight(@NotNull Activity activity);

    @NotNull
    ViewGroup getHeaderLeft(@NotNull Activity activity);

    @NotNull
    ViewGroup getHeaderCenter(@NotNull Activity activity);

    @NotNull
    ViewGroup getHeaderRight(@NotNull Activity activity);

    @NotNull
    ViewGroup getCenter(@NotNull Activity activity);

    void handleException(@NotNull Activity activity, @NotNull Exception e);

    @NotNull
    ViewPager initTitleForViewPager(@NotNull Activity activity,
                               @NotNull ViewPager.OnPageChangeListener listener,
                               @NotNull PagerAdapter adapter);

    void onSaveInstanceState(@NotNull SherlockFragmentActivity activity, @NotNull Bundle outState);
}
