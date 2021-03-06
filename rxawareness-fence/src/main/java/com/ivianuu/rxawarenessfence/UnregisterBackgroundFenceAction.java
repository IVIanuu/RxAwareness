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

package com.ivianuu.rxawarenessfence;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ivianuu.rxplayservices.ClientException;
import com.ivianuu.rxplayservices.RxPlayServices;

import io.reactivex.functions.Consumer;

/**
 * Action to unregister a background fence.
 */
class UnregisterBackgroundFenceAction {

    private String name;

    private UnregisterBackgroundFenceAction(Context context, String name) {
        this.name = name;
        RxPlayServices.actions(context, Awareness.API, this::onClientConnected, this::onClientError);
    }

    /**
     * Unregisters the fence with the given name
     *
     * @param context context to use
     * @param name    name of the fence to unregister
     */
    static void unregister(Context context, String name) {
        new UnregisterBackgroundFenceAction(context.getApplicationContext(), name);
    }

    private void onClientConnected(final GoogleApiClient googleApiClient) {
        FenceUpdateRequest fenceRequest = new FenceUpdateRequest.Builder()
                .removeFence(name)
                .build();

        Awareness.FenceApi.updateFences(googleApiClient, fenceRequest)
                .setResultCallback(status -> {
                    if (!status.isSuccess()) {
                        onClientError(new ClientException("Unable to unregister fence. " + status.getStatusMessage()));
                    }
                    googleApiClient.disconnect();
                });
    }

    private void onClientError(Throwable throwable) {
        Log.e("ReactiveAwareness", "Error when updating Fence in GoogleApiClient: " + throwable.getLocalizedMessage());
    }
}