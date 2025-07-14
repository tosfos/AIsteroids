// Replace entire file with MIDI-based system
import javax.sound.midi.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MusicSystem {
    private static Sequencer sequencer;
    private static final AtomicBoolean musicPlaying = new AtomicBoolean(false);
    private static final AtomicInteger intensityLevel = new AtomicInteger(1);
    private static float masterVolume = 0.4f;

    public static void startMusic() {
        if (!musicPlaying.get()) {
            try {
            musicPlaying.set(true);
                sequencer = MidiSystem.getSequencer();
                sequencer.open();
                Sequence sequence = createAmbientSequence();
                sequencer.setSequence(sequence);
                sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
                updateTempoAndVolume();
                sequencer.start();
            } catch (Exception e) {
                System.err.println("Error starting music: " + e.getMessage());
                musicPlaying.set(false);
            }
        }
    }

    public static void stopMusic() {
        musicPlaying.set(false);
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.stop();
            sequencer.close();
        }
    }

    public static void setIntensity(int level) {
        intensityLevel.set(Math.max(1, Math.min(5, level)));
        updateTempoAndVolume();
    }

    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateTempoAndVolume();
    }

    private static void updateTempoAndVolume() {
        if (sequencer != null) {
        int intensity = intensityLevel.get();
            // Tempo: 60-100 BPM
            int bpm = 60 + (intensity * 8);
            sequencer.setTempoInBPM(bpm);
            // Volume adjustment
            try {
                MidiChannel[] channels = MidiSystem.getSynthesizer().getChannels();
                for (MidiChannel channel : channels) {
                    if (channel != null) {
                        channel.controlChange(7, (int)(127 * masterVolume * (0.6 + intensity * 0.08)));
                    }
                }
            } catch (MidiUnavailableException e) {
                System.err.println("Error adjusting volume: " + e.getMessage());
            }
        }
    }

    private static Sequence createAmbientSequence() throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, 24);
        Track track = sequence.createTrack();

        // Set instruments (program change)
        addMidiEvent(track, ShortMessage.PROGRAM_CHANGE, 0, 49, 0, 0); // Strings for channel 0
        addMidiEvent(track, ShortMessage.PROGRAM_CHANGE, 1, 1, 0, 0); // Acoustic Grand Piano for channel 1
        addMidiEvent(track, ShortMessage.PROGRAM_CHANGE, 2, 90, 0, 0); // Pad 3 (polysynth) for channel 2

        // Beautiful ambient pattern in C major
        int[] bassNotes = {36, 41, 43, 36}; // C2, F2, G2, C2
        int[] melodyNotes = {60, 64, 67, 72, 67, 64}; // C4, E4, G4, C5, G4, E4
        int[] harmonyNotes = {55, 59, 62}; // G3, B3, D4

        long tick = 0;
        int duration = 96; // Quarter note

        // 16-bar loop
        for (int bar = 0; bar < 16; bar++) {
            // Bass line
            addNote(track, 0, bassNotes[bar % 4], 60, tick, duration * 4);

            // Melody (every 2 bars)
            if (bar % 2 == 0) {
                for (int i = 0; i < melodyNotes.length; i++) {
                    addNote(track, 1, melodyNotes[i], 50, tick + i * duration, duration);
                }
            }

            // Harmony pads (long holds)
            addNote(track, 2, harmonyNotes[bar % 3], 40, tick, duration * 8);

            tick += duration * 4;
        }

        // End track
        MetaMessage meta = new MetaMessage();
        meta.setMessage(0x2F, new byte[0], 0);
        track.add(new MidiEvent(meta, tick));

        return sequence;
    }

    private static void addMidiEvent(Track track, int type, int channel, int data1, int data2, long tick) throws InvalidMidiDataException {
        MidiMessage message = new ShortMessage(type, channel, data1, data2);
        track.add(new MidiEvent(message, tick));
    }

    private static void addNote(Track track, int channel, int note, int velocity, long tick, int duration) throws InvalidMidiDataException {
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + duration);
    }

    public static void updateMusicIntensity(int asteroidCount, int playerLives, boolean powerUpActive) {
        int newIntensity = 1 + (asteroidCount / 5);
        if (playerLives <= 1) newIntensity++;
        if (powerUpActive) newIntensity++;
        setIntensity(Math.min(5, newIntensity));
    }
}
