/*
 * Copyright 2017 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.rxawareness;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.awareness.state.BeaconState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Function;

import static com.ivianuu.rxawareness.ApiKeyGuard.API_KEY_AWARENESS_API;
import static com.ivianuu.rxawareness.ApiKeyGuard.API_KEY_BEACON_API;
import static com.ivianuu.rxawareness.ApiKeyGuard.API_KEY_PLACES_API;
import static com.ivianuu.rxawareness.ApiKeyGuard.guardWithApiKey;

/**
 * Accessor class for Reactive Context values. All methods exposed query the Snapshot API to give
 * you more information about the users current context.
 * <p>
 * All context events are provided as {@link Single}s which will provide you with exactly the
 * current context state.
 */
@SuppressLint("MissingPermission")
public class RxSnapshot {

    private final Context context;

    private RxSnapshot(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Creates a new instance of ReactiveSnapshot to give you access to all Snapshot API calls.
     * @param context context to use, will default to your application context
     * @return instance of ReactiveSnapshot
     */
    @NonNull
    public static RxSnapshot create(@NonNull Context context) {
        return new RxSnapshot(context.getApplicationContext());
    }

    /**
     * Returns the current weather information at the devices current location
     *
     * @return Single event of weather information
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<Weather> getWeather() {
        guardWithApiKey(context, API_KEY_AWARENESS_API);
        return WeatherSingle.create(context);
    }

    /**
     * Provides the current temperature at the devices current location
     *
     * @param temperatureUnit temperature unit to use
     * @return Single event of the current temperature
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<Float> getTemperature(final int temperatureUnit) {
        return getWeather()
                .map(new Function<Weather, Float>() {
                    @Override
                    public Float apply(Weather weather) throws Exception {
                        return weather.getTemperature(temperatureUnit);
                    }
                });
    }

    /**
     * Provides the current feels-like temperature at the devices current location
     *
     * @param temperatureUnit temperature unit to use
     * @return Single event of the current feels-like temperature
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<Float> getFeelsLikeTemperature(final int temperatureUnit) {
        return getWeather()
                .map(new Function<Weather, Float>() {
                    @Override
                    public Float apply(Weather weather) throws Exception {
                        return weather.getFeelsLikeTemperature(temperatureUnit);
                    }
                });
    }

    /**
     * Provides the current dew point at the devices current location
     *
     * @param temperatureUnit temperature unit to use
     * @return Single event of the current dew point
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<Float> getDewPoint(final int temperatureUnit) {
        return getWeather()
                .map(new Function<Weather, Float>() {
                    @Override
                    public Float apply(Weather weather) throws Exception {
                        return weather.getDewPoint(temperatureUnit);
                    }
                });
    }

    /**
     * Provides the current humidity at the devices current location
     *
     * @return Single event of the current humidity
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<Integer> getHumidity() {
        return getWeather()
                .map(new Function<Weather, Integer>() {
                    @Override
                    public Integer apply(Weather weather) throws Exception {
                        return weather.getHumidity();
                    }
                });
    }

    /**
     * Provides the current weather conditions at the devices current location
     *
     * @return Single event of the current weather conditions
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<List<Integer>> getWeatherConditions() {
        return getWeather()
                .map(new Function<Weather, Integer[]>() {
                    @Override
                    public Integer[] apply(Weather weather) throws Exception {
                        Integer[] conditions = new Integer[]{};
                        for (int i = 0; i < weather.getConditions().length; i++) {
                            conditions[i] = weather.getConditions()[i];
                        }
                        return conditions;
                    }
                })
                .map(new Function<Integer[], List<Integer>>() {
                    @Override
                    public List<Integer> apply(Integer[] conditions) throws Exception {
                        List<Integer> list = new ArrayList<>(conditions.length);
                        list.addAll(Arrays.asList(conditions));
                        return list;
                    }
                });
    }

    /**
     * Provides the current location of the device
     *
     * @return Single event of the current location
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<Location> getLocation() {
        guardWithApiKey(context, API_KEY_AWARENESS_API);
        return LocationSingle.create(context);
    }

    /**
     * Provides the current latitude/longitude of the device
     *
     * @return Single event of the current latitude/longitude
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<LatLng> getLatLng() {
        return getLocation()
                .map(new Function<Location, LatLng>() {
                    @Override
                    public LatLng apply(Location location) throws Exception {
                        return new LatLng(location.getLatitude(), location.getLongitude());
                    }
                });
    }

    /**
     * Provides the current speed of the device
     *
     * @return Single event of the current speed
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<Float> getSpeed() {
        return getLocation()
                .map(new Function<Location, Float>() {
                    @Override
                    public Float apply(Location location) throws Exception {
                        return location.getSpeed();
                    }
                });
    }

    /**
     * Provides the current {@link ActivityRecognitionResult} of the device
     *
     * @return Single event of the current devices activity
     */
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @CheckResult @NonNull
    public Single<ActivityRecognitionResult> getActivity() {
        guardWithApiKey(context, API_KEY_AWARENESS_API);
        return ActivitySingle.create(context);
    }

    /**
     * Provides the current most probable {@link DetectedActivity} of the device
     *
     * @return Single event of the most probable activity
     */
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @CheckResult @NonNull
    public Single<DetectedActivity> getMostProbableActivity() {
        return getActivity()
                .map(new Function<ActivityRecognitionResult, DetectedActivity>() {
                    @Override
                    public DetectedActivity apply(ActivityRecognitionResult activityRecognitionResult) throws Exception {
                        return activityRecognitionResult.getMostProbableActivity();
                    }
                });
    }

    /**
     * Provides the current most probable {@link DetectedActivity} of the device which has at least
     * the given probability. Should no activity reach this minimum probability, {@code null} will
     * be emitted.
     * <p>
     * <b>Be sure to check the result for null!</b>
     *
     * @return Single event of the most probable activity
     */
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @CheckResult @NonNull
    public Single<DetectedActivity> getMostProbableActivity(final int minimumProbability) {
        return getActivity()
                .map(new Function<ActivityRecognitionResult, DetectedActivity>() {
                    @Override
                    public DetectedActivity apply(ActivityRecognitionResult activity) throws Exception {
                        DetectedActivity mostProbableActivity = activity.getMostProbableActivity();
                        if (activity.getActivityConfidence(mostProbableActivity.getType()) < minimumProbability) {
                            return null;
                        }
                        return mostProbableActivity;
                    }
                });
    }

    /**
     * Provides the current probable {@link DetectedActivity}s of the device
     *
     * @return Single event of the most probable activities
     */
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @CheckResult @NonNull
    public Single<List<DetectedActivity>> getProbableActivities() {
        return getActivity()
                .map(new Function<ActivityRecognitionResult, List<DetectedActivity>>() {
                    @Override
                    public List<DetectedActivity> apply(ActivityRecognitionResult activity) throws Exception {
                        return activity.getProbableActivities();
                    }
                });
    }

    /**
     * Provides the current probable {@link DetectedActivity}s of the device which have at least
     * the given probability. Should no activity reach this minimum probability, the resulting list
     * will be empty.
     *
     * @param minimumProbability minimum probabilities of the activities
     * @return Single event of the most probable activities
     */
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @CheckResult @NonNull
    public Single<List<DetectedActivity>> getProbableActivities(final int minimumProbability) {
        return getActivity()
                .map(new Function<ActivityRecognitionResult, List<DetectedActivity>>() {
                    @Override
                    public List<DetectedActivity> apply(ActivityRecognitionResult activity) throws Exception {
                        List<DetectedActivity> probableActivities = activity.getProbableActivities();
                        List<DetectedActivity> matchingActivities = new ArrayList<>(probableActivities.size());

                        for (DetectedActivity probableActivity : probableActivities) {
                            if (activity.getActivityConfidence(probableActivity.getType()) >= minimumProbability) {
                                matchingActivities.add(probableActivity);
                            }
                        }

                        return matchingActivities;
                    }
                });
    }

    /**
     * Provides the current state of the headphones.
     *
     * @return Single event of {@code true} if the headphones are currently plugged in
     */
    @CheckResult @NonNull
    public Single<Boolean> headphonesPluggedIn() {
        guardWithApiKey(context, API_KEY_AWARENESS_API);
        return HeadphoneSingle.create(context);
    }

    /**
     * Provides the currently nearby places to the current device location.
     *
     * @return Single event of the currently nearby places
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @CheckResult @NonNull
    public Single<List<PlaceLikelihood>> getNearbyPlaces() {
        guardWithApiKey(context, API_KEY_AWARENESS_API);
        guardWithApiKey(context, API_KEY_PLACES_API);
        return NearbySingle.create(context);
    }

    /**
     * Provides the currently nearby beacons to the current device locations.
     *
     * @param typeFilters Beacon TypeFilters to filter for
     * @return Single event of matching nearby beacons
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @CheckResult @NonNull
    public Single<List<BeaconState.BeaconInfo>> getBeacons(@NonNull BeaconState.TypeFilter... typeFilters) {
        guardWithApiKey(context, API_KEY_AWARENESS_API);
        guardWithApiKey(context, API_KEY_BEACON_API);
        return BeaconSingle.create(context, typeFilters);
    }

    /**
     * Provides the currently nearby beacons to the current device locations.
     *
     * @param typeFilters Beacon TypeFilters to filter for
     * @return Single event of matching nearby beacons
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @CheckResult @NonNull
    public Single<List<BeaconState.BeaconInfo>> getBeacons(@NonNull Collection<BeaconState.TypeFilter> typeFilters) {
        guardWithApiKey(context, API_KEY_AWARENESS_API);
        guardWithApiKey(context, API_KEY_BEACON_API);
        return BeaconSingle.create(context, typeFilters);
    }
}
