package co.elastic.apm.android.test.activities.espresso;

import androidx.test.espresso.IdlingResource;

public interface IdlingResourceProvider {

    IdlingResource getIdlingResource();
}
