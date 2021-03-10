package videowebview.ca;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flybits.commons.library.api.FlybitsManager;
import com.flybits.commons.library.api.FlybitsScope;
import com.flybits.commons.library.api.results.callbacks.ObjectResultCallback;
import com.flybits.commons.library.exceptions.FlybitsException;
import com.flybits.commons.library.logging.Logger;
import com.flybits.commons.library.models.User;
import com.flybits.concierge.activities.DocumentActivity;
import com.flybits.concierge.activities.NotificationsActivity;
import com.flybits.concierge.activities.SettingsActivity;
import com.flybits.concierge.analytics.VisibilityObservable;
import com.flybits.concierge.analytics.VisibilityStateChangeListener;
import com.flybits.concierge.enums.DisplayType;
import com.flybits.concierge.fragments.FeedHolderFragment;
import com.flybits.concierge.fragments.OptInErrorStateFragment;
import com.flybits.concierge.fragments.OptInFragment;
import com.flybits.concierge.fragments.OptOutConfirmationFragment;
import com.flybits.concierge.fragments.TransitionLoaderFragment;
import com.flybits.concierge.videoplayer.ActivityBridge;
import com.flybits.internal.db.CommonsDatabase;
import com.flybits.internal.db.UserDAO;

import org.jetbrains.annotations.NotNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * This fragment is responsible for displaying all of the Concierge.
 * <p>
 * For instantiating this fragment please see the newInstance method.
 */
