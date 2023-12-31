package com.cometchat.pro.uikit.ui_components.chats;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.ConversationsRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.TypingIndicator;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.AppConfig;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.ui_components.shared.CometChatSnackBar;
import com.cometchat.pro.uikit.ui_components.shared.cometchatConversations.CometChatConversations;
import com.cometchat.pro.uikit.ui_resources.utils.CometChatError;
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.CustomAlertDialogHelper;
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.OnAlertDialogButtonClickListener;
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener;
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerViewSwipeListener;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

/*

* Purpose - CometChatConversationList class is a fragment used to display list of conversations and perform certain action on click of item.
            It also provide search bar to perform search operation on the list of conversations.User can search by username, groupname, last message of conversation.

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/

public class CometChatConversationList extends Fragment implements TextWatcher, OnAlertDialogButtonClickListener {

    private static final String TAG = "ConversationList";
    private static OnItemClickListener events;
    private CometChatConversations rvConversationList;    //Uses to display list of conversations.
    private ConversationsRequest conversationsRequest;    //Uses to fetch Conversations.
    private String conversationListType = UIKitSettings.getConversationsMode().toString();
    private EditText searchEdit;    //Uses to perform search operations.
    private TextView tvTitle;
    private ShimmerFrameLayout conversationShimmer;
    private RelativeLayout rlSearchBox;
    private LinearLayout noConversationView;
    private View view;
    private ImageView backIcon;

    private List<Conversation> conversationList = new ArrayList<>();
    private Conversation pinConversion = null;
    private ImageView pinUnpinImage;


    private ImageView startConversation;
    private ProgressDialog progressDialog;

    public CometChatConversationList() {
        // Required empty public constructor
    }

    /**
     * @param onItemClickListener An object of <code>OnItemClickListener&lt;T&gt;</code> abstract class helps to initialize with events
     *                            to perform onItemClick & onItemLongClick.
     * @see OnItemClickListener
     */
    public static void setItemClickListener(OnItemClickListener<Conversation> onItemClickListener) {
        events = onItemClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cometchat_conversationlist, container, false);

        rvConversationList = view.findViewById(R.id.rv_conversation_list);

        noConversationView = view.findViewById(R.id.no_conversation_view);

        searchEdit = view.findViewById(R.id.search_bar);

        tvTitle = view.findViewById(R.id.tv_title);

        backIcon = view.findViewById(R.id.back_action);

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        tvTitle.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium));

        rlSearchBox = view.findViewById(R.id.rl_search_box);

        conversationShimmer = view.findViewById(R.id.shimmer_layout);

        checkDarkMode();

        CometChatError.init(getContext());

        startConversation = view.findViewById(R.id.start_conversation);
        pinUnpinImage = view.findViewById(R.id.start_pin_unpin);


        FeatureRestriction.isStartConversationEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (booleanVal)
                    startConversation.setVisibility(View.GONE);
                else
                    startConversation.setVisibility(View.GONE);
            }
        });

        startConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CometChatStartConversation.launch(requireContext());
            }
        });
        searchEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (!textView.getText().toString().isEmpty()) {
                    progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.search));
                    refreshConversation(new CometChat.CallbackListener<List<Conversation>>() {
                        @Override
                        public void onSuccess(List<Conversation> conversationList) {
                            rvConversationList.searchConversation(textView.getText().toString(), new Filter.FilterListener() {
                                @Override
                                public void onFilterComplete(int i) {
                                    if (i==0) {
                                        searchConversation(textView.getText().toString());
                                    }
                                }
                            });
                        }
                        @Override
                        public void onError(CometChatException e) {
                            if (progressDialog != null)
                                progressDialog.dismiss();
                            CometChatSnackBar.show(getContext(), rvConversationList,
                                    CometChatError.localized(e), CometChatSnackBar.ERROR);
                        }
                    });
                }
                return true;
            }
            return false;
        });

        // Uses to fetch next list of conversations if rvConversationList (RecyclerView) is scrolled in upward direction.
        rvConversationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    makeConversationList();
                }

            }
        });

        // Used to trigger event on click of conversation item in rvConversationList (RecyclerView)
        rvConversationList.setItemClickListener(new OnItemClickListener<Conversation>() {
            @Override
            public void OnItemClick(Conversation conversation, int position) {
                if (pinConversion != null && pinConversion.getConversationId().equals(conversation.getConversationId())) {
                    pinConversion = null;
                    pinUnpinImage.setVisibility(View.GONE);
                    rvConversationList.setItemSelected(-1);
                    return;
                }

                if (events != null) {
                    rvConversationList.setItemSelected(-1);
                    pinUnpinImage.setVisibility(View.GONE);
                    events.OnItemClick(conversation, position);
                }

            }

            @Override
            public void OnItemLongClick(Conversation conversation, int position) {
                try {
                    if (AppConfig.ENABLE_PIN_MESSAGE) {
                        pinConversion = conversation;
                        pinUnpinImage.setVisibility(View.VISIBLE);
                        if (conversation.getTags() == null) {
                            pinUnpinImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.chat_pin));
                        } else {
                            pinUnpinImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.chat_unpin));
                        }
                        rvConversationList.setItemSelected(position);
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });


        // After selecting item by long press add pin by clicking pin icon on top this will
        // add tag ' isPinned' and move its position to top
        pinUnpinImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pinConversion == null) {
                    return;
                }

                String id = ""; //id of the user/group
                String conversationType = "";
                if (pinConversion.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_GROUP)) {
                    id = ((Group) pinConversion.getConversationWith()).getGuid();
                    conversationType = CometChatConstants.CONVERSATION_TYPE_GROUP;
                } else {
                    id = ((User) pinConversion.getConversationWith()).getUid();
                    conversationType = CometChatConstants.CONVERSATION_TYPE_USER;
                }

                List<String> tags = new ArrayList<>();

                if (pinConversion.getTags() == null) {
                    tags.add("pinned");
                }

                CometChat.tagConversation(id, conversationType, tags, new CometChat.CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversion) {
                        pinConversion = null;
                        rvConversationList.setItemSelected(-1);
                        pinUnpinImage.setVisibility(View.GONE);
                        conversationList.clear();
                        rvConversationList.clearList();
                        conversationsRequest = null;
                        stopHideShimmer();
                        makeConversationList();
                    }

                    @Override
                    public void onError(CometChatException e) {
                        pinConversion = null;
                        rvConversationList.setItemSelected(-1);
                        pinUnpinImage.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                });
            }
        });


        UIKitSettings.deleteConversation(false);

        RecyclerViewSwipeListener swipeHelper = new RecyclerViewSwipeListener(getContext()) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                Bitmap deleteBitmap = Utils.drawableToBitmap(ContextCompat.getDrawable(requireContext(),R.drawable.ic_delete_conversation));
                FeatureRestriction.isDeleteConversationEnabled(new FeatureRestriction.OnSuccessListener() {
                    @Override
                    public void onSuccess(Boolean booleanVal) {
                        if (booleanVal) {
                            underlayButtons.add(new RecyclerViewSwipeListener.UnderlayButton(
                                    "Delete",
                                    deleteBitmap,
                                    getResources().getColor(R.color.red),
                                    new RecyclerViewSwipeListener.UnderlayButtonClickListener() {
                                        @Override
                                        public void onClick(final int pos) {
                                            Conversation conversation = rvConversationList.getConversation(pos);
                                            if (conversation != null) {
                                                String conversationUid = "";
                                                String type = "";
                                                if (conversation.getConversationType()
                                                        .equalsIgnoreCase(CometChatConstants.CONVERSATION_TYPE_GROUP)) {
                                                    conversationUid = ((Group) conversation.getConversationWith()).getGuid();
                                                    type = CometChatConstants.CONVERSATION_TYPE_GROUP;
                                                } else {
                                                    conversationUid = ((User) conversation.getConversationWith()).getUid();
                                                    type = CometChatConstants.CONVERSATION_TYPE_USER;
                                                }
                                                String finalConversationUid = conversationUid;
                                                String finalType = type;
                                                new CustomAlertDialogHelper(getContext(),
                                                        getString(R.string.delete_conversation_message),
                                                        null,
                                                        getString(R.string.yes),
                                                        "", getString(R.string.no), new OnAlertDialogButtonClickListener() {
                                                    @Override
                                                    public void onButtonClick(AlertDialog alertDialog, View v, int which, int popupId) {
                                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                                            ProgressDialog progressDialog = ProgressDialog.show(getContext(), null,
                                                                    getString(R.string.deleting_conversation));
                                                            CometChat.deleteConversation(
                                                                    finalConversationUid, finalType,
                                                                    new CometChat.CallbackListener<String>() {
                                                                        @Override
                                                                        public void onSuccess(String s) {
                                                                            Handler handler = new Handler();
                                                                            handler.postDelayed(new Runnable() {
                                                                                public void run() {
                                                                                    alertDialog.dismiss();
                                                                                    progressDialog.dismiss();
                                                                                }
                                                                            }, 1500);
                                                                            rvConversationList.remove(conversation);
                                                                        }

                                                                        @Override
                                                                        public void onError(CometChatException e) {
                                                                            progressDialog.dismiss();
                                                                            e.printStackTrace();
                                                                        }
                                                                    });
                                                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                                                            alertDialog.dismiss();
                                                        }
                                                    }
                                                }, 1, true);

                                            }
                                        }
                                    }
                            ));
                        }
                    }
                });
            }
        };
        swipeHelper.attachToRecyclerView(rvConversationList);
        return view;
    }

    /**
     * Refresh conversion list
     * @param callbackListener
     */
    public void refreshConversation(CometChat.CallbackListener callbackListener) {
        rvConversationList.clearList();
        conversationList.clear();
        conversationsRequest = null;
        if (conversationsRequest == null) {
            conversationsRequest = new ConversationsRequest.ConversationsRequestBuilder()
                    .setLimit(50)
                    .build();
            if (conversationListType != null)
                conversationsRequest = new ConversationsRequest.ConversationsRequestBuilder()
                        .setConversationType(conversationListType)
                        .setLimit(50)
                        .build();
        }
        conversationsRequest.fetchNext(new CometChat.CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                conversationList.addAll(conversations);
                if (conversationList.size() != 0) {
                    stopHideShimmer();
                    noConversationView.setVisibility(View.GONE);
                    rvConversationList.setConversationList(conversations);
                } else {
                    checkNoConversation();
                }
                callbackListener.onSuccess(conversationList);
            }

            @Override
            public void onError(CometChatException e) {
                stopHideShimmer();
                if (getActivity() != null)
                    CometChatSnackBar.show(getContext(), rvConversationList,
                            CometChatError.localized(e), CometChatSnackBar.ERROR);
                callbackListener.onError(e);
            }
        });
    }

    private void checkDarkMode() {
        if (Utils.isDarkMode(requireContext())) {
            tvTitle.setTextColor(getResources().getColor(R.color.textColorWhite));
        } else {
            tvTitle.setTextColor(getResources().getColor(R.color.primaryTextColor));
        }
    }

    public void setConversationListType(String conversationListType) {
        this.conversationListType = conversationListType;
    }

    /**
     * This method is used to retrieve list of conversations you have done.
     * For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-messaging-retrieve-conversations" }
     *
     * @see ConversationsRequest
     */
    private void makeConversationList() {

        if (conversationsRequest == null) {
            conversationsRequest = new ConversationsRequest.ConversationsRequestBuilder().withTags(true).setLimit(50).build();
            if (conversationListType != null && !AppConfig.ENABLE_PIN_MESSAGE) {
                conversationsRequest = new ConversationsRequest.ConversationsRequestBuilder()
                            .setConversationType(conversationListType).setLimit(50).build();
            }
        }

        conversationsRequest.fetchNext(new CometChat.CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                conversationList.addAll(filterConversions(conversations));
                if (conversationList.size() != 0) {
                    stopHideShimmer();
                    noConversationView.setVisibility(View.GONE);
                    rvConversationList.setConversationList(filterConversions(conversations));
                } else {
                    checkNoConversation();
                }
            }

            @Override
            public void onError(CometChatException e) {
                stopHideShimmer();
                if (getActivity() != null)
                    CometChatSnackBar.show(getContext(), rvConversationList,
                            getString(R.string.err_default_message), CometChatSnackBar.ERROR);

            }
        });
    }

    /**
     *  This method filter the data according tag ( isPinned ) and add top to the list
     * @param conversations conversion list data
     * @return return filter data
     */
    private List<Conversation> filterConversions(List<Conversation> conversations) {
        if (AppConfig.ENABLE_PIN_MESSAGE) {

            List<Conversation> resultList = new ArrayList<>();
            List<Conversation> pinnedConversions = new ArrayList<>();
            for (Conversation conversation : conversations) {
                if (conversation.getTags() == null) {
                    resultList.add(conversation);
                }

                if (conversation.getTags() != null) {
                    pinnedConversions.add(conversation);
                }
            }

            if (pinnedConversions.size() > 0) {
                resultList.addAll(0, pinnedConversions);
            }

            return resultList;
        }
        return conversations;
    }

    private void checkNoConversation() {
        if (rvConversationList.size() == 0) {
            stopHideShimmer();
            noConversationView.setVisibility(View.VISIBLE);
            rvConversationList.setVisibility(View.GONE);
        } else {
            noConversationView.setVisibility(View.GONE);
            rvConversationList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method is used to hide shimmer effect if the list is loaded.
     */
    private void stopHideShimmer() {
        conversationShimmer.stopShimmer();
        conversationShimmer.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        rlSearchBox.setVisibility(View.VISIBLE);
    }

    /**
     * This method has message listener which recieve real time message and based on these messages, conversations are updated.
     *
     * @see CometChat#addMessageListener(String, CometChat.MessageListener)
     */
    private void addConversationListener() {
        CometChat.addMessageListener(TAG, new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage message) {
                if (rvConversationList != null) {
                    rvConversationList.refreshConversation(message);
                    checkNoConversation();
                }
            }

            @Override
            public void onMediaMessageReceived(MediaMessage message) {
                if (rvConversationList != null) {
                    rvConversationList.refreshConversation(message);
                    checkNoConversation();
                }
            }

            @Override
            public void onCustomMessageReceived(CustomMessage message) {
                if (rvConversationList != null) {
                    rvConversationList.refreshConversation(message);
                    checkNoConversation();
                }
            }

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                if (rvConversationList != null)
                    rvConversationList.setReciept(messageReceipt);
            }

            @Override
            public void onMessagesRead(MessageReceipt messageReceipt) {
                if (rvConversationList != null)
                    rvConversationList.setReciept(messageReceipt);
            }

            @Override
            public void onMessageEdited(BaseMessage message) {
                if (rvConversationList != null)
                    rvConversationList.refreshConversation(message);
            }

            @Override
            public void onMessageDeleted(BaseMessage message) {
                if (rvConversationList != null)
                    rvConversationList.refreshConversation(message);
            }

            @Override
            public void onTypingStarted(TypingIndicator typingIndicator) {
                if (rvConversationList != null)
                    rvConversationList.setTypingIndicator(typingIndicator, false);
            }

            @Override
            public void onTypingEnded(TypingIndicator typingIndicator) {
                if (rvConversationList != null)
                    rvConversationList.setTypingIndicator(typingIndicator, true);
            }
        });
        CometChat.addGroupListener(TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                if (kickedUser.getUid().equals(CometChat.getLoggedInUser().getUid())) {
                    if (rvConversationList != null)
                        updateConversation(action, true);
                } else {
                    updateConversation(action, false);
                }
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedby, User userAdded, Group addedTo) {
                updateConversation(action, false);
            }

            @Override
            public void onGroupMemberJoined(Action action, User joinedUser, Group joinedGroup) {
                updateConversation(action, false);
            }

            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                if (leftUser.getUid().equals(CometChat.getLoggedInUser().getUid())) {
                    updateConversation(action, true);
                } else {
                    updateConversation(action, false);
                }
            }

            @Override
            public void onGroupMemberScopeChanged(Action action, User updatedBy, User updatedUser, String scopeChangedTo, String scopeChangedFrom, Group group) {
                updateConversation(action, false);
            }
        });
    }

    /**
     * This method is used to update conversation received in real-time.
     *
     * @param baseMessage is object of BaseMessage.class used to get respective Conversation.
     * @param isRemove    is boolean used to check whether conversation needs to be removed or not.
     * @see CometChatHelper#getConversationFromMessage(BaseMessage) This method return the conversation
     * of receiver using baseMessage.
     */
    private void updateConversation(BaseMessage baseMessage, boolean isRemove) {
        if (rvConversationList != null) {
            Conversation conversation = CometChatHelper.getConversationFromMessage(baseMessage);
            if (isRemove)
                rvConversationList.remove(conversation);
            else
                rvConversationList.update(conversation);
            checkNoConversation();
        }
    }

    /**
     * This method is used to remove the conversation listener.
     */
    private void removeConversationListener() {
        CometChat.removeMessageListener(TAG);
        CometChat.removeGroupListener(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();

        conversationsRequest = null;
        searchEdit.addTextChangedListener(this);
        rvConversationList.clearList();
        makeConversationList();
        addConversationListener();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();
        searchEdit.removeTextChangedListener(this);
        removeConversationListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeConversationListener();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (pinConversion != null) {
            pinConversion = null;
            pinUnpinImage.setVisibility(View.GONE);
            rvConversationList.setItemSelected(-1);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
            // if searchEdit is empty then fetch all conversations.
            conversationsRequest = null;
            rvConversationList.clearList();
            makeConversationList();
        }
    }


    /**
     * This method is used to perform search operation on list of conversion database.
     *
     * @param string is a String which is used to search conversion.
     *
     * @see ConversationsRequest
     */


    private void searchConversation(String string) {
        conversationsRequest.fetchNext(new CometChat.CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                conversationList.addAll(conversations);
                if (conversations.size() != 0) {
                    rvConversationList.setConversationList(conversations);
                    searchConversation(string);
                } else {
                    rvConversationList.searchConversation(string, new Filter.FilterListener() {
                        @Override
                        public void onFilterComplete(int i) {
                            if (progressDialog!=null)
                                progressDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onError(CometChatException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onButtonClick(AlertDialog alertDialog, View v, int which, int popupId) {
        if (which == DialogInterface.BUTTON_NEGATIVE)
            alertDialog.dismiss();
    }


}
