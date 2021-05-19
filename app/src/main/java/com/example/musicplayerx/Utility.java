package com.example.musicplayerx;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.musicplayerx.service.MediaPlayerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Utility {

    public final static int KIBI = 1024;
    public final static int BYTE = 1;
    public final static int KIBIBYTE = KIBI * BYTE;

    /**
     * Private constructor to prevent instantiation
     */
    private Utility() {}

    public static <T> T[] repeatElements(T[] arr, int newLength) {
        T[] dup = Arrays.copyOf(arr, newLength);
        for (int last = arr.length; last != 0 && last < newLength; last <<= 1) {
            System.arraycopy(dup, 0, dup, last, Math.min(last << 1, newLength) - last);
        }
        return dup;
    }

    public static <T> ArrayList<T> repeatElements(ArrayList<T> arr, int newLength) {
        T[] dup = (T[]) Arrays.copyOf(arr.toArray(), newLength);
        for (int last = arr.size(); last != 0 && last < newLength; last <<= 1) {
            System.arraycopy(dup, 0, dup, last, Math.min(last << 1, newLength) - last);
        }
        ArrayList<T> dup2 = new ArrayList<T>(Arrays.asList(dup));
        return dup2;
    }

    public static void playAudio(Context context, int audioIndex, ArrayList<Audio> audioList){
        //Service is active
        //Send media with BroadcastReceiver

        ////TODO may change to passing different audioLists as well if in other fragments
        //Store the new audioIndex to SharedPreferences
        StorageUtil storage = new StorageUtil(context); ////Maybe is MainActivity
        storage.storeAudioIndex(audioIndex);
        if (audioList != null){ //Using default otherwise
            storage.storeAudio(audioList);
        }

        //Service is active
        //Send a broadcast to the service -> PLAY_NEW_AUDIO
        Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        context.sendBroadcast(broadcastIntent);

        Log.v("Utility", "broadcast sent");
    }
}
