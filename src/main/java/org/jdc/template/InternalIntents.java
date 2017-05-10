package org.jdc.template;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import org.jdc.template.ui.activity.BaseActivity;
import org.jdc.template.util.AppConstants;
import org.jdc.template.ux.about.AboutActivity;
import org.jdc.template.ux.individual.IndividualActivity;
import org.jdc.template.ux.individual.IndividualContract;
import org.jdc.template.ux.individualedit.IndividualEditActivity;
import org.jdc.template.ui.activity.SettingsActivity;
import org.jdc.template.ux.individualedit.IndividualEditContract;
import org.jdc.template.ux.individualedit.IndividualEditPresenter;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InternalIntents extends BaseActivity{

    private SharedPreferences.Editor sharedPreferencesEditor;

    @Inject
    IndividualEditPresenter individualEditPresenter;

    @Inject
    public InternalIntents() {
    }

    public void showIndividual(@Nonnull Activity activity, long individualId) {
        Intent intent = new Intent(activity, IndividualActivity.class);
        intent.putExtra(IndividualContract.Extras.EXTRA_ID, individualId);
        activity.startActivity(intent);
    }

    public void newIndividual(@Nonnull Activity activity) {
        Intent intent = new Intent(activity, IndividualEditActivity.class);
        intent.putExtra(IndividualEditContract.Extras.EXTRA_ID, -1);
        activity.startActivity(intent);
    }

    public void editIndividual(@Nonnull Activity activity, long individualId) {
        Intent intent = new Intent(activity, IndividualEditActivity.class);
        intent.putExtra(IndividualEditContract.Extras.EXTRA_ID, individualId);
        activity.startActivity(intent);
    }

    public void showSettings(@Nonnull Context context) {
        Intent settingIntent = new Intent(context, SettingsActivity.class);
        context.startActivity(settingIntent);
    }

    public void showHelp(Context context) {
        Intent aboutIntent = new Intent(context, AboutActivity.class);
        context.startActivity(aboutIntent);
    }

    public void takePicture(Activity activity, long individualId) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "individual_picture" + individualId + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", photo));
        Uri imageUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", photo);

        sharedPreferencesEditor = activity.getApplication().getSharedPreferences(AppConstants.MY_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString(AppConstants.INDIVIDUAL_PROFILE_URI, imageUri.toString());
        sharedPreferencesEditor.commit();

        activity.startActivityForResult(intent, AppConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
}
