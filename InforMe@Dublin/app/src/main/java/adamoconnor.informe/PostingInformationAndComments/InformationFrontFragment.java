package adamoconnor.informe.PostingInformationAndComments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import adamoconnor.informe.MapsAndGeofencing.Place;
import adamoconnor.informe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Locale;
import static adamoconnor.informe.MapsAndGeofencing.Place.getMonumentName;

public class InformationFrontFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    //declare text to speech.
    private TextToSpeech repeatText;

    private Boolean soundButton = true;

    //declare textView's
    private TextView information;
    private TextView title;

    //declare Image button
    private ImageButton listenButton;

    //declare slider.
    private SliderLayout sliderLayout;

    //declare hash-map for loading images etc.
    private HashMap<String,String> Hash_file_maps ;

    //declare monumentName string
    private String monumentName = null;

    //declare progress dialog.
    private ProgressDialog infoProgress;

    //declare database reference
    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get reference to the Place class
        Place activity = new Place();
        //retrieve the monument name of the monument.
        monumentName = activity.getMonumentName();

        // get the instance to the firebase reference.
        database = FirebaseDatabase.getInstance().getReference();
        // keep the database synced.
        database.keepSynced(true);

        final View view = inflater.inflate(R.layout.activity_information, container, false);

        //declare reference to the layout buttons.
        listenButton = (ImageButton)view.findViewById(R.id.informationListen);
        listenButton.setImageResource(R.drawable.soundicon);

        //declare reference to the textView's.
        information = (TextView)view.findViewById(R.id.informationText);
        title = (TextView)view.findViewById(R.id.title);

        //declare the sliderLayout.
        sliderLayout = (SliderLayout)view.findViewById(R.id.slider);

        //declare the progress bar to load up.
        infoProgress = ProgressDialog.show(getActivity(), "Loading...", "Please wait...", true);

        // load the data for the information of the monument.
        LoadData();

        //listen button start textToSpeech
        listenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(soundButton) {
                    listenButton.setImageResource(R.drawable.soundicon);

                }
                else {
                    listenButton.setImageResource(R.drawable.muteicon);
                }
                TextToSpeech(v);
            }
        });

        //start getting the information needed to speak.
        repeatText=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

                if(status == TextToSpeech.SUCCESS){
                    int result=repeatText.setLanguage(Locale.ENGLISH);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });
        return view;
    }

    /**
     * when the activity is paused shutdown the textToSpeech.
     */
    @Override
    public void onPause() {

        if(repeatText != null){
            repeatText.stop();
            repeatText.shutdown();
        }
        super.onPause();
    }

    /**
     * text to speech method used when using
     * specific sound button.
     */
    public void TextToSpeech(View view){

        if(repeatText.isSpeaking()) {

            repeatText.stop();
            soundButton = true;
        }
        // If it's not playing
        else {
            // Resume the music player
            ConvertTextToSpeech();
            soundButton = false;
        }

    }

    /**
     * convert the following text to speech
     * that was retrieved from firebase.
     */
    private void ConvertTextToSpeech() {
        String text = information.getText().toString();
        if(!TextUtils.isEmpty(text))
        {
            repeatText.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else {
            repeatText.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /**
     * load the specific data of the historic information
     * this method retrieves the text for the activity
     * with a reference to load images.
     */
    private void LoadData() {

        //reference to firebase database.
        DatabaseReference myRef = database.child("ruin");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //reference to load images method.
                informationImages();

                try {

                    String info = (dataSnapshot.child(getMonumentName().trim()).getValue().toString());
                    title.setText(monumentName.trim());
                    String regex = "\\[|\\]";
                    info = info.replaceAll(regex, "");
                    information.setText(info);

                } catch(NullPointerException ex) {
                    LoadData();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
    }

    /**
     * loading the images and add to the slider which will keep
     * spinning around.
     */
    public void informationImages() {

        // pass the name of the monument
        //get the reference to the database.
        DatabaseReference myRef = database.child("images").child(getMonumentName().trim());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot alerts) {

                Hash_file_maps = new HashMap<>();

                // load the images add numbers.
                int count = 1;
                for(DataSnapshot alert : alerts.getChildren()) {
                    // pass the monument name
                    Hash_file_maps.put(monumentName.trim()+", "+count, alert.getValue().toString());
                    count++;
                }

                // add the images to the slider.
                for(String name : Hash_file_maps.keySet()){

                    TextSliderView textSliderView = new TextSliderView(getContext());
                    textSliderView
                            .description(name)
                            .image(Hash_file_maps.get(name))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(InformationFrontFragment.this);
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle()
                            .putString("extra",name);
                    sliderLayout.addSlider(textSliderView);
                }
                // set the slider transform and duration of the slider.
                sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
                sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                sliderLayout.setCustomAnimation(new DescriptionAnimation());
                sliderLayout.setDuration(3000);
                sliderLayout.addOnPageChangeListener(InformationFrontFragment.this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
        // cancel the progress.
        infoProgress.cancel();
    }

    /**
     * stop the slider when activity closed.
     */
    @Override
    public void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }

    /**
     * used when the user clicks the image.
     * @param slider
     * reference to the slider.
     */
    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(getContext(),slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    /**
     * scroll of the slider
     * @param position
     * position of image
     * @param positionOffset
     * offset of image
     * @param positionOffsetPixels
     * offset of pixels
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    /**
     * which page is selected.
     * @param position
     * position of the page.
     */
    @Override
    public void onPageSelected(int position) {}

    /**
     * page which has changed.
     * @param state
     * which state is the page in.
     */
    @Override
    public void onPageScrollStateChanged(int state) {}
}