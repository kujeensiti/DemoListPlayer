**Demo List Player**

**Level:** Beginner

**Objectives:**

This project demonstrates:

 1. Use of `RecyclerView` to show list of songs. Each list item (cell) displays play/pause button and a seek bar to control the playback of the audio.
 2. Controlling playback of a song with a play/pause button and updating button state when playback is toggled and completed 
 3. Displaying song playback progress and controlling it with a seek bar
 4. Stopping playback when activity moves out of screen

**Description**

`AudioItem` is a simple POJO which has properties describing audio items, such as title, artist, album, genre, url etc. For this demo application, only one property is considered, that is resource id of audio from `raw` directory. A list of 256 `AudioItem`s is constructed to display in the form of vertically scrollable list. These all items are constructed with the same resource id, and as a result only one audio is played for all the cells.

`RecyclerView` optimises space by constructing (roughly) only as much cell children (or `ViewHolder`s) as displayed on the screen. When one scrolls the list upward, unused `ViewHolder` of the top-most cell, which is moving out of the screen, will be reused to display the new cell coming from the bottom. UI elements's states are updated to proper audio item `position` in `onBindViewHolder` method.

`MediaPlayer` is owned and managed by the `AudioItemAdapter`. It is made sure through `uiUpdateHandler` that the seek bar is updated only while media player is in playing state. `AudioItemAdapter.stopPlayer` is used to stop media player when acticity is paused.