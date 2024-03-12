
<script lang=ts>
    let mediaRecorder: MediaRecorder | undefined;
    let videoChunks: Blob[] = [];
    let videoURL = '';
    let isRecording = false;
    let recordings = [];
    let stream: MediaStream | undefined;
    
    async function toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }
    async function startRecording() {
        isRecording = true;
        videoChunks = [];
        try {
            stream = await navigator.mediaDevices.getUserMedia({ video: true });
            mediaRecorder = new MediaRecorder(stream);

            mediaRecorder.ondataavailable = e => videoChunks.push(e.data);
            mediaRecorder.onstop = () => {
                const videoBlob = new Blob(videoChunks, { type: 'video/webm' });
                videoURL = URL.createObjectURL(videoBlob);
            };
            mediaRecorder.start();
        } catch (err) {
            console.error("Could not start video recording:", err);
            isRecording = false;
        }
    }
    
    async function stopRecording() {
        if (mediaRecorder && mediaRecorder.state === "recording") {
            mediaRecorder.stop();
        }
        if (stream) {
            stream.getTracks().forEach(track => track.stop());
        }
        isRecording = false;
    }

    //<button on:click={toggleRecording}>{isRecording ? 'Stop' : 'Start'} recording</button>
    //bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-full
</script>

<style>
    .video-container {
        display: flex;
        flex-direction: column;
        align-items: center;
    }
    video {
        max-width: 100%;
        margin-bottom: 1rem;
    }
    button {
    color: #090909;
    padding: 0.7em 1.7em;
    font-size: 18px;
    border-radius: 0.5em;
    background: #e8e8e8;
    cursor: pointer;
    border: 1px solid #e8e8e8;
    transition: all 0.3s;
    box-shadow: 6px 6px 12px #c5c5c5, -6px -6px 12px #ffffff;
    }

    button:active {
    color: #666;
    box-shadow: inset 4px 4px 12px #c5c5c5, inset -4px -4px 12px #ffffff;
    }

</style>

<div class="video-container">
    <video controls src={videoURL}></video>
    <button on:click={toggleRecording}>{isRecording ? 'Stop' : 'Start'} recording</button>
</div>