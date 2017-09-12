package demo.sk.demolistplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        AudioChat audioChats[] = new AudioChat[128];
        rv.setAdapter(new MyAdapter(Arrays.asList(audioChats)));

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyAudioChat> {

        private List<AudioChat> audioChats;
        private int currentPlayingPosition;
        private SeekBarUpdater seekBarUpdater;

        MyAdapter(List<AudioChat> audioChats) {
            this.audioChats = audioChats;
            this.currentPlayingPosition = -1;
            seekBarUpdater = new SeekBarUpdater();
        }

        @Override
        public MyAudioChat onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyAudioChat(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyAudioChat holder, int position) {
            if (position == currentPlayingPosition) {
                seekBarUpdater.playingHolder = holder;
                holder.sbMyAudio.post(seekBarUpdater);
            } else {
                holder.sbMyAudio.removeCallbacks(seekBarUpdater);
                holder.sbMyAudio.setProgress(0);
            }
        }

        private class SeekBarUpdater implements Runnable {
            MyAudioChat playingHolder;

            @Override
            public void run() {
                if (null != mediaPlayer && playingHolder.getAdapterPosition() == currentPlayingPosition) {
                    playingHolder.sbMyAudio.setMax(mediaPlayer.getDuration());
                    playingHolder.sbMyAudio.setProgress(mediaPlayer.getCurrentPosition());
                    playingHolder.sbMyAudio.postDelayed(this, 100);
                } else {
                    playingHolder.sbMyAudio.removeCallbacks(seekBarUpdater);
                }
            }
        }

        @Override
        public int getItemCount() {
            return audioChats.size();
        }

        class MyAudioChat extends RecyclerView.ViewHolder implements View.OnClickListener {
            SeekBar sbMyAudio;
            ImageView imgPlayAudio;

            MyAudioChat(View itemView) {
                super(itemView);
                imgPlayAudio = (ImageView) itemView.findViewById(R.id.imgPlayAudio);
                imgPlayAudio.setOnClickListener(this);
                sbMyAudio = (SeekBar) itemView.findViewById(R.id.sbMyAudio);
            }

            @Override
            public void onClick(View v) {
                currentPlayingPosition = getAdapterPosition();
                if (mediaPlayer != null) {
                    if (null != seekBarUpdater.playingHolder) {
                        seekBarUpdater.playingHolder.sbMyAudio.removeCallbacks(seekBarUpdater);
                        seekBarUpdater.playingHolder.sbMyAudio.setProgress(0);
                    }
                    mediaPlayer.release();
                }
                seekBarUpdater.playingHolder = this;
                startMediaPlayer();
                sbMyAudio.setMax(mediaPlayer.getDuration());
                sbMyAudio.post(seekBarUpdater);
            }
        }

        private void startMediaPlayer() {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mp3);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    seekBarUpdater.playingHolder.sbMyAudio.removeCallbacks(seekBarUpdater);
                    seekBarUpdater.playingHolder.sbMyAudio.setProgress(0);
                    mediaPlayer.release();
                    mediaPlayer = null;
                    currentPlayingPosition = -1;
                }
            });
            mediaPlayer.start();
        }
    }

    private class AudioChat {

    }
}
