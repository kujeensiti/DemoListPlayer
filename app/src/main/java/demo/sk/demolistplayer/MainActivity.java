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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private AudioItemAdapter audioItemAdapter;

    // POJO to hold data about audio items
    private static class AudioItem {

        // raw resource id of audio item
        final int audioResId;

        private AudioItem(int audioResId) {
            this.audioResId = audioResId;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);

        // arrange cells in vertical column
        rv.setLayoutManager(new LinearLayoutManager(this));

        // add 256 stub audio items
        ArrayList<AudioItem> audioItems = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            audioItems.add(new AudioItem(R.raw.mp3));
        }
        audioItemAdapter = new AudioItemAdapter(audioItems);
        rv.setAdapter(audioItemAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioItemAdapter.stopPlayer();
    }

    private class AudioItemAdapter extends RecyclerView.Adapter<AudioItemAdapter.AudioItemsViewHolder> {

        private MediaPlayer mediaPlayer;

        private List<AudioItem> audioItems;
        private int currentPlayingPosition;
        private SeekBarUpdater seekBarUpdater;
        private AudioItemsViewHolder playingHolder;

        AudioItemAdapter(List<AudioItem> audioItems) {
            this.audioItems = audioItems;
            this.currentPlayingPosition = -1;
            seekBarUpdater = new SeekBarUpdater();
        }

        @Override
        public AudioItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AudioItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(AudioItemsViewHolder holder, int position) {
            if (position == currentPlayingPosition) {
                playingHolder = holder;
                // this view holder corresponds to the currently playing audio item
                // update its view to show playing progress
                updatePlayingView();
            } else {
                updateNonPlayingView(holder);
            }
        }


        @Override
        public void onViewRecycled(AudioItemsViewHolder holder) {
            super.onViewRecycled(holder);
            if (currentPlayingPosition == holder.getAdapterPosition()) {
                updateNonPlayingView(playingHolder);
                playingHolder = null;
            }
        }

        /**
         * Changes the view to non playing state
         * - icon is changed to play arrow
         * - seek bar disabled and remove update listener
         *
         * @param holder
         */
        private void updateNonPlayingView(AudioItemsViewHolder holder) {
            holder.sbProgress.removeCallbacks(seekBarUpdater);
            holder.sbProgress.setEnabled(false);
            holder.sbProgress.setProgress(0);
            holder.ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }

        /**
         * Changes the view to playing state
         * - icon is changed to pause
         * - seek bar enabled
         * - update listener added to seek bar, if needed
         */
        private void updatePlayingView() {
            playingHolder.sbProgress.setMax(mediaPlayer.getDuration());
            playingHolder.sbProgress.setProgress(mediaPlayer.getCurrentPosition());
            playingHolder.sbProgress.setEnabled(true);
            if (mediaPlayer.isPlaying()) {
                playingHolder.sbProgress.postDelayed(seekBarUpdater, 100);
                playingHolder.ivPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                playingHolder.sbProgress.removeCallbacks(seekBarUpdater);
                playingHolder.ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
            }
        }

        void stopPlayer() {
            if (null != mediaPlayer) {
                releaseMediaPlayer();
            }
        }

        private class SeekBarUpdater implements Runnable {
            @Override
            public void run() {
                if (null != playingHolder) {
                    playingHolder.sbProgress.setProgress(mediaPlayer.getCurrentPosition());
                    playingHolder.sbProgress.postDelayed(this, 100);
                }
            }
        }

        @Override
        public int getItemCount() {
            return audioItems.size();
        }

        // interaction listeners e.g. click, seekBarChange etc are handled in the view holder itself
        class AudioItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
            SeekBar sbProgress;
            ImageView ivPlayPause;

            AudioItemsViewHolder(View itemView) {
                super(itemView);
                ivPlayPause = (ImageView) itemView.findViewById(R.id.ivPlayPause);
                ivPlayPause.setOnClickListener(this);
                sbProgress = (SeekBar) itemView.findViewById(R.id.sbProgress);
                sbProgress.setOnSeekBarChangeListener(this);
            }

            @Override
            public void onClick(View v) {
                if (getAdapterPosition() == currentPlayingPosition) {
                    // toggle between play/pause of audio
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                } else {
                    // start another audio playback
                    currentPlayingPosition = getAdapterPosition();
                    if (mediaPlayer != null) {
                        if (null != playingHolder) {
                            updateNonPlayingView(playingHolder);
                        }
                        mediaPlayer.release();
                    }
                    playingHolder = this;
                    startMediaPlayer(audioItems.get(currentPlayingPosition).audioResId);
                }
                updatePlayingView();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }

        private void startMediaPlayer(int audioResId) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), audioResId);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseMediaPlayer();
                }
            });
            mediaPlayer.start();
        }

        private void releaseMediaPlayer() {
            if (null != playingHolder) {
                updateNonPlayingView(playingHolder);
            }
            mediaPlayer.release();
            mediaPlayer = null;
            currentPlayingPosition = -1;
        }

    }

}
