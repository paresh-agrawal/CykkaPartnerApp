package com.cykka.partner;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cykka.partner.ui.blog.BlogFragment;
import com.cykka.partner.ui.chat.ChatFragment;
import com.cykka.partner.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private final Fragment fragment1 = new ChatFragment();
    private final Fragment fragment2 = new BlogFragment();
    private final Fragment fragment3 = new ProfileFragment();
    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment active = fragment1;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView tvLogout;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Cykka");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        ifUpdateAvailable();



        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer,R.string.open, R.string.close);

        navigationView.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setOnNavigationItemSelectedListener(mOnBottomNavigationItemSelectedListener);

        tvLogout.setOnClickListener(v -> {
            logOut();
            drawer.closeDrawers();
        });

        fm.beginTransaction().add(R.id.main_container, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.main_container, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.main_container, fragment1, "1").commit();
    }

    //initialise variables with ids
    private void init() {

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_drawer);
        navigation = findViewById(R.id.nav_view);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        tvLogout = findViewById(R.id.logout);

    }

    //Check if update available on PlayStore
    private void ifUpdateAvailable() {
        final DocumentReference docRef = firebaseFirestore.collection("Version").document("Config");
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Toast.makeText(MainActivity.this, "Error : " + e.toString() , Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> snapdata = snapshot.getData();
                showDialogIfUpdateAvailable(Objects.requireNonNull(snapdata));
            } else {
                Toast.makeText(MainActivity.this, "Error : loading data" , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogIfUpdateAvailable(Map<String, Object> snapdata) {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        if (Integer.parseInt(String.valueOf(snapdata.get("VersionCode")))>versionCode){
            char databaseChar = String.valueOf(snapdata.get("VersionName")).charAt(0);
            char appChar = versionName.charAt(0);
            if (databaseChar>appChar){
                //Immediate Update
                updateDialog(1);
            }else{
                //Flexible Update
                updateDialog(2);
            }
        }
    }

    //show update dialog (Immediate or Flexible)
    @SuppressLint("SetTextI18n")
    private void updateDialog(int i) {
        Dialog dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_app_update);
        dialog.setCancelable(false);

        dialog.setOnShowListener(dialogInterface -> {

            Button update = dialog.findViewById(R.id.bt_update);
            Button noThanks = dialog.findViewById(R.id.bt_no_thanks);
            ImageView cancel = dialog.findViewById(R.id.bt_cancel);
            TextView title = dialog.findViewById(R.id.tv_update_title);

            if (i==1){
                noThanks.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                title.setText("Update Required");
            }else {
                noThanks.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                title.setText("Update Available");
            }


            cancel.setOnClickListener(v -> dialog.dismiss());

            noThanks.setOnClickListener(v -> dialog.dismiss());

            //open PlayStore on update click
            update.setOnClickListener(v -> {
                Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                }
            });
        });
        dialog.show();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnBottomNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_chat:
                        fm.beginTransaction().hide(active).show(fragment1).commit();
                        active = fragment1;
                        setActionBarTitle("Cykka");
                        return true;

                    case R.id.navigation_blog:
                        fm.beginTransaction().hide(active).show(fragment2).commit();
                        active = fragment2;
                        setActionBarTitle("Blogs");
                        return true;

                    case R.id.navigation_profile:
                        fm.beginTransaction().hide(active).show(fragment3).commit();
                        active = fragment3;
                        setActionBarTitle("Profile");
                        return true;
                }
                return false;
            };

    private NavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            Intent intent;
            Uri uri;
            switch(id)
            {
                case R.id.nav_wallet:
                    drawer.closeDrawers();
                    startActivity(new Intent(MainActivity.this, WalletActivity.class));

                    break;
                case R.id.nav_terms:
                    uri = Uri.parse("https://www.cykka.in/terms"); // missing 'http://' will cause crashed
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
                case R.id.nav_privacy:
                    uri = Uri.parse("https://www.cykka.in/privacy"); // missing 'http://' will cause crashed
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
                case R.id.nav_support:
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto","support@cykka.in", null));
                    startActivity(Intent.createChooser(intent, "Choose an Email client :"));
                    break;
                case R.id.nav_share:
                    try {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Cykka Partner");
                        String shareMessage= "\nHey! Try this new Cykka Partner App.\n\nThe best place to find new customers for"+
                                " your advisory business. Be your own boss and work at any time.\n\n";
                        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        startActivity(Intent.createChooser(shareIntent, "Choose one"));
                    } catch(Exception e) {
                        Toast.makeText(MainActivity.this, "Error loading URL", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.nav_rate_us:
                    uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                    }
                    drawer.closeDrawers();
                    break;
                default:
                    return true;
            }

            return true;
        }
    };



    private void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    //Method used in profile fragment to edit "About Me"
    public void editAboutMe(String text, String title, String subtitle) {
        Dialog dialog = new Dialog(this, R.style.WideDialog);
        @SuppressLint("InflateParams") View view  = this.getLayoutInflater().inflate(R.layout.dialog_about_me, null);
        dialog.setContentView(view);

        dialog.setOnShowListener(dialogInterface -> {

            Button btSave = dialog.findViewById(R.id.bt_save);
            Button btReset = dialog.findViewById(R.id.bt_reset);
            ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
            TextInputEditText etAboutMe = dialog.findViewById(R.id.et_about_me);
            TextView tvTitle = dialog.findViewById(R.id.tv_title);
            TextView tvSubTitle = dialog.findViewById(R.id.tv_subtitle);

            etAboutMe.setText(text);
            tvTitle.setText(title);
            tvSubTitle.setText(subtitle);

            btCancel.setOnClickListener(v -> dialog.dismiss());

            btReset.setOnClickListener(v -> etAboutMe.setText(text));

            btSave.setOnClickListener(v -> {
                if (title.equals("About Me")){
                    String finalAboutMe = Objects.requireNonNull(etAboutMe.getText()).toString();
                    if(TextUtils.isEmpty(finalAboutMe)){
                        finalAboutMe = "";
                    }
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("AboutMe", finalAboutMe);

                    final DocumentReference advisorRef = firebaseFirestore.collection("AdvisorsDatabase")
                            .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

                    advisorRef.update(dataMap);
                    dialog.dismiss();
                }else if(title.equals("Name")){
                    String finalName = Objects.requireNonNull(etAboutMe.getText()).toString();
                    if (TextUtils.isEmpty(finalName)){
                        etAboutMe.setError("Required");
                    }else{
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("Name", finalName);

                        final DocumentReference nameRef = firebaseFirestore.collection("AdvisorsDatabase")
                                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).collection("IdProofs").document("PersonalInfo");

                        nameRef.update(dataMap);
                        dialog.dismiss();
                    }
                }
            });
        });
        dialog.show();
    }

    //Method used in Profile Fragment to change preferred language by advisor
    @SuppressLint("SetTextI18n")
    public void languagePicker(String textLang) {
        Dialog dialog = new Dialog(this, R.style.WideDialog);
        @SuppressLint("InflateParams") View view  = this.getLayoutInflater().inflate(R.layout.dialog_language_picker, null);
        dialog.setContentView(view);

        dialog.setOnShowListener(dialogInterface -> {

            Button btSave = dialog.findViewById(R.id.bt_save);
            Button btReset = dialog.findViewById(R.id.bt_reset);
            ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
            CheckBox cbHindi = dialog.findViewById(R.id.lang_hindi);
            CheckBox cbEnglish = dialog.findViewById(R.id.lang_english);
            CheckBox cbAssamese = dialog.findViewById(R.id.lang_assamese);
            CheckBox cbBengali = dialog.findViewById(R.id.lang_bengali);
            CheckBox cbGerman = dialog.findViewById(R.id.lang_german);
            CheckBox cbGujarati = dialog.findViewById(R.id.lang_gujarati);
            CheckBox cbHaryanvi = dialog.findViewById(R.id.lang_haryanvi);
            CheckBox cbKannada = dialog.findViewById(R.id.lang_kannada);
            CheckBox cbKashmiri = dialog.findViewById(R.id.lang_kashmiri);
            CheckBox cbKonkani = dialog.findViewById(R.id.lang_konkani);
            CheckBox cbMaithili = dialog.findViewById(R.id.lang_maithili);
            CheckBox cbMalayalam = dialog.findViewById(R.id.lang_malayalam);
            CheckBox cbManipuri = dialog.findViewById(R.id.lang_manipuri);
            CheckBox cbMarathi = dialog.findViewById(R.id.lang_marathi);
            CheckBox cbMarwari = dialog.findViewById(R.id.lang_marwari);
            CheckBox cbNepali = dialog.findViewById(R.id.lang_nepali);
            CheckBox cbOdia = dialog.findViewById(R.id.lang_odia);
            CheckBox cbPunjabi = dialog.findViewById(R.id.lang_punjabi);
            CheckBox cbSanskrit = dialog.findViewById(R.id.lang_sanskrit);
            CheckBox cbSindhi = dialog.findViewById(R.id.lang_sindhi);
            CheckBox cbSpanish = dialog.findViewById(R.id.lang_spanish);
            CheckBox cbTamil = dialog.findViewById(R.id.lang_tamil);
            CheckBox cbTelugu = dialog.findViewById(R.id.lang_telugu);
            CheckBox cbUrdu = dialog.findViewById(R.id.lang_urdu);

            TextView tvError = dialog.findViewById(R.id.tv_error);

            Handler handler = new Handler();
            Runnable runnable = () -> tvError.setVisibility(View.INVISIBLE);


            if (textLang.contains("Hindi")){cbHindi.setChecked(true);}
            if (textLang.contains("English")){cbEnglish.setChecked(true);}
            if (textLang.contains("Assamese")){cbAssamese.setChecked(true);}
            if (textLang.contains("Bengali")){cbBengali.setChecked(true);}
            if (textLang.contains("German")){cbGerman.setChecked(true);}
            if (textLang.contains("Gujarati")){cbGujarati.setChecked(true);}
            if (textLang.contains("Haryanvi")){cbHaryanvi.setChecked(true);}
            if (textLang.contains("Kannada")){cbKannada.setChecked(true);}
            if (textLang.contains("Kashmiri")){cbKashmiri.setChecked(true);}
            if (textLang.contains("Konkani")){cbKonkani.setChecked(true);}
            if (textLang.contains("Maithili")){cbMaithili.setChecked(true);}
            if (textLang.contains("Malayalam")){cbMalayalam.setChecked(true);}
            if (textLang.contains("Manipuri")){cbManipuri.setChecked(true);}
            if (textLang.contains("Marathi")){cbMarathi.setChecked(true);}
            if (textLang.contains("Marwari")){cbMarwari.setChecked(true);}
            if (textLang.contains("Nepali")){cbNepali.setChecked(true);}
            if (textLang.contains("Odia")){cbOdia.setChecked(true);}
            if (textLang.contains("Punjabi")){cbPunjabi.setChecked(true);}
            if (textLang.contains("Sanskrit")){cbSanskrit.setChecked(true);}
            if (textLang.contains("Sindhi")){cbSindhi.setChecked(true);}
            if (textLang.contains("Spanish")){cbSpanish.setChecked(true);}
            if (textLang.contains("Tamil")){cbTamil.setChecked(true);}
            if (textLang.contains("Telugu")){cbTelugu.setChecked(true);}
            if (textLang.contains("Urdu")){cbUrdu.setChecked(true);}


            btCancel.setOnClickListener(v -> dialog.dismiss());

            btReset.setOnClickListener(v -> {
                if (textLang.contains("Hindi")){cbHindi.setChecked(true);}
                if (textLang.contains("English")){cbEnglish.setChecked(true);}
                if (textLang.contains("Assamese")){cbAssamese.setChecked(true);}
                if (textLang.contains("Bengali")){cbBengali.setChecked(true);}
                if (textLang.contains("German")){cbGerman.setChecked(true);}
                if (textLang.contains("Gujarati")){cbGujarati.setChecked(true);}
                if (textLang.contains("Haryanvi")){cbHaryanvi.setChecked(true);}
                if (textLang.contains("Kannada")){cbKannada.setChecked(true);}
                if (textLang.contains("Kashmiri")){cbKashmiri.setChecked(true);}
                if (textLang.contains("Konkani")){cbKonkani.setChecked(true);}
                if (textLang.contains("Maithili")){cbMaithili.setChecked(true);}
                if (textLang.contains("Malayalam")){cbMalayalam.setChecked(true);}
                if (textLang.contains("Manipuri")){cbManipuri.setChecked(true);}
                if (textLang.contains("Marathi")){cbMarathi.setChecked(true);}
                if (textLang.contains("Marwari")){cbMarwari.setChecked(true);}
                if (textLang.contains("Nepali")){cbNepali.setChecked(true);}
                if (textLang.contains("Odia")){cbOdia.setChecked(true);}
                if (textLang.contains("Punjabi")){cbPunjabi.setChecked(true);}
                if (textLang.contains("Sanskrit")){cbSanskrit.setChecked(true);}
                if (textLang.contains("Sindhi")){cbSindhi.setChecked(true);}
                if (textLang.contains("Spanish")){cbSpanish.setChecked(true);}
                if (textLang.contains("Tamil")){cbTamil.setChecked(true);}
                if (textLang.contains("Telugu")){cbTelugu.setChecked(true);}
                if (textLang.contains("Urdu")){cbUrdu.setChecked(true);}
            });

            btSave.setOnClickListener(v -> {
                handler.removeCallbacks(runnable);
                tvError.setVisibility(View.INVISIBLE);
                if ( !(cbHindi.isChecked() || cbEnglish.isChecked() || cbAssamese.isChecked() ||
                        cbBengali.isChecked() || cbGerman.isChecked() || cbGujarati.isChecked() ||
                        cbHaryanvi.isChecked() || cbKannada.isChecked() || cbKashmiri.isChecked() ||
                        cbKonkani.isChecked() || cbMaithili.isChecked() || cbMalayalam.isChecked() ||
                        cbManipuri.isChecked() || cbMarathi.isChecked() || cbMarwari.isChecked() ||
                        cbNepali.isChecked() || cbOdia.isChecked() || cbPunjabi.isChecked() ||
                        cbSanskrit.isChecked() || cbSindhi.isChecked() || cbSpanish.isChecked() ||
                        cbTamil.isChecked() || cbTelugu.isChecked() || cbUrdu.isChecked())){
                    tvError.setText("Please select at least one language.");
                    tvError.setVisibility(View.VISIBLE);
                    handler.postDelayed(runnable, 4000);
                }else{
                    String languages = "";
                    if (cbHindi.isChecked()){languages = languages + " Hindi,";}
                    if (cbEnglish.isChecked()){languages = languages + " English,";}
                    if (cbAssamese.isChecked()){languages = languages + " Assamese,";}
                    if (cbBengali.isChecked()){languages = languages + " Bengali,";}
                    if (cbGerman.isChecked()){languages = languages + " German,";}
                    if (cbGujarati.isChecked()){languages = languages + " Gujarati,";}
                    if (cbHaryanvi.isChecked()){languages = languages + " Haryanvi,";}
                    if (cbKannada.isChecked()){languages = languages + " Kannada,";}
                    if (cbKashmiri.isChecked()){languages = languages + " Kashmiri,";}
                    if (cbKonkani.isChecked()){languages = languages + " Konkani,";}
                    if (cbMaithili.isChecked()){languages = languages + " Maithili,";}
                    if (cbMalayalam.isChecked()){languages = languages + " Malayalam,";}
                    if (cbManipuri.isChecked()){languages = languages + " Manipuri,";}
                    if (cbMarathi.isChecked()){languages = languages + " Marathi,";}
                    if (cbMarwari.isChecked()){languages = languages + " Marwari,";}
                    if (cbNepali.isChecked()){languages = languages + " Nepali,";}
                    if (cbOdia.isChecked()){languages = languages + " Odia,";}
                    if (cbPunjabi.isChecked()){languages = languages + " Punjabi,";}
                    if (cbSanskrit.isChecked()){languages = languages + " Sanskrit,";}
                    if (cbSindhi.isChecked()){languages = languages + " Sindhi,";}
                    if (cbSpanish.isChecked()){languages = languages + " Spanish,";}
                    if (cbTamil.isChecked()){languages = languages + " Tamil,";}
                    if (cbTelugu.isChecked()){languages = languages + " Telugu,";}
                    if (cbUrdu.isChecked()){languages = languages + " Urdu,";}
                    String finalLang = languages.substring(1, languages.length()-1);

                    final DocumentReference preferredLanguageRef = firebaseFirestore.collection("AdvisorsDatabase")
                            .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).collection("IdProofs").document("OtherDetails");

                    Map<String, Object> langMap = new HashMap<>();
                    langMap.put("Languages", finalLang);
                    preferredLanguageRef.update(langMap).addOnSuccessListener(aVoid -> dialog.dismiss()).addOnFailureListener(e -> {

                    });

                }
            });
        });
        dialog.show();
    }


    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return Objects.requireNonNull(connectivityManager).getActiveNetworkInfo() != null && Objects.requireNonNull(connectivityManager.getActiveNetworkInfo()).isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_support:
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","support@cykka.in", null));
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
                break;
            case R.id.action_logout:
                logOut();
                return true;
            case R.id.action_wallet:
                startActivity(new Intent(MainActivity.this, WalletActivity.class));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


}
