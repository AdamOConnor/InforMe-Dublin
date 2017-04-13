package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejamesbond.text.DocumentView;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.example.adamoconnor.test02maps.MapsAndGeofencing.Place;
import com.example.adamoconnor.test02maps.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

import static com.example.adamoconnor.test02maps.MapsAndGeofencing.Place.getMonumentName;

/**
 * Created by Adam O'Connor on 07/04/2017.
 */

public class InformationFrontFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private TextToSpeech repeatText;
    private DocumentView information;
    private TextView title;
    private ImageButton listenButton;
    private SliderLayout sliderLayout;
    private HashMap<String,String> Hash_file_maps ;
    private String monumentName = null;
    private ProgressDialog infoProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Place activity = new Place();
        monumentName = activity.getMonumentName();

        View view = inflater.inflate(R.layout.activity_information, container, false);

        listenButton = (ImageButton)view.findViewById(R.id.informationListen);
        listenButton.setImageResource(R.drawable.soundicon);
        information = (DocumentView)view.findViewById(R.id.informationText);
        title = (TextView)view.findViewById(R.id.title);
        sliderLayout = (SliderLayout)view.findViewById(R.id.slider);

        infoProgress = ProgressDialog.show(getActivity(), "Loading...", "Please wait...", true);

        LoadData();

        listenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextToSpeech(v);
            }
        });

        repeatText=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub

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

    @Override
    public void onPause() {
        // TODO Auto-generated method stub

        if(repeatText != null){
            repeatText.stop();
            repeatText.shutdown();
        }
        super.onPause();
    }

    public void TextToSpeech(View view){

        if(repeatText.isSpeaking()) {

            listenButton.setImageResource(R.drawable.soundicon);
            repeatText.stop();
        }
        // If it's not playing
        else {
            // Resume the music player
            listenButton.setImageResource(R.drawable.muteicon);
            ConvertTextToSpeech();

        }

    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        String text = information.getText().toString();
        if(text==null||"".equals(text))
        {
            repeatText.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            listenButton.setImageResource(R.drawable.soundicon);
        }else
            repeatText.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        listenButton.setImageResource(R.drawable.muteicon);

    }

    private void LoadData() {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("ruin");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    String info = (dataSnapshot.child(getMonumentName().trim()).getValue().toString());
                    title.setText(monumentName.trim());
                    String regex = "\\[|\\]";
                    info = info.replaceAll(regex, "");
                    information.setText(info);
                    informationImages();

                } catch(NullPointerException ex) {

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

    }

    public void informationImages() {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        // pass the name of the monument
        DatabaseReference myRef = database.child("images").child(getMonumentName().trim());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot alerts) {

                Hash_file_maps = new HashMap<>();

                int count = 1;
                for(DataSnapshot alert : alerts.getChildren()) {
                    // pass the monument name
                    Hash_file_maps.put(monumentName.trim()+", "+count, alert.getValue().toString());

                    count++;
                }

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
                sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
                sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                sliderLayout.setCustomAnimation(new DescriptionAnimation());
                sliderLayout.setDuration(3000);
                sliderLayout.addOnPageChangeListener(InformationFrontFragment.this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
        infoProgress.cancel();
    }

    @Override
    public void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(getContext(),slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {}

    @Override
    public void onPageScrollStateChanged(int state) {}
}