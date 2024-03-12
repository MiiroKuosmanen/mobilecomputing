
<script lang=ts>
    let mediaRecorder: MediaRecorder | undefined;
    let audioChunks: Blob[] = [];
    let audioURL = '';
    let isRecording = false;
    let recordings = [];

    async function toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }
    async function startRecording() {
        isRecording = true;
        audioChunks = [];
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaRecorder = new MediaRecorder(stream);
            mediaRecorder.ondataavailable = e => audioChunks.push(e.data);
            mediaRecorder.onstop = () => {
                const audioBlob = new Blob(audioChunks, { type: 'audio/ogg; codecs=opus' });
                audioURL = URL.createObjectURL(audioBlob);
            };
            mediaRecorder.start();
        } catch (err) {
            console.error("Could not start audio recording:", err);
            isRecording = false;
        }
    }

    async function stopRecording() {
        if (mediaRecorder && mediaRecorder.state === "recording") {
            mediaRecorder.stop();
        }
        isRecording = false;
    }
    async function saveRecording() {
        if (audioChunks.length > 0) {
            const blob = new Blob(audioChunks, { type: 'audio/webm' });
            try {
                const handle = await (window as any).showSaveFilePicker({
                    suggestedName: 'recording.webm',
                    types: [{
                        description: 'WebM audio',
                        accept: {'audio/webm': ['.webm']},
                    }],
                });
                const writable = await handle.createWritable();
                await writable.write(blob);
                await writable.close();
                alert('Recording saved successfully!');
            } catch (err) {
                console.error('Error saving file:', err);
                alert('Error saving file. Please make sure the file is not open in another application.');
            }
        }
    }

</script>

<button on:click={toggleRecording}>
    <svelte>
        {#if isRecording}
            Stop Recording
        {:else}
            Start Recording
        {/if}
    </svelte>
</button>

{#if audioURL}
    <audio src={audioURL} controls></audio>
    <button on:click={saveRecording}>Save Recording</button>
{/if}