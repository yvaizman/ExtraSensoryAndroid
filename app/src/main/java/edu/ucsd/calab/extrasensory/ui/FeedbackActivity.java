package edu.ucsd.calab.extrasensory.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucsd.calab.extrasensory.ESApplication;
import edu.ucsd.calab.extrasensory.R;
import edu.ucsd.calab.extrasensory.data.ESActivity;
import edu.ucsd.calab.extrasensory.data.ESContinuousActivity;
import edu.ucsd.calab.extrasensory.data.ESLabelStruct;

import static edu.ucsd.calab.extrasensory.data.ESDatabaseAccessor.getESDatabaseAccessor;

/**
 * Feedback view for the user to provide the ground truth labels of what they are doing/feeling.
 * Right before starting this view you should call setFeedbackParametersBeforeStartingFeedback()
 * to inform this class what kind of feedback you need.
 * In addition, if this feedback activity is initiated from a notification
 * (or from an alert-dialog that was initiated by a notification/reminder),
 * you should add an extra with key KEY_INITIATED_BY_NOTIFICATION to the intent that starts this activity.
 */
public class FeedbackActivity extends BaseActivity {

    private static final String LOG_TAG = "[FeedbackActivity]";

    public static final int FEEDBACK_TYPE_ACTIVE = 1;
    public static final int FEEDBACK_TYPE_HISTORY_CONTINUOUS_ACTIVITY = 2;
    public static final String KEY_INITIATED_BY_NOTIFICATION = "edu.ucsd.calab.extrasensory.extra_key.initiated_by_notification";

    Button button;

    private static final String TEXT1 = "text1";
    private static final String TEXT2 = "text2";
    private static final String KEY_ROW_HEADER = "row header";
    private static final String KEY_ROW_DETAIL = "row detail";

    private static boolean feedbackFlag = false;
    private static final int ROW_MAIN = 0;
    private static final int ROW_SECONDARY = 1;
    private static final int ROW_MOOD = 2;
    private static final int ROW_VALID = 3;

    private static final String[] ROW_HEADERS = new String[] { "Main Activity", "Secondary Activities", "Mood", "Valid for" };
    private String[] _presentedLabelStrings = new String[4];

    private ESLabelStruct _labelStruct = new ESLabelStruct();
    /**
     * This parameter type is to be used to transfer parameters to the feedback view,
     * that indicate what kind of feedback to perform and pass relevant data.
     */
    public static final class FeedbackParameters {
        private int _feedbackType;
        private ESContinuousActivity _continuousActivityToEdit;

        /**
         * Default parameters - for doing active feedback
         */
        public FeedbackParameters() {
            _feedbackType = FEEDBACK_TYPE_ACTIVE;
            _continuousActivityToEdit = null;
        }

        /**
         * Parameters for doing feedback to edit the labels of a continuous activity
         * @param continuousActivityToEdit The continuous activity whose labels to edit
         */
        public FeedbackParameters(ESContinuousActivity continuousActivityToEdit) {
            _feedbackType = FEEDBACK_TYPE_HISTORY_CONTINUOUS_ACTIVITY;
            _continuousActivityToEdit = continuousActivityToEdit;
        }
    }

    private static FeedbackParameters _transientInputParameters = null;

    /**
     * Call this static function right before starting the feedback activity.
     * Set the parameters for the feedback.
     * @param inputParameters
     */
    public static void setFeedbackParametersBeforeStartingFeedback(FeedbackParameters inputParameters) {
        _transientInputParameters = inputParameters;
    }
    private FeedbackParameters _parameters = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Log.d(LOG_TAG,"activity being created");

        // Check if anyone set the transient input parameters:
        if (_transientInputParameters == null) {
            // Then behave as active feedback:
            _parameters = new FeedbackParameters();
        }
        else {
            // Quickly, copy the given input parameters and save them to the current Feedback object:
            _parameters = _transientInputParameters;
            _transientInputParameters = null;
        }

