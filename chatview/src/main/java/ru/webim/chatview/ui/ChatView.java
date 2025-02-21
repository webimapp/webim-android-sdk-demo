package ru.webim.chatview.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import ru.webim.android.sdk.Department;
import ru.webim.android.sdk.MessageTracker;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.WebimSession;
import ru.webim.chatview.ChatAdapter;
import ru.webim.chatview.ChatAdapterImpl;
import ru.webim.chatview.ChatHolderActionsImpl;
import ru.webim.chatview.R;
import ru.webim.chatview.utils.DepartmentsDialog;
import ru.webim.chatview.utils.FileHelper;
import ru.webim.chatview.utils.ViewUtils;

public class ChatView extends LinearLayout {
    private ChatList chatList;
    private ChatEditBar editBar;
    private WebimSession session;
    private DepartmentsDialog departmentsDialog;
    private String accountName;
    private String locationName;
    private FileHelper fileHelper;

    public ChatView(Context context) {
        this(context, null);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatView, defStyleAttr, R.style.ChatViewBase);
        accountName = typedArray.getString(R.styleable.ChatView_chv_account_name);
        locationName = typedArray.getString(R.styleable.ChatView_chv_location_name);
        int styleRes = typedArray.getResourceId(R.styleable.ChatView_chv_chat_style, R.style.ChatViewDefaultStyle);
        typedArray.recycle();

        Context themedContext = new ContextThemeWrapper(context, styleRes);

        View rootView = LayoutInflater.from(themedContext).inflate(R.layout.view_chatview, this, true);
        chatList = rootView.findViewById(R.id.chat_list);
        editBar = rootView.findViewById(R.id.chat_edit_bar);

        fileHelper = new FileHelper();
        chatList.setListControllerReady(listController -> editBar.setListController(listController));
    }

    public ChatList getChatList() {
        return chatList;
    }

    public ChatEditBar getChatEditBar() {
        return editBar;
    }

    public void setSession(WebimSession session) {
        if (this.session != null) {
            throw new IllegalStateException("Session is already set");
        }
        this.session = session;
    }

    public WebimSession getSession() {
        createSessionIfNeed();
        return session;
    }

    public boolean hasSession() {
        return session != null;
    }

    public FileHelper getFileHelper() {
        return fileHelper;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode()) return;
        createSessionIfNeed();

        chatList.setStream(session.getStream());
        if (chatList.getAdapter() == null) {
            ChatHolderActionsImpl holderActions = new ChatHolderActionsImpl(this);
            ChatAdapter<?> chatAdapter = new ChatAdapterImpl(holderActions);
            chatList.setAdapter(chatAdapter);
        }
        editBar.setStream(session.getStream());
        setDepartmentsHandler();

        session.resume();
        session.getStream().startChat();
        fileHelper.startWork(getContext(), session);
        editBar.setFileHelper(fileHelper);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (isInEditMode()) return;
        fileHelper.stopWork();
        session.pause();
        session.getStream().closeChat();
    }

    private void setDepartmentsHandler() {
        session.getStream().setVisitSessionStateListener((previousState, newState) -> {
            switch (newState) {
                case DEPARTMENT_SELECTION:
                    List<Department> departments = session.getStream().getDepartmentList();
                    if (departments != null) {
                        openDepartmentDialog(departments);
                    }
                    break;
                case IDLE_AFTER_CHAT:
                    if (isDialogVisible()) {
                        departmentsDialog.dismiss();
                    }
                    break;
            }
        });
    }

    private void openDepartmentDialog(@NonNull List<Department> departmentList) {
        if (isDialogVisible()) {
            departmentsDialog.dismiss();
        }

        Activity activity = ViewUtils.getRequiredActivity(getContext());
        departmentsDialog = new DepartmentsDialog(new DepartmentsDialog.DepartmentItemSelectedCallback() {
            @Override
            public void itemSelected(int position) {
                List<Department> departmentList = session.getStream().getDepartmentList();
                if (departmentList != null && !departmentList.isEmpty()) {
                    String selectedDepartment = departmentList.get(position).getKey();
                    session.getStream().startChatWithDepartmentKey(selectedDepartment);
                }
            }

            @Override
            public void onBackPressed() {
                if (activity != null) {
                    activity.onBackPressed();
                }
            }

            @Override
            public void onDismissed() {
                departmentsDialog = null;
            }
        });
        departmentsDialog.setDepartmentNames(getDepartmentsNames(departmentList));
        departmentsDialog.setCancelable(false);

        if (!(activity instanceof AppCompatActivity)) {
            Log.e(ChatHolderActionsImpl.class.getSimpleName(), "Host activity must be inherited from " + AppCompatActivity.class.getSimpleName() + " for opening image detail screen");
            return;
        }
        FragmentManager fragmentManager = ((AppCompatActivity) activity).getSupportFragmentManager();
        departmentsDialog.show(fragmentManager, DepartmentsDialog.DIALOG_TAG);
    }

    private boolean isDialogVisible() {
        return departmentsDialog != null && departmentsDialog.isVisible();
    }

    private List<String> getDepartmentsNames(List<Department> departmentList) {
        final List<String> departmentNames = new ArrayList<>();
        for (Department department : departmentList) {
            departmentNames.add(department.getName());
        }
        return departmentNames;
    }

    private void createSessionIfNeed() {
        if (!hasSession()) {
            session = createDefaultSession(Webim.newSessionBuilder());
        }
    }

    public void addMessagesSyncedCallback(MessageTracker.MessagesSyncedListener callback) {
        chatList.getSyncedWithServerCallbacks().add(callback);
    }

    public void removeMessagesSyncedCallback(MessageTracker.MessagesSyncedListener callback) {
        chatList.getSyncedWithServerCallbacks().remove(callback);
    }

    private WebimSession createDefaultSession(Webim.SessionBuilder builder) {
        builder.setContext(getContext())
            .setAccountName(accountName)
            .setLocation(locationName);
        return builder.build();
    }

    /**
     * Create ChatView with custom theme
     * @param context activity or context
     * @param styleRes your custom style resource
     * @return instance of ChatView
     */
    public static ChatView createWithStyle(Context context, int styleRes) {
        return new ChatView(new ContextThemeWrapper(context, styleRes));
    }
}