public class ConciergeFragment extends Fragment implements AuthenticationStatusListener
        , OptedStateChangeListener, FlybitsNavigator, VisibilityObservable {

    public static final String INSTANCE_CURRENT_FRAGMENT = "instance_current_fragment";
    public static final String ARG_MENU_TYPE = "flybits_con_menu_type";
    public static final String ARG_SHOW_OPT_OUT = "flybits_con_show_opt_out";
    public static final String ARG_SHOW_SETTINGS = "flybits_con_show_settings";
    public static final String ARG_OPT_OUT_TITLE = "flybits_con_opt_out_title";
    public static final String ARG_OPT_OUT_MESSAGE = "flybits_con_opt_out_message";
    public static final String CONCIERGE_FRAGMENT_TAG = "ConciergeFragmentTag"; // Fragment Tag to be used for Fragment Transaction
    public static FlybitsScope flybitsScope;
    public String CONCIERGE_LOG_TAG = "ConFragment";
    Handler handler;
    private LinearLayout lytLoader;
    private TextView txtLoaderText;
    private LinearLayout errorViewContainer;
    private View mainViewContainer;
    private Context currentContext;
    private Fragment currentFragment;
    private FragmentManager childFragmentManager;
    private boolean initializing = false;
    private boolean actionBarItemsVisible = true;
    private boolean menuVisible = false;
    private boolean isVisible = false;
    private boolean firstResume = true;
    private MenuType menuType = null;
    private boolean showOptOutOption = true;
    private boolean showSettings = true;
    private String optOutTitle = null;
    private String optOutMessage = null;
    private Set<VisibilityStateChangeListener> visibilityStateChangeListeners;
    private FlybitsConcierge flybitsConcierge;

    /**
     * Create instance of {@link com.flybits.concierge.ConciergeFragment}.
     *
     * @param menuType The {@link MenuType} appearance that will be used.
     * @return new instance of {@link com.flybits.concierge.ConciergeFragment}.
     * @deprecated Please use {@link com.flybits.concierge.ConciergeFragment#newInstance(DisplayConfiguration)} instead. deprecated in version 3.0.0, will be removed in version 4.0.0
     */
    @Deprecated
    public static com.flybits.concierge.ConciergeFragment newInstance(MenuType menuType) {
        com.flybits.concierge.ConciergeFragment fragment = new com.flybits.concierge.ConciergeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_MENU_TYPE, menuType.value);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Create instance of {@link com.flybits.concierge.ConciergeFragment}.
     *
     * @param configuration The {@link DisplayConfiguration} for the instance being created.
     * @return new instance of {@link com.flybits.concierge.ConciergeFragment}.
     */
    public static com.flybits.concierge.ConciergeFragment newInstance(DisplayConfiguration configuration) {
        com.flybits.concierge.ConciergeFragment fragment = new com.flybits.concierge.ConciergeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_MENU_TYPE, configuration.getMenuType().getValue());
        bundle.putBoolean(ARG_SHOW_OPT_OUT, configuration.getShowOptOutOption());
        bundle.putString(ARG_OPT_OUT_TITLE, configuration.getOptOutTitle());
        bundle.putString(ARG_OPT_OUT_MESSAGE, configuration.getOptOutMessage());
        bundle.putBoolean(ARG_SHOW_SETTINGS, configuration.getShowSettings());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void openSnackbar(@NotNull String content, int length) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, content, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void dismiss() {
        if (childFragmentManager != null) {
            childFragmentManager.popBackStack();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        childFragmentManager = getChildFragmentManager();

        if (getActivity() != null) {
            ActivityBridge.Companion.setCurrentActivity(getActivity());
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (savedInstanceState != null) {
            if (childFragmentManager != null) {
                currentFragment = childFragmentManager.getFragment(savedInstanceState, INSTANCE_CURRENT_FRAGMENT);
            }
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentFragment != null) {
            if (childFragmentManager != null) {
                childFragmentManager.putFragment(outState, INSTANCE_CURRENT_FRAGMENT, currentFragment);
            }
        }
        outState.putBoolean(ConciergeConstants.STATE_ERROR_PRESENT, errorViewContainer.getVisibility() == View.VISIBLE);

    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.appendTag(CONCIERGE_LOG_TAG).d("onPause()");
        if (flybitsConcierge != null) {
            flybitsConcierge.unregisterAuthenticationStateListener(this);
            flybitsConcierge.unregisterOptedStateChangeListener(this);
        }
        if (menuVisible) {
            onVisibilityStateChange(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.appendTag(CONCIERGE_LOG_TAG).d("onResume()");
        if (flybitsConcierge != null) {
            flybitsConcierge.registerAuthenticationStateListener(this);
            flybitsConcierge.registerOptedStateChangeListener(this);
        }
        if (menuVisible && !firstResume) {
            onVisibilityStateChange(true);
        }
        firstResume = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.flybits_con_fragment_concierge, container, false);
    }

    private void userUpdate(Context context, boolean optedState) {
        UserDAO userDAO = CommonsDatabase.getDatabase(context).userDao();
        User user = userDAO.getActiveUser();
        if (user != null) {
            user.setOptedIn(optedState);
            userDAO.update(user);
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerConciergeScope();

        visibilityStateChangeListeners = Collections.synchronizedSet(new HashSet<VisibilityStateChangeListener>());

        /*Code below makes it so that the options menu appears in the app bar if that setting is
         * being used. It is hosted in the fragment since we do not know what the hosting activity
         * is. */
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if (args != null) {
            menuType = MenuType.fromInt(args.getInt(ARG_MENU_TYPE, MenuType.MENU_TYPE_APP_BAR.value));
            showOptOutOption = args.getBoolean(ARG_SHOW_OPT_OUT, true);
            optOutTitle = args.getString(ARG_OPT_OUT_TITLE, null);
            optOutMessage = args.getString(ARG_OPT_OUT_MESSAGE, null);
            showSettings = args.getBoolean(ARG_SHOW_SETTINGS, true);
        }

        lytLoader = view.findViewById(R.id.concierge_fragment_lytLoader);
        txtLoaderText = view.findViewById(R.id.concierge_fragment_txtLoadingText);
        errorViewContainer = view.findViewById(R.id.concierge_fragment_error_holder);
        mainViewContainer = view.findViewById(contentLayout());

        flybitsConcierge = FlybitsConcierge.with(currentContext);
        // Call initialize the state
        if (!InternalPreferences.is2PhaseOptIn(currentContext)) {
            initializeState();
        } else if (InternalPreferences.isOnBoardingDone(currentContext)) {
            initializeState(DisplayType.SHOW_CONTENT);
        } else {
            initializeState(DisplayType.SHOW_OPT_IN);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Attempts to resolve all requirements to see the main feed for 2Phase Opt In.
     *
     * @param displayType Enum for displaying the specific fragment.
     */
    @UiThread
    public void initializeState(DisplayType displayType) {
        handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (initializing) return;
            initializing = true;
            boolean is2PhaseOptedIn = InternalPreferences.is2PhaseOptIn(currentContext);
            // reset the views to be in their default visibility
            errorViewContainer.setVisibility(View.GONE);
            mainViewContainer.setVisibility(View.INVISIBLE);
            if (is2PhaseOptedIn) {
                switch (displayType) {
                    case SHOW_OPT_IN: {
                        mainViewContainer.setVisibility(View.VISIBLE);
                        hideLoader();
                        // Show Transition Loader to open the Guided Opt In Activity.
                        if (displayType.equals(DisplayType.SHOW_OPT_IN)) {
                            currentFragment = TransitionLoaderFragment.Companion.newInstance(true);
                        }
                        if (isAdded()) {
                            if (childFragmentManager != null) {
                                childFragmentManager.beginTransaction()
                                        .replace(contentLayout(), currentFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                            setActionBarItemsVisibility(false);
                            initializing = false;
                        }
                        break;
                    }
                    case SHOW_LOADING:
                    case SHOW_ERROR: {
                        mainViewContainer.setVisibility(View.VISIBLE);
                        hideLoader();
                        if (displayType.equals(DisplayType.SHOW_OPT_IN)) {
                            currentFragment = OptInFragment.Companion.newInstance();
                        } else if (displayType.equals(DisplayType.SHOW_LOADING)) {
                            currentFragment = TransitionLoaderFragment.Companion.newInstance(false);
                        } else {
                            currentFragment = OptInErrorStateFragment.Companion.newInstance(flybitsConcierge.getOptInError());
                        }
                        if (isAdded()) {
                            if (childFragmentManager != null) {
                                childFragmentManager.beginTransaction()
                                        .replace(contentLayout(), currentFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                            setActionBarItemsVisibility(false);
                            initializing = false;
                        }
                        break;
                    }
                    case SHOW_OPT_OUT_CONFIRMATION: {
                        mainViewContainer.setVisibility(View.VISIBLE);
                        hideLoader();
                        currentFragment = OptOutConfirmationFragment.Companion.newInstance();
                        if (isAdded()) {
                            if (childFragmentManager != null) {
                                childFragmentManager.beginTransaction()
                                        .replace(contentLayout(), currentFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }
                        InternalPreferences.setOptOutConfirmationAdded(currentContext, false);
                        setActionBarItemsVisibility(false);
                        initializing = false;
                        break;
                    }
                    case SHOW_CONTENT: {
                        mainViewContainer.setVisibility(View.VISIBLE);
                        if (mainViewContainer instanceof FrameLayout) {
                            ((FrameLayout) mainViewContainer).removeAllViewsInLayout();
                        }
                        // Otherwise, show CategoryFragment only if current fragment is not instance of FeedHolderFragment
                        hideLoader();
                        //Set the showMoreTab using the boolean the client passes.
                        Bundle arguments = getArguments();
                        boolean showMoreTab = false;
                        if (arguments != null) {
                            MenuType menuType = MenuType.fromInt(arguments.getInt(ARG_MENU_TYPE, MenuType.MENU_TYPE_APP_BAR.value));
                            showMoreTab = menuType != MenuType.MENU_TYPE_APP_BAR;
                        }
                        FlybitsManager.addScope(flybitsScope);
                        currentFragment = FeedHolderFragment.Companion.newInstance(showMoreTab, showOptOutOption, optOutTitle, optOutMessage, showSettings);
                        if (isAdded()) {
                            if (childFragmentManager != null) {
                                childFragmentManager.beginTransaction()
                                        .replace(contentLayout(), currentFragment, currentFragment.getClass().getSimpleName())
                                        .addToBackStack(null)
                                        .commitAllowingStateLoss();
                            }
                            setActionBarItemsVisibility(true);
                            initializing = false;
                        }
                        break;
                    }
                }
            }
        });
    }

    /**
     * Attempts to resolve all requirements to see the main feed.
     */
    @UiThread
    public void initializeState() {
        handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (initializing) return;
            initializing = true;

            // reset the views to be in their default visibility
            errorViewContainer.setVisibility(View.GONE);
            mainViewContainer.setVisibility(View.INVISIBLE);

            //Check if authorized first
            if (flybitsConcierge.isAuthenticated()) {
                mainViewContainer.setVisibility(View.VISIBLE);
                flybitsConcierge.isOptedInLocal(new ObjectResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean optedIn) {
                        flybitsConcierge.unregisterAuthenticationStateListener(com.flybits.concierge.ConciergeFragment.this);

                        if (!optedIn) {
                            // if TNCs haven't been shown yet, and the link is initialized in the config file
                            hideLoader();
                            // if isOptOutConfirmation is true, do NOT go to OptInFragment, setOptOutConfirmation false so next time (like exit activity), it will not stop at OptOutConfirmationFragment
                            if (InternalPreferences.isOptOutConfirmationAdded(currentContext)) {
                                if (isAdded()) {
                                    // Show Transition Loader to open the Guided Opt In Activity.
                                    currentFragment = TransitionLoaderFragment.Companion.newInstance(true);
                                    if (childFragmentManager != null) {
                                        childFragmentManager.beginTransaction()
                                                .replace(contentLayout(), currentFragment)
                                                .addToBackStack(null)
                                                .commit();
                                    }
                                    setActionBarItemsVisibility(false);
                                }
                            } else {
                                if (!InternalPreferences.isOptOutConfirmation(currentContext)) {
                                    if (isAdded()) {
                                        currentFragment = OptOutConfirmationFragment.Companion.newInstance();
                                        if (childFragmentManager != null) {
                                            childFragmentManager.beginTransaction()
                                                    .replace(contentLayout(), currentFragment)
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                        setActionBarItemsVisibility(false);
                                    }
                                } else {
                                    if (isAdded()) {
                                        currentFragment = OptOutConfirmationFragment.Companion.newInstance();
                                        if (childFragmentManager != null) {
                                            childFragmentManager.beginTransaction()
                                                    .replace(contentLayout(), currentFragment)
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                        InternalPreferences.setOptOutConfirmationAdded(currentContext, false);
                                        setActionBarItemsVisibility(false);
                                    }
                                }
                            }
                        } else if (!(currentFragment instanceof FeedHolderFragment)) {
                            mainViewContainer.setVisibility(View.VISIBLE);
                            // Otherwise, show CategoryFragment only if current fragment is not instance of FeedHolderFragment
                            if (mainViewContainer instanceof FrameLayout) {
                                ((FrameLayout) mainViewContainer).removeAllViewsInLayout();
                            }
                            hideLoader();
                            //Set the showMoreTab using the boolean the client passes.
                            Bundle arguments = getArguments();
                            boolean showMoreTab = false;
                            if (arguments != null) {
                                MenuType menuType = MenuType.fromInt(arguments.getInt(ARG_MENU_TYPE, MenuType.MENU_TYPE_APP_BAR.value));
                                showMoreTab = menuType != MenuType.MENU_TYPE_APP_BAR;
                            }
                            FlybitsManager.addScope(flybitsScope);
                            if (isAdded()) {
                                currentFragment = FeedHolderFragment.Companion.newInstance(showMoreTab, showOptOutOption, optOutTitle, optOutMessage, showSettings);
                                if (childFragmentManager != null) {
                                    childFragmentManager.beginTransaction()
                                            .replace(contentLayout(), currentFragment, currentFragment.getClass().getSimpleName())
                                            .commitAllowingStateLoss();
                                }
                                setActionBarItemsVisibility(true);
                            }
                        }
                        initializing = false;
                    }

                    @Override
                    public void onException(@NotNull FlybitsException e) {
                        initializing = false;
                    }
                });
            } else {
                // Check if the authentication is in process , if not then show the error message else show the loader.
                if (!flybitsConcierge.isAuthenticating()) {
                    //Retry authentication since we are not currently authenticated, wait for callback(AuthenticationStatusListener)'s methods to be invoked
                    boolean retrySuccess = flybitsConcierge.retryAuthentication();
                    if (retrySuccess) {
                        showLoader("");
                    }
                    errorViewContainer.setVisibility(View.VISIBLE);
                    setActionBarItemsVisibility(false);
                    initializing = false;
                } else {
                    setActionBarItemsVisibility(false);
                    initializing = false;
                    showLoader("");
                }
            }
        });
    }

    private void registerConciergeScope() {
        String conciergeScope = "ConciergeScope" + this.getId();
        flybitsScope = new FlybitsScope(conciergeScope) {
            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void onConnected(Context context, User user) {
            }

            @Override
            public void onDisconnected(Context context, String jwtToken) {
            }

            @Override
            public void onAccountDestroyed(Context context, String jwtToken) {
            }

            @Override
            public void onOptedStateChange(Context context, boolean optedState) {
                Executors.newSingleThreadExecutor().execute(() ->
                        userUpdate(context, optedState));
                if (!optedState) {
                    Logger.appendTag(CONCIERGE_LOG_TAG).d("User Opted Out of Flybits.");
                    if (!InternalPreferences.isSelfOptedOut(context)) {
                        if (isAdded()) {
                            currentFragment = OptOutConfirmationFragment.newInstance();
                            if (childFragmentManager != null) {
                                childFragmentManager.beginTransaction()
                                        .replace(contentLayout(), currentFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                            setActionBarItemsVisibility(false);
                        }
                    } else {
                        InternalPreferences.setSelfUserOptedOut(context, false);

                        if (isAdded()) {
                            currentFragment = OptOutConfirmationFragment.newInstance();
                            if (childFragmentManager != null) {
                                childFragmentManager.beginTransaction()
                                        .replace(contentLayout(), currentFragment)
                                        .addToBackStack(null)
                                        .commitAllowingStateLoss();
                            }
                            setActionBarItemsVisibility(false);
                        }

                        // set stop at OptOutConfirmationFragment
                        InternalPreferences.setOptOutConfirmation(context, true);
                    }
                    FlybitsManager.removeScope(flybitsScope);
                } else {
                    Logger.appendTag(CONCIERGE_LOG_TAG).d("User has Opted In Flybits.");
                    initializing = false;
                    if (!InternalPreferences.is2PhaseOptIn(context)) {
                        initializeState();
                    } else {
                        initializeState(DisplayType.SHOW_CONTENT);
                    }
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.appendTag(CONCIERGE_LOG_TAG).d("onCreateOptionsMenu()");
        onVisibilityStateChange(true);

        if (menuType == MenuType.MENU_TYPE_APP_BAR && showSettings) { //This will happen because we need this callback for visibility now
            inflater.inflate(R.menu.menu, menu);
            menu.findItem(R.id.notifications).setVisible(actionBarItemsVisible);
            menu.findItem(R.id.settings).setVisible(actionBarItemsVisible);
            menu.findItem(R.id.declineOptin).setVisible(false);
        } else {
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        onVisibilityStateChange(false);
        menuVisible = false;
    }

    public void registerMenu() {
        if (!firstResume) {
            onVisibilityStateChange(true);
        }
        menuVisible = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            Intent startSettingsIntent = new Intent(getContext(), SettingsActivity.class);
            startSettingsIntent.putExtra(SettingsActivity.SHOW_OPT_OUT_CELL, showOptOutOption);
            startSettingsIntent.putExtra(SettingsActivity.OPT_OUT_CELL_TITLE, optOutTitle);
            startSettingsIntent.putExtra(SettingsActivity.OPT_OUT_CELL_MESSAGE, optOutMessage);
            startActivityForResult(startSettingsIntent, SettingsActivity.REQUEST_CODE);
            return true;
        } else if (id == R.id.notifications) {
            Intent startNotificationsIntent = new Intent(getContext(), NotificationsActivity.class);
            startActivity(startNotificationsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setActionBarItemsVisibility(boolean visible) {
        actionBarItemsVisible = visible;
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }

    }

    @Override
    public void openFragment(@NotNull Fragment fragment, boolean addToBackStack) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null && childFragmentManager != null) {
            FragmentTransaction transaction = childFragmentManager.beginTransaction();
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }
            transaction.replace(contentLayout(), fragment)
                    .commit();
        }

    }

    @Override
    public void openActivity(Class activity, Bundle extras) {
        Intent intent = new Intent(currentContext, activity);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * Removes the current content and shows the loader
     *
     * @param title Message to display
     */
    private void showLoader(String title) {
        if (currentFragment != null) {
            if (childFragmentManager != null) {
                childFragmentManager.beginTransaction()
                        .remove(currentFragment)
                        .commit();
            }
        }
        txtLoaderText.setText(title);
        lytLoader.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the loader
     */
    private void hideLoader() {
        lytLoader.setVisibility(View.GONE);
    }

    private int contentLayout() {
        return R.id.concierge_fragment_lytContent;
    }

    public void setCallback(IConciergeFragmentCallbacks callback) {
    }

    @Override
    public void onAuthenticated() {
        if (!InternalPreferences.is2PhaseOptIn(getContext())) {
            initializeState();
        }
    }

    @Override
    public void onAuthenticationStarted() {

    }

    @Override
    public void onAuthenticationError(FlybitsException e) {
        //Don't display error view if opt out view is already visible
        mainViewContainer.setVisibility(View.GONE);
        errorViewContainer.setVisibility(View.VISIBLE);
        hideLoader();
    }

    @Override
    public void onOptedStateChange(boolean optedIn) {
        if (getActivity() != null) {
            initializeState();
        }
    }

    @Override
    public void onSwitchFragment(DisplayType displayType) {
        if (getActivity() != null) {
            initializeState(displayType);
        }
    }

    @Override
    public boolean openUrl(@NotNull String url) {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        //verify that you're able to open url, if URL valid or not.
        if (URLUtil.isValidUrl(url)) {
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean openUrlInApp(@NotNull String url) {
        Context context = getContext();
        if (context == null) {
            return false;
        }
        //verify that you're able to open url, if URL valid or not.
        if (URLUtil.isValidUrl(url)) {
            Intent activityIntent = new Intent(context, DocumentActivity.class);
            activityIntent.putExtra(DocumentActivity.EXTRA_DOCUMENT_TYPE, DocumentActivity.DOCUMENT_TYPE_URL);
            activityIntent.putExtra(DocumentActivity.EXTRA_DOCUMENT_CLIENT_URL, url);
            startActivity(activityIntent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void openDialog(@NotNull String content, @NotNull String title) {
        new MaterialDialog.Builder(getActivity())
                .title(title)
                .content(content)
                .positiveText(android.R.string.ok)
                .dismissListener(dialogInterface -> getActivity().finish())
                .show();
    }

    @Override
    public void registerVisibilityStateChangeListener(VisibilityStateChangeListener v) {
        synchronized (visibilityStateChangeListeners) {
            visibilityStateChangeListeners.add(v);
        }
    }

    @Override
    public void unregisterVisibilityStateChangeListener(VisibilityStateChangeListener v) {
        synchronized (visibilityStateChangeListeners) {
            visibilityStateChangeListeners.remove(v);
        }
    }

    /**
     * @return Whether the [ConciergeFragment] is visible to the user.
     */
    public boolean isVisibleToUser() {
        return isVisible;
    }

    private void onVisibilityStateChange(boolean visibility) {
        if (isVisible != visibility) { //Only  broadcast if visibility state change occurred
            isVisible = visibility;
            Set<VisibilityStateChangeListener> listenersSet = new HashSet<>(visibilityStateChangeListeners);
            for (VisibilityStateChangeListener listener : listenersSet) {
                listener.onVisibilityStateChange(visibility);
            }
        }
    }

    /**
     * Enum responsible for specifying where and how the options menu will appear.
     */
    public enum MenuType {
        /**
         * Menu will appear in the app bar of the hosting activity.
         */
        MENU_TYPE_APP_BAR(0),
        /**
         * Menu will appear in the
         */
        MENU_TYPE_TAB(1);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public static MenuType fromInt(int type) {
            if (type == 0) {
                return MENU_TYPE_APP_BAR;
            } else if (type == 1) {
                return MENU_TYPE_TAB;
            } else {
                throw new IllegalArgumentException("Type is invalid use TYPE_TAB or TYPE_APP_BAR");
            }
        }

        public int getValue() {
            return value;
        }
    }

    public interface IConciergeFragmentCallbacks {
        void onTNCDecline();
    }
}
