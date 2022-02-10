package ru.webim.android.demo.client.util;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import ru.webim.android.demo.client.R;
import ru.webim.android.sdk.Survey;

import java.util.List;

public class SurveyDialog extends BottomSheetDialogFragment {
    private AnswerListener answerListener;
    private CancelSurveyListener cancelListener;
    private Survey.Question currentQuestion;
    private ImageView closeButton;
    private TextView actionTextView;
    private TextView questionTextView;
    private RadioGroup radioGroup;
    private EditText editText;
    private RatingBar ratingBar;
    private Button sendButton;
    private int radioButtonPadding;
    private String answer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.SurveyDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_survey, container, false);

        initViews(rootView);
        initViewListeners();
        initBottomBehavior();
        initSendButton();
        initCloseButton();

        return rootView;
    }

    private void initViews(View rootView) {
        closeButton = rootView.findViewById(R.id.closeButton);
        actionTextView = rootView.findViewById(R.id.actionTitleTextView);
        questionTextView = rootView.findViewById(R.id.questionTextView);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        editText = rootView.findViewById(R.id.editText);
        ratingBar = rootView.findViewById(R.id.ratingBar);
        sendButton = rootView.findViewById(R.id.sendButton);

        radioButtonPadding = (int) getContext().getResources().getDimension(R.dimen.survey_radio_button_padding);
    }

    private void initViewListeners() {
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            int newRating = (int) ratingBar.getRating();
            answer = String.valueOf(newRating);
        });
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton chosenRadioButton = getView().findViewById(radioGroup.getCheckedRadioButtonId());
            answer = String.valueOf(chosenRadioButton.getTag());
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getVisibility() == View.VISIBLE) {
                    sendButton.setEnabled(s.toString().trim().length() > 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (sendButton.isEnabled()) {
                    answer = editText.getText().toString().trim();
                }
            }
        });
    }

    private void initBottomBehavior() {
        BottomSheetBehavior<FrameLayout> bottomSheetBehavior = ((BottomSheetDialog) requireDialog()).getBehavior();
        bottomSheetBehavior.setHideable(false);
    }

    private void initSendButton() {
        sendButton.setOnClickListener(view -> {
            answerListener.onAnswer(answer);
        });
    }

    private void initCloseButton() {
        closeButton.setOnClickListener(view -> {
            dismiss();
            cancelListener.onCancel();
        });
    }

    public void setAnswerListener(AnswerListener answerListener) {
        this.answerListener = answerListener;
    }

    public void setCancelListener(CancelSurveyListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public void setCurrentQuestion(Survey.Question currentQuestion) {
        this.currentQuestion = currentQuestion;
        setLayout();
    }

    private void setLayout() {

        radioGroup.setVisibility(View.GONE);
        ratingBar.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        actionTextView.setVisibility(View.GONE);

        switch (currentQuestion.getType()) {
            case STARS:
                ratingBar.setVisibility(View.VISIBLE);
                actionTextView.setVisibility(View.VISIBLE);
                sendButton.setEnabled(true);
                break;
            case COMMENT:
                editText.setVisibility(View.VISIBLE);
                editText.setText("");
                sendButton.setEnabled(false);
                break;
            case RADIO:
                radioGroup.setVisibility(View.VISIBLE);
                createRadioButtons();
                sendButton.setEnabled(true);
                break;
            default:
                Log.w(getClass().getSimpleName(), "Unsupported question type: " + currentQuestion.getType().toString());
                dismiss();
        }
        questionTextView.setText(currentQuestion.getText());
    }

    private void createRadioButtons() {
        radioGroup.removeAllViews();
        List<String> options = currentQuestion.getOptions();
        if (options == null) {
            Log.w(getClass().getSimpleName(), "Question options can't be null for question type 'radio'");
            dismiss();
            return;
        }
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setTag(i + 1);
            radioButton.setText(option);
            radioButton.setPadding(0, radioButtonPadding, 0, radioButtonPadding);
            radioGroup.addView(
                radioButton,
                new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
            );
        }
    }

    public interface AnswerListener {
        void onAnswer(String answer);
    }

    public interface CancelSurveyListener {
        void onCancel();
    }
}