        // If got an existing activity in the input parameters, copy its labels:
        if (_parameters._feedbackType == FEEDBACK_TYPE_HISTORY_CONTINUOUS_ACTIVITY) {
            _labelStruct = new ESLabelStruct();
            _labelStruct._mainActivity = _parameters._continuousActivityToEdit.mostUpToDateMainActivity();
            _labelStruct._secondaryActivities = _parameters._continuousActivityToEdit.getSecondaryActivities();
            _labelStruct._moods = _parameters._continuousActivityToEdit.getMoods();
        }

        feedbackFlag = false;
        presentFeedbackView();

    }

    //refresh feedback list with user selections each time we open feedback page
    @Override
    protected void onStart(){
        super.onStart();
        presentFeedbackView();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG,"activity being destroyed");
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        _optionsMenu = menu;
        checkRecordingStateAndSetRedLight();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    private void presentFeedbackView() {
        switch (_parameters._feedbackType) {
            case FEEDBACK_TYPE_ACTIVE:
                Log.i(LOG_TAG,"Opening active feedback view");
                //TODO: present active feedback fields
                break;
            case FEEDBACK_TYPE_HISTORY_CONTINUOUS_ACTIVITY:
                Log.i(LOG_TAG,"Opening feedback view for continuous activity from history");
                Log.d(LOG_TAG,"Got continuous activity: " + _parameters._continuousActivityToEdit);
                //TODO: read the expected parameters of the given continuous activity
                //TODO: present pre-existing labels and the required form fields
                break;
            default:
                Log.e(LOG_TAG,"Got unsupported feedback type: " + _parameters._feedbackType);
                //TODO: how to handle such a case? leave blank/default page? present active feedback? present error message? close activity and go back to previous?
        }

        ListView listView = (ListView) findViewById(R.id.listview_activity);

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (int i = 0; i < ROW_HEADERS.length; i ++) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put(KEY_ROW_HEADER, ROW_HEADERS[i]);
            datum.put(KEY_ROW_DETAIL, _presentedLabelStrings[i]);
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {KEY_ROW_HEADER, KEY_ROW_DETAIL},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        listView.setAdapter(adapter);

        //only create feedback button once
        if(feedbackFlag == false) {
            View sendButton = getLayoutInflater().inflate(R.layout.activity_feedback_button, null);
            listView.addFooterView(sendButton);
            addListenerOnButton();
            feedbackFlag = true;
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // "position" is the position/row of the item that you have clicked
                Intent intent = null;
                switch (position) {
                    case ROW_MAIN:
                        intent = new Intent(ESApplication.getTheAppContext(), SelectionFromListActivity.class);
                        intent.putExtra(SelectionFromListActivity.LIST_TYPE_KEY, SelectionFromListActivity.LIST_TYPE_MAIN_ACTIVITY);
                        startActivityForResult(intent, ROW_MAIN);
                        break;
                    case ROW_SECONDARY:
                        intent = new Intent(ESApplication.getTheAppContext(), SelectionFromListActivity.class);
                        intent.putExtra(SelectionFromListActivity.LIST_TYPE_KEY, SelectionFromListActivity.LIST_TYPE_SECONDARY_ACTIVITIES);
                       // intent.putExtra(SelectionFromListActivity.PRESELECTED_LABELS_KEY, new String[]{"At home"});
                        startActivityForResult(intent, ROW_SECONDARY);
                        break;
                    case ROW_MOOD:
                        intent = new Intent(ESApplication.getTheAppContext(), SelectionFromListActivity.class);
                        intent.putExtra(SelectionFromListActivity.LIST_TYPE_KEY, SelectionFromListActivity.LIST_TYPE_MOODS);
                        startActivityForResult(intent, ROW_MOOD);
                        break;
                    case ROW_VALID:
                        Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

    }

    public void addListenerOnButton() {
        final Context context = this;
        button = (Button) findViewById(R.id.sendfeedback);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                boolean initiatedByNotification = getIntent().hasExtra(KEY_INITIATED_BY_NOTIFICATION);
                if(_parameters._feedbackType == FEEDBACK_TYPE_ACTIVE){
                    Log.d(LOG_TAG,"ACTIVE FEEDBACK");
                    int validForHowManyMinutes = 1;//TODO: need to analyze this value from the user input to validFor
                    ((ESApplication)getApplication()).startActiveFeedback(_labelStruct,validForHowManyMinutes,initiatedByNotification);
                    finish();
                    return;
                }

                //TODO: if this is feedback for continuous activity: go over the minute-activities and for each activity
                // update the activity's labels through the DBAccessor
                else if(_parameters._feedbackType == FEEDBACK_TYPE_HISTORY_CONTINUOUS_ACTIVITY) {
                    Log.d(LOG_TAG,"HISTORY CONTINUOUS ACTIVITY");
                    ESContinuousActivity esContAct =_parameters._continuousActivityToEdit;
                    ESActivity [] esActivityArr = esContAct.getMinuteActivities();
                    ESActivity.ESLabelSource labelSource = initiatedByNotification ?
                            ESActivity.ESLabelSource.ES_LABEL_SOURCE_NOTIFICATION_ANSWER_NOT_EXACTLY :
                            ESActivity.ESLabelSource.ES_LABEL_SOURCE_HISTORY;
                    //for each minute activity in the continuous activity
                    //call setESActivityValues()
                    for (ESActivity minute : esActivityArr){
                        getESDatabaseAccessor().setESActivityValues(minute,
                                labelSource,
                                _labelStruct._mainActivity,
                                _labelStruct._secondaryActivities,
                                _labelStruct._moods);
                    }

                    finish();
                    return;
                }
            }
        });

    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        Log.d(LOG_TAG,"got activity result with result code: " + requestCode);
        if (resultCode != Activity.RESULT_OK) {
            Log.i(LOG_TAG,"requested task was canceled.");
            return;
        }

        String[] selected = data.getStringArrayExtra(SelectionFromListActivity.SELECTED_LABELS_OUTPUT_KEY);
        String[] responses = new String[selected.length];

        if (requestCode== ROW_MAIN || requestCode== ROW_SECONDARY || requestCode== ROW_MOOD) {
            if (data == null) {
                Log.e(LOG_TAG,"Output from selection had null data");
                return;
            }
            if (!data.hasExtra(SelectionFromListActivity.SELECTED_LABELS_OUTPUT_KEY)) {
                Log.e(LOG_TAG,"Output from selection is missing the selected labels");
                return;
            }

            Log.d(LOG_TAG,"selected: ");

            String newSelected = "";
            for (int i=0;i<selected.length;i++) {
                Log.d(LOG_TAG,selected[i]);
                if(_presentedLabelStrings[requestCode] == null){
                    _presentedLabelStrings[requestCode] = "";
                    newSelected = selected[i];
                }
                else
                    newSelected = newSelected + ", " + selected[i];

                if (requestCode == ROW_MAIN) {
                    Log.d(LOG_TAG,"return from selecting main");
                    _labelStruct._mainActivity = selected[i];
                }
                else if (requestCode == ROW_SECONDARY) {
                    Log.d(LOG_TAG,"return from selecting secondary");
                    responses[i] = selected[i];
                }
                else if (requestCode == ROW_MOOD) {
                    Log.d(LOG_TAG, "return from selecting mood");
                   responses[i] = selected[i];
                }
            }

            //update the string list of user responses displayed in feedback page
            _presentedLabelStrings[requestCode] = newSelected;

            if (requestCode == ROW_SECONDARY) {
                _labelStruct._secondaryActivities = responses;
            }
            else if (requestCode == ROW_MOOD) {
                _labelStruct._moods = responses;
            }

            //Log.d(LOG_TAG, "updated response: " + _presentedLabelStrings[requestCode]);
        }
    }
}
